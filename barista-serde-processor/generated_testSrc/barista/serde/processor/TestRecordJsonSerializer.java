package barista.serde.processor;

import barista.serde.runtime.JsonCharSeq;
import barista.serde.runtime.Serializers;

import java.util.StringJoiner;

public final class TestRecordJsonSerializer {
    private TestRecordJsonSerializer() {}

    public static JsonCharSeq serialize(JsonSerializerGeneratorTests.TestRecord value) {
        StringJoiner sj = new StringJoiner(",", "{", "}");
        if (value.testRecordField() != null) {
            sj.add(
                    "\"testRecordField\":"
                            + Serializers.serialize(
                                    value.testRecordField(), v0 -> Serializers.serialize(v0)));
        }
        return new JsonCharSeq(sj.toString());
    }
}
