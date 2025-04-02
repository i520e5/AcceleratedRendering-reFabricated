package com.github.argon4w.acceleratedrendering.features.items.mixins;

import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.IAcceleratedVertexConsumer;
import com.github.argon4w.acceleratedrendering.core.buffers.graphs.IBufferGraph;
import com.github.argon4w.acceleratedrendering.core.meshes.IMesh;
import com.github.argon4w.acceleratedrendering.core.meshes.collectors.MeshCollectorCuller;
import com.github.argon4w.acceleratedrendering.core.utils.LazyMap;
import com.github.argon4w.acceleratedrendering.features.items.AcceleratedItemRenderingFeature;
import com.github.argon4w.acceleratedrendering.features.items.IAcceleratedBakedQuad;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.util.FastColor;
import net.neoforged.neoforge.client.model.IQuadTransformer;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.Map;

@Mixin(BakedQuad.class)
public abstract class BakedQuadMixin implements IAcceleratedBakedQuad {

    @Unique
    private static final Map<int[], Map<IBufferGraph, IMesh>> MESHES = new LazyMap<>(new Reference2ObjectOpenHashMap<>(), Object2ObjectOpenHashMap::new);

    @Shadow
    @Final
    protected int[] vertices;

    @Shadow
    public abstract boolean isTinted();

    @Unique
    @Override
    public void renderFast(
        Matrix4f transform,
        Matrix3f normal,
        IAcceleratedVertexConsumer extension,
        int combinedLight,
        int combinedOverlay,
        int color
    ) {
        IBufferGraph bufferGraph = extension.getBufferGraph();
        RenderType renderType = extension.getRenderType();

        Map<IBufferGraph, IMesh> meshes = MESHES.get(vertices);
        IMesh mesh = meshes.get(bufferGraph);

        if (mesh != null) {
            mesh.write(
                extension,
                getCustomColor(color),
                combinedLight,
                combinedOverlay
            );
            return;
        }

        MeshCollectorCuller meshCollectorCuller = new MeshCollectorCuller(renderType);
        VertexConsumer mesBuilder = extension.decorate(meshCollectorCuller);

        for (int i = 0; i < vertices.length / 8; i++) {
            int vertexOffset = i * IQuadTransformer.STRIDE;
            int posOffset = vertexOffset + IQuadTransformer.POSITION;
            int colorOffset = vertexOffset + IQuadTransformer.COLOR;
            int uv0Offset = vertexOffset + IQuadTransformer.UV0;
            int uv2Offset = vertexOffset + IQuadTransformer.UV2;
            int normalOffset = vertexOffset + IQuadTransformer.NORMAL;
            int packedNormal = vertices[normalOffset];

            int packedColor = vertices[colorOffset];

            int r = FastColor.ARGB32.red(packedColor);
            int g = FastColor.ARGB32.green(packedColor);
            int b = FastColor.ARGB32.blue(packedColor);
            int a = FastColor.ARGB32.alpha(packedColor);

            mesBuilder.vertex(
                Float.intBitsToFloat(vertices[posOffset + 0]),
                Float.intBitsToFloat(vertices[posOffset + 1]),
                Float.intBitsToFloat(vertices[posOffset + 2]),
                r, g, b, a,
                Float.intBitsToFloat(vertices[uv0Offset + 0]),
                Float.intBitsToFloat(vertices[uv0Offset + 1]),
                combinedOverlay,
                vertices[uv2Offset],
                ((byte) (packedNormal & 0xFF)) / 127.0f,
                ((byte) ((packedNormal >> 8) & 0xFF)) / 127.0f,
                ((byte) ((packedNormal >> 16) & 0xFF)) / 127.0f
            );
        }

        meshCollectorCuller.flush();

        mesh = AcceleratedItemRenderingFeature
            .getMeshType()
            .getBuilder()
            .build(meshCollectorCuller.getMeshCollector());

        meshes.put(bufferGraph, mesh);
        mesh.write(
            extension,
            getCustomColor(color),
            combinedLight,
            combinedOverlay
        );
    }

    @Unique
    @Override
    public int getCustomColor(int color) {
        return isTinted() ? color : -1;
    }
}
