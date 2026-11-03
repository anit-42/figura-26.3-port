package org.figuramc.figura.model.rendering.buffers;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.rendertype.RenderType;

import java.util.function.Function;

/** Writes straight into a feature renderer's staged vertex buffer (used while vanilla is preparing a model). */
public record DirectBufferSource(Function<RenderType, VertexConsumer> getter) implements FiguraBufferSource {
    @Override
    public VertexConsumer getBuffer(RenderType renderType) {
        return getter.apply(renderType);
    }
}
