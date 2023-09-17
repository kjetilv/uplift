package com.github.kjetilv.uplift.json.gens;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.github.kjetilv.uplift.json.Address;
import com.github.kjetilv.uplift.json.User;

public final class UserBuilder implements Supplier<User> {

    private String name;

    private Integer birthYear;

    private Address address;

    private boolean roadWarrior;

    private List<Object> misc;

    @Override
    public User get() {
        return new User(
            name,
            birthYear,
            address,
            roadWarrior,
            misc
        );
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setBirthYear(Integer birthYear) {
        this.birthYear = birthYear;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public void setRoadWarrior(boolean roadWarrior) {
        this.roadWarrior = roadWarrior;
    }

    public void addMisc(Object object) {
        if (misc == null) {
            misc = new ArrayList<>();
        }
        misc.add(object);
    }
}
