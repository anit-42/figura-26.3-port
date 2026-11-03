package org.figuramc.figura.model.rendering.buffers;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import org.apache.commons.lang3.function.TriFunction;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.avatar.Avatar;

/**
 * 26.x replacement for the old deferred "Figura feature renderer": the avatar is rendered right away into a
 * recording buffer, and the result is submitted as custom geometry to the given collector.
 */
public final class FiguraSubmitter {
    private FiguraSubmitter() {}

    public static <S extends EntityRenderState> void submitFiguraModel(SubmitNodeCollector collector, Avatar avatar, S renderState, TriFunction<Avatar, S, FiguraBufferSource, Void> renderer) {
        if (avatar == null || collector == null)
            return;
        RecordingBufferSource source = new RecordingBufferSource(collector);
        try {
            renderer.apply(avatar, renderState, source);
        } catch (Exception e) {
            FiguraMod.LOGGER.error("Failed to render Figura avatar", e);
        }
        source.flush();
    }
}
