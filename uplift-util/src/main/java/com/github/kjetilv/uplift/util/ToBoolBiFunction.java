package com.github.kjetilv.uplift.util;

public interface ToBoolBiFunction<T1, T2> {

    boolean applyAsBoolean(T1 t1, T2 t2);
}
