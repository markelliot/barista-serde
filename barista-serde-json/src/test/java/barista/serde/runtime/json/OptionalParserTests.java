package barista.serde.runtime.json;

import static barista.serde.runtime.json.JsonParserAsserts.assertThatError;
import static barista.serde.runtime.json.JsonParserAsserts.assertThatResult;

import barista.serde.runtime.parsec.Parser;
import barista.serde.runtime.parsec.Parsers;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import org.junit.jupiter.api.Test;

final class OptionalParserTests {
    @Test
    void testOptional_emptyOnNull() {
        assertThatResult(JsonParsers.optionalInt(), "null").isEqualTo(OptionalInt.empty());
        assertThatResult(JsonParsers.optionalLong(), "null").isEqualTo(OptionalLong.empty());
        assertThatResult(JsonParsers.optionalDouble(), "null").isEqualTo(OptionalDouble.empty());
        assertThatResult(JsonParsers.optional(Parsers.error("fail")), "null")
                .isEqualTo(Optional.empty());
    }

    @Test
    void testOptional_filledOnValid() {
        assertThatResult(JsonParsers.optionalInt(), "1").isEqualTo(OptionalInt.of(1));
        assertThatResult(JsonParsers.optionalLong(), "1").isEqualTo(OptionalLong.of(1L));
        assertThatResult(JsonParsers.optionalDouble(), "1").isEqualTo(OptionalDouble.of(1.0));
        assertThatResult(JsonParsers.optional(JsonParsers.string()), "\"test\"")
                .isEqualTo(Optional.of("test"));
    }

    @Test
    void testOptional_propagateError() {
        assertThatError(JsonParsers.optional(JsonParsers.string()), "0")
                .contains(
                        """
                Parse error at line 1, column 1: Expected a quoted string and did not find a quote:
                0
                ^
                """);
    }

    @Test
    void testOptional_nested() {
        Parser<Optional<Optional<String>>> parser =
                JsonParsers.optional(JsonParsers.optional(JsonParsers.string()));
        assertThatResult(parser, "\"test\"").isEqualTo(Optional.of(Optional.of("test")));
        assertThatResult(parser, "null").isEqualTo(Optional.empty());
        assertThatError(parser, "0")
                .contains(
                        """
            Parse error at line 1, column 1: Expected a quoted string and did not find a quote:
            0
            ^
            """);
    }
}
