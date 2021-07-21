package barista.serde.bench;

import barista.serde.annotations.SerDe;
import java.util.List;

@SerDe.Json
public record ApacheBuilds(
        String mode,
        String nodeDescription,
        String nodeName,
        int numExecutors,
        String description,
        List<Job> jobs,
        View primaryView,
        boolean quietingDown,
        int slaveAgentPort,
        boolean useCrumbs,
        boolean useSecurity,
        List<View> views) {

    @SerDe.Json
    public record Job(String name, String url, String color) {}

    @SerDe.Json
    public record View(String name, String url) {}
}
