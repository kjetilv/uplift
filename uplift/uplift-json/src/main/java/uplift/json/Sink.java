package uplift.json;

interface Sink {

    @FunctionalInterface
    interface Mark {

        boolean moved();
    }

    default Sink accept(Object obj) {
        return accept(obj.toString());
    }

    default Sink accept(boolean b) {
        return accept(String.valueOf(b));
    }

    Sink accept(String str);

    Mark mark();

    int length();
}
