package barista.serde.runtime.json;

import static barista.serde.runtime.json.JsonParserAsserts.assertThatError;
import static barista.serde.runtime.json.JsonParserAsserts.assertThatResult;

import barista.serde.runtime.parsec.Empty;
import barista.serde.runtime.parsec.Parser;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import org.junit.jupiter.api.Test;

final class JsonParsersTests {
    @Test
    void testKeyValueSeparatorParser() {
        assertThatResult(KeyValueSeparatorParser.INSTANCE, ":").isEqualTo(Empty.INSTANCE);
        assertThatResult(KeyValueSeparatorParser.INSTANCE, "   :").isEqualTo(Empty.INSTANCE);
        assertThatResult(KeyValueSeparatorParser.INSTANCE, ":   ").isEqualTo(Empty.INSTANCE);
        assertThatResult(KeyValueSeparatorParser.INSTANCE, " : ").isEqualTo(Empty.INSTANCE);
    }

    @Test
    void testKeyValueSeparatorParser_error() {
        assertThatError(KeyValueSeparatorParser.INSTANCE, "x")
                .contains(
                        """
            Parse error at line 1, column 1: Expected to find ':':
            x
            ^
            """);
        assertThatError(KeyValueSeparatorParser.INSTANCE, "")
                .contains(
                        """
            Parse error at line 1, column 1: Expected to find ':':

            ^
            """);
        assertThatError(KeyValueSeparatorParser.INSTANCE, " ")
                .contains(
                        """
            Parse error at line 1, column 1: Expected to find ':':
            \s
            ^^
            """);
    }

    @Test
    void testUnknownField() {
        assertThatError(JsonParsers.unknownField("field"), "any")
                .contains(
                        """
            Parse error at line 1, column 1: Unknown field 'field':
            any
            ^
            """);
    }

    @Test
    void testCollectionOfQuotedStrings() {
        Parser<Collection<String>> strings =
                JsonParsers.collection(JsonParsers.string(), ArrayList::new);

        assertThatResult(strings, "[]").isEqualTo(List.of());
        assertThatResult(strings, "[ ]").isEqualTo(List.of());
        assertThatResult(strings, "[\"a\"]").isEqualTo(ImmutableList.of("a"));
        assertThatResult(strings, "[\"a\", \"b\"]").isEqualTo(ImmutableList.of("a", "b"));
        assertThatResult(strings, "[\"a\",\"b\"]").isEqualTo(ImmutableList.of("a", "b"));
        assertThatResult(strings, "[  \"a\", \"b\"]").isEqualTo(ImmutableList.of("a", "b"));
        assertThatResult(strings, "[  \"a\" , \"b\" ]").isEqualTo(ImmutableList.of("a", "b"));
    }

    @Test
    void testCollection_errors() {
        Parser<Collection<String>> strings =
                JsonParsers.collection(JsonParsers.string(), ArrayList::new);

        assertThatError(strings, "[")
                .contains(
                        """
            Parse error at line 1, column 1: Expected to find ']':
            [
            ^^
            """);

        assertThatError(strings, "[\"a")
                .contains(
                        """
            Parse error at line 1, column 3: Reached end of stream looking for terminal quote:
            ["a
              ^^
            """);

        assertThatError(strings, "[\"a\"")
                .contains(
                        """
            Parse error at line 1, column 4: Expected to find ']':
            ["a"
               ^^
            """);

        assertThatError(strings, "[\"a\",")
                .contains(
                        """
            Parse error at line 1, column 5: Expected to find ']':
            ["a",
                ^^
            """);

        assertThatError(strings, "[\"a\", \"b")
                .contains(
                        """
            Parse error at line 1, column 8: Reached end of stream looking for terminal quote:
            ["a", "b
                   ^^
            """);

        assertThatError(strings, "[\"a\", \"b\"")
                .contains(
                        """
            Parse error at line 1, column 9: Expected to find ']':
            ["a", "b"
                    ^^
            """);
    }

    @Test
    void testCollection_asSet() {
        Parser<Collection<String>> strings =
                JsonParsers.collection(JsonParsers.string(), LinkedHashSet::new);

        assertThatResult(strings, "[\"a\", \"b\", \"c\", \"a\", \"b\", \"c\"]")
                .isEqualTo(ImmutableSet.of("a", "b", "c"));
    }

