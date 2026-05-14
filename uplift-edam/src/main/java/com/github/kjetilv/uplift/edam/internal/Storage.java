package com.github.kjetilv.uplift.edam.internal;

import module java.base;
import com.github.kjetilv.uplift.edam.patterns.Occurrence;
import com.github.kjetilv.uplift.hash.Hash;
import com.github.kjetilv.uplift.hash.HashKind;

interface Storage<H extends HashKind<H>> extends LongFunction<Occurrence<H>> {

    Occurrence<H> get(long index);

    default Storage<H> store(Occurrence<H> occurrence) {
        return store(List.of(occurrence));
    }

    Storage<H> store(Collection<Occurrence<H>> occurrences);

    Occurrence<H> getFirst();

    Occurrence<H> getLast();

    boolean isEmpty();

    Cursor<H> rewind();

    Cursor<H> forward();

    Cursor<H> rewind(Hash<H> hash);

    Cursor<H> forward(Hash<H> hash);

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
