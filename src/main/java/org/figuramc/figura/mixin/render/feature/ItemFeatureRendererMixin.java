package org.figuramc.figura.mixin.render.feature;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.feature.ItemFeatureRenderer;
import net.minecraft.client.renderer.feature.RenderTypeFeatureRenderer;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.ducks.FiguraCallbackHolder;
import org.figuramc.figura.model.rendering.buffers.DirectBufferSource;
import org.figuramc.figura.model.rendering.buffers.FiguraRenderCallbacks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// runs Figura callbacks around vanilla item rendering
@Mixin(ItemFeatureRenderer.class)
public abstract class ItemFeatureRendererMixin extends RenderTypeFeatureRenderer<ItemFeatureRenderer.Submit> {

    @Inject(method = "prepareSubmit", at = @At("HEAD"), cancellable = true)
    private void figura$preRender(ItemFeatureRenderer.Submit submit, CallbackInfo ci) {
        FiguraRenderCallbacks callbacks = ((FiguraCallbackHolder) (Object) submit).figura$getCallbacks();
        if (callbacks == null)
            return;

        boolean render = true;
        try {
            PoseStack poseStack = new PoseStack();
            poseStack.last().set(submit.pose());
            poseStack.pushPose();
            render = callbacks.runPre(new DirectBufferSource(this::getVertexBuilder), poseStack);
        } catch (Exception e) {
            FiguraMod.LOGGER.error("Figura pre render callback failed", e);
        }

        if (!render) {
            figura$runPost(callbacks);
            ci.cancel();
        }
    }

    @Inject(method = "prepareSubmit", at = @At("RETURN"))
    private void figura$postRender(ItemFeatureRenderer.Submit submit, CallbackInfo ci) {
        FiguraRenderCallbacks callbacks = ((FiguraCallbackHolder) (Object) submit).figura$getCallbacks();
        if (callbacks != null)
            figura$runPost(callbacks);
    }

    @Unique
    private static void figura$runPost(FiguraRenderCallbacks callbacks) {
        try {
            callbacks.runPost();
        } catch (Exception e) {
            FiguraMod.LOGGER.error("Figura post render callback failed", e);
        }
    }
}
