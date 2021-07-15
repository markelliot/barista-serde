package barista.serde.runtime.parsec;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import io.github.markelliot.result.Result;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.junit.jupiter.api.Test;

final class JsonParsersTests {

    @Test
    void testQuotedString() {
        ParseState state = ParseState.of("\"test\"");
        assertThat(JsonParsers.quotedString().parse(state).result()).contains("test");
    }

    @Test
    void testQuotedString_whitespaceComposition() {
        ParseState state = ParseState.of("    \"test\"    ");
        assertThat(Parsers.whitespace(JsonParsers.quotedString()).parse(state).result())
                .contains("test");
    }

    @Test
    void testQuotedString_escapedChars() {
        ParseState state = ParseState.of("\"\\\"test\\\"\"");
        assertThat(JsonParsers.quotedString().parse(state).result()).contains("\"test\"");
    }

    @Test
    void testQuotedString_missingStartQuote() {
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
    void testQuotedString_missingEndQuote() {
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
    void testInteger() {
        assertThat(JsonParsers.integer().parse(ParseState.of("0")).result()).contains(0);
        assertThat(JsonParsers.integer().parse(ParseState.of("1")).result()).contains(1);
        assertThat(JsonParsers.integer().parse(ParseState.of("-0")).result()).contains(0);
        assertThat(JsonParsers.integer().parse(ParseState.of("100")).result()).contains(100);
        assertThat(JsonParsers.integer().parse(ParseState.of("-100")).result()).contains(-100);
    }

    @Test
    void testInteger_errorOnInvalid() {
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
    void testParseLong() {
        assertThat(JsonParsers.longParser().parse(ParseState.of("0")).result()).contains(0L);
        assertThat(JsonParsers.longParser().parse(ParseState.of("1")).result()).contains(1L);
        assertThat(JsonParsers.longParser().parse(ParseState.of("-0")).result()).contains(0L);
        assertThat(JsonParsers.longParser().parse(ParseState.of("100")).result()).contains(100L);
        assertThat(JsonParsers.longParser().parse(ParseState.of("-100")).result()).contains(-100L);
    }

    @Test
    void testParseLong_errorOnInvalid() {
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
    void testDouble() {
        assertThat(JsonParsers.doubleParser().parse(ParseState.of("0")).result()).contains(0.0);
        assertThat(JsonParsers.doubleParser().parse(ParseState.of("0.")).result()).contains(0.0);
        assertThat(JsonParsers.doubleParser().parse(ParseState.of("-0.")).result()).contains(-0.0);
        assertThat(JsonParsers.doubleParser().parse(ParseState.of("0.0")).result()).contains(0.0);
        assertThat(JsonParsers.doubleParser().parse(ParseState.of("-Inf")).result())
                .contains(Double.NEGATIVE_INFINITY);
        assertThat(JsonParsers.doubleParser().parse(ParseState.of("NaN")).result())
                .contains(Double.NaN);
        assertThat(JsonParsers.doubleParser().parse(ParseState.of("1e10")).result()).contains(1e10);
        assertThat(JsonParsers.doubleParser().parse(ParseState.of("1.5e-10")).result())
                .contains(1.5e-10);
        assertThat(JsonParsers.doubleParser().parse(ParseState.of("1.5e+10")).result())
                .contains(1.5e+10);
    }

    @Test
    void testDouble_errorOnInvalid() {
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
    void testCollectionOfQuotedStrings() throws Exception {
        Parser<Collection<String>> strings =
                Parsers.whitespace(
                        JsonParsers.collection(JsonParsers.quotedString(), ArrayList::new));

        assertThat(strings.parse(ParseState.of("[]")).orElseThrow()).isEqualTo(List.of());

        assertThat(strings.parse(ParseState.of("[ ]")).orElseThrow()).isEqualTo(List.of());

        assertThat(
                        strings.parse(ParseState.of("""
            ["a"]
            """))
                                .orElseThrow())
                .containsExactly("a");

        assertThat(
                        strings.parse(ParseState.of("""
            ["a", "b"]
            """))
                                .orElseThrow())
                .containsExactly("a", "b");

        assertThat(
                        strings.parse(ParseState.of("""
            [ "a" , "b" ]
            """))
                                .orElseThrow())
                .containsExactly("a", "b");
    }

    @Test
    void testCollection_errors() throws Exception {
        Parser<Collection<String>> strings =
                Parsers.whitespace(
                        JsonParsers.collection(JsonParsers.quotedString(), ArrayList::new));

        assertError(
                strings,
                """
            [
            """,
                """
            Parse error at line 1, column 2: Expected to find ']':
            [
             ^
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
            Parse error at line 1, column 5: Expected to find ']':
            ["a"
                ^
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
            Parse error at line 1, column 10: Expected to find ']':
            ["a", "b"
                     ^
            """);
    }

    @Test
    void testCollection_asSet() throws Exception {
        Parser<Collection<String>> strings =
                Parsers.whitespace(
                        JsonParsers.collection(JsonParsers.quotedString(), LinkedHashSet::new));

        assertThat(
                        strings.parse(
                                        ParseState.of(
                                                """
            ["a", "b", "c", "a", "b", "c"]
            """))
                                .orElseThrow())
                .containsExactly("a", "b", "c");
    }

    @Test
    void testMap() {
        Parser<Map<String, String>> basicMap =
                JsonParsers.map(
                        Function.identity(), JsonParsers.quotedString(), LinkedHashMap::new);

        assertThat(basicMap.parse(ParseState.of("{}")).orElseThrow(ParseError::toException))
                .containsExactlyEntriesOf(ImmutableMap.of());

        assertThat(basicMap.parse(ParseState.of("{ }")).orElseThrow(ParseError::toException))
                .containsExactlyEntriesOf(ImmutableMap.of());

        assertThat(
                        basicMap.parse(
                                        ParseState.of(
                                                """
            { "a" : "A", "b":"B" ,"c": "C"}
            """))
                                .orElseThrow(ParseError::toException))
                .containsExactlyEntriesOf(ImmutableMap.of("a", "A", "b", "B", "c", "C"));
    }

    @Test
    void testMap_errors() {
        Parser<Map<String, String>> parser =
                JsonParsers.map(
                        Function.identity(), JsonParsers.quotedString(), LinkedHashMap::new);
        assertError(
                parser,
                "{",
                """
                Parse error at line 1, column 1: Expected to find '}':
                {
                ^^
                """);
        assertError(
                parser,
                "",
                """
                Parse error at line 1, column 1: Expected to find '{':

                ^
                """);
        assertError(
                parser,
                "{a",
                """
                Parse error at line 1, column 2: Expected a quoted string and did not find a quote:
                {a
                 ^
                """);
        assertError(
                parser,
                "{\"a",
                """
                Parse error at line 1, column 3: Reached end of stream looking for terminal quote:
                {"a
                  ^^
                """);
        assertError(
                parser,
                "{\"a\"",
                """
                Parse error at line 1, column 4: Expected a quoted string and did not find a quote:
                {"a"
                   ^^
                """);
        assertError(
                parser,
                "{\"a\":",
                """
                Parse error at line 1, column 5: Expected a quoted string and did not find a quote:
                {"a":
                    ^^
                """);
        assertError(
                parser,
                "{\"a\":\"",
                """
                Parse error at line 1, column 6: Reached end of stream looking for terminal quote:
                {"a":"
                     ^^
                """);
        assertError(
                parser,
                "{\"a\":\"\"",
                """
                Parse error at line 1, column 7: Expected to find '}':
                {"a":""
                      ^^
                """);
        assertError(
                parser,
                "{\"a\":\"\",",
                """
                Parse error at line 1, column 8: Expected to find '}':
                {"a":"",
                       ^^
                """);
        assertError(
                parser,
                "{\"a\":\"\",}",
                """
                Parse error at line 1, column 9: Expected a quoted string and did not find a quote:
                {"a":"",}
                        ^
                """);
    }

    private static void assertError(Parser<?> parser, String input, String error) {
        Result<?, ParseError> result = parser.parse(ParseState.of(input));
        assertThat(result.isError()).isTrue();
        assertThat(result.error().map(ParseError::errorString)).contains(error);
    }
}
