package barista.serde.runtime;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Set;
import org.junit.jupiter.api.Test;

final class SerializersTests {

    @Test
    void testSerializePrimitiveByte() {
        assertThat(Serializers.serialize((byte) -1).value()).isEqualTo("-1");
        assertThat(Serializers.serialize((byte) 0).value()).isEqualTo("0");
        assertThat(Serializers.serialize((byte) 1).value()).isEqualTo("1");
    }

    @Test
    void testSerializeObjectByte() {
        assertThat(Serializers.serialize((Byte) (byte) -1).value()).isEqualTo("-1");
        assertThat(Serializers.serialize((Byte) (byte) 0).value()).isEqualTo("0");
        assertThat(Serializers.serialize((Byte) (byte) 1).value()).isEqualTo("1");
        assertThat(Serializers.serialize((Byte) null).value()).isEqualTo("null");
    }

    @Test
    void testSerializePrimitiveShort() {
        assertThat(Serializers.serialize((short) -1).value()).isEqualTo("-1");
        assertThat(Serializers.serialize((short) 0).value()).isEqualTo("0");
        assertThat(Serializers.serialize((short) 1).value()).isEqualTo("1");
    }

    @Test
    void testSerializeObjectShort() {
        assertThat(Serializers.serialize((Short) (short) -1).value()).isEqualTo("-1");
        assertThat(Serializers.serialize((Short) (short) 0).value()).isEqualTo("0");
        assertThat(Serializers.serialize((Short) (short) 1).value()).isEqualTo("1");
        assertThat(Serializers.serialize((Short) null).value()).isEqualTo("null");
    }

    @Test
    void testSerializePrimitiveInt() {
        assertThat(Serializers.serialize(-1).value()).isEqualTo("-1");
        assertThat(Serializers.serialize(0).value()).isEqualTo("0");
        assertThat(Serializers.serialize(1).value()).isEqualTo("1");
    }

    @Test
    void testSerializeObjectInt() {
        assertThat(Serializers.serialize((Integer) (-1)).value()).isEqualTo("-1");
        assertThat(Serializers.serialize((Integer) 0).value()).isEqualTo("0");
        assertThat(Serializers.serialize((Integer) 1).value()).isEqualTo("1");
        assertThat(Serializers.serialize((Integer) null).value()).isEqualTo("null");
    }

    @Test
    void testSerializePrimitiveLong() {
        assertThat(Serializers.serialize(-1L).value()).isEqualTo("-1");
        assertThat(Serializers.serialize(0L).value()).isEqualTo("0");
        assertThat(Serializers.serialize(1L).value()).isEqualTo("1");
    }

    @Test
    void testSerializeObjectLong() {
        assertThat(Serializers.serialize((Long) (-1L)).value()).isEqualTo("-1");
        assertThat(Serializers.serialize((Long) 0L).value()).isEqualTo("0");
        assertThat(Serializers.serialize((Long) 1L).value()).isEqualTo("1");
        assertThat(Serializers.serialize((Long) null).value()).isEqualTo("null");
    }

    @Test
    void testSerializePrimitiveFloat() {
        assertThat(Serializers.serialize(-1f).value()).isEqualTo("-1.0");
        assertThat(Serializers.serialize(-1.5f).value()).isEqualTo("-1.5");
        assertThat(Serializers.serialize(0f).value()).isEqualTo("0.0");
        assertThat(Serializers.serialize(0.5f).value()).isEqualTo("0.5");
        assertThat(Serializers.serialize(1f).value()).isEqualTo("1.0");
        assertThat(Serializers.serialize(1.5f).value()).isEqualTo("1.5");

        // TODO(markelliot): make some documented decisions about what this library will produce for
        //   more exotic float values
        assertThat(Serializers.serialize(-0f).value()).isEqualTo("-0.0");
        assertThat(Serializers.serialize(Float.NaN).value()).isEqualTo("NaN");
        assertThat(Serializers.serialize(Float.NEGATIVE_INFINITY).value()).isEqualTo("-Infinity");
        assertThat(Serializers.serialize(Float.POSITIVE_INFINITY).value()).isEqualTo("Infinity");
    }

    @Test
    void testSerializeObjectFloat() {
        assertThat(Serializers.serialize((Float) (-1f)).value()).isEqualTo("-1.0");
        assertThat(Serializers.serialize((Float) (-1.5f)).value()).isEqualTo("-1.5");
        assertThat(Serializers.serialize((Float) 0f).value()).isEqualTo("0.0");
        assertThat(Serializers.serialize((Float) 0.5f).value()).isEqualTo("0.5");
        assertThat(Serializers.serialize((Float) 1f).value()).isEqualTo("1.0");
        assertThat(Serializers.serialize((Float) 1.5f).value()).isEqualTo("1.5");
        assertThat(Serializers.serialize((Float) null).value()).isEqualTo("null");
    }

