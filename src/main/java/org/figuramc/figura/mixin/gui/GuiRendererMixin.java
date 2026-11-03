package org.figuramc.figura.mixin.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.client.renderer.state.gui.pip.GuiEntityRenderState;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import org.figuramc.figura.ducks.GuiEntityRenderStateExtension;
import org.figuramc.figura.gui.FiguraGuiEntityRenderer;
import org.figuramc.figura.gui.FiguraHudRenderer;
import org.figuramc.figura.gui.FiguraPortraitRenderer;
import org.figuramc.figura.model.rendering.EntityRenderMode;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;

@Mixin(GuiRenderer.class)
public class GuiRendererMixin {

    @Shadow @Final
    private GuiRenderState renderState;

    @Shadow @Final
    private FeatureRenderDispatcher featureRenderDispatcher;

    @Shadow @Final
    private Map<Class<? extends PictureInPictureRenderState>, PictureInPictureRenderer<?>> pictureInPictureRenderers;

    // separate renderer (own texture) so the paperdoll does not collide with other entity previews in the same frame
    @Unique
    private FiguraGuiEntityRenderer figura$paperDollRenderer;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void figura$init(GuiRenderState guiRenderState, FeatureRenderDispatcher dispatcher, List<PictureInPictureRenderer<?>> list, CallbackInfo ci) {
        figura$paperDollRenderer = new FiguraGuiEntityRenderer(Minecraft.getInstance().getEntityRenderDispatcher());

        FiguraPortraitRenderer portraitRenderer = new FiguraPortraitRenderer();
        pictureInPictureRenderers.put(portraitRenderer.getRenderStateClass(), portraitRenderer);
        FiguraHudRenderer hudRenderer = new FiguraHudRenderer();
        pictureInPictureRenderers.put(hudRenderer.getRenderStateClass(), hudRenderer);
    }

    @Inject(method = "preparePictureInPictureState", at = @At("HEAD"), cancellable = true)
    private <T extends PictureInPictureRenderState> void figura$renderPaperDoll(T state, int guiScale, CallbackInfo ci) {
        if (state instanceof GuiEntityRenderStateExtension extension && extension.getRenderMode() == EntityRenderMode.PAPERDOLL) {
            figura$paperDollRenderer.prepare((GuiEntityRenderState) state, this.renderState, this.featureRenderDispatcher, guiScale);
            ci.cancel();
        }
    }

    @Inject(method = "close", at = @At("TAIL"))
    private void figura$close(CallbackInfo ci) {
        figura$paperDollRenderer.close();
    }
}
