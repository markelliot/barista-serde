package barista.serde.runtime.json;

import barista.serde.runtime.parsec.Empty;
import barista.serde.runtime.parsec.ParseError;
import barista.serde.runtime.parsec.ParseState;
import barista.serde.runtime.parsec.Parser;
import io.github.markelliot.result.Result;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.function.Function;
import java.util.function.Supplier;

final class OptionalParser<T, U> implements Parser<U> {
    public static final Parser<OptionalInt> INT =
            new OptionalParser<>(JsonParsers.integerParser(), OptionalInt::of, OptionalInt::empty);
    public static final Parser<OptionalLong> LONG =
            new OptionalParser<>(JsonParsers.longParser(), OptionalLong::of, OptionalLong::empty);
    public static final Parser<OptionalDouble> DOUBLE =
            new OptionalParser<>(
                    JsonParsers.doubleParser(), OptionalDouble::of, OptionalDouble::empty);

    public static <T> Parser<Optional<T>> of(Parser<T> parser) {
        return new OptionalParser<>(parser, Optional::of, Optional::empty);
    }

    private final Parser<T> parser;
    private final Function<T, U> fn;
    private final Supplier<U> defaultValue;

    private OptionalParser(Parser<T> parser, Function<T, U> fn, Supplier<U> defaultValue) {
        this.parser = parser;
        this.fn = fn;
        this.defaultValue = defaultValue;
    }

    @Override
    public Result<U, ParseError> parse(ParseState state) {
        if (state.current() != 'n') {
            return parser.parse(state).mapResult(fn);
        } else {
            Result<Empty, ParseError> nullResult = JsonParsers.nullParser().parse(state);
            if (nullResult.isError()) {
                return nullResult.coerce();
            }
            return Result.ok(defaultValue.get());
        }
    }
}
