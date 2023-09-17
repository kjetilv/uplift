package com.github.kjetilv.uplift.json.gens;

import java.util.function.Consumer;

import com.github.kjetilv.uplift.json.Resident;

@SuppressWarnings("SwitchStatementWithTooFewBranches")
public class ResidentCallbacks extends AppCallbacks {

    private final AppCallbacks parent;

    private final ResidentBuilder residentBuilder;

    private final Consumer<Resident> onDone;

    private String currentField;

    public ResidentCallbacks(AppCallbacks parent, Consumer<Resident> onDone) {
        this.parent = parent;
        this.residentBuilder = new ResidentBuilder();
        this.onDone = onDone;
    }

    @Override
    public AppCallbacks field(String name) {
        currentField = name;
        return this;
    }

    @Override
    public AppCallbacks string(String string) {
        return switch (currentField) {
            case "name" -> {
                residentBuilder.setName(string);
                yield this;
            }
            default -> throw new IllegalStateException("Unexpected value for " + currentField + ": " + string);
        };
    }

    @Override
    public AppCallbacks truth(boolean truth) {
        return switch (currentField) {
            case "permanent" -> {
                residentBuilder.setPermanent(truth);
                yield this;
            }
            default -> throw new IllegalStateException("Unexpected value for " + currentField + ": " + truth);
        };
    }

    @Override
    public AppCallbacks objectEnded() {
        onDone.accept(residentBuilder.get());
        return parent == null ? this : parent;
    }
}
