package com.github.argon4w.acceleratedrendering.core.extensions;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.ShaderInstance;
import org.joml.Matrix4f;

public interface ShaderInstanceExtension {
    void setDefaultUniforms(VertexFormat.Mode mode, Matrix4f frustumMatrix, Matrix4f projectionMatrix, Window window);

    static ShaderInstanceExtension of(ShaderInstance instance){
        return (ShaderInstanceExtension) instance;
    }
}
