package com.github.kjetilv.uplift.json.example.gen;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.github.kjetilv.uplift.json.example.Address;
import com.github.kjetilv.uplift.json.example.Resident;

public final class AddressBuilder implements Supplier<Address> {

    private String streetName;

    private Integer houseNumber;

    private Address.Modifier modifier;

    private List<Address.Modifier> adjacents;

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

    public void addResidents(Resident resident) {
        if (residents == null) {
            residents = new ArrayList<>();
        }
        residents.add(resident);
    }

    public void addAdjacents(Address.Modifier modifier) {
        if (adjacents == null) {
            adjacents = new ArrayList<>();
        }
        adjacents.add(modifier);
    }

    @Override
    public Address get() {
        return new Address(streetName, houseNumber, modifier, adjacents, code, residents);
    }
}
