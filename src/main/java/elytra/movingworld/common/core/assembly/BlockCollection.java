package elytra.movingworld.common.core.assembly;


import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

import java.util.HashMap;
import java.util.Iterator;

/**
 * Get a blockstate and a tile (if applicable) from a position. Contains some nice utility methods.
 */
public class BlockCollection implements Iterable<BlockEntry> {

    protected HashMap<BlockPos, BlockEntry> internalMap;
    private Vec3i min;
    private Vec3i max;

    public BlockCollection(Vec3i ORIGIN) {
        internalMap = new HashMap<BlockPos, BlockEntry>();
        min = new Vec3i(ORIGIN.getX(), ORIGIN.getY(), ORIGIN.getZ());
        max = new Vec3i(ORIGIN.getX(), ORIGIN.getY(), ORIGIN.getZ());
    }

    public Vec3i getMin() {
        return min;
    }

    public Vec3i getMax() {
        return max;
    }

    @Override
    public Iterator<BlockEntry> iterator() {
        return internalMap.values().iterator();
    }

    public void addToMap(BlockPos pos, IBlockState state, TileEntity tileEntity) {
        internalMap.put(pos, new BlockEntry(pos, state, tileEntity));

        min = new Vec3i(pos.getX() < min.getX() ? pos.getX() : min.getX(),
                pos.getY() < min.getY() ? pos.getY() : min.getY(),
                pos.getZ() < min.getZ() ? pos.getZ() : min.getZ());

        max = new Vec3i(pos.getX() > max.getX() ? pos.getX() : max.getX(),
                pos.getY() > max.getY() ? pos.getY() : max.getY(),
                pos.getZ() > max.getZ() ? pos.getZ() : max.getZ());
    }

    public void addToMap(BlockPos pos, IBlockState state) {
        addToMap(pos, state, null);
    }

    public IBlockState getBlockState(BlockPos key) {
        return internalMap.get(key).state;
    }

    public TileEntity getTile(BlockPos key) {
        return internalMap.get(key).tile;
    }

    public int size() {
        return internalMap.size();
    }

    public boolean containsBlockAtPosition(BlockPos pos) {
        return internalMap.containsKey(pos);
    }

    /**
     * Shifts the position of all blocks/tiles in this map by @param pos specified.
     *
     * @param shiftMode if true add pos, if false subtract pos
     */
    public void shiftPosition(BlockPos pos, boolean shiftMode) {
        HashMap<BlockPos, BlockEntry> newMap = new HashMap<BlockPos, BlockEntry>(); // Temp storage for the moved tiles.
        Vec3i newMin = new Vec3i(0, 0, 0);
        Vec3i newMax = new Vec3i(0, 0, 0);

        Iterator<BlockEntry> iterator = this.iterator();
        while (iterator.hasNext()) {
            BlockEntry entry = iterator.next();

            BlockPos posOfIndex = entry.pos;
            IBlockState stateOfIndex = entry.state;
            TileEntity tileOfIndex = entry.tile;

            BlockPos shiftedPos = new BlockPos(posOfIndex);
            shiftedPos = shiftMode ? shiftedPos.add(pos) : shiftedPos.subtract(pos);
            TileEntity shiftedTile = null;

            if (tileOfIndex != null) {
                shiftedTile = TileEntity.createTileEntity(null /*null because it's never even used -.-*/, tileOfIndex.serializeNBT());
                shiftedTile.setPos(shiftedPos);
            }

            newMin = new Vec3i(shiftedPos.getX() < newMin.getX() ? shiftedPos.getX() : newMin.getX(),
                    shiftedPos.getY() < newMin.getY() ? shiftedPos.getY() : newMin.getY(),
                    shiftedPos.getZ() < newMin.getZ() ? shiftedPos.getZ() : newMin.getZ());

            newMax = new Vec3i(shiftedPos.getX() > newMax.getX() ? shiftedPos.getX() : newMax.getX(),
                    shiftedPos.getY() > newMax.getY() ? shiftedPos.getY() : newMax.getY(),
                    shiftedPos.getZ() > newMax.getZ() ? shiftedPos.getZ() : newMax.getZ());

            newMap.put(shiftedPos, new BlockEntry(shiftedPos, stateOfIndex, tileOfIndex));
        }

        this.min = newMin;
        this.max = newMax;
        internalMap.clear();
        internalMap.putAll(newMap);
    }
}
