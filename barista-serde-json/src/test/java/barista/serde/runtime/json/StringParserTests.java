package barista.serde.runtime.json;

import static barista.serde.runtime.json.JsonParserAsserts.assertThatError;
import static barista.serde.runtime.json.JsonParserAsserts.assertThatResult;

import barista.serde.runtime.parsec.Parsers;
import org.junit.jupiter.api.Test;

final class StringParserTests {
    @Test
    void testQuotedString() {
        assertThatResult(JsonParsers.string(), "\"test\"").isEqualTo("test");
    }

    @Test
    void testQuotedString_whitespaceComposition() {
        assertThatResult(Parsers.whitespace(JsonParsers.string()), "    \"test\"    ")
                .isEqualTo("test");
    }

    @Test
    void testQuotedString_escapedChars() {
        assertThatResult(Parsers.whitespace(JsonParsers.string()), "\"\\\"test\\\"\"")
                .isEqualTo("\"test\"");
    }

    @Test
    void testQuotedString_missingStartQuote() {
        assertThatError(JsonParsers.string(), "test")
                .contains(
                        """
                    Parse error at line 1, column 1: Expected a quoted string and did not find a quote:
                    test
                    ^
                    """);
    }

    @Test
    void testQuotedString_missingEndQuote() {
        assertThatError(JsonParsers.string(), "\"test")
                .contains(
                        """
            Parse error at line 1, column 2: Reached end of stream looking for terminal quote:
            "test
             ^--^
            """);
    }
}
