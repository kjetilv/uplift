package com.github.kjetilv.uplift.json.anno;

import module java.base;

@Target(ElementType.RECORD_COMPONENT)
@Retention(RetentionPolicy.SOURCE)
public @interface Field {

    String value();
}
