package barista.serde.runtime.json;

import static barista.serde.runtime.json.JsonParserAsserts.assertThatError;
import static barista.serde.runtime.json.JsonParserAsserts.assertThatResult;

import org.junit.jupiter.api.Test;

final class WholeNumberParserTests {
    @Test
    void testByte_inRange() {
        assertThatResult(JsonParsers.byteParser(), "-1").isEqualTo((byte) -1);
        assertThatResult(JsonParsers.byteParser(), "0").isEqualTo((byte) 0);
        assertThatResult(JsonParsers.byteParser(), "1").isEqualTo((byte) 1);
    }

    @Test
    void testByte_outOfRange() {
        assertThatError(JsonParsers.byteParser(), "256")
                .contains(
                        """
                Parse error at line 1, column 1: Cannot parse byte from value:
                256
                ^-^
                """);
    }

    @Test
    void testShort_inRange() {
        assertThatResult(JsonParsers.shortParser(), "-1").isEqualTo((short) -1);
        assertThatResult(JsonParsers.shortParser(), "0").isEqualTo((short) 0);
        assertThatResult(JsonParsers.shortParser(), "1").isEqualTo((short) 1);
    }

    @Test
    void testShort_outOfRange() {
        assertThatError(JsonParsers.shortParser(), "65536")
                .contains(
                        """
                Parse error at line 1, column 1: Cannot parse short from value:
                65536
                ^---^
                """);
    }

    @Test
    void testInt_inRange() {
        assertThatResult(JsonParsers.integerParser(), "-1").isEqualTo(-1);
        assertThatResult(JsonParsers.integerParser(), "0").isEqualTo(0);
        assertThatResult(JsonParsers.integerParser(), "1").isEqualTo(1);
    }

    @Test
    void testInt_outOfRange() {
        assertThatError(JsonParsers.integerParser(), Integer.MAX_VALUE + "0")
                .contains(
                        """
                Parse error at line 1, column 1: Cannot parse integer from value:
                21474836470
                ^---------^
                """);
    }

    @Test
    void testLong_inRange() {
        assertThatResult(JsonParsers.longParser(), "-1").isEqualTo(-1L);
        assertThatResult(JsonParsers.longParser(), "0").isEqualTo(0L);
        assertThatResult(JsonParsers.longParser(), "1").isEqualTo(1L);
    }

    @Test
    void testLong_outOfRange() {
        assertThatError(JsonParsers.longParser(), Long.MAX_VALUE + "0")
                .contains(
                        """
                Parse error at line 1, column 1: Cannot parse long from value:
                92233720368547758070
                ^------------------^
                """);
    }

    @Test
    void testLong_invalidValues() {
        assertThatError(JsonParsers.longParser(), "test")
                .contains(
                        """
                    Parse error at line 1, column 1: Cannot parse long from value:
                    test
                    ^--^
                    """);

        assertThatError(JsonParsers.longParser(), "1e10")
                .contains(
                        """
                    Parse error at line 1, column 1: Cannot parse long from value:
                    1e10
                    ^--^
                    """);
    }
}
