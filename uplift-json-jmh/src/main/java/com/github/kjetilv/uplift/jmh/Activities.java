package com.github.kjetilv.uplift.jmh;

import com.github.kjetilv.uplift.json.JsonRecord;

import java.util.List;

@JsonRecord(root = false)
public record Activities(
    String retweeters_count,
    List<String> retweeters,
    List<String> favoriters,
    String favoriters_count,
    List<String> repliers,
    String repliers_count
) {
}
