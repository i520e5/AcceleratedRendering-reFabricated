package com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders;

import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.renderers.IAcceleratedRenderer;
import com.github.argon4w.acceleratedrendering.core.buffers.graphs.IBufferGraph;
import com.github.argon4w.acceleratedrendering.core.buffers.graphs.OutlineBufferGraph;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.RenderType;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.nio.ByteBuffer;

public class AcceleratedEntityOutlineGenerator implements IAcceleratedVertexConsumer, VertexConsumer {

    private final VertexConsumer delegate;
    private final int color;

    public AcceleratedEntityOutlineGenerator(VertexConsumer delegate, int color) {
        this.delegate = delegate;
        this.color = color;
    }

    @Override
    public VertexConsumer decorate(VertexConsumer buffer) {
        return new AcceleratedEntityOutlineGenerator(((IAcceleratedVertexConsumer) delegate).decorate(buffer), color);
    }

    @Override
    public void beginTransform(Matrix4f transform, Matrix3f normal) {
        ((IAcceleratedVertexConsumer) delegate).beginTransform(transform, normal);
    }

    @Override
    public void endTransform() {
        ((IAcceleratedVertexConsumer) delegate).endTransform();
    }

    @Override
    public boolean isAccelerated() {
        return ((IAcceleratedVertexConsumer) delegate).isAccelerated();
    }

    @Override
    public IBufferGraph getBufferGraph() {
        return new OutlineBufferGraph(((IAcceleratedVertexConsumer) delegate).getBufferGraph(), color);
    }

    @Override
    public RenderType getRenderType() {
        return ((IAcceleratedVertexConsumer) delegate).getRenderType();
    }

    @Override
    public void addClientMesh(
            ByteBuffer meshBuffer,
            int size,
            int color,
            int light,
            int overlay
    ) {
        ((IAcceleratedVertexConsumer) delegate).addClientMesh(
                meshBuffer,
                size,
                this.color,
                light,
                overlay
        );
    }

    @Override
    public void addServerMesh(
            int offset,
            int size,
            int color,
            int light,
            int overlay
    ) {
        ((IAcceleratedVertexConsumer) delegate).addServerMesh(
                offset,
                size,
                this.color,
                light,
                overlay
        );
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
    public VertexConsumer vertex(
            double pX,
            double pY,
            double pZ
    ) {
        delegate.vertex(
                pX,
                pY,
                pZ
        ).color(color);
        return this;
    }

    @Override
    public VertexConsumer vertex(
            Matrix4f pPose,
            float pX,
            float pY,
            float pZ
    ) {
        delegate.vertex(
                pPose,
                pX,
                pY,
                pZ
        ).color(color);
        return this;
    }

    @Override
    public VertexConsumer color(
            int pRed,
            int pGreen,
            int pBlue,
            int pAlpha
    ) {
        return this;
    }

    @Override
    public VertexConsumer uv(float pU, float pV) {
        delegate.uv(pU, pV);
        return this;
    }

    @Override
    public VertexConsumer overlayCoords(int pU, int pV) {
        return this;
    }

    @Override
    public VertexConsumer uv2(int pU, int pV) {
        return this;
    }

    @Override
    public VertexConsumer normal(
            float pNormalX,
            float pNormalY,
            float pNormalZ
    ) {
        return this;
    }

    @Override
    public void endVertex() {
        delegate.endVertex();
    }

    @Override
    public void defaultColor(int defaultR, int defaultG, int defaultB, int defaultA) {
        delegate.defaultColor(defaultR, defaultG, defaultB, defaultA);
    }

    @Override
    public void unsetDefaultColor() {
        delegate.unsetDefaultColor();
    }

    @Override
    public VertexConsumer normal(
            Matrix3f pPose,
            float pNormalX,
            float pNormalY,
            float pNormalZ
    ) {
        return this;
    }
}
