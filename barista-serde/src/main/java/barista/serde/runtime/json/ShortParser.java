package barista.serde.runtime.json;

import barista.serde.runtime.parsec.ParseError;
import barista.serde.runtime.parsec.ParseState;
import barista.serde.runtime.parsec.Parser;
import io.github.markelliot.result.Result;

final class ShortParser implements Parser<Short> {
    public static Parser<Short> INSTANCE = new ShortParser();

    private ShortParser() {}

    @Override
    public Result<Short, ParseError> parse(ParseState state) {
        ParseState.Mark pos = state.mark();
        int current = state.current();
        while (!JsonParsers.isValueBoundary(current)) {
            current = state.next();
        }
        try {
            return Result.ok(Short.parseShort(state.slice(pos).toString()));
        } catch (NumberFormatException nfe) {
            return Result.error(pos.error("Cannot parse short from value"));
        }
    }
}
