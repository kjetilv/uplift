package com.github.kjetilv.uplift.json;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

final class Fields {

    public static List<Token.Field> pack(Collection<Token.Field> fields) {
        byte[] cache = new byte[fields.stream().mapToInt(Token.Field::length).sum()];
        int i = 0;
        List<Token.Field> cached = new ArrayList<>(fields.size());
        for (Token.Field field : fields) {
            System.arraycopy(
                field.bytes(),
                field.offset(),
                cache, i,
                field.length()
            );
            cached.add(field.in(cache, i));
            i += field.length();
        }
        return List.copyOf(cached);
    }

    private Fields() {
    }
}
