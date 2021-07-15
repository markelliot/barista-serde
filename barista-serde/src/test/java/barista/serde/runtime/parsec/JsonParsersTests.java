package barista.serde.runtime.parsec;

import static org.assertj.core.api.Assertions.assertThat;

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
        ParseState state = Parsers.of("test");

        assertThat(JsonParsers.quotedString().parse(state).error().map(ParseError::errorString))
                .contains(
                        """
                        Parse error at line 1, column 1: Expected a quoted string and did not find a quote:
                        test
                        ^
                        """);
    }

    @Test
    public void testQuotedString_missingEndQuote() {
        ParseState state = Parsers.of("\"test");

        assertThat(JsonParsers.quotedString().parse(state).error().map(ParseError::errorString))
                .contains(
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
        assertThat(
                        JsonParsers.integer()
                                .parse(Parsers.of("test"))
                                .error()
                                .map(ParseError::errorString))
                .contains(
                        """
                        Parse error at line 1, column 1: Cannot parse integer from value:
                        test
                        ^--^
                        """);

        assertThat(
                        JsonParsers.integer()
                                .parse(Parsers.of("1e10"))
                                .error()
                                .map(ParseError::errorString))
                .contains(
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
        assertThat(
                        JsonParsers.longParser()
                                .parse(Parsers.of("test"))
                                .error()
                                .map(ParseError::errorString))
                .contains(
                        """
                        Parse error at line 1, column 1: Cannot parse long from value:
                        test
                        ^--^
                        """);

        assertThat(
                        JsonParsers.longParser()
                                .parse(Parsers.of("1e10"))
                                .error()
                                .map(ParseError::errorString))
                .contains(
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
        assertThat(
                        JsonParsers.doubleParser()
                                .parse(Parsers.of("test"))
                                .error()
                                .map(ParseError::errorString))
                .contains(
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

        assertThat(
                        strings.parse(Parsers.of("""
            [
            """))
                                .error()
                                .get()
                                .errorString())
                .isEqualTo(
                        """
            Parse error at line 1, column 1: Reached end of stream looking for end of collection:
            [
            ^^
            """);

        assertThat(
                        strings.parse(Parsers.of("""
            ["a
            """))
                                .error()
                                .get()
                                .errorString())
                .isEqualTo(
                        """
            Parse error at line 1, column 3: Reached end of stream looking for terminal quote:
            ["a
              ^^
            """);

        assertThat(
                        strings.parse(Parsers.of("""
            ["a"
            """))
                                .error()
                                .get()
                                .errorString())
                .isEqualTo(
                        """
            Parse error at line 1, column 1: Reached end of stream looking for end of collection:
            ["a"
            ^--^
            """);

        assertThat(
                        strings.parse(Parsers.of("""
            ["a",
            """))
                                .error()
                                .get()
                                .errorString())
                .isEqualTo(
                        """
            Parse error at line 1, column 6: Expected a quoted string and did not find a quote:
            ["a",
                 ^
            """);

        assertThat(
                        strings.parse(Parsers.of("""
            ["a", "b
            """))
                                .error()
                                .get()
                                .errorString())
                .isEqualTo(
                        """
            Parse error at line 1, column 8: Reached end of stream looking for terminal quote:
            ["a", "b
                   ^^
            """);

        assertThat(
                        strings.parse(Parsers.of("""
            ["a", "b"
            """))
                                .error()
                                .get()
                                .errorString())
                .isEqualTo(
                        """
            Parse error at line 1, column 1: Reached end of stream looking for end of collection:
            ["a", "b"
            ^-------^
            """);
    }
}
