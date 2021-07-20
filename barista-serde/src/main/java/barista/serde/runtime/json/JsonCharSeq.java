package barista.serde.runtime.json;

import java.util.Objects;
import java.util.stream.IntStream;

/** An alias of {@link CharSequence} representing valid, raw JSON. */
public record JsonCharSeq(CharSequence value) implements CharSequence {
    public static final JsonCharSeq NULL = new JsonCharSeq("null");
    public static final JsonCharSeq EMPTY = new JsonCharSeq("");
    public static final JsonCharSeq TRUE = new JsonCharSeq("true");
    public static final JsonCharSeq FALSE = new JsonCharSeq("false");

    @Override
    public int length() {
        return value.length();
    }

    @Override
    public char charAt(int index) {
        return value.charAt(index);
    }

    @Override
    public boolean isEmpty() {
        return value.isEmpty();
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return value.subSequence(start, end);
    }

    @Override
    public IntStream chars() {
        return value.chars();
    }

    @Override
    public IntStream codePoints() {
        return value.codePoints();
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof CharSequence other) && Objects.equals(value, other);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
