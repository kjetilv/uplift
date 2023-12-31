package com.github.kjetilv.uplift.json.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface JsonRecord {

    boolean root() default true;

    boolean readOnly() default false;

    boolean writeOnly() default false;

    String factoryClass() default "";
}
