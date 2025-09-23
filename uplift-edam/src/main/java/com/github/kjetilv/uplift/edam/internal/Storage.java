package com.github.kjetilv.uplift.edam.internal;

import module java.base;
import module uplift.edam;
import module uplift.hash;

interface Storage<K extends HashKind<K>> extends LongFunction<Occurrence<K>> {

    Occurrence<K> get(long index);

    default Storage<K> store(Occurrence<K> occurrence) {
        return store(List.of(occurrence));
    }

    Storage<K> store(Collection<Occurrence<K>> occurrences);

    Occurrence<K> getFirst();

    Occurrence<K> getLast();

    boolean isEmpty();

    Cursor<K> rewind();

    Cursor<K> forward();

    Cursor<K> rewind(Hash<K> hash);

    Cursor<K> forward(Hash<K> hash);

    long count();

    interface Cursor<K extends HashKind<K>>
        extends Iterable<Occurrence<K>>, Supplier<Occurrence<K>> {

        @Override
        default Occurrence<K> get() {
            return next().orElse(null);
        }

        @Override
        default Iterator<Occurrence<K>> iterator() {
            return spool().iterator();
        }

        default Stream<Occurrence<K>> spool() {
            return stream(this::next, _ -> true);
        }

        default Stream<Occurrence<K>> stream(
            Supplier<Optional<Occurrence<K>>> next,
            Predicate<Occurrence<K>> timeCutoff
        ) {
            return Stream.generate(next)
                .takeWhile(Optional::isPresent)
                .flatMap(Optional::stream)
                .takeWhile(timeCutoff);
        }

        Stream<Occurrence<K>> spool(Instant limit);

        Optional<Occurrence<K>> next();
    }
}
