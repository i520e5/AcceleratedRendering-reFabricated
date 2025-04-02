/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model;

import com.github.argon4w.acceleratedrendering.core.extensions.VertexFormatExtension;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import java.util.Arrays;
import java.util.List;
import net.minecraft.client.renderer.block.model.BakedQuad;

/**
 * Transformer for {@link BakedQuad baked quads}.
 *
 * @see QuadTransformers
 */
public interface IQuadTransformer {
    int STRIDE = DefaultVertexFormat.BLOCK.getVertexSize() / 4;
    int POSITION = findOffset(DefaultVertexFormat.ELEMENT_POSITION);
    int COLOR = findOffset(DefaultVertexFormat.ELEMENT_COLOR);
    int UV0 = findOffset(DefaultVertexFormat.ELEMENT_UV0);
    int UV1 = findOffset(DefaultVertexFormat.ELEMENT_UV1);
    int UV2 = findOffset(DefaultVertexFormat.ELEMENT_UV2);
    int NORMAL = findOffset(DefaultVertexFormat.ELEMENT_NORMAL);

    void processInPlace(BakedQuad quad);

    default void processInPlace(List<BakedQuad> quads) {
        for (BakedQuad quad : quads)
            processInPlace(quad);
    }

    private static int findOffset(VertexFormatElement element) {
        if (DefaultVertexFormat.BLOCK.getElements().contains(element)) {
            // Divide by 4 because we want the int offset
            return VertexFormatExtension.of(DefaultVertexFormat.BLOCK).getOffset(element) / 4;
        }
        return -1;
    }
}
