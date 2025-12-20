package com.github.kjetilv.uplift.fq.paths.ffm;

import com.github.kjetilv.uplift.fq.Tombstone;
import com.github.kjetilv.uplift.fq.AccessProvider;
import com.github.kjetilv.uplift.fq.paths.PathTombstone;
import com.github.kjetilv.uplift.fq.paths.Puller;
import com.github.kjetilv.uplift.fq.paths.Writer;

import java.nio.ByteBuffer;
import java.nio.file.Path;

public class ChannelsAccessProvider implements AccessProvider<Path, byte[]> {

    @Override
    public Puller<byte[]> puller(Path path) {
        return new ByteBufferPuller(path);
    }

    @Override
    public Writer<byte[]> writer(Path path) {
        return new ByteBufferWriter(path);
    }

    @Override
    public Tombstone<Path> tombstone(Path path) {
        return new PathTombstone(path.resolve("done"));
    }

}
