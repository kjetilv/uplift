package com.github.kjetilv.uplift.synchttp.read;

import module java.base;

/// A simple, thread-safe pool of [memory segments][MemorySegment].
public final class Segments {

    private final long baseSize;

    private final long basePoolSize;

    private final Deque<Pooled> free;

    public Segments(Arena arena) {
        this(arena, 0L, 0);
    }

    public Segments(Arena arena, long baseSize, long basePoolSize) {
        this.baseSize = baseSize > 0 ? baseSize : DEFAULT_BASE_SIZE;
        this.basePoolSize = basePoolSize > 0 ? basePoolSize : DEFAULT_BASE_POOL_SIZE;
        if (this.basePoolSize % this.baseSize != 0) {
            throw new IllegalArgumentException("Base pool size must be multiple of base size");
        }
        var memorySegment = Objects.requireNonNull(arena, "arena")
            .allocate(this.basePoolSize);
        this.free = IntStream.range(0, Math.toIntExact(this.basePoolSize / this.baseSize))
            .mapToObj(i ->
                new Pooled(memorySegment.asSlice(i * this.baseSize, this.baseSize), this.baseSize, i))
            .collect(Collectors.toCollection(ConcurrentLinkedDeque::new));
    }

    /// @return A segment from the pool. If the pool is empty, a new segment, which is freed by the
    ///     garbage collector after it is released.
    public Pooled acquire() {
        var pooled = free.pollFirst();
        return pooled == null
            ? new Pooled(Arena.ofAuto().allocate(baseSize), baseSize, UNPOOLED)
            : pooled;
    }

    public void release(Pooled pooled) {
        if (pooled != null && pooled.index() != UNPOOLED) {
            free.offerFirst(pooled);
        }
    }

    private static final int UNPOOLED = -1;

    /// 4KiB min
    private static final int DEFAULT_BASE_SIZE = 1 << 12;

    /// 16MiB
    private static final int DEFAULT_BASE_POOL_SIZE = 1 << 24;

    public record Pooled(MemorySegment segment, long size, int index) {
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + basePoolSize + "/" + baseSize + "]";
    }
}
