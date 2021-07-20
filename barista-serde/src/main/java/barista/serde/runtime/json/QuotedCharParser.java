package barista.serde.runtime.json;

import barista.serde.runtime.parsec.ParseError;
import barista.serde.runtime.parsec.ParseState;
import barista.serde.runtime.parsec.Parser;
import io.github.markelliot.result.Result;

final class QuotedCharParser implements Parser<Character> {
    public static final Parser<Character> INSTANCE = new QuotedCharParser();

    private QuotedCharParser() {}

    @Override
    public Result<Character, ParseError> parse(ParseState state) {
        ParseState.Mark start = state.mark();
        return JsonParsers.string()
                .parse(state)
                .flatMapResult(
                        r ->
                                r.length() != 1
                                        ? Result.error(start.error("Expected a single character"))
                                        : Result.ok(r.charAt(0)));
    }
}
