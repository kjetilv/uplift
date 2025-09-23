package com.github.kjetilv.uplift.edam;

import module java.base;
import module uplift.edam;
import module uplift.hash;

import static com.github.kjetilv.uplift.edam.internal.Utils.Lists.requireNotEmpty;
import static java.util.Objects.requireNonNull;

/// Analysis of repeated occurrences.  Spans
@SuppressWarnings("unused")
public sealed interface Analysis<K extends HashKind<K>> extends Timelined {

    ZoneId UTC = ZoneId.of("UTC");

    /// @return The number of occurrences of the [#trigger()] id
    default int triggerHashCount() {
        return count(trigger().hash());
    }

    /// @return The number of occurrences in all patterns in total
    default int hashesCount() {
        return count(null);
    }

    default Hash<K> triggerHash() {
        return trigger().hash();
    }

    /// @return UTC time in milliseconds resolution
    default ZonedDateTime utcTime() {
        return startTime().truncatedTo(java.time.temporal.ChronoUnit.MILLIS).atZone(UTC);
    }

    /// @return The time of the [trigger][#trigger()].
    @Override
    default Instant startTime() {
        return firstOccurrence().time();
    }

    /// @return The duration of the analysis, as in the duration between
    /// [first occurrence][#firstOccurrence()] and the [trigger][#trigger()].
    @Override
    default Duration duration() {
        return Duration.between(firstOccurrence().time(), trigger().time());
    }

    default Stream<PatternMatch<K>> combinedMatches() {
        return Stream.empty();
    }

    default Instant lastTime() {
        return trigger().time();
    }

    /// @return The number of distinct occurrences in all [#distinctPatternsCount()] total
    int distinctOccurrencesCount();

    /// @return The earliest occurrence in the analysis
    Occurrence<K> firstOccurrence();

    /// @return The occurrence that triggered the analysis, ie. the latest occurrence
    Occurrence<K> trigger();

    /// @return The number of patterns detected in this analysis
    int distinctPatternsCount();

    /// @return The number of occurences of the Id
    int count(Hash<K> hash);

    String toPatternMatchesString();

    /// @return The simple component of this analysis, if any.
    Optional<Simple<K>> simpleMatch();

    /// No repeats of the item in the given timespan/history length.
    ///
    /// @param trigger Occurrence
    record None<K extends HashKind<K>>(Occurrence<K> trigger) implements Analysis<K> {

        @Override
        public Timespan timespan() {
            return new Timespan(startTime());
        }

        @Override
        public Occurrence<K> firstOccurrence() {
            return trigger;
        }

        @Override
        public Duration duration() {
            return Duration.ZERO;
        }

        @Override
        public int distinctOccurrencesCount() {
            return 1;
        }

        @Override
        public int distinctPatternsCount() {
            return 0;
        }

        @Override
        public int count(Hash<K> hash) {
            return 1;
        }

        @Override
        public String toPatternMatchesString() {
            return trigger.hash().toShortString();
        }

        @Override
        public Optional<Simple<K>> simpleMatch() {
            return Optional.empty();
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + "[" + trigger + "]";
        }
    }

    /// Simple repeats of a single item, in the given timespan/history length.
    ///
    /// @param occurrences The occurrences
    record Simple<K extends HashKind<K>>(
        List<Occurrence<K>> occurrences
    ) implements Analysis<K> {

        @Override
        public Occurrence<K> trigger() {
            return occurrences.getLast();
        }

        @Override
        public Duration duration() {
            return Duration.between(startTime(), lastTime());
        }

        @Override
        public Timespan timespan() {
            return new Timespan(startTime(), lastTime());
        }

        @Override
        public int distinctOccurrencesCount() {
            return 1;
        }

        @Override
        public Occurrence<K> firstOccurrence() {
            return occurrences.getFirst();
        }

        @Override
        public Optional<Simple<K>> simpleMatch() {
            return Optional.of(this);
        }

        @Override
        public int distinctPatternsCount() {
            return 1;
        }

        @Override
        public int count(Hash<K> hash) {
            return Math.toIntExact(occurrences.stream()
                .filter(occ -> hash == null || hash.equals(occ.hash()))
                .count());
        }

        @Override
        public String toPatternMatchesString() {
            return Hash.toShortHashString(occurrences.stream()
                .map(Occurrence::hash)
                .toList());
        }
    }

