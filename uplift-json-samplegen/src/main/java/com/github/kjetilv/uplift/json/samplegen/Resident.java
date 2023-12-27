package com.github.kjetilv.uplift.json.samplegen;

import com.github.kjetilv.uplift.json.anno.JsonRecord;

import java.util.UUID;

@JsonRecord
public record Resident(String name, boolean permanent, UUID uuid) {

}
