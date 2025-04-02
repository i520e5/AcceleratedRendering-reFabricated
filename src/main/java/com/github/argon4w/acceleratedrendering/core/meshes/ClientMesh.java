package com.github.argon4w.acceleratedrendering.core.meshes;

import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.IAcceleratedVertexConsumer;
import com.github.argon4w.acceleratedrendering.core.meshes.collectors.MeshCollector;
import com.github.argon4w.acceleratedrendering.core.utils.ByteBufferBuilder;
import it.unimi.dsi.fastutil.objects.ReferenceLinkedOpenHashSet;

import java.nio.ByteBuffer;
import java.util.Set;

public class ClientMesh implements IMesh {

    private final int size;
    private final ByteBuffer vertexBuffer;

    public ClientMesh(int size, ByteBuffer vertexBuffer) {
        this.size = size;
        this.vertexBuffer = vertexBuffer;
    }

    @Override
    public void write(
            IAcceleratedVertexConsumer extension,
            int color,
            int light,
            int overlay
    ) {
        extension.addClientMesh(
                vertexBuffer,
                size,
                color,
                light,
                overlay
        );
    }

    public static class Builder implements IMesh.Builder {

        public static final Builder INSTANCE = new Builder();

        private final Set<ByteBufferBuilder> builders;

        private Builder() {
            this.builders = new ReferenceLinkedOpenHashSet<>();
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

            builders.add(builder);
            return new ClientMesh(vertexCount, result.createBuffer());
        }

        @Override
        public void close() {
            for (ByteBufferBuilder builder : builders) {
                builder.close();
            }
        }
    }
}
