package com.github.argon4w.acceleratedrendering.core.meshes.collectors;

import com.github.argon4w.acceleratedrendering.core.utils.CullerUtils;
import com.github.argon4w.acceleratedrendering.core.utils.TextureUtils;
import com.github.argon4w.acceleratedrendering.core.utils.Vertex;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class MeshCollectorCuller implements VertexConsumer {

    private final int polygonSize;
    private final Vertex[] polygon;
    private final NativeImage texture;
    private final MeshCollector meshCollector;

    private int vertexIndex;

    public MeshCollectorCuller(RenderType renderType) {
        this.polygonSize = renderType.mode.primitiveLength;
        this.polygon = new Vertex[this.polygonSize];
        this.texture = TextureUtils.downloadTexture(renderType, 0);
        this.meshCollector = new MeshCollector(renderType.format);

        this.vertexIndex = -1;
    }

    public void flush() {
        if (vertexIndex >= polygonSize - 1) {
            vertexIndex = -1;

            if (!CullerUtils.shouldCull(polygon, texture)) {
                for (Vertex vertex : polygon) {
                    Vector3f vertexPosition = vertex.getPosition();
                    Vector2f vertexUV = vertex.getUV();
                    Vector3f vertexNormal = vertex.getNormal();

                    meshCollector.vertex(
                            vertexPosition.x,
                            vertexPosition.y,
                            vertexPosition.z,
                            vertex.getPackedColor(),
                            vertexUV.x,
                            vertexUV.y,
                            OverlayTexture.NO_OVERLAY,
                            vertex.getPackedLight(),
                            vertexNormal.x,
                            vertexNormal.y,
                            vertexNormal.z
                    );
                }
            }
        }
    }

    @Override
    public VertexConsumer vertex(
            double pX,
            double pY,
            double pZ
    ) {
        flush();
        polygon[++ vertexIndex] = new Vertex();
        polygon[vertexIndex].getPosition().x = (float) pX;
        polygon[vertexIndex].getPosition().y = (float) pY;
        polygon[vertexIndex].getPosition().z = (float) pZ;

        return this;
    }

    @Override
    public VertexConsumer color(
            int pRed,
            int pGreen,
            int pBlue,
            int pAlpha
    ) {
        if (vertexIndex < 0) {
            throw new IllegalStateException("Vertex not building!");
        }

        polygon[vertexIndex].getColor().x = pRed;
        polygon[vertexIndex].getColor().y = pGreen;
        polygon[vertexIndex].getColor().z = pBlue;
        polygon[vertexIndex].getColor().w = pAlpha;

        return this;
    }

    @Override
    public VertexConsumer uv(float pU, float pV) {
        if (vertexIndex < 0) {
            throw new IllegalStateException("Vertex not building!");
        }

        polygon[vertexIndex].getUV().x = pU;
        polygon[vertexIndex].getUV().y = pV;

        return this;
    }

    @Override
    public VertexConsumer overlayCoords(int pU, int pV) {
        return this;
    }

    @Override
    public VertexConsumer uv2(int pU, int pV) {
        if (vertexIndex < 0) {
            throw new IllegalStateException("Vertex not building!");
        }

        polygon[vertexIndex].getLight().x = pU;
        polygon[vertexIndex].getLight().y = pV;

        return this;
    }

    @Override
    public VertexConsumer normal(
            float pNormalX,
            float pNormalY,
            float pNormalZ
    ) {
        if (vertexIndex < 0) {
            throw new IllegalStateException("Vertex not building!");
        }

        polygon[vertexIndex].getNormal().x = pNormalX;
        polygon[vertexIndex].getNormal().y = pNormalY;
        polygon[vertexIndex].getNormal().z = pNormalZ;
        return this;
    }

    @Override
    public void endVertex() {
        flush();
    }

    @Override
    public void defaultColor(int defaultR, int defaultG, int defaultB, int defaultA) {

    }

    @Override
    public void unsetDefaultColor() {

    }

    public MeshCollector getMeshCollector() {
        return meshCollector;
    }
}
