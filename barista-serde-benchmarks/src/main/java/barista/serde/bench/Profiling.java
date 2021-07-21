package barista.serde.bench;

import barista.serde.runtime.json.JsonParsers;
import barista.serde.runtime.parsec.ParseState;

final class Profiling {

    private static final String apacheBuilds = Deserialization.readResource("/apache_builds.json");

    public static void main(String[] args) {
        for (int i = 0; i < 1000; i++) {
            System.out.println(JsonParsers.any().parse(ParseState.of(apacheBuilds)).unwrap());
        }
    }
}
