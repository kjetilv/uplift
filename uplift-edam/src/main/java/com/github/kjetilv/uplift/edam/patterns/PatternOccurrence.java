package com.github.kjetilv.uplift.edam.patterns;

import module java.base;
import com.github.kjetilv.uplift.edam.internal.Utils;
import com.github.kjetilv.uplift.hash.Hash;
import com.github.kjetilv.uplift.hash.HashKind;

import static java.util.Objects.requireNonNull;

/// @param hashPattern     A pattern
/// @param occurrences Actual occurrences following the pattern
@SuppressWarnings("unused")
public record PatternOccurrence<H extends HashKind<H>>(HashPattern<H> hashPattern, List<Occurrence<H>> occurrences)
    implements Spanning, Comparable<PatternOccurrence<H>>, Iterable<Occurrence<H>> {

    public PatternOccurrence(HashPattern<H> hashPattern) {
        this(hashPattern, new ArrayList<>());
    }

    public PatternOccurrence(HashPattern<H> hashPattern, List<Occurrence<H>> occurrences) {
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
        var firstTime = occurrences.getFirst();
        var unit = Utils.Unit.of(timespan().duration());
        var tail = occurrences.stream().skip(1)
            .map(occurrence ->
                occurrence.hash().toShortString() + "+" +
                unit.print(Duration.between(firstTime.time(), occurrence.time()))
            )
            .collect(Collectors.joining(" "));
        var head = firstTime.hash().toShortString() + "@" + print(firstTime.time());
        return "<" + head + " " + tail + ">";
    }

    public Optional<PatternOccurrence<H>> matchingOccurrence(Occurrence<H> occ) {
        return matchesNext(occ.hash())
            ? Optional.of(new PatternOccurrence<>(hashPattern, add(occ)))
            : Optional.empty();
    }

    public boolean matchesNext(Hash<H> hash) {
        return occurrences.size() < hashPattern().length() && hashPattern.hash(occurrences().size()).equals(hash);
    }

    public boolean match() {
        return hashPattern.length() == occurrences.size();
    }

    public boolean isSimple() {
        return hashPattern.isSimple();
    }

    @Override
    public Iterator<Occurrence<H>> iterator() {
        return occurrences.iterator();
    }

    private List<Occurrence<H>> add(Occurrence<H> occ) {
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
