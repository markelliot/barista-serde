package barista.serde.runtime.parsec;

import io.github.markelliot.result.Result;
import java.util.function.Function;
import java.util.function.Supplier;

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

    public static Parser<Character> expect(char expectation) {
        return state -> {
            if (state.current() == expectation) {
                state.next(); // consume the expected char
                return Result.ok(expectation);
            }
            return Result.error(state.mark().error("Expected to find '" + expectation + "'"));
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

    public static <T> Parser<T> between(
            Parser<?> start, Parser<T> parser, Parser<?> end, Supplier<T> defaultValue) {
        Parser<?> maybeEnd = Parsers.maybe(end);
        return state ->
                start.parse(state)
                        .flatMapResult(
                                ignoredStart -> {
                                    // if we immediately see the end, return the default value, we
                                    // use a maybe parser to ensure we rewind because it's not an
                                    // error to have an empty result
                                    if (!maybeEnd.parse(state).isError()) {
                                        return Result.ok(defaultValue.get());
                                    }

                                    return parser.parse(state)
                                            .flatMapResult(
                                                    result ->
                                                            end.parse(state)
                                                                    .mapResult(
                                                                            ignoredEnd -> result));
                                });
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