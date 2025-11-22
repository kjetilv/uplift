package com.github.kjetilv.uplift.edam.internal;

import module java.base;
import com.github.kjetilv.uplift.hash.Hash;
import com.github.kjetilv.uplift.hash.HashBuilder;
import com.github.kjetilv.uplift.hash.HashKind;
import com.github.kjetilv.uplift.hash.Hashes;
import com.github.kjetilv.uplift.util.Bytes;

import static java.nio.charset.StandardCharsets.UTF_8;

final class ThrowableHasher<K extends HashKind<K>> implements Hasher<Throwable, K> {

    private final HashBuilder<Bytes, K> hashBuilder;

    private final boolean messages;

    private final Lock lock = new ReentrantLock();

    private final HashBuilder<String, K> strings;

    private final HashBuilder<Integer, K> ints;

    ThrowableHasher(boolean messages, HashBuilder<Bytes, K> hashBuilder) {
        this.hashBuilder = Objects.requireNonNull(hashBuilder, "idBuilder");
        this.strings = this.hashBuilder.map(ThrowableHasher::bytes);
        this.ints = this.hashBuilder.map(Hashes.intToBytes().andThen(Bytes::from));
        this.messages = messages;
    }

    @Override
    public Hash<K> hash(Throwable throwable) {
        Objects.requireNonNull(throwable, "throwable");
        var chain = Utils.ThrowableUtils.chain(throwable).toList();
        lock.lock();
        try {
            return hash(chain);
        } finally {
            lock.unlock();
        }
    }

    private Hash<K> hash(List<Throwable> chain) {
        chain.forEach(this::hashSingle);
        return hashBuilder.build();
    }

    private void hashSingle(Throwable t) {
        strings.hash(t.getClass().getName());
        if (messages) {
            strings.hash(t.getMessage());
        }
        var st = t.getStackTrace();
        ints.hash(st.length);
        for (var el : st) {
            strings(el).forEach(strings::hash);
            ints.hash(el.getLineNumber());
        }
    }

    private static Stream<String> strings(StackTraceElement el) {
        return Stream.of(
            el.getClassName(),
            el.getMethodName(),
            el.getFileName(),
            el.getModuleName(),
            el.getModuleVersion(),
            el.getClassLoaderName(),
            String.valueOf(el.getLineNumber())
        );
    }

    private static Bytes bytes(String string) {
        return string == null ? null : new Bytes(string.getBytes(UTF_8));
    }
}
