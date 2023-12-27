package com.github.kjetilv.uplift.json.samplegen;

import com.github.kjetilv.uplift.json.anno.JsonRecord;

@JsonRecord
public record Resident(String name, boolean permanent) {

}
