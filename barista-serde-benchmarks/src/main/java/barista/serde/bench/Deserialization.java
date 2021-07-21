package barista.serde.bench;

import barista.serde.runtime.json.JsonParsers;
import barista.serde.runtime.json.JsonReader;
import barista.serde.runtime.parsec.ParseState;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;
import com.jakewharton.nopen.annotation.Open;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

@Open
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
public class Deserialization {
    static String readResource(String resourcePath) {
        try {
            return new String(
                    Deserialization.class.getResourceAsStream(resourcePath).readAllBytes(),
                    StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private final String apacheBuilds = readResource("/apache_builds.json");
    private final String twitter = readResource("/twitter.json");

    private final byte[] apacheBuilds_bytes = apacheBuilds.getBytes(StandardCharsets.UTF_8);
    private final byte[] twitter_bytes = twitter.getBytes(StandardCharsets.UTF_8);

    @Benchmark
    public Object barista_jsonReader_apacheBuilds() {
        return JsonReader.any(apacheBuilds_bytes);
    }

    @Benchmark
    public Object barista_jsonReader_twitter() {
        return JsonReader.any(twitter_bytes);
    }

//    @Benchmark
//    public Object barista_jsonParsers_apacheBuilds() {
//        return JsonParsers.any().parse(ParseState.of(apacheBuilds)).unwrap();
//    }
//
//    @Benchmark
//    public Object barista_jsonParsers_twitter() {
//        return JsonParsers.any().parse(ParseState.of(twitter)).unwrap();
//    }

    private final ObjectMapper mapper =
            new ObjectMapper()
                    .registerModule(new GuavaModule())
                    .registerModule(new Jdk8Module())
                    .registerModule(new JavaTimeModule())
                    .registerModule(new AfterburnerModule())
                    .setSerializationInclusion(JsonInclude.Include.NON_ABSENT)
                    .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    @Benchmark
    public Object jackson_apacheBuilds() throws JsonProcessingException {
        return mapper.readValue(apacheBuilds, new TypeReference<Object>() {});
    }

    @Benchmark
    public Object jackson_readGenericObject_twitter() throws JsonProcessingException {
        return mapper.readValue(twitter, new TypeReference<Object>() {});
    }

    public static void main(String[] args) throws RunnerException {
        Options opt =
                new OptionsBuilder()
                        // .include(Deserialization.class.getSimpleName())
                        .include("twitter")
                        .forks(1)
                        .build();
        new Runner(opt).run();
    }
}
