package barista.serde.runtime.json;

import barista.serde.runtime.parsec.Empty;
import barista.serde.runtime.parsec.ParseError;
import barista.serde.runtime.parsec.ParseState;
import barista.serde.runtime.parsec.Parser;
import barista.serde.runtime.parsec.Parsers;
import io.github.markelliot.result.Result;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.function.Function;
import java.util.function.Supplier;

public final class JsonParsers {
    private JsonParsers() {}

    public static Parser<String> string() {
        return QuotedStringParser.INSTANCE;
    }

    public static Parser<Boolean> booleanParser() {
        return BooleanParser.INSTANCE;
    }

    public static Parser<Character> charParser() {
        return QuotedCharParser.INSTANCE;
    }

    public static Parser<Byte> byteParser() {
        return WholeNumberParser.BYTE;
    }

    public static Parser<Short> shortParser() {
        return WholeNumberParser.SHORT;
    }

    public static Parser<Integer> integerParser() {
        return WholeNumberParser.INT;
    }

    public static Parser<Long> longParser() {
        return WholeNumberParser.LONG;
    }

    public static Parser<Float> floatParser() {
        return FloatParser.INSTANCE;
    }

    public static Parser<Double> doubleParser() {
        return DoubleParser.INSTANCE;
    }

    public static Parser<Empty> nullParser() {
        return NullParser.INSTANCE;
    }

    public static <T> Parser<Optional<T>> optional(Parser<T> itemParser) {
        return OptionalParser.of(itemParser);
    }

    public static Parser<OptionalInt> optionalInt() {
        return OptionalParser.INT;
    }

    public static Parser<OptionalLong> optionalLong() {
        return OptionalParser.LONG;
    }

    public static Parser<OptionalDouble> optionalDouble() {
        return OptionalParser.DOUBLE;
    }

    static Parser<Empty> keyValueSeparator() {
        return KeyValueSeparatorParser.INSTANCE;
    }

    public static <T, C extends Collection<T>> Parser<C> collection(
            Parser<T> itemParser, Supplier<C> collectionFactory) {
        return Parsers.between(
                Parsers.expect('['),
                collectionInternal(itemParser, collectionFactory),
                Parsers.expect(']'),
                collectionFactory);
    }

    private static <T, C extends Collection<T>> Parser<C> collectionInternal(
            Parser<T> itemParser, Supplier<C> collectionFactory) {
        return state -> {
            C collection = collectionFactory.get();
            while (!state.isEndOfStream()) {
                state.skipWhitespace();

                Result<T, ParseError> item = itemParser.parse(state);
                if (item.isError()) {
                    return item.coerce();
                }
                item.mapResult(collection::add);

                state.skipWhitespace();
                if (state.current() == ',') {
                    state.next(); // consume ','
                } else {
                    break;
                }
            }
            return Result.ok(collection);
        };
    }

    public static <K, V> Parser<Map<K, V>> map(
            Function<String, K> keyFn, Parser<V> itemParser, Supplier<Map<K, V>> mapFactory) {
        return Parsers.between(
                Parsers.expect('{'),
                mapInternal(keyFn, ignored -> itemParser, mapFactory),
                Parsers.expect('}'),
                mapFactory);
    }

    private static <K, V> Parser<Map<K, V>> mapInternal(
            Function<String, K> keyFn,
            Function<K, Parser<V>> itemParser,
            Supplier<Map<K, V>> mapFactory) {
        return new MapInternalParser<>(keyFn, itemParser, mapFactory);
    }

    /**
     * Returns a parser that parses field values using the parsers returned by {@code fieldToParser}
     * and that takes the parsing results presented as a {@code Map<String, Object>} and creates the
     * desired type using {@code map}.
     *
     * <p>See {@link #objectAsMap(Function)} for additional details on authoring {@code
     * fieldToParser} functions.
     */
    // TODO(markelliot): this is one of the easiest ways to implement an object parser, but since
    //  we can generate code, it's possible we might be able to do better. A couple of variants to
    //  consider:
    //  1. Generate objectInternal() for each object we wish to parse.
    //  2. Accept a BiConsumer<String, Object> rather than creating a HashMap
    //  3. Produce a List<FieldRecord> for record FieldRecord(String field, Object value)
    public static <T> Parser<T> object(
            Function<String, Parser<?>> fieldToParser, Function<Map<String, Object>, T> map) {
        return state -> objectAsMap(fieldToParser).parse(state).mapResult(map);
    }

    /**
     * Returns a parser that parses field values using the parsers returned by {@code fieldToParser}
     * and produces a {@code Map<String, Object>} with keys corresponding to encountered keys and
     * values resulting from running the specified field value parsers.
     *
     * <p>Note: callers must supply a {@code fieldToParser} function that handles <em>any</em>
     * potential key. A common pattern is to produce a function that handles each field as a switch
     * statement and then also provides a default handler. For instance, to ignore unknown fields, a
     * caller might produce a function:
     *
     * <pre>{@code
     * field -> switch (field) {
     *     case 'known' -> JsonParsers.string();
     *     default -> JsonParsers.any();
     * }
     * }</pre>
     *
     * <p>Similarly, to produce a parser that errors when encountering unknown fields:
     *
     * <pre>{@code
     * field -> switch (field) {
     *     case 'known' -> JsonParsers.string();
     *     default -> JsonParsers.unknownField(field);
     * }
     * }</pre>
     */
    public static Parser<Map<String, Object>> objectAsMap(
            Function<String, Parser<?>> fieldToParser) {
        return Parsers.between(
                Parsers.expect('{'),
                mapInternal(
                        Function.identity(),
                        field -> Parsers.composeResult(fieldToParser.apply(field), r -> (Object) r),
                        HashMap::new),
                Parsers.expect('}'),
                Map::of);
    }

    /**
     * Returns a parser that will parse well-formed JSON values of arbitrary depth.
     *
     * <p>This parser produces values (booleans, numbers, strings or nulls), collections, or maps,
     * with the following rules:
     *
     * <ul>
     *   <li>Numbers always map to {@link Double}
     *   <li>Nulls always map to {@link Optional#empty()}
     *   <li>Maps will always have {@link String} keys
     *   <li>Maps will have an iteration order corresponding to the appearance of keys in the
     *       underlying JSON.
     *   <li>Collections will always be an {@link java.util.List}
     *   <li>When map or collection values are maps or collections, the values are parsed using this
     *       parser
     * </ul>
     */
    public static Parser<Object> any() {
        return AnyValueParser.INSTANCE;
    }

    /** Returns a parser that always produces an error indicating an unknown field key. */
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
