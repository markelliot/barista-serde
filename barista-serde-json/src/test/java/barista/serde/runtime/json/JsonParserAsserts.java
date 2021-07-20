package barista.serde.runtime.json;

import static org.assertj.core.api.Assertions.assertThat;

import barista.serde.runtime.parsec.ParseError;
import barista.serde.runtime.parsec.ParseState;
import barista.serde.runtime.parsec.Parser;
import org.assertj.core.api.ObjectAssert;
import org.assertj.core.api.OptionalAssert;

final class JsonParserAsserts {

    private JsonParserAsserts() {}

    static <T> ObjectAssert<T> assertThatResult(Parser<T> parser, String str) {
        return assertThat(parser.parse(ParseState.of(str))
            .orElseThrow(err -> new IllegalStateException(err.errorString())));
    }

    static <T> OptionalAssert<String> assertThatError(Parser<T> parser, String str) {
        return assertThat(parser.parse(ParseState.of(str)).error().map(ParseError::errorString));
    }
}
