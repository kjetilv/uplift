package com.github.kjetilv.uplift.json.gens;

import java.util.function.Consumer;
import java.util.function.Supplier;

import com.github.kjetilv.uplift.json.User;

@SuppressWarnings("SwitchStatementWithTooFewBranches")
public final class UserCallbacks extends AppCallbacks  {

    private final UserBuilder userBuilder;

    private final Consumer<User> onDone;

    private final AppCallbacks parent;

    private User user;

    public UserCallbacks(AppCallbacks parent, Consumer<User> onDone) {
        this.parent = parent;
        this.userBuilder = new UserBuilder();
        this.onDone = onDone;
    }

    @Override
    public AppCallbacks objectStarted() {
        if (currentField() == null) {
            return this;
        }
        return switch (currentField()) {
            case "address" -> new AddressCallbacks(this, userBuilder::setAddress);
            default -> fail();
        };
    }

    @Override
    public AppCallbacks string(String string) {
        return switch (currentField()) {
            case "name" -> {
                userBuilder.setName(string);
                yield this;
            }
            case "misc" -> {
                userBuilder.addMisc(string);
                yield this;
            }
            default -> fail(string);
        };
    }

    @Override
    public AppCallbacks number(Number number) {
        return switch (currentField()) {
            case "birthYear" -> {
                userBuilder.setBirthYear(number.intValue());
                yield this;
            }
            case "misc" -> {
                userBuilder.addMisc(number);
                yield this;
            }
            default -> fail(number);
        };
    }

    @Override
    public AppCallbacks truth(boolean truth) {
        return switch (currentField()) {
            case "roadWarrior" -> {
                userBuilder.setRoadWarrior(truth);
                yield this;
            }
            case "misc" -> {
                userBuilder.addMisc(truth);
                yield this;
            }
            default -> fail(truth);
        };
    }

    @Override
    public AppCallbacks objectEnded() {
        onDone.accept(userBuilder.get());
        return parent == null ? this : parent;
    }
}
