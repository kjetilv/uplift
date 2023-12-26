package com.github.kjetilv.uplift.json.example.gen;

import java.util.function.Consumer;

import com.github.kjetilv.uplift.json.events.AbstractCallbacks;
import com.github.kjetilv.uplift.json.example.User;

public final class UserCallbacks extends AbstractCallbacks<UserBuilder, User> {

    public UserCallbacks(Consumer<User> onDone) {
        super(new UserBuilder(), null, onDone);
        onInteger("misc", UserBuilder::addMisc);
        onInteger("birthYear", UserBuilder::setBirthYear);
        onString("name", UserBuilder::setName);
        onBoolean("roadWarrior", UserBuilder::setRoadWarrior);
        onByte("maxAge", UserBuilder::setMaxAge);
        onString("aliases", UserBuilder::addAliases);
        onObject("address", () ->
            new AddressCallbacks(this, builder()::setAddress));
    }
}
