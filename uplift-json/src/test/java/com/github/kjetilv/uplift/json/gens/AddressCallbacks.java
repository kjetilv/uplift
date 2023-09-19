package com.github.kjetilv.uplift.json.gens;

import java.util.function.Consumer;

import com.github.kjetilv.uplift.json.AbstractCallbacks;
import com.github.kjetilv.uplift.json.Address;

public final class AddressCallbacks extends AbstractCallbacks<AddressBuilder, Address> {

    public AddressCallbacks(AbstractCallbacks<?, ?> parent, Consumer<Address> onDone) {
        super(new AddressBuilder(), parent, onDone);
        onInteger("houseNumber", AddressBuilder::setHouseNumber);
        onInteger("code", AddressBuilder::setCode);
        onString("streetName", AddressBuilder::setStreetName);
        onStringly("modifier", Address.Modifier::valueOf, AddressBuilder::setModifier);
        onObject("residents", () ->
            new ResidentCallbacks<>(this, builder()::addResident));
    }
}
