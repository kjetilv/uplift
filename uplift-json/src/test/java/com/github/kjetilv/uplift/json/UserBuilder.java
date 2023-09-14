package com.github.kjetilv.uplift.json;

import java.util.function.Supplier;

public class UserBuilder implements Supplier<User> {

    private String name;

    private Integer birthYear;

    private Address address;

    private boolean roadWarrior;

    @Override
    public User get() {
        return new User(
            name,
            birthYear,
            address,
            roadWarrior
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
}
