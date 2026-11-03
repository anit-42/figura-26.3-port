package org.figuramc.figura.gui;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.renderpearl.api.textures.GpuSampler;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.gui.BlitRenderState;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.gui.widgets.permissions.PlayerPermPackElement;
import org.figuramc.figura.model.rendering.buffers.RecordingBufferSource;

/**
 * Renders an avatar's head (portrait) into the GUI.
 * 26.x: rendering goes through the vanilla picture-in-picture texture; the avatar is recorded and submitted
 * as custom geometry to the PiP collector. If the avatar has no portrait, the skin face is blitted instead.
 */
public class FiguraPortraitRenderer extends PictureInPictureRenderer<FiguraPortraitRenderState> {

    private boolean renderSkin;

    public FiguraPortraitRenderer() {
        super();
    }

    @Override
    public Class<FiguraPortraitRenderState> getRenderStateClass() {
        return FiguraPortraitRenderState.class;
    }

    @Override
    protected void renderToTexture(FiguraPortraitRenderState portraitState, PoseStack poseStack, SubmitNodeCollector collector) {
        Minecraft.getInstance().gameRenderer.lighting().setupFor(Lighting.Entry.ITEMS_FLAT);

        Avatar avatar = portraitState.avatar();
        if (avatar != null) {
            RecordingBufferSource bufferSource = new RecordingBufferSource(collector);
            renderSkin = !avatar.renderHeadForPortrait(bufferSource, poseStack, LightCoordsUtil.FULL_BRIGHT, portraitState.modelScale(), portraitState.upsideDown());
            bufferSource.flush();
        } else {
            renderSkin = true;
        }
    }

    @Override
    protected boolean textureIsReadyToBlit(FiguraPortraitRenderState state) {
        // avatars animate, always re-render
        return false;
    }

    @Override
    protected void blitTexture(FiguraPortraitRenderState state, GuiRenderState guiRenderState) {
        if (state.avatar() != null && !renderSkin) {
            super.blitTexture(state, guiRenderState);
            return;
        }

        if (state.fallbackSkin() != null) {
            Identifier texture = state.fallbackSkin();
            // face
            blit(guiRenderState, state, texture, 8 / 64f, 16 / 64f, 8 / 64f, 16 / 64f);
            // hat
            blit(guiRenderState, state, texture, 40 / 64f, 48 / 64f, 8 / 64f, 16 / 64f);
        } else {
            blit(guiRenderState, state, PlayerPermPackElement.UNKNOWN, 0f, 1f, 0f, 1f);
        }
    }

    private static void blit(GuiRenderState guiRenderState, FiguraPortraitRenderState state, Identifier texture, float u0, float u1, float v0, float v1) {
        AbstractTexture tex = Minecraft.getInstance().getTextureManager().getTexture(texture);
        GpuTextureView view = tex.getTextureView();
        GpuSampler sampler = tex.getSampler();
        guiRenderState.addBlitToCurrentLayer(new BlitRenderState(
                RenderPipelines.GUI_TEXTURED,
                TextureSetup.singleTexture(view, sampler),
                state.pose(),
                state.x0(), state.y0(), state.x1(), state.y1(),
                u0, u1, v0, v1,
                -1,
                state.scissorArea(),
                null
        ));
    }

    @Override
    protected String getTextureLabel() {
        return "figura-portrait";
    }
}
