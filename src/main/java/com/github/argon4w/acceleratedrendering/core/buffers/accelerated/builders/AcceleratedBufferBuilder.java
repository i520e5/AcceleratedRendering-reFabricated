package com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders;

import com.github.argon4w.acceleratedrendering.core.CoreFeature;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.AcceleratedBufferSetPool;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.pools.ElementBufferPool;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.pools.MappedBufferPool;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.pools.VertexBufferPool;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.renderers.IAcceleratedRenderer;
import com.github.argon4w.acceleratedrendering.core.buffers.graphs.BlankBufferGraph;
import com.github.argon4w.acceleratedrendering.core.buffers.graphs.IBufferGraph;
import com.github.argon4w.acceleratedrendering.core.programs.extras.IExtraVertexData;
import com.github.argon4w.acceleratedrendering.core.utils.ColorUtils;
import com.github.argon4w.acceleratedrendering.core.utils.MemUtils;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.FastColor;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

public class AcceleratedBufferBuilder implements IAcceleratedVertexConsumer, VertexConsumer {

    private final VertexBufferPool.VertexBuffer vertexBuffer;
    private final MappedBufferPool.Pooled varyingBuffer;
    private final ElementBufferPool.ElementSegment elementSegment;
    private final AcceleratedBufferSetPool.BufferSet bufferSet;

    private final IBufferGraph bufferGraph;
    private final RenderType renderType;
    private final VertexFormat.Mode mode;
    private final long vertexSize;
    private final int polygonSize;
    private final int polygonElementCount;

    protected boolean defaultColorSet;
    private int defaultR = 255;
    private int defaultG = 255;
    private int defaultB = 255;
    private int defaultA = 255;

    private final long posOffset;
    private final long colorOffset;
    private final long uv0Offset;
    private final long uv1Offset;
    private final long uv2Offset;
    private final long normalOffset;

    private int elementCount;
    private int vertexCount;
    private long vertexAddress;
    private long transformAddress;
    private long normalAddress;
    private int activeSharing;
    private int cachedSharing;

    private Matrix4f cachedTransform;
    private Matrix3f cachedNormal;

    private final Matrix4f cachedTransformValue;
    private final Matrix3f cachedNormalValue;

    public AcceleratedBufferBuilder(
            VertexBufferPool.VertexBuffer vertexBuffer,
            MappedBufferPool.Pooled varyingBuffer,
            ElementBufferPool.ElementSegment elementSegment,
            AcceleratedBufferSetPool.BufferSet bufferSet,
            RenderType renderType
    ) {
        this.vertexBuffer = vertexBuffer;
        this.varyingBuffer = varyingBuffer;
        this.elementSegment = elementSegment;
        this.bufferSet = bufferSet;

        this.bufferGraph = new BlankBufferGraph(renderType);
        this.renderType = renderType;
        this.mode = this.renderType.mode;
        this.vertexSize = this.bufferSet.getVertexSize();
        this.polygonSize = this.mode.primitiveLength;
        this.polygonElementCount = this.mode.indexCount(this.polygonSize);

        this.posOffset = bufferSet.getOffset(DefaultVertexFormat.ELEMENT_POSITION);
        this.colorOffset = bufferSet.getOffset(DefaultVertexFormat.ELEMENT_COLOR);
        this.uv0Offset = bufferSet.getOffset(DefaultVertexFormat.ELEMENT_UV0);
        this.uv1Offset = bufferSet.getOffset(DefaultVertexFormat.ELEMENT_UV1);
        this.uv2Offset = bufferSet.getOffset(DefaultVertexFormat.ELEMENT_UV2);
        this.normalOffset = bufferSet.getOffset(DefaultVertexFormat.ELEMENT_NORMAL);

        this.elementCount = 0;
        this.vertexCount = 0;
        this.vertexAddress = -1;
        this.transformAddress = -1;
        this.normalAddress = -1;
        this.activeSharing = -1;
        this.cachedSharing = -1;

        this.cachedTransform = null;
        this.cachedNormal = null;

        this.cachedTransformValue = new Matrix4f();
        this.cachedNormalValue = new Matrix3f();
    }

    @Override
    public VertexConsumer vertex(
            Matrix4f transform,
            float pX,
            float pY,
            float pZ
    ) {
        beginTransform(transform, cachedNormalValue);
        return vertex(
                pX,
                pY,
                pZ
        );
    }

