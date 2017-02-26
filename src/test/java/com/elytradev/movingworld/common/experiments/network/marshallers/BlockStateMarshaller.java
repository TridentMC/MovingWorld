package com.elytradev.movingworld.common.experiments.network.marshallers;

import com.elytradev.concrete.Marshaller;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraftforge.fml.common.network.ByteBufUtils;

/**
 * Created by darkevilmac on 2/24/2017.
 */
public class BlockStateMarshaller implements Marshaller<IBlockState> {
    public static final String MARSHALLER_NAME = "com.elytradev.movingworld.common.experiments.network.marshallers.BlockStateMarshaller";
    public static final BlockStateMarshaller INSTANCE = new BlockStateMarshaller();

    @Override
    public IBlockState unmarshal(ByteBuf in) {
        return Block.getStateById(ByteBufUtils.readVarInt(in, 5));
    }

    @Override
    public void marshal(ByteBuf out, IBlockState iBlockState) {
        ByteBufUtils.writeVarInt(out, Block.getStateId(iBlockState), 5);
    }
}
