package darkevilmac.movingworld.common.core.world;

import darkevilmac.movingworld.common.core.IMovingWorld;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DimensionType;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.storage.WorldInfo;
import org.lwjgl.util.vector.Vector2f;

public class MovingWorldInfo extends WorldInfo {

    public IMovingWorld movingWorld;
    public WorldInfo parentWorldInfo;

    public MovingWorldInfo(WorldInfo parentWorldInfo) {
        this.parentWorldInfo = parentWorldInfo;
    }

    public MovingWorldInfo(NBTTagCompound nbt, WorldInfo parentWorldInfo, IMovingWorld movingWorld) {
        this.movingWorld = movingWorld;
        this.parentWorldInfo = parentWorldInfo;

        if (nbt.hasKey("Version", 10)) {
            NBTTagCompound nbttagcompound = nbt.getCompoundTag("Version");
            this.versionName = nbttagcompound.getString("Name");
            this.versionId = nbttagcompound.getInteger("Id");
            this.versionSnapshot = nbttagcompound.getBoolean("Snapshot");
        }

        this.randomSeed = nbt.getLong("RandomSeed");

        if (nbt.hasKey("generatorName", 8)) {
            String s1 = nbt.getString("generatorName");
            this.terrainType = WorldType.parseWorldType(s1);

            if (this.terrainType == null) {
                this.terrainType = WorldType.DEFAULT;
            } else if (this.terrainType.isVersioned()) {
                int i = 0;

                if (nbt.hasKey("generatorVersion", 99)) {
                    i = nbt.getInteger("generatorVersion");
                }

                this.terrainType = this.terrainType.getWorldTypeForGeneratorVersion(i);
            }

            if (nbt.hasKey("generatorOptions", 8)) {
                this.generatorOptions = nbt.getString("generatorOptions");
            }
        }

        this.theGameType = WorldSettings.GameType.getByID(nbt.getInteger("GameType"));

        if (nbt.hasKey("MapFeatures", 99)) {
            this.mapFeaturesEnabled = nbt.getBoolean("MapFeatures");
        } else {
            this.mapFeaturesEnabled = true;
        }

        this.spawnX = nbt.getInteger("SpawnX");
        this.spawnY = nbt.getInteger("SpawnY");
        this.spawnZ = nbt.getInteger("SpawnZ");
        this.totalTime = nbt.getLong("Time");

        if (nbt.hasKey("DayTime", 99)) {
            this.worldTime = nbt.getLong("DayTime");
        } else {
            this.worldTime = this.totalTime;
        }

        this.lastTimePlayed = nbt.getLong("LastPlayed");
        this.sizeOnDisk = nbt.getLong("SizeOnDisk");
        this.levelName = nbt.getString("LevelName");
        this.saveVersion = nbt.getInteger("version");
        this.cleanWeatherTime = nbt.getInteger("clearWeatherTime");
        this.rainTime = nbt.getInteger("rainTime");
        this.raining = nbt.getBoolean("raining");
        this.thunderTime = nbt.getInteger("thunderTime");
        this.thundering = nbt.getBoolean("thundering");
        this.hardcore = nbt.getBoolean("hardcore");

        if (nbt.hasKey("initialized", 99)) {
            this.initialized = nbt.getBoolean("initialized");
        } else {
            this.initialized = true;
        }

        if (nbt.hasKey("allowCommands", 99)) {
            this.allowCommands = nbt.getBoolean("allowCommands");
        } else {
            this.allowCommands = this.theGameType == WorldSettings.GameType.CREATIVE;
        }

        if (nbt.hasKey("Player", 10)) {
            this.playerTag = nbt.getCompoundTag("Player");
            this.dimension = this.playerTag.getInteger("Dimension");
        }

        if (nbt.hasKey("GameRules", 10)) {
            this.theGameRules.readFromNBT(nbt.getCompoundTag("GameRules"));
        }

        if (nbt.hasKey("Difficulty", 99)) {
            this.difficulty = EnumDifficulty.getDifficultyEnum(nbt.getByte("Difficulty"));
        }

        if (nbt.hasKey("DifficultyLocked", 1)) {
            this.difficultyLocked = nbt.getBoolean("DifficultyLocked");
        }

        if (nbt.hasKey("BorderCenterX", 99)) {
            this.borderCenterX = nbt.getDouble("BorderCenterX");
        }

        if (nbt.hasKey("BorderCenterZ", 99)) {
            this.borderCenterZ = nbt.getDouble("BorderCenterZ");
        }

        if (nbt.hasKey("BorderSize", 99)) {
            this.borderSize = nbt.getDouble("BorderSize");
        }

        if (nbt.hasKey("BorderSizeLerpTime", 99)) {
            this.borderSizeLerpTime = nbt.getLong("BorderSizeLerpTime");
        }

        if (nbt.hasKey("BorderSizeLerpTarget", 99)) {
            this.borderSizeLerpTarget = nbt.getDouble("BorderSizeLerpTarget");
        }

        if (nbt.hasKey("BorderSafeZone", 99)) {
            this.borderSafeZone = nbt.getDouble("BorderSafeZone");
        }

        if (nbt.hasKey("BorderDamagePerBlock", 99)) {
            this.borderDamagePerBlock = nbt.getDouble("BorderDamagePerBlock");
        }

        if (nbt.hasKey("BorderWarningBlocks", 99)) {
            this.borderWarningDistance = nbt.getInteger("BorderWarningBlocks");
        }

        if (nbt.hasKey("BorderWarningTime", 99)) {
            this.borderWarningTime = nbt.getInteger("BorderWarningTime");
        }

        if (nbt.hasKey("DimensionData", 10)) {
            NBTTagCompound nbttagcompound1 = nbt.getCompoundTag("DimensionData");

            for (String s : nbttagcompound1.getKeySet()) {
                this.dimensionData.put(DimensionType.getById(Integer.parseInt(s)), nbttagcompound1.getCompoundTag(s));
            }
        }

        if (nbt.hasKey("MovingWorld")) {
            NBTTagCompound movingWorldInfo = nbt.getCompoundTag("MovingWorld");
            int[] coreBlockPosArray = movingWorldInfo.getIntArray("CoreBlockPos");
            int[] minBlockPosArray = movingWorldInfo.getIntArray("MinBlockPos");
            int[] maxBlockPosArray = movingWorldInfo.getIntArray("MaxBlockPos");
            double posX = movingWorldInfo.getDouble("PosX");
            double posY = movingWorldInfo.getDouble("PosY");
            double posZ = movingWorldInfo.getDouble("PosZ");
            double motionX = movingWorldInfo.getDouble("MotionX");
            double motionY = movingWorldInfo.getDouble("MotionY");
            double motionZ = movingWorldInfo.getDouble("MotionZ");
            float rotationYaw = movingWorldInfo.getFloat("RotationYaw");
            float rotationPitch = movingWorldInfo.getFloat("RotationPitch");

            if (movingWorld != null) {
                movingWorld.setCoreBlock(new BlockPos(coreBlockPosArray[0], coreBlockPosArray[1], coreBlockPosArray[2]));
                movingWorld.setBounds(new BlockPos(minBlockPosArray[0], minBlockPosArray[1], minBlockPosArray[2]),
                        new BlockPos(maxBlockPosArray[0], maxBlockPosArray[1], maxBlockPosArray[2]));
                movingWorld.move(new Vec3d(posX, posY, posZ), true);
                movingWorld.setMotion(new Vec3d(motionX, motionY, motionZ));
                movingWorld.setRotation(new Vector2f(rotationYaw, rotationPitch));
            }
        }
    }

