package uplift.json;

interface Source {

    default String lexeme() {
        return lexeme(false);
    }

    String lexeme(boolean quoted);

    char chomp();

    int line();

    int column();

    int position();

    char peek();

    char peekNext();

    void advance();

    void reset();

    boolean done();
}
