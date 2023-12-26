package com.github.kjetilv.uplift.json.samplegen;

import com.github.kjetilv.uplift.json.anno.JsRec;

@JsRec
public record Resident(String name, boolean permanent) {

}
