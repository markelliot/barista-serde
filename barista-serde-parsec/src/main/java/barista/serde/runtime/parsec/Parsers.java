package barista.serde.runtime.parsec;

import io.github.markelliot.result.Result;
import java.util.function.Function;
import java.util.function.Supplier;

public final class Parsers {

    private Parsers() {}

    /** Returns a parser that rewinds state if in error. */
    public static <T> Parser<T> maybe(Parser<T> parser) {
        return new MaybeParser<>(parser);
    }

    /** Returns a parser that first consumes any whitespace characters. */
    public static <T> Parser<T> whitespace(Parser<T> parser) {
        return state -> {
            state.skipWhitespace();
            return parser.parse(state);
        };
    }

    public static Parser<Character> expect(char expectation) {
        return new ExpectParser(expectation);
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

    public static <T> Parser<T> between(
            Parser<?> start, Parser<T> parser, Parser<?> end, Supplier<T> defaultValue) {
        return new BetweenParser<>(start, parser, end, defaultValue);
    }

    /**
     * Returns a parser that runs the provided parser and maps the result according to {@code fn}.
     */
    public static <T, U> Parser<U> composeResult(Parser<T> parser, Function<T, U> fn) {
        return state -> parser.parse(state).mapResult(fn);
    }

    /** Returns a parser that always produces the provided error. */
    public static <T> Parser<T> error(String error) {
        return state -> Result.error(state.mark().error(error));
    }
}
