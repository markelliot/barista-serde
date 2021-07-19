package barista.serde.runtime.json;

import barista.serde.runtime.parsec.ParseError;
import barista.serde.runtime.parsec.ParseState;
import barista.serde.runtime.parsec.ParseState.Mark;
import barista.serde.runtime.parsec.Parser;
import io.github.markelliot.result.Result;

final class QuotedStringParser implements Parser<String> {
    public static final Parser<String> INSTANCE = new QuotedStringParser();

    private QuotedStringParser() {}

    @Override
    public Result<String, ParseError> parse(ParseState state) {
        int current = state.current();
        if (current != '"') {
            return Result.error(
                    state.mark().error("Expected a quoted string and did not find a quote"));
        }
        current = state.next();

        Mark start = state.mark();
        int last = -1;
        while (current != ParseState.EOS && !(current == '"' && last != '\\')) {
            last = current;
            current = state.next();
        }
        if (state.isEndOfStream()) {
            return Result.error(start.error("Reached end of stream looking for terminal quote"));
        }
        Result<String, ParseError> result =
                Result.ok(JsonStrings.unescape(state.slice(start)).toString());

        state.next(); // consume final quote
        return result;
    }
}
