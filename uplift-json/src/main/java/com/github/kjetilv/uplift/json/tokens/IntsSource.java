package com.github.kjetilv.uplift.json.tokens;

import java.util.function.IntSupplier;

public final class IntsSource extends AbstractBytesSource {

    public IntsSource(IntSupplier nextChar) {
        super(nextChar);
    }
}
