package barista.serde.runtime.parsec;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.markelliot.result.Result;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.Test;

final class JsonParsersTests {

    @Test
    public void testQuotedString() {
        ParseState state = Parsers.of("\"test\"");
        assertThat(JsonParsers.quotedString().parse(state).result()).contains("test");
    }

    @Test
    public void testQuotedString_whitespaceComposition() {
        ParseState state = Parsers.of("    \"test\"    ");
        assertThat(Parsers.whitespace(JsonParsers.quotedString()).parse(state).result())
                .contains("test");
    }

    @Test
    public void testQuotedString_escapedChars() {
        ParseState state = Parsers.of("\"\\\"test\\\"\"");
        assertThat(JsonParsers.quotedString().parse(state).result()).contains("\"test\"");
    }

    @Test
    public void testQuotedString_missingStartQuote() {
        assertError(
                JsonParsers.quotedString(),
                "test",
                """
                        Parse error at line 1, column 1: Expected a quoted string and did not find a quote:
                        test
                        ^
                        """);
    }

    @Test
    public void testQuotedString_missingEndQuote() {
        assertError(
                JsonParsers.quotedString(),
                "\"test",
                """
                Parse error at line 1, column 2: Reached end of stream looking for terminal quote:
                "test
                 ^--^
                """);
    }

    @Test
    public void testInteger() {
        assertThat(JsonParsers.integer().parse(Parsers.of("0")).result()).contains(0);
        assertThat(JsonParsers.integer().parse(Parsers.of("1")).result()).contains(1);
        assertThat(JsonParsers.integer().parse(Parsers.of("-0")).result()).contains(0);
        assertThat(JsonParsers.integer().parse(Parsers.of("100")).result()).contains(100);
        assertThat(JsonParsers.integer().parse(Parsers.of("-100")).result()).contains(-100);
    }

    @Test
    public void testInteger_errorOnInvalid() {
        assertError(
                JsonParsers.integer(),
                "test",
                """
                        Parse error at line 1, column 1: Cannot parse integer from value:
                        test
                        ^--^
                        """);

        assertError(
                JsonParsers.integer(),
                "1e10",
                """
                        Parse error at line 1, column 1: Cannot parse integer from value:
                        1e10
                        ^--^
                        """);
    }

    @Test
    public void testParseLong() {
        assertThat(JsonParsers.longParser().parse(Parsers.of("0")).result()).contains(0L);
        assertThat(JsonParsers.longParser().parse(Parsers.of("1")).result()).contains(1L);
        assertThat(JsonParsers.longParser().parse(Parsers.of("-0")).result()).contains(0L);
        assertThat(JsonParsers.longParser().parse(Parsers.of("100")).result()).contains(100L);
        assertThat(JsonParsers.longParser().parse(Parsers.of("-100")).result()).contains(-100L);
    }

    @Test
    public void testParseLong_errorOnInvalid() {
        assertError(
                JsonParsers.longParser(),
                "test",
                """
                        Parse error at line 1, column 1: Cannot parse long from value:
                        test
                        ^--^
                        """);

        assertError(
                JsonParsers.longParser(),
                "1e10",
                """
                        Parse error at line 1, column 1: Cannot parse long from value:
                        1e10
                        ^--^
                        """);
    }

    @Test
    public void testDouble() {
        assertThat(JsonParsers.doubleParser().parse(Parsers.of("0")).result()).contains(0.0);
        assertThat(JsonParsers.doubleParser().parse(Parsers.of("0.")).result()).contains(0.0);
        assertThat(JsonParsers.doubleParser().parse(Parsers.of("-0.")).result()).contains(-0.0);
        assertThat(JsonParsers.doubleParser().parse(Parsers.of("0.0")).result()).contains(0.0);
        assertThat(JsonParsers.doubleParser().parse(Parsers.of("-Inf")).result())
                .contains(Double.NEGATIVE_INFINITY);
        assertThat(JsonParsers.doubleParser().parse(Parsers.of("NaN")).result())
                .contains(Double.NaN);
        assertThat(JsonParsers.doubleParser().parse(Parsers.of("1e10")).result()).contains(1e10);
        assertThat(JsonParsers.doubleParser().parse(Parsers.of("1.5e-10")).result())
                .contains(1.5e-10);
        assertThat(JsonParsers.doubleParser().parse(Parsers.of("1.5e+10")).result())
                .contains(1.5e+10);
    }

    @Test
    public void testDouble_errorOnInvalid() {
        assertError(
                JsonParsers.doubleParser(),
                "test",
                """
                        Parse error at line 1, column 1: Cannot parse double from value:
                        test
                        ^--^
                        """);
    }

    @Test
    public void testCollectionOfQuotedStrings() throws Exception {
        Parser<Collection<String>> strings =
                Parsers.whitespace(
                        JsonParsers.collection(JsonParsers.quotedString(), ArrayList::new));

        assertThat(strings.parse(Parsers.of("""
            []
            """)).result().get())
                .isEqualTo(List.of());

        assertThat(strings.parse(Parsers.of("""
            [ ]
            """)).result().get())
                .isEqualTo(List.of());

        assertThat(strings.parse(Parsers.of("""
            ["a"]
            """)).orElseThrow())
                .containsExactly("a");

        assertThat(
                        strings.parse(Parsers.of("""
            ["a", "b"]
            """))
                                .result()
                                .get())
                .containsExactly("a", "b");

        assertThat(
                        strings.parse(Parsers.of("""
            [ "a" , "b" ]
            """))
                                .result()
                                .get())
                .containsExactly("a", "b");
    }

    @Test
    public void testCollection_errors() throws Exception {
        Parser<Collection<String>> strings =
                Parsers.whitespace(
                        JsonParsers.collection(JsonParsers.quotedString(), ArrayList::new));

        assertError(
                strings,
                """
            [
            """,
                """
            Parse error at line 1, column 1: Reached end of stream looking for end of collection:
            [
            ^^
            """);

        assertError(
                strings,
                """
            ["a
            """,
                """
            Parse error at line 1, column 3: Reached end of stream looking for terminal quote:
            ["a
              ^^
            """);

        assertError(
                strings,
                """
            ["a"
            """,
                """
            Parse error at line 1, column 1: Reached end of stream looking for end of collection:
            ["a"
            ^--^
            """);

        assertError(
                strings,
                """
            ["a",
            """,
                """
            Parse error at line 1, column 6: Expected a quoted string and did not find a quote:
            ["a",
                 ^
            """);

        assertError(
                strings,
                """
            ["a", "b
            """,
                """
            Parse error at line 1, column 8: Reached end of stream looking for terminal quote:
            ["a", "b
                   ^^
            """);

        assertError(
                strings,
                """
            ["a", "b"
            """,
                """
            Parse error at line 1, column 1: Reached end of stream looking for end of collection:
            ["a", "b"
            ^-------^
            """);
    }

    private static void assertError(Parser<?> parser, String input, String error) {
        Result<?, ParseError> result = parser.parse(Parsers.of(input));
        assertThat(result.isError()).isTrue();
        assertThat(result.error().map(ParseError::errorString)).contains(error);
    }
}
