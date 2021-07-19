package barista.serde.runtime.json;

import barista.serde.runtime.parsec.ParseError;
import barista.serde.runtime.parsec.ParseState;
import barista.serde.runtime.parsec.Parser;
import io.github.markelliot.result.Result;

final class LongParser implements Parser<Long> {
    public static final Parser<Long> INSTANCE = new LongParser();

    private LongParser() {}

    @Override
    public Result<Long, ParseError> parse(ParseState state) {
        ParseState.Mark pos = state.mark();
        int current = state.current();
        while (!JsonParsers.isValueBoundary(current)) {
            current = state.next();
        }
        try {
            return Result.ok(Long.parseLong(state.slice(pos).toString()));
        } catch (NumberFormatException nfe) {
            return Result.error(pos.error("Cannot parse long from value"));
        }
    }
}
