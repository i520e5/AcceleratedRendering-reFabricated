package com.github.argon4w.acceleratedrendering.core.buffers;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexMultiConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.OutlineBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.FastColor;

import java.util.Optional;

public class SimpleOutlineBufferSource implements MultiBufferSource {

    private final MultiBufferSource bufferSource;
    private final MultiBufferSource outlineBufferSource;

    private int color;

    public SimpleOutlineBufferSource(MultiBufferSource bufferSource, MultiBufferSource outlineBufferSource) {
        this.bufferSource = bufferSource;
        this.outlineBufferSource = outlineBufferSource;
    }

    @Override
    public VertexConsumer getBuffer(RenderType pRenderType) {
        if (pRenderType.isOutline()) {
            return new OutlineBufferSource.EntityOutlineGenerator(
                outlineBufferSource.getBuffer(pRenderType),
                FastColor.ARGB32.red(color),
                FastColor.ARGB32.green(color),
                FastColor.ARGB32.blue(color),
                FastColor.ARGB32.alpha(color)
            );
        }

        Optional<RenderType> outline = pRenderType.outline();
        VertexConsumer buffer = bufferSource.getBuffer(pRenderType);

        if (outline.isEmpty()) {
            return buffer;
        }

        return VertexMultiConsumer.create(
            buffer,
            new OutlineBufferSource.EntityOutlineGenerator(
                outlineBufferSource.getBuffer(outline.get()),
                FastColor.ARGB32.red(color),
                FastColor.ARGB32.green(color),
                FastColor.ARGB32.blue(color),
                FastColor.ARGB32.alpha(color)
            )
        );
    }

    public void setColor(int color) {
        this.color = color;
    }
}
