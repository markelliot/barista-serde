package barista.serde.runtime.parsec;

import barista.serde.runtime.JsonStrings;
import io.github.markelliot.result.Result;
import java.util.Collection;
import java.util.Map;
import java.util.function.Supplier;

public final class JsonParsers {
    private static final Map<String, Double> SPECIAL_NUMBERS =
            Map.of(
                    "-Infinity", Double.NEGATIVE_INFINITY,
                    "-Inf", Double.NEGATIVE_INFINITY,
                    "Infinity", Double.POSITIVE_INFINITY,
                    "Inf", Double.POSITIVE_INFINITY,
                    "NaN", Double.NaN);

    private JsonParsers() {}

    public static Parser<String> quotedString() {
        return state -> {
            int current = state.current();
            if (current != '"') {
                return Result.error(
                        state.mark().error("Expected a quoted string and did not find a quote"));
            }
            current = state.next();

            ParseState.Mark start = state.mark();
            int last = -1;
            while (current != -1 && !(current == '"' && last != '\\')) {
                last = current;
                current = state.next();
            }
            if (state.current() == -1) {
                return Result.error(
                        start.error("Reached end of stream looking for terminal quote"));
            }
            Result<String, ParseError> result =
                    Result.ok(JsonStrings.unescape(state.slice(start)).toString());

            state.next(); // consume final quote
            return result;
        };
    }

    public static Parser<Integer> integer() {
        return state -> {
            ParseState.Mark pos = state.mark();
            int current = state.current();
            while (!isValueBoundary(current)) {
                current = state.next();
            }
            try {
                return Result.ok(Integer.parseInt(state.slice(pos).toString()));
            } catch (NumberFormatException nfe) {
                return Result.error(pos.error("Cannot parse integer from value"));
            }
        };
    }

    public static Parser<Long> longParser() {
        return state -> {
            ParseState.Mark pos = state.mark();
            int current = state.current();
            while (!isValueBoundary(current)) {
                current = state.next();
            }
            try {
                return Result.ok(Long.parseLong(state.slice(pos).toString()));
            } catch (NumberFormatException nfe) {
                return Result.error(pos.error("Cannot parse long from value"));
            }
        };
    }

    public static Parser<Double> doubleParser() {
        return state -> {
            ParseState.Mark pos = state.mark();
            int current = state.current();
            while (!isValueBoundary(current)) {
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
        };
    }

    public static <T, C extends Collection<T>> Parser<C> collection(
            Parser<T> itemParser, Supplier<C> collectionFactory) {
        Parser<T> liberalItemParser = Parsers.whitespace(itemParser);
        return state -> {
            C collection = collectionFactory.get();

            if (state.current() != '[') {
                return Result.error(state.mark().error("Expected to find start of a collection"));
            }
            ParseState.Mark collectionStart = state.mark();

            state.next();
            // consume whitespace
            Parsers.whitespace().parse(state);

            while (state.current() != -1 && state.current() != ']') {
                Result<T, ParseError> item = liberalItemParser.parse(state);
                if (item.isError()) {
                    return Result.error(item.error().get());
                }
                collection.add(item.result().get());

                // consume trailing whitespace
                Parsers.whitespace().parse(state);
                if (state.current() == ',') {
                    state.next();
                }
            }
            if (state.current() == -1) {
                return Result.error(
                        collectionStart.error(
                                "Reached end of stream looking for end of collection"));
            }
            state.next(); // consume list end marker
            return Result.ok(collection);
        };
    }

    private static boolean isValueBoundary(int character) {
        return Character.isWhitespace(character)
                || switch (character) {
                    case '[', ']', '{', '}', ',', -1 -> true;
                    default -> false;
                };
    }
}
