package org.figuramc.figura.mixin.render.feature;

import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import org.figuramc.figura.ducks.FiguraCallbackHolder;
import org.figuramc.figura.model.rendering.buffers.FiguraRenderCallbacks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ModelFeatureRenderer.Submit.class)
public class ModelFeatureRenderer$SubmitMixin implements FiguraCallbackHolder {
    @Unique
    private FiguraRenderCallbacks figura$callbacks;

    @Override
    public FiguraRenderCallbacks figura$getCallbacks() {
        return figura$callbacks;
    }

    @Override
    public void figura$setCallbacks(FiguraRenderCallbacks callbacks) {
        this.figura$callbacks = callbacks;
    }
}
