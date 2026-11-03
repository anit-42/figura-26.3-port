package org.figuramc.figura.mixin.render;

import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.math.vector.FiguraVec3;
import org.figuramc.figura.utils.RenderUtils;
import org.figuramc.figura.utils.ui.UIHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Camera.class)
public abstract class CameraMixin {

    @Shadow private float xRot;
    @Shadow private float yRot;

    @Unique private Avatar avatar;

    @Shadow protected abstract void setRotation(float yaw, float pitch);
    @Shadow protected abstract void move(float x, float y, float z);

    @Shadow private Entity entity;

    // 26.x: Camera#setup was replaced by alignWithEntity(partialTick)
    @Inject(method = "alignWithEntity", at = @At(value = "HEAD"))
    private void setupAvatarVar(float f, CallbackInfo ci) {
        avatar = AvatarManager.getAvatar(this.entity);
    }

    // Neo adds roll in addition to pitch and yaw
    @Inject(method = "alignWithEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setRotation(FF)V", shift = At.Shift.AFTER))
    private void setupRot(float f, CallbackInfo ci) {
        if (!RenderUtils.vanillaModelAndScript(avatar)) {
            avatar = null;
            return;
        }

        float x = xRot;
        float y = yRot;

        FiguraVec3 rot = avatar.luaRuntime.renderer.cameraRot;
        if (rot != null && rot.notNaN()) {
            x = (float) rot.x;
            y = (float) rot.y;
        }

        FiguraVec3 offset = avatar.luaRuntime.renderer.cameraOffsetRot;
        if (offset != null && offset.notNaN()) {
            x += (float) offset.x;
            y += (float) offset.y;
        }

        setRotation(y, x);
    }

    @ModifyArg(method = "alignWithEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setPosition(DDD)V"), index = 0)
    private double setupPivotX(double originalX) {
        if (RenderUtils.vanillaModelAndScript(avatar)) {
            double x = originalX;

            FiguraVec3 piv = avatar.luaRuntime.renderer.cameraPivot;
            if (piv != null && piv.notNaN()) {
                x = piv.x;
            }

            FiguraVec3 offset = avatar.luaRuntime.renderer.cameraOffsetPivot;
            if (offset != null && offset.notNaN()) {
                x += offset.x;
            }
            return x;
        }
        return originalX;
    }

    @ModifyArg(method = "alignWithEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setPosition(DDD)V"), index = 1)
    private double setupPivotY(double originalY) {
        if (RenderUtils.vanillaModelAndScript(avatar)) {
            double y = originalY;

            FiguraVec3 piv = avatar.luaRuntime.renderer.cameraPivot;
            if (piv != null && piv.notNaN()) {
                y = piv.y;
            }

            FiguraVec3 offset = avatar.luaRuntime.renderer.cameraOffsetPivot;
            if (offset != null && offset.notNaN()) {
                y += offset.y;
            }
            return y;
        }
        return originalY;
    }

    @ModifyArg(method = "alignWithEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setPosition(DDD)V"), index = 2)
    private double setupPivotZ(double originalZ) {
        if (RenderUtils.vanillaModelAndScript(avatar)) {
            double z = originalZ;

            FiguraVec3 piv = avatar.luaRuntime.renderer.cameraPivot;
            if (piv != null && piv.notNaN()) {
                z = piv.z;
            }

            FiguraVec3 offset = avatar.luaRuntime.renderer.cameraOffsetPivot;
            if (offset != null && offset.notNaN()) {
                z += offset.z;
            }
            return z;
        }
        return originalZ;
    }


    @Inject(method = "alignWithEntity", at = @At(value = "RETURN"))
    private void setupPos(float f, CallbackInfo ci) {
        if (RenderUtils.vanillaModelAndScript(avatar)) {
            FiguraVec3 pos = avatar.luaRuntime.renderer.cameraPos;
            if (pos != null && pos.notNaN())
                move((float) -pos.z, (float) pos.y, (float) -pos.x);

            avatar = null;
        }
    }

    // camera roll and matrix override (moved here from GameRenderer#renderLevel in 26.x)
    @com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation(method = "getViewRotationMatrix", at = @At(value = "INVOKE", target = "Lorg/joml/Matrix4f;rotation(Lorg/joml/Quaternionfc;)Lorg/joml/Matrix4f;"))
    private org.joml.Matrix4f figura$viewRotation(org.joml.Matrix4f instance, org.joml.Quaternionfc quat, com.llamalad7.mixinextras.injector.wrapoperation.Operation<org.joml.Matrix4f> original) {
        Avatar av = AvatarManager.getAvatar(this.entity);
        if (!RenderUtils.vanillaModelAndScript(av))
            return original.call(instance, quat);

        float z = 0f;
        FiguraVec3 rot = av.luaRuntime.renderer.cameraRot;
        if (rot != null)
            z = (float) rot.z;
        FiguraVec3 offset = av.luaRuntime.renderer.cameraOffsetRot;
        if (offset != null)
            z += (float) offset.z;

        instance.identity();
        instance.rotate(com.mojang.math.Axis.ZP.rotationDegrees(z));

        org.figuramc.figura.math.matrix.FiguraMat4 mat = av.luaRuntime.renderer.cameraMat;
        if (mat != null)
            instance.set(mat.toMatrix4f());

        instance.rotate(quat);
        return instance;
    }

    @Inject(method = "xRot", at = @At("HEAD"), cancellable = true)
    private void xRot(CallbackInfoReturnable<Float> cir) {
        if (UIHelper.paperdoll)
            cir.setReturnValue(0f);
    }

    @Inject(method = "yRot", at = @At("HEAD"), cancellable = true)
    private void yRot(CallbackInfoReturnable<Float> cir) {
        if (UIHelper.paperdoll)
            cir.setReturnValue(0f);
    }
}
