package darkevilmac.movingworld.common.core.entity;

import com.mojang.authlib.GameProfile;
import darkevilmac.movingworld.common.core.IMovingWorld;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.world.WorldServer;

public class EntityPlayerProxy extends EntityPlayerMP {

    public EntityPlayerMP entityPlayerMP;
    public IMovingWorld movingWorld;

    public EntityPlayerProxy(MinecraftServer server, WorldServer worldIn, GameProfile profile, PlayerInteractionManager interactionManager) {
        super(server, worldIn, profile, interactionManager);
    }
}
