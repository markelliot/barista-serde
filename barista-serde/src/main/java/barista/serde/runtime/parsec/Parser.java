package barista.serde.runtime.parsec;

import io.github.markelliot.result.Result;

public interface Parser<T> {
    Result<T, ParseError> parse(ParseState state);
}
