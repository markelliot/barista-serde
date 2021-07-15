package barista.serde.runtime;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public final class DateTimeSerializers {
    private DateTimeSerializers() {}

    public static JsonCharSeq serialize(Instant instant) {
        return serialize(instant, DateTimeFormatter.ISO_DATE_TIME);
    }

    public static JsonCharSeq serialize(Instant instant, DateTimeFormatter formatter) {
        return Serializers.serialize(formatter.format(instant));
    }

    public static JsonCharSeq serialize(LocalDate value) {
        return serialize(value, DateTimeFormatter.ISO_LOCAL_DATE);
    }

    public static JsonCharSeq serialize(LocalDate value, DateTimeFormatter formatter) {
        return Serializers.serialize(formatter.format(value));
    }
}
