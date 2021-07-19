package barista.serde.runtime.json;

import barista.serde.runtime.parsec.ParseError;
import barista.serde.runtime.parsec.ParseState;
import barista.serde.runtime.parsec.Parser;
import io.github.markelliot.result.Result;
import java.util.Map;

final class FloatParser implements Parser<Float> {
    public static final Parser<Float> INSTANCE = new FloatParser();

    private static final Map<String, Float> SPECIAL_NUMBERS =
            Map.of(
                    "-Infinity", Float.NEGATIVE_INFINITY,
                    "-Inf", Float.NEGATIVE_INFINITY,
                    "Infinity", Float.POSITIVE_INFINITY,
                    "Inf", Float.POSITIVE_INFINITY,
                    "NaN", Float.NaN);

    private FloatParser() {}

    @Override
    public Result<Float, ParseError> parse(ParseState state) {
        ParseState.Mark pos = state.mark();
        int current = state.current();
        while (!JsonParsers.isValueBoundary(current)) {
            current = state.next();
        }

        // TODO(markelliot): maybe we should have a flag to allow this, it's a little more
        //  liberal than the JSON spec, which disallows these special values
        String numString = state.slice(pos).toString();
        if (SPECIAL_NUMBERS.containsKey(numString)) {
            return Result.ok(SPECIAL_NUMBERS.get(numString));
        }

        try {
            return Result.ok(Float.parseFloat(numString));
        } catch (NumberFormatException nfe) {
            return Result.error(pos.error("Cannot parse float from value"));
        }
    }
}
