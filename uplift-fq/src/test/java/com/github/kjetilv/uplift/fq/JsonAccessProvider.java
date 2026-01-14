package com.github.kjetilv.uplift.fq;

import com.github.kjetilv.uplift.fq.io.ChannelIO;
import com.github.kjetilv.uplift.fq.paths.Reader;
import com.github.kjetilv.uplift.fq.paths.Writer;
import com.github.kjetilv.uplift.json.Json;
import com.github.kjetilv.uplift.json.bytes.MemorySegmentIntsBytesSource;
import com.github.kjetilv.uplift.json.io.ReadException;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

public class JsonAccessProvider implements AccessProvider<Path, Map<String, Object>> {

    private final Json json;

    private final Supplier<Arena> arena;

    private final byte[] ln;

    public JsonAccessProvider(Json json, Supplier<Arena> arena, byte separator) {
        this.json = Objects.requireNonNull(json, "json");
        this.arena = Objects.requireNonNull(arena, "arena");
        this.ln = new byte[] {separator};
    }

    @Override
    public Reader<Map<String, Object>> reader(Path source) {
        try (var randomAccessFile = ChannelIO.randomAccessFile(source)) {
            MemorySegment result;
            Arena arena1 = arena.get();
            try {
                result = randomAccessFile.getChannel()
                    .map(
                        FileChannel.MapMode.READ_ONLY,
                        0,
                        randomAccessFile.length(),
                        arena1 == null ? Arena.ofAuto() : arena1
                    );
            } catch (Exception e) {
                throw new IllegalStateException("Failed to map " + randomAccessFile, e);
            }
            var memorySegment = result;
            var bytesSource = new MemorySegmentIntsBytesSource(memorySegment);
            return new Reader<>() {

                @Override
                public Map<String, Object> read() {
                    try {
                        return json.map(bytesSource);
                    } catch (ReadException e) {
                        return null;
                    }
                }

                @Override
                public void close() {
                    ChannelIO.close(randomAccessFile, source);
                }
            };
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("resource")
    @Override
    public Writer<Map<String, Object>> writer(Path source) {
        var randomAccessFile = ChannelIO.randomAccessFile(source, true);
        var channel = randomAccessFile.getChannel();
        return new Writer<>() {

            @Override
            public Writer<Map<String, Object>> write(Map<String, Object> line) {
                json.write(line, channel);
                ln(channel, source);
                return this;
            }

            @Override
            public void close() {
                ChannelIO.close(randomAccessFile, source);
            }
        };
    }

    private void ln(FileChannel channel, Path source) {
        while (true) {
            try {
                if (channel.write(ByteBuffer.wrap(ln)) == 1) {
                    return;
                }
            } catch (Exception e) {
                throw new IllegalStateException("Could not write to " + source, e);
            }
        }
    }

}
