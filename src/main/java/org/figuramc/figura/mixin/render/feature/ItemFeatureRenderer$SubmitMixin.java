package org.figuramc.figura.mixin.render.feature;

import net.minecraft.client.renderer.feature.ItemFeatureRenderer;
import org.figuramc.figura.ducks.FiguraCallbackHolder;
import org.figuramc.figura.model.rendering.buffers.FiguraRenderCallbacks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ItemFeatureRenderer.Submit.class)
public class ItemFeatureRenderer$SubmitMixin implements FiguraCallbackHolder {
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
