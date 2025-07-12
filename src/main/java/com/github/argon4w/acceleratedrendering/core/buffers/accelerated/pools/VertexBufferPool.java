package com.github.argon4w.acceleratedrendering.core.buffers.accelerated.pools;

import com.github.argon4w.acceleratedrendering.core.backends.buffers.MappedBuffer;
import com.github.argon4w.acceleratedrendering.core.backends.buffers.MutableBuffer;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.AcceleratedBufferSetPool;
import com.github.argon4w.acceleratedrendering.core.utils.SimpleResetPool;
import lombok.Getter;
import org.apache.commons.lang3.mutable.MutableLong;

import static org.lwjgl.opengl.GL44.GL_DYNAMIC_STORAGE_BIT;

public class VertexBufferPool extends SimpleResetPool<VertexBufferPool.VertexBuffer, Void> {

	private			final AcceleratedBufferSetPool.BufferSet	bufferSet;
	@Getter private	final MutableBuffer							vertexBufferOut;
	private			final MutableLong							vertexBufferSegments;
	private			final MutableLong							vertexBufferOutSize;
	private			final MutableLong							vertexBufferOutUsedSize;

	public VertexBufferPool(int size, AcceleratedBufferSetPool.BufferSet bufferSet) {
		super(size, null);

		this.bufferSet					= bufferSet;
		this.vertexBufferOut			= new MutableBuffer	(64L * size, GL_DYNAMIC_STORAGE_BIT);
		this.vertexBufferSegments		= new MutableLong	(0L);
		this.vertexBufferOutSize		= new MutableLong	(64L * size);
		this.vertexBufferOutUsedSize	= new MutableLong	(0L);
	}

	public void prepare() {
		vertexBufferOut		.resizeTo	(vertexBufferOutSize.getValue());
		vertexBufferSegments.setValue	(0L);
	}

	@Override
	public void delete() {
		vertexBufferOut	.delete();
		super			.delete();
	}

	@Override
	public void reset() {
		vertexBufferOutUsedSize	.setValue(0L);
		super					.reset();
	}

	@Override
	protected VertexBuffer create(Void context, int i) {
		return new VertexBuffer();
	}

	@Override
	protected void reset(VertexBuffer vertexBuffer) {
		vertexBuffer.poolReset();
	}

	@Override
	protected void delete(VertexBuffer vertexBuffer) {
		vertexBuffer.poolDelete();
	}

	@Override
	public boolean test(VertexBuffer vertexBuffer) {
		return vertexBufferOutUsedSize.addAndGet(vertexBuffer.getSize()) <= (2L * 1024L * 1024L * 1024L);
	}

	@Getter
	public class VertexBuffer extends MappedBuffer {

		private long offset;

		public VertexBuffer() {
			super(64L);
			this.offset = -1;
		}

		@Override
		public void onExpand(long bytes) {
			vertexBufferOutSize		.add(bytes);
			vertexBufferOutUsedSize	.add(bytes);
		}

		@Override
		public void delete() {
			throw new IllegalStateException("Pooled buffers cannot be deleted directly.");
		}

		@Override
		public void reset() {
			throw new IllegalStateException("Pooled buffers cannot be reset directly.");
		}

		private void poolDelete() {
			super.delete();
		}

		private void poolReset() {
			super.reset();
			offset = -1L;
		}

		public void allocateOffset() {
			offset = vertexBufferSegments.getAndAdd(position / bufferSet.getVertexSize());
		}
	}
}
