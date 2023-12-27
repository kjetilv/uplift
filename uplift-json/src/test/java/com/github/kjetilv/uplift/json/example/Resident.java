package com.github.kjetilv.uplift.json.example;

import com.github.kjetilv.uplift.json.anno.JsonRecord;

@JsonRecord
public record Resident(String name, boolean permanent) {

}
