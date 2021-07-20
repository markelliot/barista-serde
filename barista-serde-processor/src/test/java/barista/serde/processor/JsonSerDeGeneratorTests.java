package barista.serde.processor;

import static org.assertj.core.api.Assertions.assertThat;

import barista.serde.annotations.SerDe;
import barista.serde.runtime.json.JsonCharSeq;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

final class JsonSerDeGeneratorTests {

    @SerDe.Json
    public record TestRecord(Optional<String> testRecordField) {}

    @SerDe.Json
    public record OtherRecord(Map<String, TestRecord> otherRecordField) {}

    @Test
    void testOtherRecordSerializationOutput() {
        assertThat(
                        OtherRecordJsonSerDe.serialize(
                                new OtherRecord(Map.of("1", new TestRecord(Optional.of("foo"))))))
                .isEqualTo("{\"otherRecordField\":{\"1\":{\"testRecordField\":\"foo\"}}}");
    }

    @Test
    void testOtherRecordDeserializes() throws Exception {
        assertThat(
                        OtherRecordJsonSerDe.deserialize(
                                        new JsonCharSeq(
                                                "{\"otherRecordField\":{\"1\":{\"testRecordField\":\"foo\"}}}"))
                                .orElseThrow())
                .isEqualTo(
                        new OtherRecord(ImmutableMap.of("1", new TestRecord(Optional.of("foo")))));
    }
}
