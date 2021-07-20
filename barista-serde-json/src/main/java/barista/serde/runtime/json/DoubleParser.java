package barista.serde.runtime.json;

import barista.serde.runtime.parsec.ParseError;
import barista.serde.runtime.parsec.ParseState;
import barista.serde.runtime.parsec.Parser;
import io.github.markelliot.result.Result;
import java.util.Map;

final class DoubleParser implements Parser<Double> {
    public static final Parser<Double> INSTANCE = new DoubleParser();

    private static final Map<String, Double> SPECIAL_NUMBERS =
            Map.of(
                    "-Infinity", Double.NEGATIVE_INFINITY,
                    "-Inf", Double.NEGATIVE_INFINITY,
                    "Infinity", Double.POSITIVE_INFINITY,
                    "Inf", Double.POSITIVE_INFINITY,
                    "NaN", Double.NaN);

    private DoubleParser() {}

    @Override
    public Result<Double, ParseError> parse(ParseState state) {
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
            return Result.ok(Double.parseDouble(numString));
        } catch (NumberFormatException nfe) {
            return Result.error(pos.error("Cannot parse double from value"));
        }
    }
}
