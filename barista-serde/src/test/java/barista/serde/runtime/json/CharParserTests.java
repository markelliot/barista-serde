package barista.serde.runtime.json;

import static barista.serde.runtime.json.JsonParserAsserts.assertThatError;
import static barista.serde.runtime.json.JsonParserAsserts.assertThatResult;

import barista.serde.runtime.parsec.Parser;
import org.junit.jupiter.api.Test;

final class CharParserTests {
    private static final Parser<Character> parser = JsonParsers.charParser();

    @Test
    void testChar() {
        assertThatResult(parser, "\"a\"").isEqualTo('a');
        assertThatResult(parser, "\"\\n\"").isEqualTo('\n');
    }

    @Test
    void testNoChar() {
        assertThatError(parser, "\"\"")
                .contains(
                        """
                Parse error at line 1, column 1: Expected a single character:
                ""
                ^^
                """);
    }

    @Test
    void testTooManyChars() {
        assertThatError(parser, "\"ab\"")
                .contains(
                        """
                Parse error at line 1, column 1: Expected a single character:
                "ab"
                ^--^
                """);
    }
}
