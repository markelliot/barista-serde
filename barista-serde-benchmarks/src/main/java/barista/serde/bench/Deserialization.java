package barista.serde.bench;

import barista.serde.runtime.json.JsonParsers;
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
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
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

    @Benchmark
    public Object barista_readGenericObject_apacheBuilds() {
        return JsonParsers.any().parse(ParseState.of(apacheBuilds)).unwrap();
    }

    private final ObjectMapper mapper =
            new ObjectMapper()
                    .registerModule(new GuavaModule())
                    .registerModule(new Jdk8Module())
                    .registerModule(new JavaTimeModule())
                    .registerModule(new AfterburnerModule())
                    .setSerializationInclusion(JsonInclude.Include.NON_ABSENT)
                    .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    @Benchmark
    public Object jackson_readGenericObject_apacheBuilds() throws JsonProcessingException {
        return mapper.readValue(apacheBuilds, new TypeReference<Map<String, Object>>() {});
    }

    public static void main(String[] args) throws RunnerException {
        Options opt =
                new OptionsBuilder()
                        .include(Deserialization.class.getSimpleName())
                        .forks(1)
                        .build();
        new Runner(opt).run();
    }
}
