package barista.serde.runtime.json;

import barista.serde.runtime.parsec.ParseError;
import barista.serde.runtime.parsec.ParseState;
import barista.serde.runtime.parsec.Parser;
import io.github.markelliot.result.Result;
import java.util.function.Function;

final class WholeNumberParser<T> implements Parser<T> {
    public static final Parser<Byte> BYTE = new WholeNumberParser<>(Byte::parseByte, "byte");
    public static final Parser<Short> SHORT = new WholeNumberParser<>(Short::parseShort, "short");
    public static final Parser<Integer> INT = new WholeNumberParser<>(Integer::parseInt, "integer");
    public static final Parser<Long> LONG = new WholeNumberParser<>(Long::parseLong, "long");

    private final Function<String, T> fn;
    private final String name;

    private WholeNumberParser(Function<String, T> fn, String name) {
        this.fn = fn;
        this.name = name;
    }

    @Override
    public Result<T, ParseError> parse(ParseState state) {
        ParseState.Mark pos = state.mark();
        int current = state.current();
        while (!JsonParsers.isValueBoundary(current)) {
            current = state.next();
        }
        try {
            return Result.ok(fn.apply(state.slice(pos).toString()));
        } catch (NumberFormatException nfe) {
            return Result.error(pos.error("Cannot parse " + name + " from value"));
        }
    }
}
