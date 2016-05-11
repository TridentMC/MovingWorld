package elytra.movingworld.common.network;

import com.unascribed.lambdanetwork.DataType;
import com.unascribed.lambdanetwork.LambdaNetwork;
import com.unascribed.lambdanetwork.LambdaNetworkBuilder;
import net.minecraftforge.fml.relauncher.Side;

public class CommonNetworking {

    public static LambdaNetwork NETWORK;

    public static void setupNetwork() {
        LambdaNetworkBuilder builder = LambdaNetwork.builder();
        builder.channel("MovingWorld");

        registerPackets(builder);

        NETWORK = builder.build();
    }

    private static void registerPackets(LambdaNetworkBuilder builder) {
        builder.packet("DimensionSyncMessage").boundTo(Side.CLIENT)
                .with(DataType.ARBITRARY, "dims")
                .with(DataType.BOOLEAN, "register")
                .handledOnMainThreadBy((entityPlayer, token) -> {
                    //TODO: Register/UnRegister all MovingWorlds with DimensionManager on the client.
                });
    }

}
