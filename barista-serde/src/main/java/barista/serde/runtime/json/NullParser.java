package barista.serde.runtime.json;

import barista.serde.runtime.parsec.Empty;
import barista.serde.runtime.parsec.ParseError;
import barista.serde.runtime.parsec.ParseState;
import barista.serde.runtime.parsec.Parser;
import barista.serde.runtime.parsec.Parsers;
import io.github.markelliot.result.Result;

final class NullParser implements Parser<Empty> {
    public static final Parser<Empty> INSTANCE = new NullParser();

    private static final Parser<String> NULL = Parsers.expect("null");

    private NullParser() {}

    @Override
    public Result<Empty, ParseError> parse(ParseState state) {
        return NULL.parse(state).mapResult(ignored -> Empty.INSTANCE);
    }
}
