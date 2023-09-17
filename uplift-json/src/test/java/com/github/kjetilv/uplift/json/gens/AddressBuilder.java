package com.github.kjetilv.uplift.json.gens;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.github.kjetilv.uplift.json.Address;
import com.github.kjetilv.uplift.json.Resident;

public final class AddressBuilder implements Supplier<Address> {

    private String streetName;

    private Integer houseNumber;

    private Address.Modifier modifier;

    private Integer code;

    private List<Resident> residents;

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

    public void addResident(Resident resident) {
        if (residents == null) {
            residents = new ArrayList<>();
        }
        residents.add(resident);
    }

    @Override
    public Address get() {
        return new Address(
            streetName,
            houseNumber,
            modifier,
            code,
            residents
        );
    }
}
