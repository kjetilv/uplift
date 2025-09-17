package com.github.kjetilv.uplift.edam.throwables;

import com.github.kjetilv.uplift.edam.Info;
import com.github.kjetilv.uplift.edam.patterns.Occurrence;
import com.github.kjetilv.uplift.hash.Hash;
import com.github.kjetilv.uplift.hash.HashKind;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public record ThrowableInfo<K extends HashKind<K>>(
    Throwable source,
    Occurrence<K> occurrence,
    List<String> full,
    List<String> brief,
    List<String> messages
) implements Info<Throwable, K> {

    public ThrowableInfo {
        Objects.requireNonNull(occurrence, "occurrence");
    }

    @Override
    public Hash<K> hash() {
        return occurrence.hash();
    }

    public int volume() {
        return full.stream().mapToInt(String::length).sum();
    }

    public int lines() {
        return full.size();
    }

    public String causeChain() {
        if (messages.size() > 1) {
            return messages()
                       .stream()
                       .limit(messages.size() - 1)
                       .collect(Collectors.joining(SEP, PREFIX, ""))
                   + END
                   + messages.getLast()
                   + SUFFIX;
        }
        return PREFIX + messages.getFirst() + SUFFIX;
    }

    private static final String SEP = " ⊢ ";

    private static final String END = " ⟘ ";

    private static final String PREFIX = Hash.LPAR;

    private static final String SUFFIX = Hash.RPAR;

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" +
               occurrence + ": " +
               full.size() + " chars, " +
               messages.size() + " messages" +
               "]";
    }
}
