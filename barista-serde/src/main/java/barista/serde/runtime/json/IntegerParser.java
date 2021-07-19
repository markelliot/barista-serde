package barista.serde.runtime.json;

import barista.serde.runtime.parsec.ParseError;
import barista.serde.runtime.parsec.ParseState;
import barista.serde.runtime.parsec.Parser;
import io.github.markelliot.result.Result;

final class IntegerParser implements Parser<Integer> {
    public static final Parser<Integer> INSTANCE = new IntegerParser();

    private IntegerParser() {}

    @Override
    public Result<Integer, ParseError> parse(ParseState state) {
        ParseState.Mark pos = state.mark();
        int current = state.current();
        while (!JsonParsers.isValueBoundary(current)) {
            current = state.next();
        }
        try {
            return Result.ok(Integer.parseInt(state.slice(pos).toString()));
        } catch (NumberFormatException nfe) {
            return Result.error(pos.error("Cannot parse integer from value"));
        }
    }
}
