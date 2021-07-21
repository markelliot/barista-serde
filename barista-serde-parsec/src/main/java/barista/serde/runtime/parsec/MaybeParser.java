package barista.serde.runtime.parsec;

import io.github.markelliot.result.Result;

final class MaybeParser<T> implements Parser<T> {
    private final Parser<T> parser;

    MaybeParser(Parser<T> parser) {
        this.parser = parser;
    }

    @Override
    public Result<T, ParseError> parse(ParseState state) {
        ParseState.Mark pos = state.mark();
        Result<T, ParseError> result = parser.parse(state);
        if (result.isError()) {
            state.rewind(pos);
        }
        return result;
    }
}
