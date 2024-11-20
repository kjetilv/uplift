package com.github.kjetilv.uplift.jmh;

import com.github.kjetilv.uplift.json.JsonRecord;

import java.net.URI;
import java.util.List;

@JsonRecord(root = false)
public record Entities(
    List<String> hashtags,
    List<UserMention> user_mentions,
    List<URI> urls
) {

    @JsonRecord
    public record UserMention(
        List<Long> indices,
        String name,
        String id_str,
        Long id,
        String screen_name
    ) {
    }
}
