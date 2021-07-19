package barista.serde.runtime.json;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.StringJoiner;
import java.util.function.Function;

public final class Serializers {

    private Serializers() {}

    public static JsonCharSeq serialize(byte value) {
        return new JsonCharSeq(String.valueOf(value));
    }

    public static JsonCharSeq serialize(Byte value) {
        return new JsonCharSeq(String.valueOf(value));
    }

    public static JsonCharSeq serialize(short value) {
        return new JsonCharSeq(String.valueOf(value));
    }

    public static JsonCharSeq serialize(Short value) {
        return new JsonCharSeq(String.valueOf(value));
    }

    public static JsonCharSeq serialize(int value) {
        return new JsonCharSeq(String.valueOf(value));
    }

    public static JsonCharSeq serialize(Integer value) {
        return new JsonCharSeq(String.valueOf(value));
    }

    public static JsonCharSeq serialize(long value) {
        return new JsonCharSeq(String.valueOf(value));
    }

    public static JsonCharSeq serialize(Long value) {
        return new JsonCharSeq(String.valueOf(value));
    }

    public static JsonCharSeq serialize(float value) {
        return new JsonCharSeq(String.valueOf(value));
    }

    public static JsonCharSeq serialize(Float value) {
        return new JsonCharSeq(String.valueOf(value));
    }

    public static JsonCharSeq serialize(double value) {
        return new JsonCharSeq(String.valueOf(value));
    }

    public static JsonCharSeq serialize(Double value) {
        return new JsonCharSeq(String.valueOf(value));
    }

    public static JsonCharSeq serialize(String value) {
        if (value == null) {
            return JsonCharSeq.NULL;
        }
        return new JsonCharSeq("\"" + JsonStrings.escape(value) + "\"");
    }

    public static <T> JsonCharSeq serialize(
            Optional<T> optional, Function<T, JsonCharSeq> serializer) {
        // disallow null optionals to avoid handling 3-states
        Objects.requireNonNull(optional);
        // TODO(markelliot): may want to offer some kind of selection of NULL vs. EMPTY hadnling
        //   because this may be buggy when encountering Collection<Optional<T>> or Map<?,
        // Optional<T>>
        return optional.map(serializer).orElse(JsonCharSeq.EMPTY);
    }

    public static <T> JsonCharSeq serialize(OptionalInt optional) {
        Objects.requireNonNull(optional);
        return optional.isPresent() ? serialize(optional.getAsInt()) : JsonCharSeq.EMPTY;
    }

    public static <T> JsonCharSeq serialize(OptionalLong optional) {
        Objects.requireNonNull(optional);
        return optional.isPresent() ? serialize(optional.getAsLong()) : JsonCharSeq.EMPTY;
    }

    public static <T> JsonCharSeq serialize(OptionalDouble optional) {
        Objects.requireNonNull(optional);
        return optional.isPresent() ? serialize(optional.getAsDouble()) : JsonCharSeq.EMPTY;
    }

    public static <T> JsonCharSeq serialize(
            Collection<T> collection, Function<T, JsonCharSeq> serializer) {
        if (collection == null) {
            return JsonCharSeq.NULL;
        }

        Objects.requireNonNull(serializer);

        StringJoiner sj = new StringJoiner(",", "[", "]");
        for (T item : collection) {
            sj.add(serializer.apply(item));
        }
        return new JsonCharSeq(sj.toString());
    }

    /**
     * Serializes the provided Map using {@code keySerializer} to create keys, which must be valid
     * JSON map keys (typically quoted strings).
     */
    public static <K, V> JsonCharSeq serialize(
            Map<K, V> map,
            Function<K, JsonCharSeq> keySerializer,
            Function<V, JsonCharSeq> valueSerializer) {
        if (map == null) {
            return new JsonCharSeq("null");
        }

        Objects.requireNonNull(keySerializer);
        Objects.requireNonNull(valueSerializer);

        StringJoiner sj = new StringJoiner(",", "{", "}");
        for (Map.Entry<K, V> entry : map.entrySet()) {
            sj.add(
                    keySerializer.apply(entry.getKey())
                            + ":"
                            + valueSerializer.apply(entry.getValue()));
        }
        return new JsonCharSeq(sj.toString());
    }
}
