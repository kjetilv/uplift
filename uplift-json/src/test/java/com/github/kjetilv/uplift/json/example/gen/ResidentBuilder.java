package com.github.kjetilv.uplift.json.example.gen;

import java.util.function.Supplier;

import com.github.kjetilv.uplift.json.example.Resident;

public final class ResidentBuilder implements Supplier<Resident> {

    private String name;

    private boolean permanent;

    public void setName(String name) {
        this.name = name;
    }

    public void setPermanent(boolean permanent) {
        this.permanent = permanent;
    }

    @Override
    public Resident get() {
        return new Resident(name, permanent);
    }
}
