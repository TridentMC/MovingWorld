package darkevilmac.movingworld.asm.mixin.core.client.renderer;

import darkevilmac.movingworld.entity.ISelectableEntity;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@SideOnly(Side.CLIENT)
@Mixin(EntityRenderer.class)
public class MixinEntityRenderer {

    @Redirect(method = "getMouseOver", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;canBeCollidedWith()Z"))
    public boolean onCanCollide(Entity entity, float partialTicks) {
        return ((ISelectableEntity) entity).canBeSelected();
    }

}
