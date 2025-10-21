package barista.serde.runtime.parsec;

import com.markelliot.result.Result;

public interface Parser<T> {
    Result<T, ParseError> parse(ParseState state);
}
