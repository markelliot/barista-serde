package barista.serde.runtime.json;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

final class JsonReaderTests {
    @Test
    void basic() {
        // JsonReader.any("\"test\"".getBytes(StandardCharsets.UTF_8));
        System.out.println(JsonReader.any("[{\"a\": .1, \"b\": 2}]".getBytes(StandardCharsets.UTF_8)).unwrap());
    }

}
