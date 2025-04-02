package com.github.argon4w.acceleratedrendering.features.items.mixins;

import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.IAcceleratedVertexConsumer;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.renderers.IAcceleratedRenderer;
import com.github.argon4w.acceleratedrendering.core.buffers.graphs.IBufferGraph;
import com.github.argon4w.acceleratedrendering.core.meshes.IMesh;
import com.github.argon4w.acceleratedrendering.core.meshes.collectors.MeshCollectorCuller;
import com.github.argon4w.acceleratedrendering.core.utils.DirectionUtils;
import com.github.argon4w.acceleratedrendering.core.utils.IntLazyMap;
import com.github.argon4w.acceleratedrendering.features.items.AcceleratedItemRenderContext;
import com.github.argon4w.acceleratedrendering.features.items.AcceleratedItemRenderingFeature;
import com.github.argon4w.acceleratedrendering.features.items.IAcceleratedBakedModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.FastColor;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.IQuadTransformer;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;
import java.util.Map;

@Mixin(SimpleBakedModel.class)
public abstract class SimpleBakedModelMixin implements IAcceleratedBakedModel, IAcceleratedRenderer<AcceleratedItemRenderContext> {

    @Shadow
    public abstract List<BakedQuad> getQuads(BlockState pState, Direction pDirection, RandomSource pRandom);

    @Unique
    private final Map<IBufferGraph, Int2ObjectMap<IMesh>> meshes = new Object2ObjectOpenHashMap<>();

    @Override
    public void renderItemFast(ItemStack itemStack, PoseStack poseStack, IAcceleratedVertexConsumer extension, int combinedLight, int combinedOverlay) {
        PoseStack.Pose pose = poseStack.last();

        extension.doRender(
            this,
            new AcceleratedItemRenderContext(
                itemStack,
                null,
                null
            ),
            pose.pose(),
            pose.normal(),
            combinedLight,
            combinedOverlay,
            -1
        );
    }

    @Override
    public void render(
        VertexConsumer vertexConsumer,
        AcceleratedItemRenderContext context,
        Matrix4f transform,
        Matrix3f normal,
        int light,
        int overlay,
        int color
    ) {
        ItemStack itemStack = context.getItemStack();
        ItemColor itemColor = context.getItemColor();
        IAcceleratedVertexConsumer extension = (IAcceleratedVertexConsumer) vertexConsumer;

        IBufferGraph bufferGraph = extension.getBufferGraph();
        RenderType renderType = extension.getRenderType();

        Int2ObjectMap<IMesh> layers = meshes.get(bufferGraph);

        extension.beginTransform(transform, normal);

        if (layers != null) {
            for (int layer : layers.keySet()) {
                IMesh mesh = layers.get(layer);

                mesh.write(
                    extension,
                    getCustomColor(layer, itemColor.getColor(itemStack, layer)),
                    light,
                    overlay
                );
            }

            extension.endTransform();
            return;
        }

        layers = new Int2ObjectLinkedOpenHashMap<>();
        meshes.put(bufferGraph, layers);

        IntLazyMap<MeshCollectorCuller> meshCollectors = new IntLazyMap<>(() -> new MeshCollectorCuller(renderType));

        for (Direction direction : DirectionUtils.FULL) {
            for (BakedQuad quad : getQuads(null, direction, null)) {
                MeshCollectorCuller meshCollectorCuller = meshCollectors.get(quad.getTintIndex());
                VertexConsumer meshBuilder = extension.decorate(meshCollectorCuller);

                int[] data = quad.getVertices();

                for (int i = 0; i < data.length / 8; i++) {
                    int vertexOffset = i * IQuadTransformer.STRIDE;
                    int posOffset = vertexOffset + IQuadTransformer.POSITION;
                    int colorOffset = vertexOffset + IQuadTransformer.COLOR;
                    int uv0Offset = vertexOffset + IQuadTransformer.UV0;
                    int uv2Offset = vertexOffset + IQuadTransformer.UV2;
                    int normalOffset = vertexOffset + IQuadTransformer.NORMAL;
                    int packedNormal = data[normalOffset];

                    float normalX = ((byte) (packedNormal & 0xFF)) / 127.0f;
                    float normalY = ((byte) ((packedNormal >> 8) & 0xFF)) / 127.0f;
                    float normalZ = ((byte) ((packedNormal >> 16) & 0xFF)) / 127.0f;

                    if (normalX == 0 && normalY == 0 && normalZ == 0) {
                        normalX = quad.getDirection().getNormal().getX();
                        normalY = quad.getDirection().getNormal().getY();
                        normalZ = quad.getDirection().getNormal().getZ();
                    }

                    int packedColor = data[colorOffset];

                    int r = FastColor.ARGB32.red(packedColor);
                    int g = FastColor.ARGB32.green(packedColor);
                    int b = FastColor.ARGB32.blue(packedColor);
                    int a = FastColor.ARGB32.alpha(packedColor);

                    meshBuilder.vertex(
                        Float.intBitsToFloat(data[posOffset + 0]),
                        Float.intBitsToFloat(data[posOffset + 1]),
                        Float.intBitsToFloat(data[posOffset + 2]),
                        r, g, b, a,
                        Float.intBitsToFloat(data[uv0Offset + 0]),
                        Float.intBitsToFloat(data[uv0Offset + 1]),
                        -1,
                        data[uv2Offset],
                        normalX,
                        normalY,
                        normalZ
                    );
                }
            }
        }

        for (int layer : meshCollectors.keySet()) {
            MeshCollectorCuller meshCollectorCuller = meshCollectors.get(layer);
            meshCollectorCuller.flush();

            IMesh mesh = AcceleratedItemRenderingFeature
                .getMeshType()
                .getBuilder()
                .build(meshCollectorCuller.getMeshCollector());

            layers.put(layer, mesh);
            mesh.write(
                extension,
                getCustomColor(layer, itemColor.getColor(itemStack, layer)),
                light,
                overlay
            );
        }

        extension.endTransform();
    }


    @Unique
    @Override
    public boolean isAccelerated() {
        return true;
    }

    @Unique
    @Override
    public int getCustomColor(int layer, int color) {
        return layer == -1 ? -1 : color;
    }
}
