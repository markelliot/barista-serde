package barista.serde.runtime.json;

import static barista.serde.runtime.json.JsonParserAsserts.assertThatError;
import static org.assertj.core.api.Assertions.assertThat;

import barista.serde.runtime.parsec.ParseState;
import barista.serde.runtime.parsec.Parser;
import org.junit.jupiter.api.Test;

final class BooleanParserTests {
    private static final Parser<Boolean> parser = JsonParsers.booleanParser();

    @Test
    public void testTrue() {
        assertThat(parser.parse(ParseState.of("true")).unwrap()).isTrue();
    }

    @Test
    void testFalse() {
        assertThat(parser.parse(ParseState.of("false")).unwrap()).isFalse();
    }

    @Test
    void testErrorOnNull() {
        assertThatError(parser, "null")
                .contains(
                        """
            Parse error at line 1, column 1: Expected value 'true' or 'false':
            null
            ^
            """);
    }

    @Test
    void testErrorOnQuotes() {
        assertThatError(parser, "\"true\"")
                .contains(
                        """
            Parse error at line 1, column 1: Expected value 'true' or 'false':
            "true"
            ^
            """);
    }
}
