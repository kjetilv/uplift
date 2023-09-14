package com.github.kjetilv.uplift.json;

import java.util.function.Supplier;

public class AddressBuilder implements Supplier<Address> {

    private String streetName;

    private Integer houseNumber;

    private Address.Modifier modifier;

    private Integer code;

    public void setStreetName(String streetName) {
        this.streetName = streetName;
    }

    public void setHouseNumber(Integer houseNumber) {
        this.houseNumber = houseNumber;
    }

    public void setModifier(Address.Modifier modifier) {
        this.modifier = modifier;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    @Override
    public Address get() {
        return new Address(
            streetName,
            houseNumber,
            modifier,
            code
        );
    }
}
