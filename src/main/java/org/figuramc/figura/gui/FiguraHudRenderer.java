package org.figuramc.figura.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.model.rendering.buffers.RecordingBufferSource;
import org.joml.Matrix4f;

/**
 * Renders the HUD parent parts of the camera entity's avatar.
 * The PiP texture covers the whole GUI; the pose is moved so the origin is the GUI's top-left corner,
 * one unit = one GUI pixel, y pointing down, matching what HUD parts got before 26.x.
 */
public class FiguraHudRenderer extends PictureInPictureRenderer<FiguraHudRenderState> {

    @Override
    public Class<FiguraHudRenderState> getRenderStateClass() {
        return FiguraHudRenderState.class;
    }

    @Override
    protected float getTranslateY(int height, int guiScale) {
        // put the origin at the top edge of the texture instead of the bottom
        return 0f;
    }

    @Override
    protected boolean textureIsReadyToBlit(FiguraHudRenderState state) {
        return false;
    }

    @Override
    protected void renderToTexture(FiguraHudRenderState state, PoseStack poseStack, SubmitNodeCollector collector) {
        try {
            // origin was put at the top center, move it to the top left corner
            poseStack.translate(-(state.x1() - state.x0()) / 2f, 0f, 0f);
            poseStack.last().pose().mul(state.guiPose());

            RecordingBufferSource bufferSource = new RecordingBufferSource(collector);
            state.avatar().hudRender(poseStack, bufferSource, state.entity(), state.tickDelta());
            bufferSource.flush();
        } catch (Exception e) {
            FiguraMod.LOGGER.error("Failed to render avatar HUD", e);
        }
    }

    @Override
    protected String getTextureLabel() {
        return "figura-hud";
    }
}
