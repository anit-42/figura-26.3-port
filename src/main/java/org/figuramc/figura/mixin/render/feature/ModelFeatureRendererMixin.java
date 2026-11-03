package org.figuramc.figura.mixin.render.feature;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.feature.RenderTypeFeatureRenderer;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.ducks.FiguraCallbackHolder;
import org.figuramc.figura.model.rendering.buffers.DirectBufferSource;
import org.figuramc.figura.model.rendering.buffers.FiguraRenderCallbacks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

// runs Figura callbacks around vanilla model rendering, and can skip vanilla's animation setup
@Mixin(ModelFeatureRenderer.class)
public abstract class ModelFeatureRendererMixin extends RenderTypeFeatureRenderer<ModelFeatureRenderer.Submit<?>> {

    @WrapOperation(method = "prepareModel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/Model;setupAnim(Ljava/lang/Object;)V"))
    private <S> void figura$setupAnim(Model<? super S> model, Object state, Operation<Void> original, ModelFeatureRenderer.Submit<S> submit) {
        FiguraRenderCallbacks callbacks = ((FiguraCallbackHolder) (Object) submit).figura$getCallbacks();
        if (callbacks != null && callbacks.preventAnimSetup())
            return;
        original.call(model, state);
    }

    @WrapOperation(method = "prepareModel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/Model;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V"))
    private <S> void figura$renderToBuffer(Model<? super S> model, PoseStack poseStack, VertexConsumer consumer, int light, int overlay, int color, Operation<Void> original, ModelFeatureRenderer.Submit<S> submit) {
        FiguraRenderCallbacks callbacks = ((FiguraCallbackHolder) (Object) submit).figura$getCallbacks();
        if (callbacks == null) {
            original.call(model, poseStack, consumer, light, overlay, color);
            return;
        }

        boolean render = true;
        try {
            // callbacks get their own stack (with a spare level) so their push/pop cannot unbalance vanilla's
            PoseStack callbackStack = new PoseStack();
            callbackStack.last().set(poseStack.last());
            callbackStack.pushPose();
            render = callbacks.runPre(new DirectBufferSource(this::getVertexBuilder), callbackStack);
        } catch (Exception e) {
            FiguraMod.LOGGER.error("Figura pre render callback failed", e);
        }

        if (render)
            original.call(model, poseStack, consumer, light, overlay, color);

        try {
            callbacks.runPost();
        } catch (Exception e) {
            FiguraMod.LOGGER.error("Figura post render callback failed", e);
        }
    }
}
