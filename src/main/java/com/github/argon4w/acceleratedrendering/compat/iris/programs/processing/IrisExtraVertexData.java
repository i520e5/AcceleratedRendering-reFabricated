package com.github.argon4w.acceleratedrendering.compat.iris.programs.processing;

import com.github.argon4w.acceleratedrendering.core.programs.extras.IExtraVertexData;
import com.github.argon4w.acceleratedrendering.core.extensions.VertexFormatExtension;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.irisshaders.iris.uniforms.CapturedRenderingState;
import net.irisshaders.iris.vertices.IrisVertexFormats;
import org.lwjgl.system.MemoryUtil;

public class IrisExtraVertexData implements IExtraVertexData {

    private final int entityOffset;
    private final int entityIdOffset;

    public IrisExtraVertexData(VertexFormat vertexFormat) {
        this.entityOffset = VertexFormatExtension.of(vertexFormat).getOffset(IrisVertexFormats.ENTITY_ELEMENT);
        this.entityIdOffset = VertexFormatExtension.of(vertexFormat).getOffset(IrisVertexFormats.ENTITY_ID_ELEMENT);
    }

    @Override
    public void addExtraVertex(long address) {
        if (entityOffset != -1) {
            MemoryUtil.memPutShort(address + entityOffset + 0L, (short) -1);
            MemoryUtil.memPutShort(address + entityOffset + 2L, (short) -1);
        }

        if (entityIdOffset != -1) {
            MemoryUtil.memPutShort(address + entityIdOffset + 0L, (short) CapturedRenderingState.INSTANCE.getCurrentRenderedEntity());
            MemoryUtil.memPutShort(address + entityIdOffset + 2L, (short) CapturedRenderingState.INSTANCE.getCurrentRenderedBlockEntity());
            MemoryUtil.memPutShort(address + entityIdOffset + 4L, (short) CapturedRenderingState.INSTANCE.getCurrentRenderedItem());
        }
    }

    @Override
    public void addExtraVarying(long address) {

    }
}
