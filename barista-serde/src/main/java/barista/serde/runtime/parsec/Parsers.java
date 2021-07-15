package barista.serde.runtime.parsec;

import io.github.markelliot.result.Result;

public final class Parsers {

    private Parsers() {}

    /** Returns a parser that rewinds state if in error. */
    public static <T> Parser<T> maybe(Parser<T> parser) {
        return state -> {
            ParseState.Mark pos = state.mark();
            Result<T, ParseError> result = parser.parse(state);
            if (result.isError()) {
                state.rewind(pos);
            }
            return result;
        };
    }

    /** Returns a parser that first consumes any whitespace characters. */
    public static <T> Parser<T> whitespace(Parser<T> parser) {
        return state -> {
            whitespace().parse(state);
            return parser.parse(state);
        };
    }

    /** Returns a parser that consumes any whitespace characters. */
    public static Parser<Empty> whitespace() {
        return state -> {
            // throw away whitespace characters
            // we skip the isEndOfStream check because the EOS marker is not whitespace
            while (Character.isWhitespace(state.current())) {
                state.next();
            }
            return Result.ok(Empty.INSTANCE);
        };
    }

    public static Parser<String> expect(String expectation) {
        return state -> {
            ParseState.Mark pos = state.mark();
            for (int i = 0; i < expectation.length(); i++) {
                if (state.current() != expectation.charAt(i)) {
                    return Result.error(pos.error("Expected to find '" + expectation + "'"));
                }
                if (state.isEndOfStream()) {
                    return Result.error(pos.error("Unexpectedly reached end of stream."));
                }
                state.next();
            }
            return Result.ok(expectation);
        };
    }

    public static <T> Parser<T> between(Parser<?> start, Parser<T> parser, Parser<?> end) {
        return state ->
                start.parse(state)
                        .flatMapResult(ignored -> parser.parse(state))
                        .flatMapResult(result -> end.parse(state).mapResult(ignored -> result));
    }
}
