package org.figuramc.figura.model.rendering.buffers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.figuramc.figura.FiguraMod;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Records everything Figura draws for one submission and forwards it to the submit pipeline
 * as custom geometry, one submit per render type. Vertices are already in final (camera relative)
 * space, so the replay ignores the pose handed back by the feature renderer.
 */
public class RecordingBufferSource implements FiguraBufferSource {
    private static final PoseStack IDENTITY = new PoseStack();

    private final OrderedSubmitNodeCollector collector;
    private final Map<RenderType, RecordingVertexConsumer> buffers = new LinkedHashMap<>();

    public RecordingBufferSource(OrderedSubmitNodeCollector collector) {
        this.collector = collector;
    }

    @Override
    public SubmitNodeCollector collector() {
        return collector instanceof SubmitNodeCollector full ? full : null;
    }

    @Override
    public VertexConsumer getBuffer(RenderType renderType) {
        return buffers.computeIfAbsent(renderType, rt -> new RecordingVertexConsumer());
    }

    @Override
    public void endBatch() {
        flush();
    }

    @Override
    public void endLastBatch() {
        flush();
    }

    public void flush() {
        for (Map.Entry<RenderType, RecordingVertexConsumer> entry : buffers.entrySet()) {
            RecordingVertexConsumer recorded = entry.getValue();
            if (recorded.isEmpty())
                continue;
            collector.submitCustomGeometry(IDENTITY, entry.getKey(), (pose, consumer) -> {
                try {
                    recorded.replay(consumer);
                } catch (Exception e) {
                    FiguraMod.LOGGER.error("Failed to replay Figura geometry", e);
                }
            });
        }
        buffers.clear();
    }
}
