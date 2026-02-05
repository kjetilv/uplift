package com.github.kjetilv.uplift.util;

import java.util.Optional;

public final class May {

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static <T> Maybe<? extends T> be(Optional<? extends T> optional) {
        return Maybe.a(optional);
    }

    private May(){}
}
