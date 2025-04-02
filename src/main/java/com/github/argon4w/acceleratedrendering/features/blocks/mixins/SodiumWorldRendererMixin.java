package com.github.argon4w.acceleratedrendering.features.blocks.mixins;

import com.github.argon4w.acceleratedrendering.core.CoreBuffers;
import com.github.argon4w.acceleratedrendering.core.buffers.SimpleCrumblingBufferSource;
import com.github.argon4w.acceleratedrendering.features.blocks.AcceleratedBlockEntityRenderingFeature;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import me.jellysquid.mods.sodium.client.render.SodiumWorldRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.BlockDestructionProgress;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

import java.util.SortedSet;

@Pseudo
@Mixin(SodiumWorldRenderer.class)
public class SodiumWorldRendererMixin {

    @WrapOperation(method = "renderBlockEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/blockentity/BlockEntityRenderDispatcher;render(Lnet/minecraft/world/level/block/entity/BlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;)V"))
    private static void wrapRenderBlockEntity(
            BlockEntityRenderDispatcher instance,
            BlockEntity pBlockEntity,
            float pPartialTick,
            PoseStack pPoseStack,
            MultiBufferSource pBufferSource,
            Operation<Void> original,
            @Local(argsOnly = true) Long2ObjectMap<SortedSet<BlockDestructionProgress>> blockBreakingProgressions
    ) {
        if (!AcceleratedBlockEntityRenderingFeature.isEnabled()) {
            original.call(
                    instance,
                    pBlockEntity,
                    pPartialTick,
                    pPoseStack,
                    pBufferSource
            );
            return;
        }

        if (!AcceleratedBlockEntityRenderingFeature.shouldUseAcceleratedPipeline()) {
            original.call(
                    instance,
                    pBlockEntity,
                    pPartialTick,
                    pPoseStack,
                    pBufferSource
            );
            return;
        }

        BlockPos blockPos = pBlockEntity.getBlockPos();
        MultiBufferSource bufferSource = CoreBuffers.CORE;
        SortedSet<BlockDestructionProgress> destructionProgresses = blockBreakingProgressions.get(blockPos.asLong());

        if (destructionProgresses == null || destructionProgresses.isEmpty()) {
            original.call(
                    instance,
                    pBlockEntity,
                    pPartialTick,
                    pPoseStack,
                    bufferSource
            );
            return;
        }

        int progress = destructionProgresses.last().getProgress();

        if (progress >= 0) {
            bufferSource = new SimpleCrumblingBufferSource(
                    bufferSource,
                    bufferSource.getBuffer(ModelBakery.DESTROY_TYPES.get(progress)),
                    pPoseStack.last(),
                    1.0f
            );
        }

        original.call(
                instance,
                pBlockEntity,
                pPartialTick,
                pPoseStack,
                bufferSource
        );
    }
}
