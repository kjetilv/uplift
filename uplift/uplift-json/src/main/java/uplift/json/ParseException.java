package uplift.json;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.stream.Collectors;

class ParseException extends RuntimeException {

    private final Token token;

    private final TokenType[] expected;

    ParseException(Token token, TokenType... expected) {
        super("Invalid token " + token + ", expected one of " + tokens(expected));
        this.token = token;
        this.expected = expected.clone();
    }

    Collection<TokenType> getExpected() {
        return EnumSet.copyOf(Arrays.asList(expected));
    }

    Token getToken() {
        return token;
    }

    private static String tokens(TokenType... expected) {
        return Arrays.stream(expected).map(Enum::name).collect(Collectors.joining(", "));
    }
}
