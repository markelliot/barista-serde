package barista.serde.processor;

import static org.assertj.core.api.Assertions.assertThat;

import barista.serde.annotations.SerDe;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

final class JsonSerializerGeneratorTests {

    @SerDe.Json
    public record TestRecord(Optional<String> testRecordField) {}

    @SerDe.Json
    public record OtherRecord(Map<String, TestRecord> otherRecordField) {}

    @Test
    void testOtherRecordSerializationOutput() {
        assertThat(
                        OtherRecordJsonSerializer.serialize(
                                new OtherRecord(Map.of("1", new TestRecord(Optional.of("foo"))))))
                .isEqualTo("{\"otherRecordField\":{\"1\":{\"testRecordField\":\"foo\"}}}");
    }
}