    @Test
    void testSerializePrimitiveDouble() {
        assertThat(Serializers.serialize(-1.0).value()).isEqualTo("-1.0");
        assertThat(Serializers.serialize(-1.5).value()).isEqualTo("-1.5");
        assertThat(Serializers.serialize(0.0).value()).isEqualTo("0.0");
        assertThat(Serializers.serialize(0.5).value()).isEqualTo("0.5");
        assertThat(Serializers.serialize(1.0).value()).isEqualTo("1.0");
        assertThat(Serializers.serialize(1.5).value()).isEqualTo("1.5");

        // TODO(markelliot): make some documented decisions about what this library will produce for
        //   more exotic float values
        assertThat(Serializers.serialize(-0.0).value()).isEqualTo("-0.0");
        assertThat(Serializers.serialize(Double.NaN).value()).isEqualTo("NaN");
        assertThat(Serializers.serialize(Double.NEGATIVE_INFINITY).value()).isEqualTo("-Infinity");
        assertThat(Serializers.serialize(Double.POSITIVE_INFINITY).value()).isEqualTo("Infinity");
    }

    @Test
    void testSerializeObjectDouble() {
        assertThat(Serializers.serialize((Double) (-1.0)).value()).isEqualTo("-1.0");
        assertThat(Serializers.serialize((Double) (-1.5)).value()).isEqualTo("-1.5");
        assertThat(Serializers.serialize((Double) 0.0).value()).isEqualTo("0.0");
        assertThat(Serializers.serialize((Double) 0.5).value()).isEqualTo("0.5");
        assertThat(Serializers.serialize((Double) 1.0).value()).isEqualTo("1.0");
        assertThat(Serializers.serialize((Double) 1.5).value()).isEqualTo("1.5");
    }

    @Test
    void testSerializeString() {
        assertThat(Serializers.serialize("").value()).isEqualTo("\"\"");
        assertThat(Serializers.serialize("test").value()).isEqualTo("\"test\"");
        assertThat(Serializers.serialize((String) null).value()).isEqualTo("null");
    }

    @Test
    void testSerializeOptional() {
        assertThat(Serializers.serialize(Optional.of("test"), Serializers::serialize).value())
                .isEqualTo("\"test\"");
        assertThat(Serializers.serialize(Optional.of(1.0), Serializers::serialize).value())
                .isEqualTo("1.0");
        assertThat(
                        Serializers.serialize(
                                        Optional.empty(),
                                        _err -> {
                                            throw new IllegalStateException();
                                        })
                                .value())
                .isEqualTo("");
    }

    @Test
    void testSerializeOptionalInt() {
        assertThat(Serializers.serialize(OptionalInt.of(1)).value()).isEqualTo("1");
        assertThat(
                        Serializers.serialize(
                                        Optional.empty(),
                                        _err -> {
                                            throw new IllegalStateException();
                                        })
                                .value())
                .isEqualTo("");
    }

    @Test
    void testSerializeOptionalLong() {
        assertThat(Serializers.serialize(OptionalLong.of(1L)).value()).isEqualTo("1");
        assertThat(
                        Serializers.serialize(
                                        Optional.empty(),
                                        _err -> {
                                            throw new IllegalStateException();
                                        })
                                .value())
                .isEqualTo("");
    }

    @Test
    void testSerializeOptionalDouble() {
        assertThat(Serializers.serialize(OptionalDouble.of(1.0)).value()).isEqualTo("1.0");
        assertThat(
                        Serializers.serialize(
                                        Optional.empty(),
                                        _err -> {
                                            throw new IllegalStateException();
                                        })
                                .value())
                .isEqualTo("");
    }

    @Test
    void testSerializeList() {
        assertThat(Serializers.serialize(List.of("a", "b", "c"), Serializers::serialize).value())
                .isEqualTo("[\"a\",\"b\",\"c\"]");
        assertThat(Serializers.serialize(Set.of("a"), Serializers::serialize).value())
                .isEqualTo("[\"a\"]");
        assertThat(Serializers.serialize(List.<String>of(), Serializers::serialize).value())
                .isEqualTo("[]");
    }

    @Test
    void testSerializeMap() {
        assertThat(
                        Serializers.serialize(
                                        ImmutableMap.of("a", 1, "b", 2),
                                        Serializers::serialize,
                                        Serializers::serialize)
                                .value())
                .isEqualTo("{\"a\":1,\"b\":2}");
        assertThat(
                        Serializers.serialize(
                                        ImmutableMap.of("a", 1),
                                        Serializers::serialize,
                                        Serializers::serialize)
                                .value())
                .isEqualTo("{\"a\":1}");
        assertThat(
                        Serializers.serialize(
                                        Map.<String, Integer>of(),
                                        Serializers::serialize,
                                        Serializers::serialize)
                                .value())
                .isEqualTo("{}");
    }
}
