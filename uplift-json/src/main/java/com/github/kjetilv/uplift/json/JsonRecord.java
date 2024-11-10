package com.github.kjetilv.uplift.json;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface JsonRecord {

    boolean root() default true;

    String factoryClass() default "";
}
