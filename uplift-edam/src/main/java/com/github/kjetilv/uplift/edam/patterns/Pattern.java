package com.github.kjetilv.uplift.edam.patterns;

import module java.base;
import module uplift.hash;

import static com.github.kjetilv.uplift.edam.internal.Utils.Lists.requireNotEmpty;
import static java.util.Objects.requireNonNull;

/// @param hashes Hashes in this pattern
@SuppressWarnings("unused")
public record Pattern<K extends HashKind<K>>(List<Hash<K>> hashes) implements Comparable<Pattern<K>>, Iterable<Hash<K>> {

    @SafeVarargs
    @SuppressWarnings("unused")
    public Pattern(Hash<K>... hashes) {
        this(Cycles.find(hashes));
    }

    public Pattern {
        requireNotEmpty(hashes, "ids");
    }

    public String toShortString() {
        return toShortString(null);
    }

    public String toShortString(String delimiter) {
        int len = length();
        return len == 1
            ? shortHashes().getFirst()
            : "[" + len + "/" + String.join(delimiter == null ? "" : delimiter, shortHashes()) + "]";
    }

    public List<String> shortHashes() {
        return hashes.stream()
            .map(Hash::toShortString)
            .toList();
    }

    @Override
    public int compareTo(Pattern<K> pattern) {
        int length = length();
        int lengthCompare = Integer.compare(length, pattern.length());
        if (lengthCompare != 0) {
            return lengthCompare;
        }
        for (int i = 0; i < length; i++) {
            int compared = hash(i).compareTo(pattern.hash(i));
            if (compared != 0) {
                return compared;
            }
        }
        return 0;
    }

    public Hash<K> hash(int index) {
        return hashes.get(index);
    }

    public String toStringCustom(int digestLength) {
        return toStringCustom(hash -> hash.toStringCustom(digestLength));
    }

    public int count(Hash<K> hash) {
        return Math.toIntExact(hashes.stream()
            .filter(hash::equals).count());
    }

    public Pattern<K> cyclicSubPattern() {
        List<Hash<K>> base = Cycles.find(hashes);
        return base.size() == hashes.size()
            ? this
            : new Pattern<>(base);
    }

    public boolean isCandidate(Hash<K> start, Hash<K> end) {
        return hashes.size() == 1 && start.equals(end) && hashes.getFirst().equals(start)
               || hashes.size() > 1 && hashes.getFirst().equals(start) && hashes.getLast().equals(end);
    }

    public Pattern<K> with(Pattern<K> pattern) {
        return new Pattern<>(
            Stream.of(hashes(), pattern.hashes())
                .flatMap(List::stream)
                .toList()
        );
    }

    public int length() {
        return hashes.size();
    }

    @Override
    public Iterator<Hash<K>> iterator() {
        return hashes.iterator();
    }

    public boolean validOccurrences(List<Occurrence<K>> occurrences) {
        Iterator<Occurrence<K>> iterator = occurrences.iterator();
        for (Hash<K> hash : this) {
            if (!iterator.hasNext()) {
                return true;
            }
            if (!hash.equals(iterator.next().hash())) {
                return false;
            }
        }
        return true;
    }

    public PatternOccurrence<K> at(Instant... times) {
        return new PatternOccurrence<>(
            this,
            zip(Arrays.asList(times), hashes, Occurrence::new)
        );
    }

    public boolean isSimple() {
        return hashes.size() == 1;
    }

    public boolean isCombined() {
        return hashes.size() > 1;
    }

    private Stream<Hash<K>> hashStream() {
        return hashes.stream();
    }

    private String toStringCustom(Function<Hash<K>, String> toString) {
        return getClass().getSimpleName() + "[/" + hashes.size() + ":" +
               "<" +
               hashStream()
                   .map(toString)
                   .collect(Collectors.joining(" ")) +
               ">]";
    }

    private static <T1, T2, R> List<R> zip(Iterable<T1> t1, Iterable<T2> t2, BiFunction<T1, T2, R> f) {
        Iterator<T1> i1 = requireNonNull(t1, "t1").iterator();
        Iterator<T2> i2 = requireNonNull(t2, "t2").iterator();
        requireNonNull(f, "f");
        List<R> rs = new ArrayList<>();
        while (i1.hasNext() && i2.hasNext()) {
            rs.add(f.apply(i1.next(), i2.next()));
        }
        return List.copyOf(rs);
    }

    @Override
    public String toString() {
        int size = hashes.size();
        Function<Hash<K>, String> toString = size > 3 ? Hash::toShortString : Hash::toString;
        return toStringCustom(toString);
    }
}
