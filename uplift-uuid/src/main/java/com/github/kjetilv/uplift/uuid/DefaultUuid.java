package com.github.kjetilv.uplift.uuid;

import java.util.UUID;

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
        return "⎨" + sub.substring(0, 5) + "⋯" + sub.substring(Uuid.DIGEST_LENGTH - 3, Uuid.DIGEST_LENGTH) + "⎬";
    }
}
