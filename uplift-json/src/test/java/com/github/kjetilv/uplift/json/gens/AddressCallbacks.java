package com.github.kjetilv.uplift.json.gens;

import java.util.function.Consumer;

import com.github.kjetilv.uplift.json.AbstractCallbacks;
import com.github.kjetilv.uplift.json.Address;

public final class AddressCallbacks extends AbstractCallbacks<Address> {

    public AddressCallbacks(AbstractCallbacks<?> parent, Consumer<Address> onDone) {
        super(parent, onDone);
        AddressBuilder addressBuilder = new AddressBuilder();
        onInteger("houseNumber", addressBuilder::setHouseNumber);
        onInteger("code", addressBuilder::setCode);
        onString("streetName", addressBuilder::setStreetName);
        onEnum("modifier", Address.Modifier::valueOf, addressBuilder::setModifier);
        onObject("residents", () -> new ResidentCallbacks(this, addressBuilder::addResident));
        get(addressBuilder);
    }
}