    @Test
    void testMap() {
        Parser<Map<String, String>> basicMap =
                JsonParsers.map(Function.identity(), JsonParsers.string(), LinkedHashMap::new);

        assertThatResult(basicMap, "{}").isEqualTo(ImmutableMap.of());
        assertThatResult(basicMap, "{ }").isEqualTo(ImmutableMap.of());
        assertThatResult(basicMap, "{ \"a\" : \"A\", \"b\":\"B\" ,\"c\": \"C\"}")
                .isEqualTo(ImmutableMap.of("a", "A", "b", "B", "c", "C"));
    }

    @Test
    void testMap_errors() {
        Parser<Map<String, String>> parser =
                JsonParsers.map(Function.identity(), JsonParsers.string(), LinkedHashMap::new);
        assertThatError(parser, "{")
                .contains(
                        """
                Parse error at line 1, column 1: Expected to find '}':
                {
                ^^
                """);
        assertThatError(parser, "")
                .contains(
                        """
                Parse error at line 1, column 1: Expected to find '{':

                ^
                """);
        assertThatError(parser, "{a")
                .contains(
                        """
                Parse error at line 1, column 2: Expected a quoted string and did not find a quote:
                {a
                 ^
                """);
        assertThatError(parser, "{\"a")
                .contains(
                        """
                Parse error at line 1, column 3: Reached end of stream looking for terminal quote:
                {"a
                  ^^
                """);
        assertThatError(parser, "{\"a\"")
                .contains(
                        """
                Parse error at line 1, column 4: Expected to find ':':
                {"a"
                   ^^
                """);
        assertThatError(parser, "{\"a\":")
                .contains(
                        """
                Parse error at line 1, column 5: Expected a quoted string and did not find a quote:
                {"a":
                    ^^
                """);
        assertThatError(parser, "{\"a\":\"")
                .contains(
                        """
                Parse error at line 1, column 6: Reached end of stream looking for terminal quote:
                {"a":"
                     ^^
                """);
        assertThatError(parser, "{\"a\":\"\"")
                .contains(
                        """
                Parse error at line 1, column 7: Expected to find '}':
                {"a":""
                      ^^
                """);
        assertThatError(parser, "{\"a\":\"\",")
                .contains(
                        """
                Parse error at line 1, column 8: Expected to find '}':
                {"a":"",
                       ^^
                """);
        assertThatError(parser, "{\"a\":\"\",}")
                .contains(
                        """
                Parse error at line 1, column 9: Expected a quoted string and did not find a quote:
                {"a":"",}
                        ^
                """);
    }

    @Test
    void testAny() {
        assertThatResult(JsonParsers.any(), "null").isEqualTo(Optional.empty());
        assertThatResult(JsonParsers.any(), "true").isEqualTo(Boolean.TRUE);
        assertThatResult(JsonParsers.any(), "0").isEqualTo(0.0);
        assertThatResult(JsonParsers.any(), "\"test\"").isEqualTo("test");
        assertThatResult(JsonParsers.any(), "[\"test\"]").isEqualTo(ImmutableList.of("test"));
        assertThatResult(JsonParsers.any(), "{\"a\": \"A\"}").isEqualTo(ImmutableMap.of("a", "A"));
        assertThatResult(JsonParsers.any(), "[[\"test\"]]")
                .isEqualTo(ImmutableList.of(ImmutableList.of("test")));
    }

    record TestObj(String a, int b, double c, Collection<String> d) {}

    @Test
    void testObjectAsMap() {
        Parser<TestObj> parser =
                JsonParsers.object(
                        field ->
                                switch (field) {
                                    case "a" -> JsonParsers.string();
                                    case "b" -> JsonParsers.integerParser();
                                    case "c" -> JsonParsers.doubleParser();
                                    case "d" -> JsonParsers.collection(
                                            JsonParsers.string(), ArrayList::new);
                                    default -> JsonParsers.any();
                                },
                        map ->
                                new TestObj(
                                        (String) map.get("a"),
                                        (int) map.get("b"),
                                        (double) map.get("c"),
                                        (Collection<String>) map.get("d")));

        assertThatResult(
                        parser,
                        """
            {"e": { "foo": "bar" }, "a": "test", "b": 1, "c": 0.1, "d": ["a", "b", "c"]}
            """)
                .isEqualTo(new TestObj("test", 1, 0.1, ImmutableList.of("a", "b", "c")));
    }
}
