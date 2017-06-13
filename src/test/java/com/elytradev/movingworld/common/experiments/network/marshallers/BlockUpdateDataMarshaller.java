package com.elytradev.movingworld.common.experiments.network.marshallers;

import com.elytradev.concrete.network.Marshaller;
import com.elytradev.movingworld.common.experiments.network.messages.server.MessageMultiBlockChange;
import io.netty.buffer.ByteBuf;

/**
 * Created by darkevilmac on 2/25/2017.
 */
public class BlockUpdateDataMarshaller implements Marshaller<MessageMultiBlockChange.BlockUpdateData> {

    public static final String MARSHALLER_NAME = "com.elytradev.movingworld.common.experiments.network.marshallers.BlockUpdateDataMarshaller";
    public static final BlockUpdateDataMarshaller INSTANCE = new BlockUpdateDataMarshaller();

    @Override
    public MessageMultiBlockChange.BlockUpdateData unmarshal(ByteBuf in) {
        MessageMultiBlockChange.BlockUpdateData blockUpdateData = new MessageMultiBlockChange.BlockUpdateData();
        blockUpdateData.readFromNetwork(in);
        return blockUpdateData;
    }

    @Override
    public void marshal(ByteBuf out, MessageMultiBlockChange.BlockUpdateData blockUpdateData) {
        blockUpdateData.writeToNetwork(out);
    }
}
