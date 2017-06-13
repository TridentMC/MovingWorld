package com.elytradev.movingworld.common.experiments.network.messages.server;

import com.elytradev.concrete.network.Message;
import com.elytradev.concrete.network.NetworkContext;
import com.elytradev.concrete.network.annotation.field.MarshalledAs;
import com.elytradev.concrete.network.annotation.type.ReceivedOn;
import com.elytradev.movingworld.client.experiments.EntityPlayerSPProxy;
import com.elytradev.movingworld.common.experiments.MovingWorldExperimentsMod;
import com.elytradev.movingworld.common.experiments.network.MovingWorldExperimentsNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Handles opening of forge mod guis on clients, sourced from a movingworld.
 */

@ReceivedOn(Side.CLIENT)
public class MessageOpenGui extends Message {

    public BlockPos pos;
    public String modId;

    @MarshalledAs("int")
    public int modGuiId, windowId, dimension;

    public MessageOpenGui(NetworkContext ctx) {
        super(ctx);
    }

    public MessageOpenGui(BlockPos pos, String modId, int modGuiId, int windowId, int dimension) {
        super(MovingWorldExperimentsNetworking.networkContext);
        this.pos = pos;
        this.modId = modId;
        this.modGuiId = modGuiId;
        this.windowId = windowId;
        this.dimension = dimension;
    }

    @Override
    protected void handle(EntityPlayer sender) {
        EntityPlayer player = Minecraft.getMinecraft().player;
        EntityPlayer proxy = EntityPlayerSPProxy.getProxyForPlayer((EntityPlayerSP) player, null, false);

        if (proxy == null)
            return; // nope, im out.

        WorldClient movingWorld = (WorldClient) MovingWorldExperimentsMod.modProxy.getClientDB().getWorldFromDim(dimension);
        proxy.openGui(modId, modGuiId, movingWorld, pos.getX(), pos.getY(), pos.getZ());
        proxy.openContainer.windowId = windowId;
    }
}