    @Override
    public void updateTagCompound(NBTTagCompound nbt, NBTTagCompound playerNbt) {
        nbt.setLong("RandomSeed", this.parentWorldInfo.getSeed());
        nbt.setString("generatorName", this.parentWorldInfo.getTerrainType().getWorldTypeName());
        nbt.setInteger("generatorVersion", this.parentWorldInfo.getTerrainType().getGeneratorVersion());
        nbt.setString("generatorOptions", this.parentWorldInfo.getGeneratorOptions());
        nbt.setInteger("GameType", this.parentWorldInfo.getGameType().getID());
        nbt.setBoolean("MapFeatures", this.parentWorldInfo.isMapFeaturesEnabled());
        nbt.setInteger("SpawnX", this.parentWorldInfo.getSpawnX());
        nbt.setInteger("SpawnY", this.parentWorldInfo.getSpawnY());
        nbt.setInteger("SpawnZ", this.parentWorldInfo.getSpawnZ());
        nbt.setLong("Time", this.parentWorldInfo.getWorldTotalTime());
        nbt.setLong("DayTime", this.parentWorldInfo.getWorldTime());
        nbt.setLong("SizeOnDisk", this.parentWorldInfo.getSizeOnDisk());
        nbt.setLong("LastPlayed", MinecraftServer.getCurrentTimeMillis());
        nbt.setString("LevelName", this.parentWorldInfo.getWorldName());
        nbt.setInteger("version", this.parentWorldInfo.getSaveVersion());
        nbt.setInteger("clearWeatherTime", this.parentWorldInfo.getCleanWeatherTime());
        nbt.setInteger("rainTime", this.parentWorldInfo.getRainTime());
        nbt.setBoolean("raining", this.parentWorldInfo.isRaining());
        nbt.setInteger("thunderTime", this.parentWorldInfo.getThunderTime());
        nbt.setBoolean("thundering", this.parentWorldInfo.isThundering());
        nbt.setBoolean("hardcore", this.parentWorldInfo.isHardcoreModeEnabled());
        nbt.setBoolean("allowCommands", this.parentWorldInfo.areCommandsAllowed());
        nbt.setBoolean("initialized", this.parentWorldInfo.isInitialized());
        nbt.setDouble("BorderCenterX", this.parentWorldInfo.getBorderCenterX());
        nbt.setDouble("BorderCenterZ", this.parentWorldInfo.getBorderCenterZ());
        nbt.setDouble("BorderSize", this.parentWorldInfo.getBorderSize());
        nbt.setLong("BorderSizeLerpTime", this.parentWorldInfo.getBorderLerpTime());
        nbt.setDouble("BorderSafeZone", this.parentWorldInfo.getBorderSafeZone());
        nbt.setDouble("BorderDamagePerBlock", this.parentWorldInfo.getBorderDamagePerBlock());
        nbt.setDouble("BorderSizeLerpTarget", this.parentWorldInfo.getBorderSize());
        nbt.setDouble("BorderWarningBlocks", (double) this.parentWorldInfo.getBorderWarningDistance());
        nbt.setDouble("BorderWarningTime", (double) this.parentWorldInfo.getBorderWarningTime());

        if (movingWorld != null) {
            //Save MovingWorld stuff.
            NBTTagCompound movingWorldInfo = new NBTTagCompound();
            movingWorldInfo.setIntArray("CoreBlockPos", new int[]{movingWorld.coreBlock().getX(), movingWorld.coreBlock().getY(), movingWorld.coreBlock().getZ()});
            movingWorldInfo.setIntArray("MinBlockPos", new int[]{movingWorld.min().getX(), movingWorld.min().getY(), movingWorld.min().getZ()});
            movingWorldInfo.setIntArray("MaxBlockPos", new int[]{movingWorld.max().getX(), movingWorld.max().getY(), movingWorld.max().getZ()});
            movingWorldInfo.setDouble("PosX", movingWorld.worldTranslation().xCoord);
            movingWorldInfo.setDouble("PosY", movingWorld.worldTranslation().yCoord);
            movingWorldInfo.setDouble("PosZ", movingWorld.worldTranslation().zCoord);
            movingWorldInfo.setDouble("MotionX", movingWorld.motion().xCoord);
            movingWorldInfo.setDouble("MotionY", movingWorld.motion().yCoord);
            movingWorldInfo.setDouble("MotionZ", movingWorld.motion().zCoord);
            movingWorldInfo.setFloat("RotationYaw", movingWorld.rotation().x);
            movingWorldInfo.setFloat("RotationPitch", movingWorld.rotation().y);
            nbt.setTag("MovingWorld", movingWorldInfo);
        }
        if (this.parentWorldInfo.getDifficulty() != null) {
            nbt.setByte("Difficulty", (byte) this.parentWorldInfo.getDifficulty().getDifficultyId());
        }

        nbt.setBoolean("DifficultyLocked", this.parentWorldInfo.isDifficultyLocked());
        nbt.setTag("GameRules", this.parentWorldInfo.getGameRulesInstance().writeToNBT());

        if (playerNbt != null) {
            nbt.setTag("Player", playerNbt);
        }
    }

    @Override
    public EnumDifficulty getDifficulty() {
        return parentWorldInfo.getDifficulty();
    }


}
