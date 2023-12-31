package com.github.kjetilv.uplift.json.samplegen;

import com.github.kjetilv.uplift.json.anno.JsonRecord;

import java.util.Map;
import java.util.UUID;

@JsonRecord(root = false)
public record Resident(String name, boolean permanent, UUID uuid, Map<String, Object> properties) {

}
