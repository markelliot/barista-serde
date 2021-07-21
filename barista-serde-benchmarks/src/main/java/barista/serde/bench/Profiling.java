package barista.serde.bench;

import barista.serde.runtime.json.JsonReader;
import java.nio.charset.StandardCharsets;

final class Profiling {

    private static final String apacheBuilds = Deserialization.readResource("/apache_builds.json");

    public static void main(String[] args) {
        byte[] bytes = apacheBuilds.getBytes(StandardCharsets.UTF_8);
        for (int i = 0; i < 50; i++) {
            System.out.println(JsonReader.any(bytes).unwrap());
        }
    }
}
