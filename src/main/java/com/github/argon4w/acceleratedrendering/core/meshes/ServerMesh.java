package com.github.argon4w.acceleratedrendering.core.meshes;

import com.github.argon4w.acceleratedrendering.core.backends.buffers.MappedBuffer;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.IAcceleratedVertexConsumer;
import com.github.argon4w.acceleratedrendering.core.meshes.collectors.MeshCollector;
import com.github.argon4w.acceleratedrendering.core.utils.ByteBufferBuilder;
import com.github.argon4w.acceleratedrendering.core.utils.LazyMap;
import com.mojang.blaze3d.vertex.VertexFormat;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.Map;

public class ServerMesh implements IMesh {

    private final int size;
    private final int offset;

    public ServerMesh(int size, int offset) {
        this.size = size;
        this.offset = offset;
    }

    @Override
    public void write(
            IAcceleratedVertexConsumer extension,
            int color,
            int light,
            int overlay
    ) {
        extension.addServerMesh(
                offset,
                size,
                color,
                light,
                overlay
        );
    }

    public static class Builder implements IMesh.Builder {

        public static final Builder INSTANCE = new Builder();

        public final Map<VertexFormat, MappedBuffer> storageBuffers;

        private Builder() {
            this.storageBuffers = new LazyMap<>(() -> new MappedBuffer(1024L, true));
        }

        @Override
        public IMesh build(MeshCollector collector) {
            int vertexCount = collector.getVertexCount();

            if (vertexCount == 0) {
                return EmptyMesh.INSTANCE;
            }

            ByteBufferBuilder builder = collector.getBuffer();
            ByteBufferBuilder.Result result = builder.build();

            if (result == null) {
                builder.close();
                return EmptyMesh.INSTANCE;
            }

            ByteBuffer clientBuffer = result.createBuffer();
            MappedBuffer serverBuffer = storageBuffers.get(collector.getVertexFormat());

            long capacity = clientBuffer.capacity();
            long position = serverBuffer.getPosition();

            MemoryUtil.memCopy(
                    MemoryUtil.memAddress0(clientBuffer),
                    serverBuffer.reserve(capacity),
                    capacity
            );

            builder.close();
            return new ServerMesh(vertexCount, (int) position);
        }

        @Override
        public void close() {
            for (MappedBuffer buffer : storageBuffers.values()) {
                buffer.delete();
            }
        }
    }
}
