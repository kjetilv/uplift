package com.github.kjetilv.uplift.edam.internal;

import module java.base;
import module uplift.edam;
import module uplift.hash;
import com.github.kjetilv.uplift.edam.internal.Utils.ThrowableUtils;

record ThrowableInfoProvider<K extends HashKind<K>>(int briefCount)
    implements InfoProvider<Throwable, ThrowableInfo<K>, K> {

    ThrowableInfoProvider() {
        this(1);
    }

    @Override
    public ThrowableInfo<K> build(
        Throwable item,
        Occurrence<K> occurrence,
        Function<Throwable, String> printer
    ) {
        return new ThrowableInfo<>(
            item,
            occurrence,
            ThrowableUtils.lines(item),
            ThrowableUtils.lines(item, briefCount),
            ThrowableUtils.chain(item)
                .map(printer == null ? defaultPrinter() : printer)
                .toList()
        );
    }

    private static Function<Throwable, String> defaultPrinter() {
        return throwable ->
            name(throwable) + ": " + throwable.getMessage();
    }

    private static String name(Throwable throwable) {
        return switch (throwable) {
            case NullPointerException _ -> "NPE";
            case IllegalArgumentException _ -> "IAE";
            case IllegalStateException _ -> "ISE";
            case ArrayIndexOutOfBoundsException _ -> "AIOE";
            case UnsupportedOperationException _ -> "UOE";
            case null -> "null";
            default -> {
                Class<? extends Throwable> tc = throwable.getClass();
                yield tc.getModule().getName().equals("java.base")
                    ? tc.getSimpleName()
                    : tc.getName();
            }
        };
    }
}
