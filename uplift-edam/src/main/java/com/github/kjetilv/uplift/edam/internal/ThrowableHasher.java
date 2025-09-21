package com.github.kjetilv.uplift.edam.internal;

import module uplift.hash;
import module uplift.util;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;

final class ThrowableHasher<K extends HashKind<K>> implements Hasher<Throwable, K> {

    private final HashBuilder<Bytes, K> hashBuilder;

    private final boolean messages;

    private final Lock lock = new ReentrantLock();

    private final HashBuilder<String, K> shb;

    private final HashBuilder<Integer, K> ihb;

    ThrowableHasher(boolean messages, HashBuilder<Bytes, K> hashBuilder) {
        this.hashBuilder = Objects.requireNonNull(hashBuilder, "idBuilder");
        this.shb = this.hashBuilder.map(ThrowableHasher::bytes);
        this.ihb = this.hashBuilder.map(Hashes.intToBytes().andThen(Bytes::from));
        this.messages = messages;
    }

    @Override
    public Hash<K> hash(Throwable throwable) {
        List<Throwable> chain = chain(throwable);
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
        shb.accept(t.getClass().getName());
        if (messages) {
            shb.accept(t.getMessage());
        }
        StackTraceElement[] st = t.getStackTrace();
        ihb.hash(st.length);
        for (StackTraceElement el : st) {
            hashElement(el);
        }
    }

    private void hashElement(StackTraceElement el) {
        strings(el).forEach(shb::hash);
        ihb.hash(el.getLineNumber());
    }

    private static Stream<String> strings(StackTraceElement el) {
        return Stream.of(
            el.getClassName(),
            el.getMethodName(),
            el.getFileName(),
            el.getModuleName(),
            el.getModuleVersion(),
            el.getClassLoaderName()
        );
    }

    private static List<Throwable> chain(Throwable throwable) {
        return Utils.ThrowableUtils.chain(Objects.requireNonNull(throwable, "throwable"))
            .toList();
    }

    private static Bytes bytes(String string) {
        return string == null ? null : new Bytes(string.getBytes(UTF_8));
    }
}
