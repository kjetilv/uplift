package com.github.kjetilv.uplift.uuid;

import module java.base;

import static java.util.Objects.requireNonNull;

record DefaultUuid(UUID uuid) implements Uuid {

    DefaultUuid {
        requireNonNull(uuid, "uuid");
    }

    DefaultUuid(String digest) {
        this(Uuids.uuid(digest));
    }

    @Override
    public String toString() {
        String sub = digest();
        return "⎨" + sub.substring(0, 5) + "⋯" + sub.substring(DIGEST_LENGTH - 3, DIGEST_LENGTH) + "⎬";
    }
}
