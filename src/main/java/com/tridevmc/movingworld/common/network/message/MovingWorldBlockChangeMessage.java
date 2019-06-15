package com.tridevmc.movingworld.common.network.message;


import com.tridevmc.compound.network.message.Message;
import com.tridevmc.compound.network.message.RegisteredMessage;
import com.tridevmc.movingworld.common.chunk.CompressedChunkData;
import com.tridevmc.movingworld.common.entity.EntityMovingWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.fml.LogicalSide;

/**
 * Sends MobileChunk block data to clients.
 */
@RegisteredMessage(channel = "movingworld", destination = LogicalSide.CLIENT)
public class MovingWorldBlockChangeMessage extends Message {

    public EntityMovingWorld movingWorld;
    public CompressedChunkData compressedChunkData;

    public MovingWorldBlockChangeMessage() {
        super();
    }

    public MovingWorldBlockChangeMessage(EntityMovingWorld movingWorld, CompressedChunkData compressedChunkData) {
        super();
        this.movingWorld = movingWorld;
        this.compressedChunkData = compressedChunkData;
    }

    @Override
    public void handle(PlayerEntity sender) {
        if (movingWorld == null || movingWorld.getMobileChunk() == null || compressedChunkData == null)
            return;
        compressedChunkData.loadBlocks(movingWorld.getMobileChunk());
    }
}
