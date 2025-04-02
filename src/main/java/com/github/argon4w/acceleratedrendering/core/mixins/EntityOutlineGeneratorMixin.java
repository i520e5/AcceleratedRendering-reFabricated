package com.github.argon4w.acceleratedrendering.core.mixins;

import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.AcceleratedEntityOutlineGenerator;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.IAcceleratedVertexConsumer;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.renderers.DecoratedRenderer;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.renderers.IAcceleratedRenderer;
import com.github.argon4w.acceleratedrendering.core.buffers.graphs.IBufferGraph;
import com.mojang.blaze3d.vertex.DefaultedVertexConsumer;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.FastColor;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.nio.ByteBuffer;

@Mixin(targets = "net.minecraft.client.renderer.OutlineBufferSource$EntityOutlineGenerator")
public abstract class EntityOutlineGeneratorMixin extends DefaultedVertexConsumer implements IAcceleratedVertexConsumer{

    @Shadow @Final private VertexConsumer delegate;

    @Unique
    @Override
    public VertexConsumer decorate(VertexConsumer buffer) {
        return new AcceleratedEntityOutlineGenerator(buffer, FastColor.ARGB32.color(defaultA, defaultR, defaultG, defaultB));
    }

    @Unique
    @Override
    public void beginTransform(Matrix4f transform, Matrix3f normal) {
        throw new UnsupportedOperationException("Unsupported Operation.");
    }

    @Unique
    @Override
    public void endTransform() {
        throw new UnsupportedOperationException("Unsupported Operation.");
    }

    @Unique
    @Override
    public boolean isAccelerated() {
        return ((IAcceleratedVertexConsumer) delegate).isAccelerated();
    }

    @Unique
    @Override
    public IBufferGraph getBufferGraph() {
        throw new UnsupportedOperationException("Unsupported Operation.");
    }

    @Unique
    @Override
    public RenderType getRenderType() {
        throw new UnsupportedOperationException("Unsupported Operation.");
    }

    @Unique
    @Override
    public void addClientMesh(
            ByteBuffer meshBuffer,
            int size,
            int color,
            int light,
            int overlay
    ) {
        throw new UnsupportedOperationException("Unsupported Operation.");
    }

    @Unique
    @Override
    public void addServerMesh(
            int offset,
            int size,
            int color,
            int light,
            int overlay
    ) {
        throw new UnsupportedOperationException("Unsupported Operation.");
    }

    @Unique
    @Override
    public <T>  void doRender(
            IAcceleratedRenderer<T> renderer,
            T context,
            Matrix4f transform,
            Matrix3f normal,
            int light,
            int overlay,
            int color
    ) {
        ((IAcceleratedVertexConsumer) delegate).doRender(
                new DecoratedRenderer<>(renderer, this),
                context,
                transform,
                normal,
                light,
                overlay,
                color
        );
    }
}
