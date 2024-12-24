package com.github.kjetilv.uplift.json.bytes;

import java.util.Objects;
import java.util.function.IntSupplier;

public final class IntsBytesSource extends AbstractBytesSource {

    public IntsBytesSource(IntSupplier nextChar) {
        super(Objects.requireNonNull(nextChar, "nextChar"));
    }
}
