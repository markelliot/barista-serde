package barista.serde.runtime.parsec;

import io.github.markelliot.result.Result;

public final class Parsers {

    private Parsers() {}

    public static ParseState of(String str) {
        return new ParseState(str);
    }

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

    public static Parser<Void> whitespace() {
        return state -> {
            // throw away whitespace characters
            while (Character.isWhitespace(state.current())) {
                state.next();
            }
            return null;
        };
    }

    public static Parser<String> expect(String expectation) {
        return state -> {
            ParseState.Mark pos = state.mark();
            for (int i = 0; i < expectation.length(); i++) {
                if (state.current() != expectation.charAt(i)) {
                    return Result.error(pos.error("Expected to find '" + expectation + "'"));
                }
                if (state.current() == -1) {
                    return Result.error(pos.error("Unexpectedly reached end of stream."));
                }
                state.next();
            }
            return Result.ok(expectation);
        };
    }

    public static <T> Parser<T> between(Parser<?> start, Parser<T> parser, Parser<?> end) {
        return state -> {
            Result<?, ParseError> startResult = start.parse(state);
            if (startResult.isError()) {
                return Result.error(startResult.error().get());
            }

            Result<T, ParseError> result = parser.parse(state);

            Result<?, ParseError> endResult = end.parse(state);
            if (endResult.isError()) {
                return Result.error(endResult.error().get());
            }

            return result;
        };
    }

    public static Parser<String> match(char character) {
        return state -> {
            int current = state.current();
            if (current == -1) {
                return Result.error(
                        state.mark()
                                .error(
                                        "Reached end of stream while looking for '"
                                                + character
                                                + "'"));
            }
            if (current != character) {
                return Result.error(state.mark().error("Expected '" + character + "'"));
            }

            return Result.ok(Character.toString(character));
        };
    }
}
