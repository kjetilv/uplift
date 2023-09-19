package com.github.kjetilv.uplift.json.gens;

import java.util.function.Consumer;

import com.github.kjetilv.uplift.json.AbstractCallbacks;
import com.github.kjetilv.uplift.json.User;

public final class UserCallbacks extends AbstractCallbacks<UserBuilder, User> {

    public UserCallbacks(Consumer<User> onDone) {
        super(new UserBuilder(), null, onDone);
        onInteger("misc", UserBuilder::addMisc);
        onInteger("birthYear", UserBuilder::setBirthYear);
        onString("name", UserBuilder::setName);
        onString("misc", UserBuilder::addMisc);
        onTruth("roadWarrior", UserBuilder::setRoadWarrior);
        onByte("maxAge", UserBuilder::setMaxAge);
        onTruth("misc", UserBuilder::addMisc);
        onObject("address", () ->
            new AddressCallbacks(this, builder()::setAddress));
    }
}
