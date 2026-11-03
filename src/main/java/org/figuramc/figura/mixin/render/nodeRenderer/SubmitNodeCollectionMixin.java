package org.figuramc.figura.mixin.render.nodeRenderer;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.feature.ItemFeatureRenderer;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.UvMapping;
import net.minecraft.client.resources.model.geometry.ItemQuads;
import net.minecraft.world.item.ItemDisplayContext;
import org.figuramc.figura.ducks.FiguraCallbackHolder;
import org.figuramc.figura.model.rendering.buffers.FiguraRenderCallbacks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Moves the pre/post render callbacks Figura put on a Model / ModelPart / ItemDisplayContext
 * onto the submit records vanilla creates, so they run when the feature renderer prepares them.
 */
@Mixin(SubmitNodeCollection.class)
public class SubmitNodeCollectionMixin {
    @Unique
    private FiguraRenderCallbacks figura$pendingModelCallbacks;
    @Unique
    private FiguraRenderCallbacks figura$pendingItemCallbacks;

    @Inject(method = "submitModel", at = @At("HEAD"))
    private <S> void figura$captureModelCallbacks(Model<? super S> model, S state, PoseStack poseStack, RenderType renderType, int light, int overlay, int tint, UvMapping uvMapping, int outline, CallbackInfo ci) {
        // Model.Simple wraps a single ModelPart (submitModelPart), so look at its root part too
        figura$pendingModelCallbacks = FiguraRenderCallbacks.drain(model, model.root());
    }

    @ModifyExpressionValue(method = "submitModel", at = @At(value = "NEW", target = "net/minecraft/client/renderer/feature/ModelFeatureRenderer$Submit"))
    private ModelFeatureRenderer.Submit<?> figura$attachModelCallbacks(ModelFeatureRenderer.Submit<?> submit) {
        if (figura$pendingModelCallbacks != null)
            ((FiguraCallbackHolder) (Object) submit).figura$setCallbacks(figura$pendingModelCallbacks);
        return submit;
    }

    @Inject(method = "submitModel", at = @At("RETURN"))
    private void figura$clearModelCallbacks(CallbackInfo ci) {
        figura$pendingModelCallbacks = null;
    }

    @Inject(method = "submitItem", at = @At("HEAD"))
    private void figura$captureItemCallbacks(PoseStack poseStack, ItemDisplayContext displayContext, int light, int overlay, int outline, int[] tints, ItemQuads quads, ItemStackRenderState.FoilType foilType, CallbackInfo ci) {
        figura$pendingItemCallbacks = FiguraRenderCallbacks.drain(displayContext);
    }

    @ModifyExpressionValue(method = "submitItem", at = @At(value = "NEW", target = "net/minecraft/client/renderer/feature/ItemFeatureRenderer$Submit"))
    private ItemFeatureRenderer.Submit figura$attachItemCallbacks(ItemFeatureRenderer.Submit submit) {
        if (figura$pendingItemCallbacks != null)
            ((FiguraCallbackHolder) (Object) submit).figura$setCallbacks(figura$pendingItemCallbacks);
        return submit;
    }

    @Inject(method = "submitItem", at = @At("RETURN"))
    private void figura$clearItemCallbacks(CallbackInfo ci) {
        figura$pendingItemCallbacks = null;
    }
}
