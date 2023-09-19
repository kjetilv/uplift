package com.github.kjetilv.uplift.json.gens;

import java.util.function.Consumer;

import com.github.kjetilv.uplift.json.AbstractCallbacks;
import com.github.kjetilv.uplift.json.Resident;

public class ResidentCallbacks<P> extends AbstractCallbacks<ResidentBuilder, Resident> {

    public ResidentCallbacks(AbstractCallbacks<?, ?> parent, Consumer<Resident> onDone) {
        super(new ResidentBuilder(), parent, onDone);
        ResidentBuilder residentBuilder = new ResidentBuilder();
        onString("name", ResidentBuilder::setName);
        onTruth("permanent", ResidentBuilder::setPermanent);
    }
}
