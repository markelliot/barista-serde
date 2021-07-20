package barista.serde.runtime.parsec;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

final class ParsersTests {
    @Test
    public void testExpect_success() throws Exception {
        ParseState state = ParseState.of("string");
        Parser<String> parser = Parsers.expect("string");

        assertThat(parser.parse(state).orElseThrow()).isEqualTo("string");
    }

    @Test
    public void testExpect_error() throws Exception {
        ParseState state = ParseState.of("stri...not");
        Parser<String> parser = Parsers.expect("string");

        assertThat(parser.parse(state).error().map(ParseError::errorString))
                .contains(
                        """
                Parse error at line 1, column 1: Expected to find 'string':
                stri...not
                ^--^
                """);
    }

    @Test
    public void testWhitespaceThenExpect_error() throws Exception {
        ParseState state = ParseState.of("    stri...not");
        Parser<String> parser = Parsers.whitespace(Parsers.expect("string"));

        assertThat(parser.parse(state).error().map(ParseError::errorString))
                .contains(
                        """
            Parse error at line 1, column 5: Expected to find 'string':
                stri...not
                ^--^
            """);
    }
}
