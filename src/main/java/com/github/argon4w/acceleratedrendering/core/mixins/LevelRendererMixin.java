package com.github.argon4w.acceleratedrendering.core.mixins;

import com.github.argon4w.acceleratedrendering.core.CoreBuffers;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = LevelRenderer.class, priority = 998)
public class LevelRendererMixin {

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/OutlineBufferSource;endOutlineBatch()V"))
    public void endOutlineBatches(
        PoseStack poseStack,
        float partialTick,
        long finishNanoTime,
        boolean renderBlockOutline,
        Camera camera,
        GameRenderer gameRenderer,
        LightTexture lightTexture,
        Matrix4f projectionMatrix,
        CallbackInfo ci
    ) {
        CoreBuffers.POS_TEX_COLOR.drawBuffers();
        CoreBuffers.POS_TEX_COLOR.clearBuffers();
    }

    @WrapOperation(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;endLastBatch()V"))
    public void drawCoreBuffers(MultiBufferSource.BufferSource instance, Operation<Void> original) {
        CoreBuffers.ENTITY.drawBuffers();
        CoreBuffers.BLOCK.drawBuffers();
        CoreBuffers.POS_TEX.drawBuffers();
        CoreBuffers.POS_COLOR_TEX_LIGHT.drawBuffers();

        CoreBuffers.ENTITY.clearBuffers();
        CoreBuffers.BLOCK.clearBuffers();
        CoreBuffers.POS_TEX.clearBuffers();
        CoreBuffers.POS_COLOR_TEX_LIGHT.clearBuffers();

        original.call(instance);
    }
}
