package com.github.argon4w.acceleratedrendering.features.modelparts.mixins;

import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.IAcceleratedVertexConsumer;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.renderers.IAcceleratedRenderer;
import com.github.argon4w.acceleratedrendering.core.buffers.graphs.IBufferGraph;
import com.github.argon4w.acceleratedrendering.core.meshes.IMesh;
import com.github.argon4w.acceleratedrendering.core.meshes.collectors.MeshCollectorCuller;
import com.github.argon4w.acceleratedrendering.core.utils.ColorUtils;
import com.github.argon4w.acceleratedrendering.features.entities.AcceleratedEntityRenderingFeature;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.FastColor;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;

@Mixin(ModelPart.class)
public class ModelPartMixin implements IAcceleratedRenderer<Void> {

    @Shadow
    @Final
    private List<ModelPart.Cube> cubes;

    @Unique
    private final Map<IBufferGraph, IMesh> meshes = new Object2ObjectOpenHashMap<>();

    @Inject(method = "compile", at = @At("HEAD"), cancellable = true)
    public void compileFast(
        PoseStack.Pose pose,
        VertexConsumer vertexConsumer,
        int packedLight,
        int packedOverlay,
        float red,
        float green,
        float blue,
        float alpha,
        CallbackInfo ci
    ) {
        IAcceleratedVertexConsumer extension = (IAcceleratedVertexConsumer) vertexConsumer;

        if (!AcceleratedEntityRenderingFeature.isEnabled()) {
            return;
        }

        if (!AcceleratedEntityRenderingFeature.shouldUseAcceleratedPipeline()) {
            return;
        }

        if (!extension.isAccelerated()) {
            return;
        }

        ci.cancel();

        extension.doRender(
            this,
            null,
            pose.pose(),
            pose.normal(),
            packedLight,
            packedOverlay,
            ColorUtils.argb32Color(red, green, blue, alpha)
        );
    }

    @Unique
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
        IAcceleratedVertexConsumer extension = ((IAcceleratedVertexConsumer) vertexConsumer);

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

        MeshCollectorCuller meshCollectorCuller = new MeshCollectorCuller(renderType);
        VertexConsumer meshBuilder = extension.decorate(meshCollectorCuller);

        for (ModelPart.Cube cube : cubes) {
            for (ModelPart.Polygon polygon : cube.polygons) {
                Vector3f polygonNormal = polygon.normal;

                for (ModelPart.Vertex vertex : polygon.vertices) {
                    Vector3f vertexPosition = vertex.pos;

                    meshBuilder.vertex(
                        vertexPosition.x / 16.0f,
                        vertexPosition.y / 16.0f,
                        vertexPosition.z / 16.0f,
                        1,1,1,1,
                        vertex.u,
                        vertex.v,
                        overlay,
                        0,
                        polygonNormal.x,
                        polygonNormal.y,
                        polygonNormal.z
                    );
                }
            }
        }

        meshCollectorCuller.flush();

        mesh = AcceleratedEntityRenderingFeature
            .getMeshType()
            .getBuilder()
            .build(meshCollectorCuller.getMeshCollector());

        meshes.put(bufferGraph, mesh);
        mesh.write(
            extension,
            color,
            light,
            overlay
        );

        extension.endTransform();
    }
}
