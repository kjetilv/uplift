package com.github.kjetilv.uplift.fq;

public interface Fio<I, O> {

    O read(I line);

    I write(O value);
}
