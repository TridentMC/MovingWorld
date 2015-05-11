package darkevilmac.movingworld.mrot;

import darkevilmac.movingworld.MovingWorld;
import net.minecraft.block.Block;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MetaRotations {
    public Map<Integer, BlockMetaRotation> metaRotationMap;
    private File metaRotationsDirectory;

    public MetaRotations() {
        metaRotationMap = new HashMap<Integer, BlockMetaRotation>();
    }

    public boolean hasBlock(Block block) {
        return metaRotationMap.containsKey(Block.getIdFromBlock(block));
    }

    public int getRotatedMeta(Block block, int meta, int rotate) {
        if (rotate == 0) return meta;
        BlockMetaRotation rotation = metaRotationMap.get(Block.getIdFromBlock(block));
        if (rotation == null) return meta;
        return rotation.getRotatedMeta(meta, rotate);
    }

    public void addMetaRotation(Block block, int bitmask, int... metarotation) {
        if (block == null) {
            MovingWorld.logger.error("Adding null block meta rotations");
            return;
        }
        MovingWorld.logger.trace("Adding meta rotations (block=" + Block.blockRegistry.getNameForObject(block) + ", id=" + Block.getIdFromBlock(block) + ", mask=" + bitmask + ", rot=" + Arrays.toString(metarotation) + ")");

        metaRotationMap.put(Block.getIdFromBlock(block), new BlockMetaRotation(block, metarotation, bitmask));
    }

    public void setConfigDirectory(File configdirectory) {
        metaRotationsDirectory = new File(configdirectory, "MovingWorld\\MetaRotation");
        if (!metaRotationsDirectory.isDirectory()) {
            metaRotationsDirectory.mkdirs();
        }
    }

    public void registerMetaRotationFile(String fileName, InputStream iStream) throws IOException {
        File rotFile = new File(metaRotationsDirectory + "\\" + fileName);
        if (!rotFile.exists()) {
            rotFile.createNewFile();
            FileOutputStream oStream = new FileOutputStream(rotFile);
            IOUtils.copy(iStream, oStream);
            iStream.close();
            oStream.close();
            MovingWorld.instance.logger.debug("Created " + fileName + " meta rotation");
        }
        MovingWorld.instance.logger.debug(fileName + " ready to load");
    }

    public boolean parseMetaRotations(BufferedReader reader) throws IOException, OutdatedMrotException {
        boolean hasversionno = false;
        int lineno = 0;
        String line;
        String[] as;
        while ((line = reader.readLine()) != null) {
            lineno++;
            if (line.startsWith("#") || line.length() == 0) {
                continue;
            } else if (line.startsWith("version=")) {
                hasversionno = true;
                as = line.split("=");
                if (as.length != 2) {
                    mrotError("Version number is invalid", lineno);
                    throw new OutdatedMrotException("?");
                }
                String modversion = /*ArchimedesShipMod.MOD_VERSION*/"NULL";
                String version = as[1].trim();
                if (!version.equals(modversion)) {
                    throw new OutdatedMrotException(version);
                }
                continue;
            }

            Block[] blocks;
            int mask = 0xFFFFFFFF;
            int[] rot = new int[4];

            as = line.split(";");
            if (as.length < 3) {
                mrotError("Not enough parameters", lineno);
                continue;
            }

            String[] blocksstr = as[0].split(",");
            blocks = new Block[blocksstr.length];
            for (int i = 0; i < blocksstr.length; i++) {
                String name = blocksstr[i].trim();
                blocks[i] = Block.getBlockFromName(name);
                if (blocks[i] == null) {
                    mrotError("No block exists for " + name, lineno);
                }
            }

            try {
                mask = Integer.decode(as[1].trim()).intValue();
                String[] srot = as[2].split(",");
                for (int i = 0; i < rot.length; i++) {
                    rot[i] = Integer.parseInt(srot[i].trim());
                }
            } catch (NumberFormatException e) {
                mrotError(e.getLocalizedMessage(), lineno);
            }

            for (Block b : blocks) {
                addMetaRotation(b, mask, rot);
            }
        }
        return hasversionno;
    }

    public void mrotError(String msg, int lineno) {
        MovingWorld.logger.warn("Error in metarotation file at line " + lineno + " (" + msg + ")");
    }

    public void readMetaRotationFiles() {
        if (metaRotationsDirectory == null) throw new NullPointerException("Config folder has not been initialized");
        metaRotationMap.clear();

        try {
            try {
                readMetaRotationFile(new File(metaRotationsDirectory, "vanilla.mrot"));
            } catch (OutdatedMrotException ome) {
                MovingWorld.logger.info("Outdated vanilla.mrot detected: " + ome.getLocalizedMessage());
                createDefaultMrot();
                readMetaRotationFile(new File(metaRotationsDirectory, "vanilla.mrot"));
            } catch (FileNotFoundException fnfe) {
                MovingWorld.logger.info("vanilla.mrot file not found: " + fnfe.getLocalizedMessage());
                createDefaultMrot();
                readMetaRotationFile(new File(metaRotationsDirectory, "vanilla.mrot"));
            } catch (Exception e0) {
                throw e0;
            }
        } catch (Exception e1) {
            MovingWorld.logger.error("Could not load default meta rotations", e1);
        }

        //Discover other defaults.
        File thisClass = new File(getClass().getResource("MetaRotations.class").getPath());
        File modMetaRotations = new File(thisClass.getParentFile().getPath() + "\\mod");

        if (modMetaRotations != null && modMetaRotations.isDirectory() && modMetaRotations.listFiles() != null && !Arrays.asList(modMetaRotations.listFiles()).isEmpty()) {
            List<File> discovered = Arrays.asList(modMetaRotations.listFiles());
            if (discovered != null && !discovered.isEmpty()) {
                for (File file : discovered) {
                    try {
                        registerMetaRotationFile(file.getName(), new FileInputStream(file));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        File[] files = metaRotationsDirectory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File f, String name) {
                return !name.equals("vanilla.mrot") && name.endsWith(".mrot");
            }
        });

        for (File f : files) {
            try {
                readMetaRotationFile(f);
            } catch (OutdatedMrotException ome) {
                ome.printStackTrace();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void readMetaRotationFile(File file) throws IOException, OutdatedMrotException {
        MovingWorld.logger.info("Reading metarotation file: " + file.getName());

        BufferedReader reader = new BufferedReader(new FileReader(file));
        boolean flag = parseMetaRotations(reader);
        if (!flag && file.getName().equals("vanilla.mrot")) {
            throw new OutdatedMrotException("pre-1.4.4");
        }
        reader.close();
    }

    public void createDefaultMrot() {
        MovingWorld.logger.info("Creating vanilla.mrot");
        try {
            registerMetaRotationFile("vanilla.mrot", getClass().getResourceAsStream("/darkevilmac/movingworld/mrot/vanilla.mrot"));
        } catch (IOException e) {
            MovingWorld.instance.logger.error("UNABLE TO LOAD VANILLA.MROT");
        }
    }
}
