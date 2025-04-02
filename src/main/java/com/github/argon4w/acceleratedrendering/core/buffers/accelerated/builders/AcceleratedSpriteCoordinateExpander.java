package com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders;

import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.renderers.IAcceleratedRenderer;
import com.github.argon4w.acceleratedrendering.core.buffers.graphs.IBufferGraph;
import com.github.argon4w.acceleratedrendering.core.buffers.graphs.SpriteBufferGraph;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.nio.ByteBuffer;

public class AcceleratedSpriteCoordinateExpander implements IAcceleratedVertexConsumer, VertexConsumer {

    private final VertexConsumer delegate;
    private final TextureAtlasSprite sprite;

    public AcceleratedSpriteCoordinateExpander(VertexConsumer delegate, TextureAtlasSprite sprite) {
        this.delegate = delegate;
        this.sprite = sprite;
    }

    @Override
    public VertexConsumer decorate(VertexConsumer buffer) {
        return new AcceleratedSpriteCoordinateExpander(((IAcceleratedVertexConsumer) delegate).decorate(buffer), sprite);
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
        return new SpriteBufferGraph(((IAcceleratedVertexConsumer) delegate).getBufferGraph(), sprite);
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
                color,
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
                color,
                light,
                overlay
        );
    }

    @Override
    public <T> void doRender(
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
        );
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
        );
        return this;
    }

    @Override
    public VertexConsumer uv(float pU, float pV) {
        delegate.uv(sprite.getU(pU), sprite.getV(pV));
        return this;
    }

    @Override
    public VertexConsumer overlayCoords(int pU, int pV) {
        delegate.overlayCoords(pU, pV);
        return this;
    }

    @Override
    public VertexConsumer uv2(int pU, int pV) {
        delegate.uv2(pU, pV);
        return this;
    }

    @Override
    public VertexConsumer color(
            int pRed,
            int pGreen,
            int pBlue,
            int pAlpha
    ) {
        delegate.color(
                pRed,
                pGreen,
                pBlue,
                pAlpha
        );
        return this;
    }

    @Override
    public VertexConsumer normal(
            float pNormalX,
            float pNormalY,
            float pNormalZ
    ) {
        delegate.normal(
                pNormalX,
                pNormalY,
                pNormalZ
        );
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
        delegate.normal(
                pPose,
                pNormalX,
                pNormalY,
                pNormalZ
        );
        return this;
    }

    @Override
    public void vertex(float x, float y, float z, float red, float green, float blue, float alpha, float texU, float texV, int overlayUV, int lightmapUV, float normalX, float normalY, float normalZ) {
        delegate.vertex(x, y, z, red, green, blue, alpha, sprite.getU(texU), sprite.getV(texV), overlayUV, lightmapUV, normalX, normalY, normalZ);
    }
}
