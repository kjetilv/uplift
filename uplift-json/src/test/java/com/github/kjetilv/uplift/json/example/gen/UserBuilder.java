package com.github.kjetilv.uplift.json.example.gen;

import com.github.kjetilv.uplift.json.example.Address;
import com.github.kjetilv.uplift.json.example.User;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public final class UserBuilder implements Supplier<User> {

    private String name;

    private Integer birthYear;

    private Address address;

    private boolean roadWarrior;

    private List<String> aliases;

    private List<Integer> misc;

    private byte maxAge;

    @Override
    public User get() {
        return new User(
            name,
            birthYear,
            address,
            roadWarrior,
            maxAge,
            aliases,
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

    public void addMisc(Integer misc) {
        if (this.misc == null) {
            this.misc = new ArrayList<>();
        }
        this.misc.add(misc);
    }

    public void addAliases(String aliases) {
        if (this.aliases == null) {
            this.aliases = new ArrayList<>();
        }
        this.aliases.add(aliases);
    }

    void setMaxAge(byte maxAge) {
        this.maxAge = maxAge;
    }
}
