package com.github.argon4w.acceleratedrendering.core.extensions;

import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;

public interface VertexFormatExtension {

    int getOffset(VertexFormatElement element);

    static VertexFormatExtension of(VertexFormat vertexFormat){
        return (VertexFormatExtension) vertexFormat;
    }
}
