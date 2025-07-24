package com.github.kjetilv.uplift.json.mame;

import com.github.kjetilv.uplift.edamame.KeyHandler;
import com.github.kjetilv.uplift.edamame.MapsMemoizer;
import com.github.kjetilv.uplift.edamame.MapsMemoizers;
import com.github.kjetilv.uplift.hash.HashKind;
import com.github.kjetilv.uplift.json.Callbacks;
import com.github.kjetilv.uplift.json.Token;

import java.util.function.Consumer;

public final class Mame {

    public static <H extends HashKind<H>> Callbacks climb(H kind, Consumer<Object> onDone) {
        MapsMemoizer<H, Token.Field> memoizer = MapsMemoizers.create(
            (KeyHandler<Token.Field>) key -> (Token.Field) key,
            kind
        );
        return TreeClimber.climb(kind, tree -> onDone.accept(tree.hashed()));
    }

    private Mame() {
    }
}
