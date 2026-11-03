package org.figuramc.figura.mixin.render;

import org.figuramc.figura.model.rendering.buffers.FiguraSubmitter;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.ducks.FiguraEntityRenderStateExtension;
import org.figuramc.figura.math.vector.FiguraVec4;
import org.figuramc.figura.model.rendering.EntityRenderMode;
import org.figuramc.figura.utils.ColorUtils;
import org.figuramc.figura.utils.RenderUtils;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {

    @Shadow @Final private EntityRenderDispatcher entityRenderDispatcher;
    @Shadow @Final private RenderBuffers renderBuffers;

    @ModifyArg(method = "submitEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EntityRenderDispatcher;submit(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lnet/minecraft/client/renderer/state/level/CameraRenderState;DDDLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;)V"))
    private <S extends EntityRenderState> S renderLevelRenderEntity(S entityRenderState) {
        Avatar avatar = AvatarManager.getAvatar(entityRenderState);
        if (avatar != null)
            avatar.renderMode = EntityRenderMode.RENDER;
        return entityRenderState;
    }

    @Inject(method = "submitEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EntityRenderDispatcher;submit(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lnet/minecraft/client/renderer/state/level/CameraRenderState;DDDLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;)V"))
    private <S extends EntityRenderState> void renderEntity(PoseStack poseStack, LevelRenderState levelRenderState, SubmitNodeCollector submitNodeCollector, CallbackInfo ci, @Local S entityRenderState) {
        Integer entityId = ((FiguraEntityRenderStateExtension)entityRenderState).figura$getEntityId();
        if (entityId == null) return;
        Entity entity = Minecraft.getInstance().level.getEntity(entityId);
        float tickDelta = ((FiguraEntityRenderStateExtension)entityRenderState).figura$getTickDelta();

        Avatar av = AvatarManager.getAvatar(entityRenderState);

        if (av == null)
            return;


        
        FiguraSubmitter.submitFiguraModel(submitNodeCollector, av, entityRenderState, ((avatar, entityState, multiBufferSource) -> {

            // 26.x: the outline color travels with the render state
            if (entityState.outlineColor != 0 && RenderUtils.vanillaModelAndScript(avatar) && avatar.luaRuntime.renderer.outlineColor != null)
                entityState.outlineColor = ColorUtils.rgbToInt(avatar.luaRuntime.renderer.outlineColor) | 0xFF000000;

            FiguraMod.pushProfiler(FiguraMod.MOD_ID);
            FiguraMod.pushProfiler(avatar);
            FiguraMod.pushProfiler("worldRender");
            Vec3 cameraPos = levelRenderState.cameraRenderState.pos;

            avatar.worldRender(entity, cameraPos.x(), cameraPos.y(), cameraPos.z(), poseStack, multiBufferSource, entityRenderDispatcher.getPackedLightCoords(entity, tickDelta), tickDelta, EntityRenderMode.WORLD);

            FiguraMod.popProfiler(3);

            return null;
        }));

    }

    // 26.x: LevelRenderer#renderLevel became LevelRenderer#render (submits happen inside it)
    @Inject(method = "render", at = @At("HEAD"))
    private void onRenderLevel(CallbackInfo ci) {
        float delta = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
        AvatarManager.executeAll("worldRender", avatar -> avatar.render(delta));
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void afterRenderLevel(CallbackInfo ci) {
        float delta = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
        AvatarManager.executeAll("postWorldRender", avatar -> avatar.postWorldRenderEvent(delta));
    }

    @org.spongepowered.asm.mixin.injection.ModifyVariable(method = "submitHitOutline", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private int renderHitOutline(int colorInt) {
        Avatar avatar = AvatarManager.getAvatar(Minecraft.getInstance().getCameraEntity());
        FiguraVec4 color;

        if (avatar == null || avatar.luaRuntime == null || (color = avatar.luaRuntime.renderer.blockOutlineColor) == null)
            return colorInt;

        return ARGB.colorFromFloat((float) color.w, (float) color.x, (float) color.y, (float) color.z);
    }
}