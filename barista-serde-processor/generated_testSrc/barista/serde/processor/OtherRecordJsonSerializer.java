package barista.serde.processor;

import barista.serde.runtime.JsonCharSeq;
import barista.serde.runtime.Serializers;
import java.util.StringJoiner;

public final class OtherRecordJsonSerializer {
  private OtherRecordJsonSerializer() {
  }

  public static JsonCharSeq serialize(JsonSerializerGeneratorTests.OtherRecord value) {
    StringJoiner sj = new StringJoiner(",", "{", "}");
    if (value.otherRecordField() != null) {
      sj.add("\"otherRecordField\":" + Serializers.serialize(value.otherRecordField(), k0 -> Serializers.serialize(k0), v0 -> TestRecordJsonSerializer.serialize(v0)));
    }
    return new JsonCharSeq(sj.toString());
  }
}