    @Override
    public VertexConsumer vertex(
            double pX,
            double pY,
            double pZ
    ) {
        long vertexAddress = vertexBuffer.reserve(vertexSize);
        long varyingAddress = varyingBuffer.reserve(4L * 4L);

        this.vertexAddress = vertexAddress;

        MemoryUtil.memPutFloat(vertexAddress + posOffset + 0L, (float) pX);
        MemoryUtil.memPutFloat(vertexAddress + posOffset + 4L, (float) pY);
        MemoryUtil.memPutFloat(vertexAddress + posOffset + 8L, (float) pZ);

        MemoryUtil.memPutInt(varyingAddress + 0L * 4L, 0);
        MemoryUtil.memPutInt(varyingAddress + 1L * 4L, activeSharing);
        MemoryUtil.memPutInt(varyingAddress + 2L * 4L, -1);

        IExtraVertexData data = bufferSet.getExtraVertex(mode);
        data.addExtraVertex(vertexAddress);
        data.addExtraVarying(varyingAddress);

        vertexCount ++;
        elementCount ++;

        if (elementCount >= polygonSize) {
            elementSegment.countPolygons(polygonElementCount);
            elementCount = 0;
            activeSharing = -1;
        }

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
        if (uv1Offset == -1) {
            return this;
        }

        if (vertexAddress == -1) {
            throw new IllegalStateException("Vertex not building!");
        }

        MemoryUtil.memPutShort(vertexAddress + uv1Offset + 0L, (short) pU);
        MemoryUtil.memPutShort(vertexAddress + uv1Offset + 2L, (short) pV);

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
            Matrix3f normalTransform,
            float pNormalX,
            float pNormalY,
            float pNormalZ
    ) {

        if (activeSharing == -1) {
            return VertexConsumer.super.normal(
                normalTransform,
                    pNormalX,
                    pNormalY,
                    pNormalZ
            );
        }

        if (!normalTransform.equals(cachedNormal)) {
            MemUtils.putMatrix3x4f(normalAddress, normalTransform);
        }

        return normal(
                pNormalX,
                pNormalY,
                pNormalZ
        );
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
    public void vertex(
            float pX,
            float pY,
            float pZ,
            float red, float green, float blue, float alpha,
            float pU,
            float pV,
            int pPackedOverlay,
            int pPackedLight,
            float pNormalX,
            float pNormalY,
            float pNormalZ
    ) {
        long vertexAddress = vertexBuffer.reserve(vertexSize);
        long varyingAddress = varyingBuffer.reserve(4L * 4L);
        IExtraVertexData data = bufferSet.getExtraVertex(mode);

        data.addExtraVertex(vertexAddress);
        data.addExtraVarying(varyingAddress);

        MemoryUtil.memPutFloat(vertexAddress + posOffset + 0L, pX);
        MemoryUtil.memPutFloat(vertexAddress + posOffset + 4L, pY);
        MemoryUtil.memPutFloat(vertexAddress + posOffset + 8L, pZ);

        if (defaultColorSet) {
            MemoryUtil.memPutByte(vertexAddress + colorOffset + 0L, (byte) defaultR);
            MemoryUtil.memPutByte(vertexAddress + colorOffset + 1L, (byte) defaultG);
            MemoryUtil.memPutByte(vertexAddress + colorOffset + 2L, (byte) defaultB);
            MemoryUtil.memPutByte(vertexAddress + colorOffset + 3L, (byte) defaultA);
        }

        MemoryUtil.memPutInt(varyingAddress + 0L * 4L, 0);
        MemoryUtil.memPutInt(varyingAddress + 1L * 4L, activeSharing);
        MemoryUtil.memPutInt(varyingAddress + 2L * 4L, -1);

        if (colorOffset != -1) {
            MemoryUtil.memPutInt(
                vertexAddress + colorOffset + 0L,
                FastColor.ARGB32.color(
                    (int) (red * 255),
                    (int) (green * 255),
                    (int) (blue * 255),
                    (int) (alpha * 255)
                )
            );
        }

        if (uv0Offset != -1) {
            MemoryUtil.memPutFloat(vertexAddress + uv0Offset + 0L, pU);
            MemoryUtil.memPutFloat(vertexAddress + uv0Offset + 4L, pV);
        }

        if (uv1Offset != -1) {
            MemoryUtil.memPutInt(vertexAddress + uv1Offset + 0L, pPackedOverlay);
        }

        if (uv2Offset != -1) {
            MemoryUtil.memPutInt(vertexAddress + uv2Offset + 0L, pPackedLight);
        }

        if (normalOffset != -1) {
            MemUtils.putNormal(vertexAddress + normalOffset + 0L, pNormalX);
            MemUtils.putNormal(vertexAddress + normalOffset + 1L, pNormalY);
            MemUtils.putNormal(vertexAddress + normalOffset + 2L, pNormalZ);
        }

        vertexCount ++;
        elementCount ++;

        if (elementCount >= polygonSize) {
            elementSegment.countPolygons(polygonElementCount);
            elementCount = 0;
            activeSharing = -1;
        }
    }

    @Override
    public void defaultColor(int defaultR, int defaultG, int defaultB, int defaultA) {
        this.defaultR = defaultR;
        this.defaultG = defaultG;
        this.defaultB = defaultB;
        this.defaultA = defaultA;
        this.defaultColorSet = true;
    }

    @Override
    public void unsetDefaultColor() {
        this.defaultColorSet = false;
    }

    @Override
    public void beginTransform(Matrix4f transform, Matrix3f normal) {
        if (CoreFeature.shouldCacheIdenticalPose()
                && transform.equals(cachedTransform)
                && normal.equals(cachedNormal)
        ) {
            activeSharing = cachedSharing;
            return;
        }

        cachedTransform = cachedTransformValue.set(transform);
        cachedNormal = cachedNormalValue.set(normal);

        cachedSharing = bufferSet.getSharing();
        activeSharing = cachedSharing;

        transformAddress = bufferSet.reserveSharing();
        normalAddress = transformAddress + 4L * 4L * 4L;

        MemUtils.putMatrix4f(transformAddress, transform);
        MemUtils.putMatrix3x4f(normalAddress, normal);
    }

    @Override
    public void endTransform() {
        cachedTransform = null;
        cachedNormal = null;
        activeSharing = -1;
        cachedSharing = -1;
    }

    @Override
    public void addClientMesh(
            ByteBuffer meshBuffer,
            int size,
            int color,
            int light,
            int overlay
    ) {
        long bufferSize = vertexSize * size;
        long vertexAddress = vertexBuffer.reserve(bufferSize);
        long varyingAddress = varyingBuffer.reserve(4L * 4L * size);

        IExtraVertexData data = bufferSet.getExtraVertex(mode);
        data.addExtraVertex(vertexAddress);
        data.addExtraVarying(varyingAddress);

        MemoryUtil.memCopy(
                MemoryUtil.memAddress0(meshBuffer),
                vertexAddress,
                bufferSize
        );

        if (colorOffset != -1) {
            MemoryUtil.memPutInt(vertexAddress + colorOffset, ColorUtils.ARGB32toRGBA32(color));
        }

        if (uv1Offset != -1) {
            MemoryUtil.memPutInt(vertexAddress + uv1Offset, overlay);
        }

        if (uv2Offset != -1) {
            MemoryUtil.memPutInt(vertexAddress + uv2Offset, light);
        }

        MemoryUtil.memPutInt(varyingAddress + 1L * 4L, activeSharing);
        MemoryUtil.memPutInt(varyingAddress + 2L * 4L, -1);

        for (int i = 0; i < size; i++) {
            MemoryUtil.memPutInt(varyingAddress + i * 4L * 4L, i);
        }

        elementSegment.countPolygons(mode.indexCount(size));
        vertexCount += size;
    }

    @Override
    public void addServerMesh(
            int offset,
            int size,
            int color,
            int light,
            int overlay
    ) {
        long meshOffset = offset / vertexSize;
        long vertexAddress = vertexBuffer.reserve(vertexSize * size);
        long varyingAddress = varyingBuffer.reserve(4L * 4L * size);

        IExtraVertexData data = bufferSet.getExtraVertex(mode);
        data.addExtraVertex(vertexAddress);
        data.addExtraVarying(varyingAddress);

        if (colorOffset != -1) {
            MemoryUtil.memPutInt(vertexAddress + colorOffset, ColorUtils.ARGB32toRGBA32(color));
        }

        if (uv1Offset != -1) {
            MemoryUtil.memPutInt(vertexAddress + uv1Offset, overlay);
        }

        if (uv2Offset != -1) {
            MemoryUtil.memPutInt(vertexAddress + uv2Offset, light);
        }

        MemoryUtil.memPutInt(varyingAddress + 1L * 4L, activeSharing);
        MemoryUtil.memPutInt(varyingAddress + 2L * 4L, (int) meshOffset);

        for (int i = 0; i < size; i++) {
            MemoryUtil.memPutInt(varyingAddress + i * 4L * 4L, i);
        }

        elementSegment.countPolygons(mode.indexCount(size));
        vertexCount += size;
    }

    @Override
    public <T>  void doRender(
            IAcceleratedRenderer<T> renderer,
            T context,
            Matrix4f transform,
            Matrix3f normal,
            int light,
            int overlay,
            int color
    ) {
        renderer.render(
                this,
                context,
                transform,
                normal,
                light,
                overlay,
                color
        );
    }

    @Override
    public void endVertex() {

    }

    @Override
    public VertexConsumer decorate(VertexConsumer buffer) {
        return buffer;
    }

    @Override
    public boolean isAccelerated() {
        return true;
    }

    @Override
    public IBufferGraph getBufferGraph() {
        return bufferGraph;
    }

    @Override
    public RenderType getRenderType() {
        return renderType;
    }

    public boolean isEmpty() {
        return vertexCount == 0;
    }

    public int getVertexCount() {
        return vertexCount;
    }

    public int getVertexOffset() {
        return vertexBuffer.getOffset();
    }

    public VertexBufferPool.VertexBuffer getVertexBuffer() {
        return vertexBuffer;
    }

    public MappedBufferPool.Pooled getVaryingBuffer() {
        return varyingBuffer;
    }

    public ElementBufferPool.ElementSegment getElementSegment() {
        return elementSegment;
    }
}
