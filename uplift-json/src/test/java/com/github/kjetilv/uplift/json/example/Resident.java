package com.github.kjetilv.uplift.json.example;

import com.github.kjetilv.uplift.json.anno.JsRec;

@JsRec
public record Resident(String name, boolean permanent) {

}
