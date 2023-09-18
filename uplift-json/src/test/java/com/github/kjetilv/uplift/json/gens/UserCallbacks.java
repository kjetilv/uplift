package com.github.kjetilv.uplift.json.gens;

import java.util.function.Consumer;

import com.github.kjetilv.uplift.json.AbstractCallbacks;
import com.github.kjetilv.uplift.json.User;

public final class UserCallbacks extends AbstractCallbacks<User> {

    public UserCallbacks(AbstractCallbacks<?> parent, Consumer<User> onDone) {
        super(parent, onDone);
        UserBuilder userBuilder = new UserBuilder();
        onInteger("misc", userBuilder::addMisc);
        onInteger("birthYear", userBuilder::setBirthYear);
        onString("name", userBuilder::setName);
        onString("misc", userBuilder::addMisc);
        onTruth("roadWarrior", userBuilder::setRoadWarrior);
        onByte("maxAge", userBuilder::setMaxAge);
        onTruth("misc", userBuilder::addMisc);
        onObject("address", () ->
            new AddressCallbacks(this, userBuilder::setAddress));
        get(userBuilder);
    }
}
