package com.github.kjetilv.uplift.json.gen;

import static org.assertj.core.api.Assertions.assertThat;

public interface Sessions {

    static Sessions create(
        String fqName,
        String source
    ) {
        return SessionsImpl.session(fqName, source);
    }

    Object read(String json);

    String write(Object object);

    default Object readAndVerify(String json) {
        var object = read(json);
        var json2 = write(object);
        var object2 = read(json2);
        assertThat(object2).isEqualTo(object);
        return object2;
    }
}
