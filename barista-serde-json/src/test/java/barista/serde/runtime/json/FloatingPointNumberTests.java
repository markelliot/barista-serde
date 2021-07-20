package barista.serde.runtime.json;

import static barista.serde.runtime.json.JsonParserAsserts.assertThatError;
import static barista.serde.runtime.json.JsonParserAsserts.assertThatResult;

import barista.serde.runtime.parsec.Parser;
import org.junit.jupiter.api.Test;

final class FloatingPointNumberTests {
    @Test
    void testFloat() {
        Parser<Float> parser = JsonParsers.floatParser();

        assertThatResult(parser, "0").isEqualTo(0.0f);
        assertThatResult(parser, "0.").isEqualTo(0.0f);
        assertThatResult(parser, "-0.").isEqualTo(-0.0f);
        assertThatResult(parser, "0.0").isEqualTo(0.0f);
        assertThatResult(parser, "-Inf").isEqualTo(Float.NEGATIVE_INFINITY);
        assertThatResult(parser, "Inf").isEqualTo(Float.POSITIVE_INFINITY);
        assertThatResult(parser, "NaN").isEqualTo(Float.NaN);
        assertThatResult(parser, "1e10").isEqualTo(1e10f);
        assertThatResult(parser, "1.5e-10").isEqualTo(1.5e-10f);
        assertThatResult(parser, "1.5e+10").isEqualTo(1.5e+10f);
    }

    @Test
    void testFloat_errorOnInvalid() {
        assertThatError(JsonParsers.floatParser(), "test")
                .contains(
                        """
            Parse error at line 1, column 1: Cannot parse float from value:
            test
            ^--^
            """);
    }

    @Test
    void testDouble() {
        Parser<Double> parser = JsonParsers.doubleParser();

        assertThatResult(parser, "0").isEqualTo(0.0);
        assertThatResult(parser, "0.").isEqualTo(0.0);
        assertThatResult(parser, "-0.").isEqualTo(-0.0);
        assertThatResult(parser, "0.0").isEqualTo(0.0);
        assertThatResult(parser, "-Inf").isEqualTo(Double.NEGATIVE_INFINITY);
        assertThatResult(parser, "Inf").isEqualTo(Double.POSITIVE_INFINITY);
        assertThatResult(parser, "NaN").isEqualTo(Double.NaN);
        assertThatResult(parser, "1e10").isEqualTo(1e10);
        assertThatResult(parser, "1.5e-10").isEqualTo(1.5e-10);
        assertThatResult(parser, "1.5e+10").isEqualTo(1.5e+10);
    }

    @Test
    void testDouble_errorOnInvalid() {
        assertThatError(JsonParsers.doubleParser(), "test")
                .contains(
                        """
                    Parse error at line 1, column 1: Cannot parse double from value:
                    test
                    ^--^
                    """);
    }
}
