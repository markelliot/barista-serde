package barista.serde.runtime.json;

import barista.serde.runtime.parsec.Empty;
import barista.serde.runtime.parsec.ParseError;
import barista.serde.runtime.parsec.ParseState;
import barista.serde.runtime.parsec.Parser;
import barista.serde.runtime.parsec.Parsers;
import io.github.markelliot.result.Result;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public final class JsonParsers {
    private JsonParsers() {}

    public static Parser<String> quotedString() {
        return QuotedStringParser.INSTANCE;
    }

    public static Parser<Integer> integerParser() {
        return IntegerParser.INSTANCE;
    }

    public static Parser<Long> longParser() {
        return LongParser.INSTANCE;
    }

    public static Parser<Double> doubleParser() {
        return DoubleParser.INSTANCE;
    }

    public static <T, C extends Collection<T>> Parser<C> collection(
            Parser<T> itemParser, Supplier<C> collectionFactory) {
        return Parsers.between(
                Parsers.expect('['),
                collectionInternal(itemParser, collectionFactory),
                Parsers.whitespace(Parsers.expect(']')),
                collectionFactory);
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

    public static Parser<Empty> keyValueSeparator() {
        return KeyValueSeparatorParser.INSTANCE;
    }

    public static <K, V, M extends Map<K, V>> Parser<M> map(
            Function<String, K> keyFn, Parser<V> itemParser, Supplier<M> mapFactory) {
        return Parsers.between(
                Parsers.expect('{'),
                mapInternal(keyFn, itemParser, mapFactory),
                Parsers.whitespace(Parsers.expect('}')),
                mapFactory);
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

                Result<Empty, ParseError> sep = keyValueSeparator().parse(state);
                if (sep.isError()) {
                    return sep.coerce();
                }

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

    // TODO(markelliot): this is one of the easiest ways to implement an object parser, but since
    //  we can generate code, it's possible we might  be able to do better. A couple of variants to
    //  consider:
    //  1. Generate objectInternal() for each object we wish to parse.
    //  2. Accept a BiConsumer<String, Object> rather than creating a LinkedHashMap
    //  3. Produce a List<FieldRecord> for record FieldRecord(String field, Object value)
    public static Parser<Map<String, Object>> object(Function<String, Parser<?>> fieldToParser) {
        return Parsers.between(
                Parsers.expect('{'),
                objectInternal(fieldToParser),
                Parsers.whitespace(Parsers.expect('}')),
                Map::of);
    }

    public static <T> Parser<T> object(
            Function<String, Parser<?>> fieldToParser, Function<Map<String, Object>, T> map) {
        return state -> object(fieldToParser).parse(state).mapResult(map);
    }

    private static Parser<Map<String, Object>> objectInternal(
            Function<String, Parser<?>> fieldToParser) {
        return state -> {
            Map<String, Object> map = new LinkedHashMap<>();
            while (!state.isEndOfStream()) {
                Parsers.whitespace().parse(state);

                Result<String, ParseError> key = quotedString().parse(state);
                if (key.isError()) {
                    return key.coerce();
                }

                Result<Empty, ParseError> sep = keyValueSeparator().parse(state);
                if (sep.isError()) {
                    return sep.coerce();
                }

                String fieldName = key.unwrap();
                Result<?, ParseError> item = fieldToParser.apply(fieldName).parse(state);
                if (item.isError()) {
                    return item.coerce();
                }

                Object fieldValue = item.unwrap();
                map.put(fieldName, fieldValue);

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

    /**
     * Returns a parser that will consume well-formed JSON values of arbitrary depth.
     *
     * <p>The parser produces results that may be numbers, strings, lists or maps, and the lists and
     * maps may contain numbers, strings lists or maps. The result maps returned by this parser will
     * always have string keys. The numbers returned by this parser will always be doubles.
     */
    public static Parser<Object> valueConsumingParser() {
        return ValueConsumingParser.INSTANCE;
    }

    public static <T> Parser<T> unknownField(String field) {
        return Parsers.error("Unknown field '" + field + "'");
    }

    static boolean isValueBoundary(int character) {
        return Character.isWhitespace(character)
                || switch (character) {
                    case '[', ']', '{', '}', ',', ParseState.EOS -> true;
                    default -> false;
                };
    }
}
