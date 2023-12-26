package com.github.kjetilv.uplift.json.example.gen;

import java.util.function.Consumer;

import com.github.kjetilv.uplift.json.events.AbstractCallbacks;
import com.github.kjetilv.uplift.json.example.Resident;

public class ResidentCallbacks extends AbstractCallbacks<ResidentBuilder, Resident> {

    public ResidentCallbacks(AbstractCallbacks<?, ?> parent, Consumer<Resident> onDone) {
        super(new ResidentBuilder(), parent, onDone);
        onString("name", ResidentBuilder::setName);
        onBoolean("permanent", ResidentBuilder::setPermanent);
    }
}
