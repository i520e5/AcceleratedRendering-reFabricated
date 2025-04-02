package com.github.argon4w.acceleratedrendering.core.utils;

import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

public class ByteBufferBuilder implements AutoCloseable {
    private static final MemoryUtil.MemoryAllocator ALLOCATOR = MemoryUtil.getAllocator(false);
    public long pointer;
    public int size;
    public int currentOffset;
    private int nextResultOffset;
    private int resultCount;
    private int currentResultId;

    public ByteBufferBuilder(int size) {
        this.size = size;
        this.pointer = ALLOCATOR.malloc(size);
        if (this.pointer == 0L) {
            throw new OutOfMemoryError("Failed to allocate " + size + " bytes");
        }
    }

    public long reserve(int bytes) {
        int i = this.currentOffset;
        int j = i + bytes;
        this.ensureCapacity(j);
        this.currentOffset = j;
        return this.pointer + (long)i;
    }

    private void ensureCapacity(int size) {
        if (size > this.size) {
            int i = Math.min(this.size, 2097152);
            int j = Math.max(this.size + i, size);
            this.resize(j);
        }

    }

    private void resize(int newSize) {
        this.pointer = ALLOCATOR.realloc(this.pointer, newSize);
        if (this.pointer == 0L) {
            throw new OutOfMemoryError("Failed to resize buffer from " + this.size + " bytes to " + newSize + " bytes");
        } else {
            this.size = newSize;
        }
    }

    @Nullable
    public Result build() {
        this.checkValid();
        int i = this.nextResultOffset;
        int j = this.currentOffset - i;
        if (j == 0) {
            return null;
        } else {
            this.nextResultOffset = this.currentOffset;
            ++this.resultCount;
            return new Result(i, j, this.currentResultId);
        }
    }

    public void clear() {
        this.discardAll();
    }

    public void discardAll() {
        this.checkValid();
        if (this.resultCount > 0) {
            this.discard();
            this.resultCount = 0;
        }

    }

    public boolean isValid(int id) {
        return id == this.currentResultId;
    }

    void freeResult() {
        if (--this.resultCount <= 0) {
            this.discard();
        }

    }

    private void discard() {
        int i = this.currentOffset - this.nextResultOffset;
        if (i > 0) {
            MemoryUtil.memCopy(this.pointer + (long)this.nextResultOffset, this.pointer, (long)i);
        }

        this.currentOffset = i;
        this.nextResultOffset = 0;
        ++this.currentResultId;
    }

    public void close() {
        if (this.pointer != 0L) {
            ALLOCATOR.free(this.pointer);
            this.pointer = 0L;
            this.currentResultId = -1;
        }

    }

    private void checkValid() {
        if (this.pointer == 0L) {
            throw new IllegalStateException("Buffer has been freed");
        }
    }

    public class Result implements AutoCloseable {
        private final int pointer;
        private final int capacity;
        private final int resultId;
        private boolean closed;

        Result(int pointer, int capacity, int resultId) {
            this.pointer = pointer;
            this.capacity = capacity;
            this.resultId = resultId;
        }

        public ByteBuffer createBuffer() {
            if (!ByteBufferBuilder.this.isValid(this.resultId)) {
                throw new IllegalStateException("Buffer has been freed.");
            } else {
                return MemoryUtil.memByteBuffer(ByteBufferBuilder.this.pointer + (long)this.pointer, this.capacity);
            }
        }

        public void close() {
            if (!this.closed) {
                this.closed = true;
                if (ByteBufferBuilder.this.isValid(this.resultId)) {
                    ByteBufferBuilder.this.freeResult();
                }
            }

        }
    }
}