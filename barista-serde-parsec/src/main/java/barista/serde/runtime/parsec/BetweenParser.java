package barista.serde.runtime.parsec;

import io.github.markelliot.result.Result;
import java.util.function.Supplier;

final class BetweenParser<T> implements Parser<T> {
    private final Parser<?> start;
    private final Parser<T> parser;
    private final Parser<?> maybeEnd;
    private final Parser<?> end;
    private final Supplier<T> defaultValue;

    BetweenParser(Parser<?> start, Parser<T> parser, Parser<?> end, Supplier<T> defaultValue) {
        this.start = start;
        this.parser = parser;
        this.maybeEnd = Parsers.maybe(end);
        this.end = end;
        this.defaultValue = defaultValue;
    }

    @Override
    public Result<T, ParseError> parse(ParseState state) {
        Result<?, ParseError> startResult = start.parse(state);
        if (startResult.isError()) {
            return startResult.coerce();
        }

        // if we immediately see the end, return the default value, we
        // use a maybe parser to ensure we rewind because it's not an
        // error to have an empty result
        if (!maybeEnd.parse(state).isError()) {
            return Result.ok(defaultValue.get());
        }

        Result<T, ParseError> result = parser.parse(state);
        if (result.isError()) {
            return result;
        }

        Result<?, ParseError> endResult = end.parse(state);
        if (endResult.isError()) {
            return endResult.coerce();
        }

        return result;
    }
}
