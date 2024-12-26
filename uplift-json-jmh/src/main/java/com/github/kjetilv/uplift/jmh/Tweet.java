package com.github.kjetilv.uplift.jmh;

import com.github.kjetilv.uplift.json.JsonRecord;

import java.net.URI;
import java.util.List;

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
    String retweet_count,
    Entities entities,
    User user,
    boolean favorited
) {

    @JsonRecord(root = false)
    public record Entities(
        List<Hashtag> hashtags,
        List<UserMention> user_mentions,
        List<Url> urls
    ) {

        @JsonRecord(root = false)
        public record UserMention(
            List<Long> indices,
            String name,
            String id_str,
            Long id,
            String screen_name
        ) {
        }

        @JsonRecord(root = false)
        public record Hashtag(
            String text,
            List<Integer> indices
        ) {
        }
    }

    @JsonRecord(root = false)
    public record Url(
        List<String> indices,
        URI display_url,
        URI expanded_url,
        URI url
    ) {
    }

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

    @JsonRecord(root = false)
    public record User(
        String following,
        String notifications,
        boolean profile_background_tile,
        boolean contributors_enabled,
        boolean verified,
        Long friends_count,
        boolean is_translator,
        URI profile_background_image_url_https,
        String profile_link_color,
        Long listed_count,
        String profile_sidebar_border_color,
        URI profile_image_url,
        String description,
        boolean default_profile,
        Long favourites_count,
        String created_at,
        boolean profile_use_background_image,
        boolean show_all_inline_media,
        boolean geo_enabled,
        String time_zone,
        String profile_background_color,
        boolean default_profile_image,
        URI profile_background_image_url,
        Long followers_count,
        boolean protectedd,
        URI url,
        URI profile_image_url_https,
        String id_str,
        String lang,
        String name,
        Long statuses_count,
        String profile_text_color,
        Long id,
        String follow_request_sent,
        Long utc_offset,
        String profile_sidebar_fill_color,
        String location,
        String screen_name
    ) {
    }
}
