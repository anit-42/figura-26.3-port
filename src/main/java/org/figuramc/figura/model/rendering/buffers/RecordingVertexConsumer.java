package org.figuramc.figura.model.rendering.buffers;

import com.mojang.blaze3d.vertex.VertexConsumer;

import java.util.Arrays;

/**
 * Captures vertices written by Figura so they can be replayed later into a real buffer
 * (via SubmitNodeCollector#submitCustomGeometry).
 */
public class RecordingVertexConsumer implements VertexConsumer {
    private static final int STRIDE = 13; // x y z color u v uv1 uv2 nx ny nz lineWidth flags
    private static final int F_COLOR = 1, F_UV = 2, F_UV1 = 4, F_UV2 = 8, F_NORMAL = 16, F_LINE = 32;

    private float[] data = new float[STRIDE * 64];
    private int count = 0;

    public boolean isEmpty() {
        return count == 0;
    }

    private int base() {
        return (count - 1) * STRIDE;
    }

    private void flag(int f) {
        int b = base() + 12;
        data[b] = Float.intBitsToFloat(Float.floatToRawIntBits(data[b]) | f);
    }

    @Override
    public VertexConsumer addVertex(float x, float y, float z) {
        if ((count + 1) * STRIDE > data.length)
            data = Arrays.copyOf(data, data.length * 2);
        int b = count * STRIDE;
        Arrays.fill(data, b, b + STRIDE, 0f);
        data[b] = x;
        data[b + 1] = y;
        data[b + 2] = z;
        count++;
        return this;
    }

    @Override
    public VertexConsumer setColor(int r, int g, int b, int a) {
        return setColor((a & 0xFF) << 24 | (r & 0xFF) << 16 | (g & 0xFF) << 8 | (b & 0xFF));
    }

    @Override
    public VertexConsumer setColor(int argb) {
        if (count == 0) return this;
        data[base() + 3] = Float.intBitsToFloat(argb);
        flag(F_COLOR);
        return this;
    }

    @Override
    public VertexConsumer setUv(float u, float v) {
        if (count == 0) return this;
        data[base() + 4] = u;
        data[base() + 5] = v;
        flag(F_UV);
        return this;
    }

    @Override
    public VertexConsumer setUv1(int u, int v) {
        if (count == 0) return this;
        data[base() + 6] = Float.intBitsToFloat((u & 0xFFFF) | (v & 0xFFFF) << 16);
        flag(F_UV1);
        return this;
    }

    @Override
    public VertexConsumer setUv2(int u, int v) {
        if (count == 0) return this;
        data[base() + 7] = Float.intBitsToFloat((u & 0xFFFF) | (v & 0xFFFF) << 16);
        flag(F_UV2);
        return this;
    }

    @Override
    public VertexConsumer setUv3(float u, float v) {
        return this;
    }

    @Override
    public VertexConsumer setNormal(float x, float y, float z) {
        if (count == 0) return this;
        data[base() + 8] = x;
        data[base() + 9] = y;
        data[base() + 10] = z;
        flag(F_NORMAL);
        return this;
    }

    @Override
    public VertexConsumer setLineWidth(float width) {
        if (count == 0) return this;
        data[base() + 11] = width;
        flag(F_LINE);
        return this;
    }

    public void replay(VertexConsumer out) {
        for (int i = 0; i < count; i++) {
            int b = i * STRIDE;
            int flags = Float.floatToRawIntBits(data[b + 12]);
            out.addVertex(data[b], data[b + 1], data[b + 2]);
            if ((flags & F_COLOR) != 0) out.setColor(Float.floatToRawIntBits(data[b + 3]));
            if ((flags & F_UV) != 0) out.setUv(data[b + 4], data[b + 5]);
            if ((flags & F_UV1) != 0) {
                int p = Float.floatToRawIntBits(data[b + 6]);
                out.setUv1(p & 0xFFFF, p >>> 16);
            }
            if ((flags & F_UV2) != 0) {
                int p = Float.floatToRawIntBits(data[b + 7]);
                out.setUv2(p & 0xFFFF, p >>> 16);
            }
            if ((flags & F_NORMAL) != 0) out.setNormal(data[b + 8], data[b + 9], data[b + 10]);
            if ((flags & F_LINE) != 0) out.setLineWidth(data[b + 11]);
        }
    }
}
