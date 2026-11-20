package com.github.kjetilv.uplift.edam.internal;

import module java.base;
import com.github.kjetilv.uplift.edam.patterns.Occurrence;
import com.github.kjetilv.uplift.hash.Hash;
import com.github.kjetilv.uplift.hash.HashKind;

public interface Storage<T extends Supplier<Hash<H>>, H extends HashKind<H>> extends LongFunction<Occurrence<H>> {

    T get(long index);

    default Storage<T, H> store(T occurrence) {
        return store(List.of(occurrence));
    }

    Storage<T, H> store(Collection<T> occurrences);

    T getFirst();

    T getLast();

    boolean isEmpty();

    Cursor<T, H> rewind();

    Cursor<T, H> forward();

    Cursor<T, H> rewind(Hash<H> hash);

    Cursor<T, H> forward(Hash<H> hash);

    long count();

    interface Cursor<T extends Supplier<Hash<H>>, H extends HashKind<H>>
        extends Iterable<T>, Supplier<T> {

        @Override
        default T get() {
            return next().orElse(null);
        }

        @Override
        default Iterator<T> iterator() {
            return spool().iterator();
        }

        default Stream<T> spool() {
            return stream(this::next, _ -> true);
        }

        default Stream<T> stream(
            Supplier<Optional<T>> next,
            Predicate<T> timeCutoff
        ) {
            return Stream.generate(next)
                .takeWhile(Optional::isPresent)
                .flatMap(Optional::stream)
                .takeWhile(timeCutoff);
        }

        Stream<T> spool(Instant limit);

        Optional<T> next();
    }
}
