package org.figuramc.figura.mixin.render;

import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.utils.RenderUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// 26.x: FOV state moved from GameRenderer to Camera
@Mixin(Camera.class)
public class CameraFovMixin {
    @Shadow private float fovModifier;
    @Shadow private Entity entity;

    @Inject(method = "tickFov", at = @At("RETURN"))
    private void figura$tickFov(CallbackInfo ci) {
        Avatar avatar = AvatarManager.getAvatar(this.entity);
        if (RenderUtils.vanillaModelAndScript(avatar)) {
            Float fov = avatar.luaRuntime.renderer.fov;
            if (fov != null) this.fovModifier = fov;
        }
    }
}
