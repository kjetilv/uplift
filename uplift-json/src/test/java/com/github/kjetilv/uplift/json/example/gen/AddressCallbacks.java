package com.github.kjetilv.uplift.json.example.gen;

import java.util.function.Consumer;

import com.github.kjetilv.uplift.json.events.AbstractCallbacks;
import com.github.kjetilv.uplift.json.example.Address;

public final class AddressCallbacks extends AbstractCallbacks<AddressBuilder, Address> {

    public AddressCallbacks(AbstractCallbacks<?, ?> parent, Consumer<Address> onDone) {
        super(new AddressBuilder(), parent, onDone);
        onInteger("houseNumber", AddressBuilder::setHouseNumber);
        onInteger("code", AddressBuilder::setCode);
        onString("streetName", AddressBuilder::setStreetName);
        onEnum("modifier", Address.Modifier::valueOf, AddressBuilder::setModifier);
        onObject("residents", () ->
            new ResidentCallbacks(this, builder()::addResidents));
        onEnum("adjacents", Address.Modifier::valueOf, AddressBuilder::addAdjacents);
    }
}
