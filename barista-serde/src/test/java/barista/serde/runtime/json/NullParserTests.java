package barista.serde.runtime.json;

import static barista.serde.runtime.json.JsonParserAsserts.assertThatError;
import static barista.serde.runtime.json.JsonParserAsserts.assertThatResult;

import barista.serde.runtime.parsec.Empty;
import org.junit.jupiter.api.Test;

final class NullParserTests {
    @Test
    void testNull() {
        assertThatResult(JsonParsers.nullParser(), "null").isEqualTo(Empty.INSTANCE);
    }

    @Test
    void testNotNull() {
        assertThatError(JsonParsers.nullParser(), "notnull")
                .contains(
                        """
            Parse error at line 1, column 1: Expected to find 'null':
            notnull
            ^^
            """);
    }
}
