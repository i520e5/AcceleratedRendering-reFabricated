package com.github.argon4w.acceleratedrendering.compat.iris.environments;

import com.github.argon4w.acceleratedrendering.compat.iris.IrisRenderType;
import com.github.argon4w.acceleratedrendering.core.backends.buffers.IServerBuffer;
import com.github.argon4w.acceleratedrendering.core.buffers.environments.IBufferEnvironment;
import com.github.argon4w.acceleratedrendering.core.meshes.ServerMesh;
import com.github.argon4w.acceleratedrendering.core.programs.culling.ICullingProgramSelector;
import com.github.argon4w.acceleratedrendering.core.programs.culling.LoadCullingProgramSelectorEvent;
import com.github.argon4w.acceleratedrendering.core.programs.dispatchers.IPolygonProgramDispatcher;
import com.github.argon4w.acceleratedrendering.core.programs.dispatchers.TransformProgramDispatcher;
import com.github.argon4w.acceleratedrendering.core.programs.extras.CompositeExtraVertex;
import com.github.argon4w.acceleratedrendering.core.programs.extras.IExtraVertexData;
import com.github.argon4w.acceleratedrendering.core.programs.processing.IPolygonProcessor;
import com.github.argon4w.acceleratedrendering.core.programs.processing.LoadPolygonProcessorEvent;
import com.github.argon4w.acceleratedrendering.core.extensions.VertexFormatExtension;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.irisshaders.iris.api.v0.IrisApi;
import net.irisshaders.iris.vertices.ImmediateState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModLoader;

public class IrisBufferEnvironment implements IBufferEnvironment {

    private final IBufferEnvironment vanillaSubSet;
    private final IBufferEnvironment irisSubSet;

    public IrisBufferEnvironment(
            IBufferEnvironment vanillaSubSet,
            VertexFormat vanillaVertexFormat,
            VertexFormat irisVertexFormat,
            ResourceLocation transformProgram
    ) {
        this.vanillaSubSet = vanillaSubSet;
        this.irisSubSet = new IrisSubSet(
                vanillaVertexFormat,
                irisVertexFormat,
                transformProgram
        );
    }

    private boolean shouldUseIrisSubSet() {
        return IrisApi.getInstance().isShaderPackInUse() && ImmediateState.isRenderingLevel;
    }

    private IBufferEnvironment getSubSet() {
        return shouldUseIrisSubSet() ? irisSubSet : vanillaSubSet;
    }

    @Override
    public void setupBufferState() {
        getSubSet().setupBufferState();
    }

    @Override
    public IExtraVertexData getExtraVertex(VertexFormat.Mode mode) {
        return getSubSet().getExtraVertex(mode);
    }

    @Override
    public VertexFormat getActiveFormat() {
        return getSubSet().getActiveFormat();
    }

    @Override
    public IServerBuffer getServerMeshBuffer() {
        return getSubSet().getServerMeshBuffer();
    }

    @Override
    public TransformProgramDispatcher selectTransformProgramDispatcher() {
        return getSubSet().selectTransformProgramDispatcher();
    }

    @Override
    public IPolygonProgramDispatcher selectCullProgramDispatcher(RenderType renderType) {
        return getSubSet().selectCullProgramDispatcher(renderType);
    }

    @Override
    public IPolygonProgramDispatcher selectProcessingProgramDispatcher(VertexFormat.Mode mode) {
        return getSubSet().selectProcessingProgramDispatcher(mode);
    }

    @Override
    public RenderType getRenderType(RenderType renderType) {
        return getSubSet().getRenderType(renderType);
    }

    @Override
    public boolean isAccelerated(VertexFormat vertexFormat) {
        return getSubSet().isAccelerated(vertexFormat);
    }

    @Override
    public int getOffset(VertexFormatElement element) {
        return getSubSet().getOffset(element);
    }

    @Override
    public int getVertexSize() {
        return getSubSet().getVertexSize();
    }

    public static class IrisSubSet implements IBufferEnvironment {

        private final VertexFormat vanillaVertexFormat;
        private final VertexFormat irisVertexFormat;

        private final TransformProgramDispatcher transformProgramDispatcher;
        private final ICullingProgramSelector cullingProgramSelector;
        private final IPolygonProcessor polygonProcessor;

        public IrisSubSet(
                VertexFormat vanillaVertexFormat,
                VertexFormat irisVertexFormat,
                ResourceLocation transformProgram
        ) {
            this.vanillaVertexFormat = vanillaVertexFormat;
            this.irisVertexFormat = irisVertexFormat;

            this.transformProgramDispatcher = new TransformProgramDispatcher(transformProgram);
            this.cullingProgramSelector = ModLoader.postEventWithReturn(new LoadCullingProgramSelectorEvent(this.irisVertexFormat)).getSelector();
            this.polygonProcessor = ModLoader.postEventWithReturn(new LoadPolygonProcessorEvent(this.irisVertexFormat)).getProcessor();
        }

        @Override
        public void setupBufferState() {
            irisVertexFormat.setupBufferState();
        }

        @Override
        public boolean isAccelerated(VertexFormat vertexFormat) {
            return this.vanillaVertexFormat == vertexFormat || this.irisVertexFormat == vertexFormat;
        }

        @Override
        public IExtraVertexData getExtraVertex(VertexFormat.Mode mode) {
            return new CompositeExtraVertex(cullingProgramSelector.getExtraVertex(mode), polygonProcessor.getExtraVertex(mode));
        }

        @Override
        public VertexFormat getActiveFormat() {
            return irisVertexFormat;
        }

        @Override
        public IServerBuffer getServerMeshBuffer() {
            return ServerMesh.Builder.INSTANCE.storageBuffers.get(irisVertexFormat);
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
            return renderType.format == vanillaVertexFormat ? new IrisRenderType(renderType, irisVertexFormat) : new IrisRenderType(renderType, renderType.format);
        }

        @Override
        public int getOffset(VertexFormatElement element) {
            return VertexFormatExtension.of(irisVertexFormat).getOffset(element);
        }

        @Override
        public int getVertexSize() {
            return irisVertexFormat.getVertexSize();
        }
    }
}
