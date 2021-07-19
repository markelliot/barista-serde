package barista.serde.runtime.json;

import barista.serde.runtime.parsec.ParseError;
import barista.serde.runtime.parsec.ParseState;
import barista.serde.runtime.parsec.Parser;
import io.github.markelliot.result.Result;

final class ByteParser implements Parser<Byte> {
    public static Parser<Byte> INSTANCE = new ByteParser();

    private ByteParser() {}

    @Override
    public Result<Byte, ParseError> parse(ParseState state) {
        ParseState.Mark pos = state.mark();
        int current = state.current();
        while (!JsonParsers.isValueBoundary(current)) {
            current = state.next();
        }
        try {
            return Result.ok(Byte.parseByte(state.slice(pos).toString()));
        } catch (NumberFormatException nfe) {
            return Result.error(pos.error("Cannot parse byte from value"));
        }
    }
}
