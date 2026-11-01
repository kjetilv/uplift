package com.github.kjetilv.uplift.s3;

import module java.base;
import module java.net.http;
import com.github.kjetilv.uplift.kernel.Env;
import com.github.kjetilv.uplift.kernel.EnvLookup;
import com.github.kjetilv.uplift.kernel.io.BytesIO;
import com.github.kjetilv.uplift.kernel.io.Range;

import static java.net.http.HttpClient.Version.HTTP_1_1;

@SuppressWarnings("unused")
public interface S3Accessor {

    static S3Accessor fromEnvironment(Env env, Executor executor) {
        var client = HttpClient.newBuilder()
            .executor(executor)
            .version(HTTP_1_1)
            .build();
        return new DefaultS3Accessor(
            env,
            client,
            Optional.ofNullable(
                EnvLookup.get("taninim.bucket", "TANINIM_BUCKET")
            ).orElse("taninim-water"),
            null
        );
    }

    default Stream<String> list(String prefix) {
        return listInfos(prefix).stream()
            .map(RemoteInfo::key);
    }

    default Collection<RemoteInfo> listInfos(String prefix) {
        return remoteInfos(prefix).values();
    }

    default Map<String, Long> remoteSizes() {
        return remoteSizes(null);
    }

    default Map<String, Long> remoteSizes(String prefix) {
        return remoteInfos(prefix).entrySet()
            .stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> entry.getValue().size()
            ));
    }

    default OptionalLong remoteSize(String name) {
        return remoteInfo(name).map(info -> OptionalLong.of(info.size())).orElseGet(OptionalLong::empty);
    }

    default Optional<RemoteInfo> remoteInfo(String name) {
        return Optional.ofNullable(remoteInfos(name))
            .map(remoteInfos -> remoteInfos.get(name));
    }

    default Optional<Instant> lastModifiedRemote(String name) {
        return remoteInfo(name).map(RemoteInfo::lastModified);
    }

    default Optional<Long> length(String name) {
        return remoteInfo(name).map(RemoteInfo::size);
    }

    default Map<String, RemoteInfo> remoteInfos() {
        return remoteInfos(null);
    }

    default Optional<URI> presign(String name, Duration timeToLive) {
        return Optional.empty();
    }

    default Optional<? extends InputStream> stream(String name) {
        return stream(name, null);
    }

    default void put(String remoteName, String contents) {
        put(remoteName, contents.getBytes(StandardCharsets.UTF_8));
    }

    /// Reads the stream fully into memory and uploads it. The stream is not closed.
    ///
    /// @param remoteName  Object key
    /// @param inputStream Contents
    /// @param length      Expected number of bytes, or a negative number if not known
    /// @throws IllegalArgumentException If length is not negative and differs from the number of bytes read
    default void put(String remoteName, InputStream inputStream, long length) {
        var bytes = BytesIO.readInputStream(inputStream);
        if (length >= 0 && bytes.length != length) {
            throw new IllegalArgumentException(
                "Expected " + length + " bytes for " + remoteName + ", got " + bytes.length);
        }
        put(remoteName, bytes);
    }

    default void remove(String... objects) {
        remove(Arrays.asList(objects));
    }

    Optional<? extends InputStream> stream(String name, Range range);

    /// @param remoteName Object key
    /// @param bytes      Contents. Must not be modified while the call is in progress.
    void put(String remoteName, byte[] bytes);

    Map<String, RemoteInfo> remoteInfos(String prefix);

    void remove(Collection<String> objects);

    record RemoteInfo(
        String key,
        Instant lastModified,
        long size
    ) {

        @Override
        public String toString() {
            return getClass().getSimpleName() + "[" + key + "@" + lastModified + ": " + size + " bytes]";
        }
    }
}
