package barista.serde.runtime.json;

import barista.serde.runtime.parsec.Empty;
import barista.serde.runtime.parsec.ParseError;
import barista.serde.runtime.parsec.ParseState;
import barista.serde.runtime.parsec.Parser;
import barista.serde.runtime.parsec.Parsers;
import io.github.markelliot.result.Result;

final class KeyValueSeparatorParser implements Parser<Empty> {
    public static final Parser<Empty> INSTANCE = new KeyValueSeparatorParser();

    private static final Parser<Character> colon = Parsers.expect(':');

    private KeyValueSeparatorParser() {}

    @Override
    public Result<Empty, ParseError> parse(ParseState state) {
        state.skipWhitespace();
        Result<Character, ParseError> sep = colon.parse(state);
        state.skipWhitespace();
        return sep.mapResult(r -> Empty.INSTANCE);
    }
}
