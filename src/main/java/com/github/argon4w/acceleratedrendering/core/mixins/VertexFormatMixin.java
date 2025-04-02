package com.github.argon4w.acceleratedrendering.core.mixins;

import com.github.argon4w.acceleratedrendering.core.utils.VertexFormatUtils;
import com.github.argon4w.acceleratedrendering.core.extensions.VertexFormatExtension;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import it.unimi.dsi.fastutil.ints.IntList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VertexFormat.class)
public abstract class VertexFormatMixin implements VertexFormatExtension {
    @Shadow public abstract ImmutableList<VertexFormatElement> getElements();

    @Shadow @Final public IntList offsets;

    @Unique
    private final int[] offsetsByElement = new int[32];

    @Inject(method = "<init>", at = @At("RETURN"))
    void constructor(ImmutableMap<String, VertexFormatElement> elementMapping, CallbackInfo ci) {
        for (int i = 0; i < offsetsByElement.length; i++) {
            VertexFormatElement vertexFormatElement = VertexFormatUtils.elementById(i);
            int j = vertexFormatElement != null ? this.getElements().indexOf(vertexFormatElement) : -1;
            this.offsetsByElement[i] = j != -1 ? this.offsets.getInt(j) : -1;
        }
    }

    @Override
    public int getOffset(VertexFormatElement element) {
        return offsetsByElement[VertexFormatUtils.elementId(element)];
    }
}
