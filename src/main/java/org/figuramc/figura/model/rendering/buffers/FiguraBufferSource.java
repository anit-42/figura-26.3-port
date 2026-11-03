package org.figuramc.figura.model.rendering.buffers;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.jetbrains.annotations.Nullable;

/**
 * Replacement for the vanilla FiguraBufferSource, which was removed in 26.2.
 * Hands out a vertex consumer per render type. Implementations either record geometry and later
 * submit it through the submit pipeline, or write straight into a feature renderer's staged buffer.
 */
public interface FiguraBufferSource {
    VertexConsumer getBuffer(RenderType renderType);

    /** The collector geometry is being submitted to, when available (null while a feature renderer is preparing). */
    @Nullable
    default SubmitNodeCollector collector() {
        return null;
    }

    default void endBatch() {}

    default void endBatch(RenderType renderType) {}

    default void endLastBatch() {}
}
