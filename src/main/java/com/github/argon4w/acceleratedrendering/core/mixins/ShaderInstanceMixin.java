package com.github.argon4w.acceleratedrendering.core.mixins;

import com.github.argon4w.acceleratedrendering.core.extensions.ShaderInstanceExtension;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.ShaderInstance;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ShaderInstance.class)
public class ShaderInstanceMixin implements ShaderInstanceExtension {
    @Shadow
    public void setSampler(String name, Object textureId) {
        throw new RuntimeException("Stub!");
    }

    @Shadow @Final @Nullable public Uniform MODEL_VIEW_MATRIX;

    @Shadow @Final @Nullable public Uniform PROJECTION_MATRIX;

    @Shadow @Final @Nullable public Uniform COLOR_MODULATOR;

    @Shadow @Final @Nullable public Uniform GLINT_ALPHA;

    @Shadow @Final @Nullable public Uniform FOG_START;

    @Shadow @Final @Nullable public Uniform FOG_END;

    @Shadow @Final @Nullable public Uniform FOG_COLOR;

    @Shadow @Final @Nullable public Uniform FOG_SHAPE;

    @Shadow @Final @Nullable public Uniform TEXTURE_MATRIX;

    @Shadow @Final @Nullable public Uniform GAME_TIME;

    @Shadow @Final @Nullable public Uniform SCREEN_SIZE;

    @Shadow @Final @Nullable public Uniform LINE_WIDTH;

    public void setDefaultUniforms(VertexFormat.Mode mode, Matrix4f frustumMatrix, Matrix4f projectionMatrix, Window window) {
        for(int i = 0; i < 12; ++i) {
            int j = RenderSystem.getShaderTexture(i);
            this.setSampler("Sampler" + i, j);
        }

        if (this.MODEL_VIEW_MATRIX != null) {
            this.MODEL_VIEW_MATRIX.set(frustumMatrix);
        }

        if (this.PROJECTION_MATRIX != null) {
            this.PROJECTION_MATRIX.set(projectionMatrix);
        }

        if (this.COLOR_MODULATOR != null) {
            this.COLOR_MODULATOR.set(RenderSystem.getShaderColor());
        }

        if (this.GLINT_ALPHA != null) {
            this.GLINT_ALPHA.set(RenderSystem.getShaderGlintAlpha());
        }

        if (this.FOG_START != null) {
            this.FOG_START.set(RenderSystem.getShaderFogStart());
        }

        if (this.FOG_END != null) {
            this.FOG_END.set(RenderSystem.getShaderFogEnd());
        }

        if (this.FOG_COLOR != null) {
            this.FOG_COLOR.set(RenderSystem.getShaderFogColor());
        }

        if (this.FOG_SHAPE != null) {
            this.FOG_SHAPE.set(RenderSystem.getShaderFogShape().getIndex());
        }

        if (this.TEXTURE_MATRIX != null) {
            this.TEXTURE_MATRIX.set(RenderSystem.getTextureMatrix());
        }

        if (this.GAME_TIME != null) {
            this.GAME_TIME.set(RenderSystem.getShaderGameTime());
        }

        if (this.SCREEN_SIZE != null) {
            this.SCREEN_SIZE.set((float)window.getWidth(), (float)window.getHeight());
        }

        if (this.LINE_WIDTH != null && (mode == VertexFormat.Mode.LINES || mode == VertexFormat.Mode.LINE_STRIP)) {
            this.LINE_WIDTH.set(RenderSystem.getShaderLineWidth());
        }

        RenderSystem.setupShaderLights((ShaderInstance)(Object) this);
    }
}
