package com.github.kjetilv.uplift.synchttp.read;

import module java.base;
import com.github.kjetilv.uplift.util.RuntimeCloseable;

/// A simple pool of [memory segments][MemorySegment].
public final class Segments implements RuntimeCloseable {

    private final long baseSize;

    private final Arena arena;

    private final boolean close;

    private final MemorySegment memorySegment;

    private final boolean[] allocations;

    private final long basePoolSize;

    public Segments() {
        this(null, 0L, 0L);
    }

    public Segments(Arena arena) {
        this(arena, 0L, 0);
    }

    public Segments(Arena arena, long baseSize, long basePoolSize) {
        this.baseSize = baseSize > 0 ? baseSize : DEFAULT_BASE_SIZE;
        this.basePoolSize = basePoolSize > 0 ? basePoolSize : DEFAULT_BASE_POOL_SIZE;
        if (this.basePoolSize % this.baseSize != 0) {
            throw new IllegalArgumentException("Base pool size must be multiple of base size");
        }

        if (arena == null) {
            this.arena = Arena.ofAuto();
            this.close = false;
        } else {
            this.arena = arena;
            this.close = true;
        }
        this.memorySegment = this.arena.allocate(this.basePoolSize);
        this.allocations = new boolean[(int) (this.basePoolSize / this.baseSize)];
    }

    public Pooled acquire() {
        for (int i = 0; i < allocations.length; i++) {
            if (!allocations[i]) {
                allocations[i] = true;
                return new Pooled(
                    memorySegment.asSlice(i * baseSize, baseSize),
                    baseSize,
                    i
                );
            }
        }
        throw new IllegalStateException("No more segments available");
    }

    public void release(Pooled pooled) {
        if (pooled != null) {
            allocations[pooled.index()] = false;
        }
    }

    @Override
    public void close() {
        if (close) {
            arena.close();
        }
    }

    /// 512 bytes min
    private static final int DEFAULT_BASE_SIZE = 1 << 12;

    private static final int DEFAULT_BASE_POOL_SIZE = 1 << 24;

    public record Pooled(MemorySegment segment, long size, int index) {
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + basePoolSize + "/" + baseSize + "]";
    }
}
