package com.github.argon4w.acceleratedrendering.features.entities;

import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.IAcceleratedVertexConsumer;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.renderers.IAcceleratedRenderer;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.FastColor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Unique;

public class AcceleratedEntityShadowRenderer implements IAcceleratedRenderer<AcceleratedEntityShadowRenderer.Context> {

    @Unique
    @Override
    public void render(
        VertexConsumer vertexConsumer,
        Context context,
        Matrix4f transform,
        Matrix3f normal,
        int light,
        int overlay,
        int color
    ) {
        IAcceleratedVertexConsumer extension = (IAcceleratedVertexConsumer) vertexConsumer;

        LevelReader levelReader = context.getLevelReader();
        ChunkAccess chunkAccess = context.getChunkAccess();
        BlockPos blockPos = context.getBlockPos();
        Vector3f position = context.getPosition();
        float size = context.getSize();
        float weight = context.getWeight();

        BlockPos belowPos = context.getBlockPos().below();
        BlockState blockState = chunkAccess.getBlockState(belowPos);

        if (blockState.getRenderShape() == RenderShape.INVISIBLE) {
            return;
        }

        int levelBrightness = levelReader.getMaxLocalRawBrightness(blockPos);

        if (levelBrightness <= 3) {
            return;
        }

        if (!blockState.isCollisionShapeFullBlock(chunkAccess, belowPos)) {
            return;
        }

        VoxelShape voxelShape = blockState.getShape(chunkAccess, belowPos);

        if (voxelShape.isEmpty()) {
            return;
        }

        float dimensionBrightness = LightTexture.getBrightness(levelReader.dimensionType(), levelBrightness);
        float shadowTransparency = weight * 0.5F * dimensionBrightness * 255.0f;

        if (shadowTransparency < 0.0F) {
            return;
        }

        if (shadowTransparency > 255.0F) {
            shadowTransparency = 255.0F;
        }
        int r = FastColor.ARGB32.red(color);
        int g = FastColor.ARGB32.green(color);
        int b = FastColor.ARGB32.blue(color);

        AABB bounds = voxelShape.bounds();

        float minX = blockPos.getX() + (float) bounds.minX;
        float maxX = blockPos.getX() + (float) bounds.maxX;
        float minY = blockPos.getY() + (float) bounds.minY;
        float minZ = blockPos.getZ() + (float) bounds.minZ;
        float maxZ = blockPos.getZ() + (float) bounds.maxZ;

        float minPosX = minX - position.x;
        float maxPosX = maxX - position.x;
        float minPosY = minY - position.y;
        float minPosZ = minZ - position.z;
        float maxPosZ = maxZ - position.z;

        float u0 = -minPosX / 2.0f / size + 0.5f;
        float u1 = -maxPosX / 2.0f / size + 0.5f;
        float v0 = -minPosZ / 2.0f / size + 0.5f;
        float v1 = -maxPosZ / 2.0f / size + 0.5f;

        extension.beginTransform(transform, normal);

        vertexConsumer.vertex(
            minPosX,
            minPosY,
            minPosZ,
            r,
            g,
            b,
            shadowTransparency,
            u0,
            v0,
            overlay,
            light,
            0.0f,
            1.0f,
            0.0f
        );

        vertexConsumer.vertex(
            minPosX,
            minPosY,
            maxPosZ,
            r,
            g,
            b,
            shadowTransparency,
            u0,
            v1,
            overlay,
            light,
            0.0f,
            1.0f,
            0.0f
        );

        vertexConsumer.vertex(
            maxPosX,
            minPosY,
            maxPosZ,
            r,
            g,
            b,
            shadowTransparency,
            u1,
            v1,
            overlay,
            light,
            0.0f,
            1.0f,
            0.0f
        );

        vertexConsumer.vertex(
            maxPosX,
            minPosY,
            minPosZ,
            r,
            g,
            b,
            shadowTransparency,
            u1,
            v0,
            overlay,
            light,
            0.0f,
            1.0f,
            0.0f
        );

        extension.endTransform();
    }

    public static class Context {

        private final LevelReader levelReader;
        private final ChunkAccess chunkAccess;
        private final BlockPos blockPos;
        private final Vector3f position;
        private final float size;
        private final float weight;

        public Context(
            LevelReader levelReader,
            ChunkAccess chunkAccess,
            BlockPos blockPos,
            Vector3f position,
            float size,
            float weight
        ) {
            this.levelReader = levelReader;
            this.chunkAccess = chunkAccess;
            this.blockPos = blockPos;
            this.position = position;
            this.size = size;
            this.weight = weight;
        }

        public LevelReader getLevelReader() {
            return levelReader;
        }

        public ChunkAccess getChunkAccess() {
            return chunkAccess;
        }

        public BlockPos getBlockPos() {
            return blockPos;
        }

        public Vector3f getPosition() {
            return position;
        }

        public float getSize() {
            return size;
        }

        public float getWeight() {
            return weight;
        }
    }
}
