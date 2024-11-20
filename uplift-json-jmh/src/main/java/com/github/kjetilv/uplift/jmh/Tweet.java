package com.github.kjetilv.uplift.jmh;

import com.github.kjetilv.uplift.json.JsonRecord;

@JsonRecord
public record Tweet(
    Long id,
    String id_str,
    Long in_reply_to_status_id,
    String in_reply_to_status_id_str,
    Long in_reply_to_user_id,
    String in_reply_to_user_id_str,
    String text,
    Activities activities,
    boolean truncated,
    boolean retweeted,
    String source,
    String created_at,
    Long retweet_count,
    Entities entities,
    User user,
    boolean favorited
) {
}
