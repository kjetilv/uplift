package com.github.kjetilv.uplift.edam.patterns;

import module java.base;
import module uplift.hash;
import com.github.kjetilv.uplift.edam.internal.Utils;

import static java.util.Objects.requireNonNull;

/// @param hashPattern     A pattern
/// @param occurrences Actual occurrences following the pattern
@SuppressWarnings("unused")
public record PatternOccurrence<K extends HashKind<K>>(HashPattern<K> hashPattern, List<Occurrence<K>> occurrences)
    implements Spanning, Comparable<PatternOccurrence<K>>, Iterable<Occurrence<K>> {

    public PatternOccurrence(HashPattern<K> hashPattern) {
        this(hashPattern, new ArrayList<>());
    }

    public PatternOccurrence(HashPattern<K> hashPattern, List<Occurrence<K>> occurrences) {
        if (hashPattern.validOccurrences(occurrences)) {
            this.hashPattern = requireNonNull(hashPattern, "pattern");
            this.occurrences = Utils.Lists.nonNullSorted(occurrences, "occurrences");
        } else {
            throw new IllegalArgumentException(hashPattern + ": Not an occurrence: " + occurrences);
        }
    }

    public Timespan timespan() {
        return Timespan.of(occurrences);
    }

    @Override
    public int compareTo(PatternOccurrence other) {
        return timespan().compareTo(other.timespan());
    }

    public String toStringBody() {
        Occurrence<K> firstTime = occurrences.getFirst();
        Utils.Unit unit = Utils.Unit.of(timespan().duration());
        String tail = occurrences.stream().skip(1)
            .map(occurrence ->
                occurrence.hash().toShortString() + "+" +
                unit.print(Duration.between(firstTime.time(), occurrence.time()))
            )
            .collect(Collectors.joining(" "));
        String head = firstTime.hash().toShortString() + "@" + print(firstTime.time());
        return "<" + head + " " + tail + ">";
    }

    public Optional<PatternOccurrence<K>> matchingOccurrence(Occurrence<K> occ) {
        return matchesNext(occ.hash())
            ? Optional.of(new PatternOccurrence<>(hashPattern, add(occ)))
            : Optional.empty();
    }

    public boolean matchesNext(Hash<K> hash) {
        return occurrences.size() < hashPattern().length() && hashPattern.hash(occurrences().size()).equals(hash);
    }

    public boolean match() {
        return hashPattern.length() == occurrences.size();
    }

    public boolean isSimple() {
        return hashPattern.isSimple();
    }

    @Override
    public Iterator<Occurrence<K>> iterator() {
        return occurrences.iterator();
    }

    private List<Occurrence<K>> add(Occurrence<K> occ) {
        return Stream.concat(occurrences.stream(), Stream.of(occ))
            .toList();
    }

    private static String print(Instant time) {
        return time.truncatedTo(ChronoUnit.MILLIS)
            .atZone(ZoneId.systemDefault())
            .format(DateTimeFormatter.ISO_OFFSET_TIME);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + hashPattern + ": " + occurrences + "]";
    }
}
