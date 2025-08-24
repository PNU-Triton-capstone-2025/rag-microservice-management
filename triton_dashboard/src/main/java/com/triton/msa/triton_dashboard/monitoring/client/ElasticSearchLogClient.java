package com.triton.msa.triton_dashboard.monitoring.client;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ElasticSearchLogClient {
    private final ElasticsearchClient esClient;

    public List<String> getServices(Long projectId) {
        try {
            SearchResponse<Void> response = esClient.search(s -> s
                            .index("project-" + projectId + "-logs-*")
                            .size(0)
                            .aggregations("services", a -> a
                                    .terms(t -> t
                                            .field("kubernetes.container.name.keyword")
                                    )
                            ),
                    Void.class
            );

            return response.aggregations()
                    .get("services")
                    .sterms()
                    .buckets()
                    .array()
                    .stream()
                    .map(bucket -> bucket.key().stringValue())
                    .toList();
        }
        catch (IOException ex) {
            log.error("Failed to analyze services due to Elasticsearch communication error", ex);
            return Collections.emptyList();
        }
    }

    public List<String> getRecentErrorLogs(Long projectId, String serviceName, int minutes) {
        Instant now = Instant.now();
        Instant past = now.minus(minutes, ChronoUnit.MINUTES);

        Query query = Query.of(q -> q
                .bool(b -> b
                        .must(m -> m
                                .term(t -> t
                                        .field("kubernetes.container.name.keyword")
                                        .value(serviceName)
                                )
                        )
                        .must(m -> m
                                .match(t -> t
                                        .field("log_level")
                                        .query("ERROR")
                                )
                        )
                        .filter(f -> f
                                .range(r -> r
                                        .field("@timestamp")
                                        .gte(JsonData.of(past.toString()))
                                        .lte(JsonData.of(now.toString()))
                                )
                        )
                )
        );

        try {
            SearchResponse<Map> response = esClient.search(s -> s
                            .index("project-" + projectId + "-logs-*")
                            .query(query)
                            .size(100),
                    Map.class
            );

            return response
                    .hits()
                    .hits()
                    .stream()
                    .map(Hit::source)
                    .filter(source -> source != null && source.containsKey("log_message"))
                    .map(source -> source.get("log_message").toString())
                    .toList();
        }
        catch (IOException ex) {
            log.error("Failed to analyze logs due to Elasticsearch communication error", ex);
            return Collections.emptyList();
        }
    }
}
