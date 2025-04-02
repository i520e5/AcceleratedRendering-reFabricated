package com.github.argon4w.acceleratedrendering.core.meshes.collectors;

import com.github.argon4w.acceleratedrendering.core.extensions.VertexFormatExtension;
import com.github.argon4w.acceleratedrendering.core.utils.ByteBufferBuilder;
import com.github.argon4w.acceleratedrendering.core.utils.ColorUtils;
import com.github.argon4w.acceleratedrendering.core.utils.MemUtils;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.util.FastColor;
import org.lwjgl.system.MemoryUtil;

public class MeshCollector implements VertexConsumer {

    private final VertexFormat vertexFormat;;
    private final ByteBufferBuilder buffer;

    private final int vertexSize;
    private final long posOffset;
    private final long colorOffset;
    private final long uv0Offset;
    private final long uv2Offset;
    private final long normalOffset;

    private long vertexAddress;
    private int vertexCount;

    public MeshCollector(VertexFormat vertexFormat) {
        this.vertexFormat = vertexFormat;
        this.buffer = new ByteBufferBuilder(1024);

        this.vertexSize = this.vertexFormat.getVertexSize();
        this.colorOffset = VertexFormatExtension.of(this.vertexFormat).getOffset(DefaultVertexFormat.ELEMENT_COLOR);
        this.posOffset = VertexFormatExtension.of(this.vertexFormat).getOffset(DefaultVertexFormat.ELEMENT_POSITION);
        this.uv0Offset = VertexFormatExtension.of(this.vertexFormat).getOffset(DefaultVertexFormat.ELEMENT_UV);
        this.uv2Offset = VertexFormatExtension.of(this.vertexFormat).getOffset(DefaultVertexFormat.ELEMENT_UV2);
        this.normalOffset = VertexFormatExtension.of(this.vertexFormat).getOffset(DefaultVertexFormat.ELEMENT_NORMAL);

        this.vertexAddress = -1L;
        this.vertexCount = 0;
    }

    @Override
    public VertexConsumer vertex(
            double pX,
            double pY,
            double pZ
    ) {
        vertexCount++;
        vertexAddress = buffer.reserve(vertexSize);

        MemoryUtil.memPutFloat(vertexAddress + posOffset + 0L, (float) pX);
        MemoryUtil.memPutFloat(vertexAddress + posOffset + 4L, (float) pY);
        MemoryUtil.memPutFloat(vertexAddress + posOffset + 8L, (float) pZ);

        return this;
    }

    @Override
    public VertexConsumer color(
            int pRed,
            int pGreen,
            int pBlue,
            int pAlpha
    ) {
        if (colorOffset == -1) {
            return this;
        }

        if (vertexAddress == -1) {
            throw new IllegalStateException("Vertex not building!");
        }

        MemoryUtil.memPutByte(vertexAddress + colorOffset + 0L, (byte) pRed);
        MemoryUtil.memPutByte(vertexAddress + colorOffset + 1L, (byte) pGreen);
        MemoryUtil.memPutByte(vertexAddress + colorOffset + 2L, (byte) pBlue);
        MemoryUtil.memPutByte(vertexAddress + colorOffset + 3L, (byte) pAlpha);

        return this;
    }

    @Override
    public VertexConsumer uv(float pU, float pV) {
        if (uv0Offset == -1) {
            return this;
        }

        if (vertexAddress == -1) {
            throw new IllegalStateException("Vertex not building!");
        }

        MemoryUtil.memPutFloat(vertexAddress + uv0Offset + 0L, pU);
        MemoryUtil.memPutFloat(vertexAddress + uv0Offset + 4L, pV);

        return this;
    }

    @Override
    public VertexConsumer overlayCoords(int pU, int pV) {
        return this;
    }

    @Override
    public VertexConsumer uv2(int pU, int pV) {
        if (uv2Offset == -1) {
            return this;
        }

        if (vertexAddress == -1) {
            throw new IllegalStateException("Vertex not building!");
        }

        MemoryUtil.memPutShort(vertexAddress + uv2Offset + 0L, (short) pU);
        MemoryUtil.memPutShort(vertexAddress + uv2Offset + 2L, (short) pV);

        return this;
    }

    @Override
    public VertexConsumer normal(
            float pNormalX,
            float pNormalY,
            float pNormalZ
    ) {
        if (normalOffset == -1) {
            return this;
        }

        if (vertexAddress == -1) {
            throw new IllegalStateException("Vertex not building!");
        }

        MemUtils.putNormal(vertexAddress + normalOffset + 0L, pNormalX);
        MemUtils.putNormal(vertexAddress + normalOffset + 1L, pNormalY);
        MemUtils.putNormal(vertexAddress + normalOffset + 2L, pNormalZ);

        return this;
    }

    @Override
    public void endVertex() {

    }

    @Override
    public void vertex(
        float x,
        float y,
        float z,
        float red,
        float green,
        float blue,
        float alpha,
        float texU,
        float texV,
        int overlayUV,
        int lightmapUV,
        float normalX,
        float normalY,
        float normalZ
    ) {
        vertex(
            x,
            y,
            z,
            FastColor.ARGB32.color(
                (int) (alpha * 255),
                (int) (red * 255),
                (int) (green * 255),
                (int) (blue * 255)
            ),
            texU,
            texV,
            overlayUV,
            lightmapUV,
            normalX,
            normalY,
            normalZ
        );

    }

    @Override
    public void defaultColor(int defaultR, int defaultG, int defaultB, int defaultA) {

    }

    @Override
    public void unsetDefaultColor() {

    }

    public void vertex(
            float pX,
            float pY,
            float pZ,
            int pColor,
            float pU,
            float pV,
            int pPackedOverlay,
            int pPackedLight,
            float pNormalX,
            float pNormalY,
            float pNormalZ
    ) {
        vertexCount++;
        vertexAddress = buffer.reserve(vertexSize);

        MemoryUtil.memPutFloat(vertexAddress + posOffset + 0L, pX);
        MemoryUtil.memPutFloat(vertexAddress + posOffset + 4L, pY);
        MemoryUtil.memPutFloat(vertexAddress + posOffset + 8L, pZ);

        if (colorOffset != -1L) {
            MemoryUtil.memPutInt(vertexAddress + colorOffset + 0L, ColorUtils.ARGB32toABGR32(pColor));
        }

        if (uv0Offset != -1L) {
            MemoryUtil.memPutFloat(vertexAddress + uv0Offset + 0L, pU);
            MemoryUtil.memPutFloat(vertexAddress + uv0Offset + 4L, pV);
        }

        if (uv2Offset != -1L) {
            MemoryUtil.memPutInt(vertexAddress + uv2Offset + 0L, pPackedLight);
        }

        if (normalOffset != -1L) {
            MemUtils.putNormal(vertexAddress + normalOffset + 0L, pNormalX);
            MemUtils.putNormal(vertexAddress + normalOffset + 1L, pNormalY);
            MemUtils.putNormal(vertexAddress + normalOffset + 2L, pNormalZ);
        }
    }

    public ByteBufferBuilder getBuffer() {
        return buffer;
    }

    public int getVertexCount() {
        return vertexCount;
    }

    public VertexFormat getVertexFormat() {
        return vertexFormat;
    }
}
