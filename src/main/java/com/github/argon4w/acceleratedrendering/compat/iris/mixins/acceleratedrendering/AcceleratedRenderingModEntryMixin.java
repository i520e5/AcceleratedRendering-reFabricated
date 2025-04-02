package com.github.argon4w.acceleratedrendering.compat.iris.mixins.acceleratedrendering;

import com.github.argon4w.acceleratedrendering.AcceleratedRenderingModEntry;
import com.github.argon4w.acceleratedrendering.compat.iris.programs.IrisPrograms;
import com.github.argon4w.acceleratedrendering.core.utils.VertexFormatUtils;
import net.irisshaders.iris.vertices.IrisVertexFormats;
import net.neoforged.bus.api.IEventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = AcceleratedRenderingModEntry.class, remap = false)
public class AcceleratedRenderingModEntryMixin {

    @Inject(method = "conditionalInitialize", at = @At("TAIL"))
    public void registerIrisEvents(
        IEventBus modEventBus,
        CallbackInfo ci
    ) {
        modEventBus.register(IrisPrograms.class);
        VertexFormatUtils.registerElement(IrisVertexFormats.ENTITY_ELEMENT);
        VertexFormatUtils.registerElement(IrisVertexFormats.ENTITY_ID_ELEMENT);
        VertexFormatUtils.registerElement(IrisVertexFormats.MID_TEXTURE_ELEMENT);
        VertexFormatUtils.registerElement(IrisVertexFormats.TANGENT_ELEMENT);
        VertexFormatUtils.registerElement(IrisVertexFormats.MID_BLOCK_ELEMENT);
        VertexFormatUtils.registerElement(IrisVertexFormats.PADDING_SHORT);
    }
}
