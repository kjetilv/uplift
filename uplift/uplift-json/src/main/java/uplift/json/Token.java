package uplift.json;

import static java.util.Objects.requireNonNull;

record Token(
    TokenType type,
    String lexeme,
    Object literal,
    int line,
    int column
) {

    Token {
        requireNonNull(type, "type");
    }

    boolean comma() {
        return this.type == TokenType.COMMA;
    }

    boolean isNot(TokenType type) {
        return this.type != type;
    }

    private String value() {
        if (type().print()) {
            int length = lexeme.length();
            String printed =
                length > 10 ? lexeme.substring(0, 9) + "⋯" + " [" + length + "]" : lexeme;
            return printed + ":";
        }
        return "";
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + value() + type + " @ " + line + ":" + column + "]";
    }
}
