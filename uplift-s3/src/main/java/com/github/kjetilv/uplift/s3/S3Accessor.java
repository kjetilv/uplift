package com.github.kjetilv.uplift.s3;

import module java.base;
import module java.net.http;
import module uplift.kernel;

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
            null);
    }

    default Stream<String> list(String prefix) {
        return listInfos(prefix).stream().map(RemoteInfo::key);
    }

    default Collection<RemoteInfo> listInfos(String prefix) {
        return remoteInfos(prefix).values();
    }

    default Optional<? extends InputStream> stream(String name) {
        return stream(name, null);
    }

    default Map<String, Long> remoteSizes() {
        return remoteSizes(null);
    }

    default Map<String, Long> remoteSizes(String prefix) {
        return remoteInfos(prefix).entrySet().stream().collect(Collectors.toMap(
            Map.Entry::getKey,
            entry -> entry.getValue().size()
        ));
    }

    default OptionalLong remoteSize(String name) {
        return remoteInfo(name).map(info -> OptionalLong.of(info.size())).orElseGet(OptionalLong::empty);
    }

    default Optional<RemoteInfo> remoteInfo(String name) {
        var remoteInfo = remoteInfos(name);
        if (remoteInfo == null || remoteInfo.isEmpty()) {
            return Optional.empty();
        }
        if (remoteInfo.size() == 1) {
            return remoteInfo.values().stream().findAny();
        }
        throw new IllegalArgumentException(remoteInfo.size() + " entries for prefix " + name);
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

    Optional<? extends InputStream> stream(String name, Range range);

    default void put(String contents, String remoteName) {
        var bytes = contents.getBytes(StandardCharsets.UTF_8);
        put(remoteName, new ByteArrayInputStream(bytes), bytes.length);
    }

    default void put(String remoteName, byte[] bytes) {
        try (var data = new ByteArrayInputStream(bytes)) {
            put(remoteName, data, bytes.length);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to put " + bytes.length + " bytes @ " + remoteName, e);
        }
    }

    void put(String remoteName, InputStream inputStream, long length);

    Map<String, RemoteInfo> remoteInfos(String prefix);

    default void remove(String... objects) {
        remove(Arrays.asList(objects));
    }

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
