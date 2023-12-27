package com.github.kjetilv.uplift.json.example.gen;

import com.github.kjetilv.uplift.json.events.AbstractFactory;
import com.github.kjetilv.uplift.json.example.User;

public final class UserFactory extends AbstractFactory<UserBuilder, User, UserCallbacks> {

    public static UserFactory INSTANCE = new UserFactory();

    private UserFactory() {
        super(UserCallbacks::create);
    }
}
