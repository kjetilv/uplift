package com.github.kjetilv.uplift.jmh;

import com.github.kjetilv.uplift.json.JsonRecord;

import java.net.URI;

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
