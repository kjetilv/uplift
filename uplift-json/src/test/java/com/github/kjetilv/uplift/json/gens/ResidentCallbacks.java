package com.github.kjetilv.uplift.json.gens;

import java.util.function.Consumer;

import com.github.kjetilv.uplift.json.AbstractCallbacks;
import com.github.kjetilv.uplift.json.Resident;

public class ResidentCallbacks extends AbstractCallbacks<Resident> {

    public ResidentCallbacks(AbstractCallbacks<?> parent, Consumer<Resident> onDone) {
        super(parent, onDone);
        ResidentBuilder residentBuilder = new ResidentBuilder();
        onString("name", residentBuilder::setName);
        onTruth("permanent", residentBuilder::setPermanent);
        get(residentBuilder);
    }
}
