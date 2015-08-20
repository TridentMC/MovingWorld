package darkevilmac.movingworld.common.util;

import com.google.common.collect.HashBiMap;
import darkevilmac.movingworld.common.chunk.LocatedBlock;
import net.minecraft.world.ChunkPosition;

import java.util.ArrayList;

public class LocatedBlockList extends ArrayList<LocatedBlock> {

    private HashBiMap<ChunkPosition, LocatedBlock> posMap;

    public LocatedBlockList() {
        super();
        posMap = HashBiMap.create();
    }

    public LocatedBlockList(int initialSize) {
        super(initialSize);
    }

    @Override
    public boolean add(LocatedBlock locatedBlock) {
        if (!posMap.containsKey(locatedBlock.coords))
            posMap.put(locatedBlock.coords, locatedBlock);
        return super.add(locatedBlock);
    }

    @Override
    public void add(int index, LocatedBlock locatedBlock) {
        if (!posMap.containsKey(locatedBlock.coords))
            posMap.put(locatedBlock.coords, locatedBlock);
        super.add(index, locatedBlock);
    }

    public LocatedBlock getLBOfPos(ChunkPosition pos) {
        return posMap.get(pos);
    }

    public boolean containsLBOfPos(ChunkPosition pos) {
        return posMap.containsKey(pos);
    }

}