    /// Various repeated sequences involving the item, in the given timespan/history length.  Can be
    /// [projected][#simple()] onto a [simple][Simple] analysis.
    ///
    /// @param trigger Trigger for the analysis
    /// @param matches The matches
    record Multiple<K extends HashKind<K>>(
        Occurrence<K> trigger,
        List<PatternMatch<K>> matches
    ) implements Analysis<K> {

        public Multiple {
            requireNonNull(trigger, "occurrence");
            requireNotEmpty(matches, "sequences");
            if (matches.stream()
                    .filter(PatternMatch::isSimple).count() != 1) {
                throw new IllegalArgumentException("Execpted exactly one simple match, got: " + matches);
            }
        }

        @Override
        public Duration duration() {
            return Duration.between(startTime(), lastTime());
        }

        @Override
        public Timespan timespan() {
            return new Timespan(startTime(), lastTime());
        }

        @Override
        public int distinctOccurrencesCount() {
            return Math.toIntExact(
                matches.stream()
                    .map(PatternMatch::occurrences)
                    .flatMap(Collection::stream)
                    .map(PatternOccurrence::occurrences)
                    .flatMap(Collection::stream)
                    .map(Occurrence::hash)
                    .distinct()
                    .count());
        }

        @Override
        public int distinctPatternsCount() {
            return matches.size();
        }

        @Override
        public int count(Hash<K> hash) {
            Stream<Occurrence<K>> occurrences = matches.stream().flatMap(patternMatch ->
                patternMatch.occurrences()
                    .stream()
                    .flatMap(patternOccurrence ->
                        patternOccurrence.occurrences()
                            .stream()
                            .filter(occurrence ->
                                hash == null || hash.equals(occurrence.hash()))));
            return Math.toIntExact(occurrences.distinct().count());
        }

        @Override
        public String toPatternMatchesString() {
            return PatternMatch.toShortString(matches);
        }

        @Override
        public Optional<Simple<K>> simpleMatch() {
            return matches.stream()
                .filter(PatternMatch::isSimple)
                .findFirst()
                .flatMap(PatternMatch::singleOccurrence)
                .map(Simple::new);
        }

        @Override
        public Stream<PatternMatch<K>> combinedMatches() {
            return matches.stream()
                .filter(PatternMatch::isCombined);
        }

        @SafeVarargs
        public final List<PatternOccurrence<K>> occurrences(Hash<K>... hashes) {
            return occurrences(List.of(hashes));
        }

        /// Project this analysis onto a simple analysis, showing only the number of
        /// times the [triggers][#trigger()] has occurred
        ///
        /// @return Simple analysis
        public Simple<K> simple() {
            PatternMatch<K> simplePattern = matches().stream()
                .filter(PatternMatch::isSimple)
                .findFirst()
                .orElseThrow(() ->
                    new IllegalStateException("No simple occurrence found: " + this));
            List<Occurrence<K>> occurrences = simplePattern.occurrences()
                .stream()
                .map(PatternOccurrence::occurrences)
                .flatMap(Collection::stream)
                .toList();
            return new Simple<>(occurrences);
        }

        public List<PatternOccurrence<K>> occurrences(List<Hash<K>> hashes) {
            return matches.stream()
                .filter(patternOccurrences1 ->
                    patternOccurrences1.hashPattern().hashes().equals(hashes))
                .map(PatternMatch::occurrences)
                .flatMap(List::stream)
                .toList();
        }

        public Occurrence<K> firstOccurrence() {
            return matches.stream()
                .min(Comparator.comparing(PatternMatch::startTime))
                .map(PatternMatch::firstOccurrence)
                .orElseThrow();
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + "[" + trigger + ": " +
                   "/" + matches.size() +
                   "<" + matches.stream()
                       .map(PatternMatch::toString)
                       .collect(Collectors.joining(",")) +
                   ">]";
        }
    }
}
