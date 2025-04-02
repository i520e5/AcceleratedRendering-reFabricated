package com.github.argon4w.acceleratedrendering.compat.iris.mixins.iris;

import com.google.common.collect.ImmutableMap;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.irisshaders.iris.vertices.IrisVertexFormats;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = IrisVertexFormats.class, priority = Integer.MAX_VALUE)
public class IrisVertexFormatsMixin {

    @Shadow @Final @Mutable public static VertexFormat ENTITY;
    @Shadow @Final @Mutable public static VertexFormat GLYPH;
    @Shadow @Final public static VertexFormatElement ENTITY_ID_ELEMENT;
    @Shadow @Final public static VertexFormatElement MID_TEXTURE_ELEMENT;
    @Shadow @Final public static VertexFormatElement TANGENT_ELEMENT;

    @WrapOperation(method = "<clinit>", at = @At(value = "FIELD", target = "Lnet/irisshaders/iris/vertices/IrisVertexFormats;ENTITY:Lcom/mojang/blaze3d/vertex/VertexFormat;", opcode = Opcodes.PUTSTATIC))
    private static void addPaddingForEntityFormat(VertexFormat value, Operation<Void> original) {
        ImmutableMap.Builder<String, VertexFormatElement> builder = ImmutableMap.builder();
        builder.put("Position", DefaultVertexFormat.ELEMENT_POSITION);
        builder.put("Color", DefaultVertexFormat.ELEMENT_COLOR);
        builder.put("UV0", DefaultVertexFormat.ELEMENT_UV0);
        builder.put("UV1", DefaultVertexFormat.ELEMENT_UV1);
        builder.put("UV2", DefaultVertexFormat.ELEMENT_UV2);
        builder.put("Normal", DefaultVertexFormat.ELEMENT_NORMAL);
        builder.put("Padding1", DefaultVertexFormat.ELEMENT_PADDING);
        builder.put("iris_Entity", ENTITY_ID_ELEMENT);
        builder.put("Padding2x1", DefaultVertexFormat.ELEMENT_PADDING);
        builder.put("Padding2x2", DefaultVertexFormat.ELEMENT_PADDING);
        builder.put("mc_midTexCoord", MID_TEXTURE_ELEMENT);
        builder.put("at_tangent", TANGENT_ELEMENT);
        original.call(new VertexFormat(builder.build()));
    }

    @WrapOperation(method = "<clinit>", at = @At(value = "FIELD", target = "Lnet/irisshaders/iris/vertices/IrisVertexFormats;GLYPH:Lcom/mojang/blaze3d/vertex/VertexFormat;", opcode = Opcodes.PUTSTATIC))
    private static void addPaddingForGlyphFormat(VertexFormat value, Operation<Void> original) {
        ImmutableMap.Builder<String, VertexFormatElement> builder = ImmutableMap.builder();
        builder.put("Position", DefaultVertexFormat.ELEMENT_POSITION);
        builder.put("Color", DefaultVertexFormat.ELEMENT_COLOR);
        builder.put("UV0", DefaultVertexFormat.ELEMENT_UV0);
        builder.put("UV2", DefaultVertexFormat.ELEMENT_UV2);
        builder.put("Normal", DefaultVertexFormat.ELEMENT_NORMAL);
        builder.put("Padding1", DefaultVertexFormat.ELEMENT_PADDING);
        builder.put("iris_Entity", ENTITY_ID_ELEMENT);
        builder.put("Padding2x1", DefaultVertexFormat.ELEMENT_PADDING);
        builder.put("Padding2x2", DefaultVertexFormat.ELEMENT_PADDING);
        builder.put("mc_midTexCoord", MID_TEXTURE_ELEMENT);
        builder.put("at_tangent", TANGENT_ELEMENT);
        original.call(new VertexFormat(builder.build()));
    }
}
