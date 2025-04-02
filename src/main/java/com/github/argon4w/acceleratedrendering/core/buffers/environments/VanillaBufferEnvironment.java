package com.github.argon4w.acceleratedrendering.core.buffers.environments;

import com.github.argon4w.acceleratedrendering.core.backends.buffers.IServerBuffer;
import com.github.argon4w.acceleratedrendering.core.meshes.ServerMesh;
import com.github.argon4w.acceleratedrendering.core.programs.culling.ICullingProgramSelector;
import com.github.argon4w.acceleratedrendering.core.programs.culling.LoadCullingProgramSelectorEvent;
import com.github.argon4w.acceleratedrendering.core.programs.dispatchers.IPolygonProgramDispatcher;
import com.github.argon4w.acceleratedrendering.core.programs.dispatchers.TransformProgramDispatcher;
import com.github.argon4w.acceleratedrendering.core.programs.extras.IExtraVertexData;
import com.github.argon4w.acceleratedrendering.core.programs.processing.IPolygonProcessor;
import com.github.argon4w.acceleratedrendering.core.programs.processing.LoadPolygonProcessorEvent;
import com.github.argon4w.acceleratedrendering.core.extensions.VertexFormatExtension;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModLoader;

public class VanillaBufferEnvironment implements IBufferEnvironment {

    private final VertexFormat vertexFormat;

    private final TransformProgramDispatcher transformProgramDispatcher;
    private final ICullingProgramSelector cullingProgramSelector;
    private final IPolygonProcessor polygonProcessor;

    public VanillaBufferEnvironment(VertexFormat vertexFormat, ResourceLocation key) {
        this.vertexFormat = vertexFormat;

        this.transformProgramDispatcher = new TransformProgramDispatcher(key);
        this.cullingProgramSelector = ModLoader.postEventWithReturn(new LoadCullingProgramSelectorEvent(this.vertexFormat)).getSelector();
        this.polygonProcessor = ModLoader.postEventWithReturn(new LoadPolygonProcessorEvent(this.vertexFormat)).getProcessor();
    }

    @Override
    public void setupBufferState() {
        vertexFormat.setupBufferState();
    }

    @Override
    public IExtraVertexData getExtraVertex(VertexFormat.Mode mode) {
        return cullingProgramSelector.getExtraVertex(mode);
    }

    @Override
    public VertexFormat getActiveFormat() {
        return vertexFormat;
    }

    @Override
    public IServerBuffer getServerMeshBuffer() {
        return ServerMesh.Builder.INSTANCE.storageBuffers.get(vertexFormat);
    }

    @Override
    public TransformProgramDispatcher selectTransformProgramDispatcher() {
        return transformProgramDispatcher;
    }

    @Override
    public IPolygonProgramDispatcher selectCullProgramDispatcher(RenderType renderType) {
        return cullingProgramSelector.select(renderType);
    }

    @Override
    public IPolygonProgramDispatcher selectProcessingProgramDispatcher(VertexFormat.Mode mode) {
        return polygonProcessor.select(mode);
    }

    @Override
    public RenderType getRenderType(RenderType renderType) {
        return renderType;
    }

    @Override
    public boolean isAccelerated(VertexFormat vertexFormat) {
        return this.vertexFormat == vertexFormat;
    }

    @Override
    public int getOffset(VertexFormatElement element) {
        return VertexFormatExtension.of(vertexFormat).getOffset(element);
    }

    @Override
    public int getVertexSize() {
        return vertexFormat.getVertexSize();
    }
}
