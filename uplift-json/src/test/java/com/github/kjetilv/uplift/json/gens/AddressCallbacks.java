package com.github.kjetilv.uplift.json.gens;

import java.util.function.Consumer;

import com.github.kjetilv.uplift.json.Address;

@SuppressWarnings("SwitchStatementWithTooFewBranches")
public final class AddressCallbacks extends AppCallbacks {

    private final AppCallbacks parent;

    private final AddressBuilder addressBuilder;

    private final Consumer<Address> onDone;

    public AddressCallbacks(AppCallbacks parent, Consumer<Address> onDone) {
        this.parent = parent;
        this.onDone = onDone;
        addressBuilder = new AddressBuilder();
    }

    @Override
    public AppCallbacks objectStarted() {
        if (currentField() == null) {
            return this;
        }
        return switch (currentField()) {
            case "residents" -> new ResidentCallbacks(this, addressBuilder::addResident);
            default -> fail();
        };
    }

    @Override
    public AppCallbacks string(String string) {
        return switch (currentField()) {
            case "streetName" -> {
                addressBuilder.setStreetName(string);
                yield this;
            }
            case "modifier" -> {
                addressBuilder.setModifier(Address.Modifier.valueOf(string));
                yield this;
            }
            default -> fail(string);
        };
    }

    @Override
    public AppCallbacks number(Number number) {
        return switch (currentField()) {
            case "houseNumber" -> {
                addressBuilder.setHouseNumber(number.intValue());
                yield this;
            }
            case "code" -> {
                addressBuilder.setCode(number.intValue());
                yield this;
            }
            default -> fail(number);
        };
    }

    @Override
    public AppCallbacks objectEnded() {
        onDone.accept(addressBuilder.get());
        return parent == null ? this : parent;
    }
}
