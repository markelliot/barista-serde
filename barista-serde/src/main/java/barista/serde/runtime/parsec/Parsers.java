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

    public static <T> Parser<T> prefix(Parser<?> prefix, Parser<T> andThen) {
        return state -> prefix.parse(state).flatMapResult(ignored -> andThen.parse(state));
    }

    /** Returns a parser that first consumes any whitespace characters. */
    public static <T> Parser<T> whitespace(Parser<T> parser) {
        return prefix(whitespace(), parser);
    }

    /** Returns a parser that consumes any whitespace characters. */
    public static Parser<Empty> whitespace() {
        return WhitespaceParser.INSTANCE;
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

    /** Returns a parser that always produces the provided error. */
    public static <T> Parser<T> error(String error) {
        return state -> Result.error(state.mark().error(error));
    }
}
