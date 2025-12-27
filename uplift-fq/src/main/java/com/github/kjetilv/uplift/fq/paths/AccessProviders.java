package com.github.kjetilv.uplift.fq.paths;

import com.github.kjetilv.uplift.fq.AccessProvider;
import com.github.kjetilv.uplift.fq.paths.bytes.StreamAccessProvider;
import com.github.kjetilv.uplift.fq.paths.ffm.ChannelAccessProvider;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.time.Duration;
import java.util.function.BiConsumer;
import java.util.function.Function;

public final class AccessProviders {

    public static AccessProvider<Path, byte[]> stream(boolean gzipped, BiConsumer<Path, Duration> onMax) {
        return new StreamAccessProvider(gzipped, onMax);
    }

    public static AccessProvider<Path, ByteBuffer> channelBuffers() {
        return channelBuffers((byte) '\n');
    }

    public static AccessProvider<Path, byte[]> channelBytes() {
        return channelBytes((byte) '\n');
    }

    public static AccessProvider<Path, ByteBuffer> channelBuffers(byte separator) {
        return new ChannelAccessProvider<>(
            Arena::ofAuto,
            separator,
            MemorySegment::asByteBuffer,
            Function.identity(),
            () -> ByteBuffer.wrap(new byte[] {separator})
        );
    }

    public static ChannelAccessProvider<byte[]> channelBytes(byte separator) {
        return new ChannelAccessProvider<>(
            Arena::ofAuto,
            separator,
            segment ->
                segment.toArray(ValueLayout.JAVA_BYTE)
            ,
            ByteBuffer::wrap,
            () -> ByteBuffer.wrap(new byte[] {separator})
        );
    }

    private AccessProviders() {
    }
}
