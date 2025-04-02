package com.github.argon4w.acceleratedrendering.features.text;

import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.IAcceleratedVertexConsumer;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.renderers.IAcceleratedRenderer;
import com.github.argon4w.acceleratedrendering.core.buffers.graphs.IBufferGraph;
import com.github.argon4w.acceleratedrendering.core.meshes.IMesh;
import com.github.argon4w.acceleratedrendering.core.meshes.collectors.MeshCollector;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.renderer.RenderType;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.Map;

public class AcceleratedBakedGlyphRenderer implements IAcceleratedRenderer<Void> {

    private final Map<IBufferGraph, IMesh> meshes;
    private final BakedGlyph bakedGlyph;
    private final boolean italic;

    public AcceleratedBakedGlyphRenderer(BakedGlyph bakedGlyph, boolean italic) {
        this.meshes = new Object2ObjectOpenHashMap<>();
        this.bakedGlyph = bakedGlyph;
        this.italic = italic;
    }

    @Override
    public void render(
        VertexConsumer vertexConsumer,
        Void context,
        Matrix4f transform,
        Matrix3f normal,
        int light,
        int overlay,
        int color
    ) {
        IAcceleratedVertexConsumer extension = (IAcceleratedVertexConsumer) vertexConsumer;

        IBufferGraph bufferGraph = extension.getBufferGraph();
        RenderType renderType = extension.getRenderType();

        IMesh mesh = meshes.get(bufferGraph);

        extension.beginTransform(transform, normal);

        if (mesh != null) {
            mesh.write(
                extension,
                color,
                light,
                overlay
            );

            extension.endTransform();
            return;
        }

        MeshCollector meshCollector = new MeshCollector(renderType.format);

        addGlyphQuad(
            extension.decorate(meshCollector),
            italic,
            bakedGlyph,
            new Vector3f()
        );

        mesh = AcceleratedTextRenderingFeature
            .getMeshType()
            .getBuilder()
            .build(meshCollector);

        meshes.put(bufferGraph, mesh);
        mesh.write(
            extension,
            color,
            light,
            overlay
        );

        extension.endTransform();
    }

    public static void addGlyphQuad(
        VertexConsumer vertexConsumer,
        boolean italic,
        BakedGlyph glyph,
        Vector3f offset
    ) {
        float italicOffsetUp = italic ? 1.0f - 0.25f * glyph.up : 0.0f;
        float italicOffsetDown = italic ? 1.0f - 0.25f * glyph.down : 0.0f;

        addGlyphVertex(
            vertexConsumer,
            offset.x + glyph.left + italicOffsetUp,
            offset.y + glyph.up,
            offset.z,
            glyph.u0,
            glyph.v0
        );
        addGlyphVertex(
            vertexConsumer,
            offset.x + glyph.left + italicOffsetDown,
            offset.y + glyph.down,
            offset.z,
            glyph.u0,
            glyph.v1
        );
        addGlyphVertex(
            vertexConsumer,
            offset.x + glyph.right + italicOffsetDown,
            offset.y + glyph.down,
            offset.z,
            glyph.u1,
            glyph.v1
        );
        addGlyphVertex(
            vertexConsumer,
            offset.x + glyph.right + italicOffsetUp,
            offset.y + glyph.up,
            offset.z,
            glyph.u1,
            glyph.v0
        );
    }

    public static void addGlyphVertex(
        VertexConsumer vertexConsumer,
        float positionX,
        float positionY,
        float positionZ,
        float u,
        float v
    ) {
        vertexConsumer
            .vertex(
                positionX,
                positionY,
                positionZ
            )
            .color(-1)
            .uv(u, v)
            .uv2(0, 0)
            .endVertex();
    }
}
