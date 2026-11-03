package org.figuramc.figura.model.rendering.buffers;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.TextRenderable;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

/** 26.x replacement for Font#drawInBatch / drawInBatch8xOutline writing into a FiguraBufferSource. */
public final class FontRenderHelper {
    private FontRenderHelper() {}

    private static void emit(Font.PreparedText prepared, Matrix4fc matrix, FiguraBufferSource buffer, Font.DisplayMode mode, int light) {
        Matrix4f pose = new Matrix4f(matrix);
        prepared.visit(new Font.GlyphVisitor() {
            @Override
            public void acceptRenderable(TextRenderable renderable) {
                renderable.render(pose, buffer.getBuffer(renderable.renderType(mode)), light, false);
            }
        });
    }

    public static void drawInBatch(Font font, FormattedCharSequence text, float x, float y, int color, boolean shadow, Matrix4fc matrix, FiguraBufferSource buffer, Font.DisplayMode mode, int backgroundColor, int light) {
        emit(font.prepareText(text, x, y, color, shadow, false, backgroundColor), matrix, buffer, mode, light);
    }

    public static void drawInBatch(Font font, Component text, float x, float y, int color, boolean shadow, Matrix4fc matrix, FiguraBufferSource buffer, Font.DisplayMode mode, int backgroundColor, int light) {
        drawInBatch(font, text.getVisualOrderText(), x, y, color, shadow, matrix, buffer, mode, backgroundColor, light);
    }

    public static void drawInBatch(Font font, String text, float x, float y, int color, boolean shadow, Matrix4fc matrix, FiguraBufferSource buffer, Font.DisplayMode mode, int backgroundColor, int light) {
        emit(font.prepareText(text, x, y, color, shadow, backgroundColor), matrix, buffer, mode, light);
    }

    public static void drawInBatch8xOutline(Font font, FormattedCharSequence text, float x, float y, int color, int outlineColor, Matrix4fc matrix, FiguraBufferSource buffer, int light) {
        emit(font.prepare8xTextOutline(text, x, y, outlineColor), matrix, buffer, Font.DisplayMode.NORMAL, light);
        drawInBatch(font, text, x, y, color, false, matrix, buffer, Font.DisplayMode.POLYGON_OFFSET, 0, light);
    }

    public static void drawBackground(Font font, float x0, float y0, float x1, float y1, int color, Matrix4fc matrix, FiguraBufferSource buffer, Font.DisplayMode mode, int light) {
        TextRenderable bg = font.prepareBackground(x0, y0, x1, y1, color);
        bg.render(new Matrix4f(matrix), buffer.getBuffer(bg.renderType(mode)), light, false);
    }
}
