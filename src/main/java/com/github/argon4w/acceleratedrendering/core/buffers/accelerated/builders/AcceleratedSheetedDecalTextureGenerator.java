package com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders;

import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.renderers.IAcceleratedRenderer;
import com.github.argon4w.acceleratedrendering.core.buffers.graphs.DecalBufferGraph;
import com.github.argon4w.acceleratedrendering.core.buffers.graphs.IBufferGraph;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.nio.ByteBuffer;

public class AcceleratedSheetedDecalTextureGenerator implements IAcceleratedVertexConsumer, VertexConsumer {

    private final VertexConsumer delegate;
    private final Matrix4f cameraInverse;
    private final Matrix3f normalInverse;
    private final float textureScale;

    private final Vector3f cachedCamera;
    private final Vector3f cachedNormal;

    private float vertexX;
    private float vertexY;
    private float vertexZ;

    public AcceleratedSheetedDecalTextureGenerator(
            VertexConsumer delegate,
            Matrix4f cameraInverse,
            Matrix3f normalInverse,
            float textureScale
    ) {
        this.delegate = delegate;
        this.cameraInverse = cameraInverse;
        this.normalInverse = normalInverse;
        this.textureScale = textureScale;

        this.cachedCamera = new Vector3f();
        this.cachedNormal = new Vector3f();

        this.vertexX = 0;
        this.vertexY = 0;
        this.vertexZ = 0;
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
        return new DecalBufferGraph(((IAcceleratedVertexConsumer) delegate).getBufferGraph(), cameraInverse);
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
                -1,
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
                -1,
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
        vertexX = (float) pX;
        vertexY = (float) pY;
        vertexZ = (float) pZ;

        delegate.vertex(
                pX,
                pY,
                pZ
        );
        return this;
    }

    @Override
    public VertexConsumer uv(float pU, float pV) {
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
        delegate.color(-1);
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

        Vector3f normal = normalInverse.transform(
                pNormalX,
                pNormalY,
                pNormalZ,
                cachedNormal
        );

        Vector3f camera = cameraInverse.transformPosition(
                vertexX,
                vertexY,
                vertexZ,
                cachedCamera
        );

        Direction direction = Direction.getNearest(
                normal.x(),
                normal.y(),
                normal.z()
        );

        camera.rotateY((float) Math.PI);
        camera.rotateX((float) (-Math.PI / 2));
        camera.rotate(direction.getRotation());

        delegate.uv(-camera.x() * textureScale, -camera.y() * textureScale);
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
    public VertexConsumer decorate(VertexConsumer buffer) {
        return new AcceleratedSheetedDecalTextureGenerator(
                ((IAcceleratedVertexConsumer) delegate).decorate(buffer),
                cameraInverse,
                normalInverse,
                textureScale
        );
    }
}
