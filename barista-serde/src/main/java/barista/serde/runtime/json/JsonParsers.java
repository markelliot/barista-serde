package barista.serde.runtime.json;

import barista.serde.runtime.JsonStrings;
import barista.serde.runtime.parsec.ParseError;
import barista.serde.runtime.parsec.ParseState;
import barista.serde.runtime.parsec.ParseState.Mark;
import barista.serde.runtime.parsec.Parser;
import barista.serde.runtime.parsec.Parsers;
import io.github.markelliot.result.Result;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
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

            Mark start = state.mark();
            int last = -1;
            while (current != ParseState.EOS && !(current == '"' && last != '\\')) {
                last = current;
                current = state.next();
            }
            if (state.isEndOfStream()) {
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
        return Parsers.between(
                Parsers.expect("["),
                Parsers.whitespace(collectionInternal(itemParser, collectionFactory)),
                Parsers.whitespace(Parsers.expect("]")));
    }

    private static <T, C extends Collection<T>> Parser<C> collectionInternal(
            Parser<T> itemParser, Supplier<C> collectionFactory) {
        return state -> {
            C collection = collectionFactory.get();
            while (!state.isEndOfStream()) {
                Parsers.whitespace().parse(state);
                Result<T, ParseError> item = itemParser.parse(state);
                if (item.isError()) {
                    return item.coerce();
                }
                item.mapResult(collection::add);

                // consume trailing whitespace
                Parsers.whitespace().parse(state);
                if (state.current() == ',') {
                    state.next(); // consume ','
                } else {
                    break;
                }
            }
            return Result.ok(collection);
        };
    }

    public static <K, V, M extends Map<K, V>> Parser<M> map(
            Function<String, K> keyFn, Parser<V> itemParser, Supplier<M> mapFactory) {
        return Parsers.between(
                Parsers.expect("{"),
                Parsers.whitespace(mapInternal(keyFn, itemParser, mapFactory)),
                Parsers.whitespace(Parsers.expect("}")));
    }

    private static <K, V, M extends Map<K, V>> Parser<M> mapInternal(
            Function<String, K> keyFn, Parser<V> itemParser, Supplier<M> mapFactory) {
        return state -> {
            M map = mapFactory.get();
            while (!state.isEndOfStream()) {
                Parsers.whitespace().parse(state);

                Result<K, ParseError> key = quotedString().parse(state).mapResult(keyFn);
                if (key.isError()) {
                    return key.coerce();
                }

                Parsers.whitespace().parse(state);
                Parsers.expect(":").parse(state);
                Parsers.whitespace().parse(state);

                Result<V, ParseError> item = itemParser.parse(state);
                if (item.isError()) {
                    return item.coerce();
                }

                map.put(key.unwrap(), item.unwrap());

                // consume trailing whitespace
                Parsers.whitespace().parse(state);
                if (state.current() == ',') {
                    state.next(); // consume ','
                } else {
                    break;
                }
            }
            return Result.ok(map);
        };
    }

    private static boolean isValueBoundary(int character) {
        return Character.isWhitespace(character)
                || switch (character) {
                    case '[', ']', '{', '}', ',', ParseState.EOS -> true;
                    default -> false;
                };
    }
}
