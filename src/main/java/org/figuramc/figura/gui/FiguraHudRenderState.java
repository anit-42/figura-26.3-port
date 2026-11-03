package org.figuramc.figura.gui;

import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import net.minecraft.world.entity.Entity;
import org.figuramc.figura.avatar.Avatar;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;

/** Full-screen picture-in-picture state used to draw an avatar's HUD parts (26.x retained-mode GUI). */
public record FiguraHudRenderState(
        Avatar avatar,
        @Nullable Entity entity,
        float tickDelta,
        Matrix3x2f guiPose,
        int x0, int y0, int x1, int y1,
        @Nullable ScreenRectangle scissorArea,
        @Nullable ScreenRectangle bounds
) implements PictureInPictureRenderState {
    public FiguraHudRenderState(Avatar avatar, @Nullable Entity entity, float tickDelta, Matrix3x2f guiPose, int width, int height) {
        this(avatar, entity, tickDelta, guiPose, 0, 0, width, height, null, PictureInPictureRenderState.getBounds(0, 0, width, height, null));
    }

    @Override
    public float scale() {
        return 1f;
    }
}
