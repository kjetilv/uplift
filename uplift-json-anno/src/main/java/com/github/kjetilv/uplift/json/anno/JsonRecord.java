package com.github.kjetilv.uplift.json.anno;

import module java.base;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface JsonRecord {

    boolean root() default true;

    String factoryClass() default "";
}
