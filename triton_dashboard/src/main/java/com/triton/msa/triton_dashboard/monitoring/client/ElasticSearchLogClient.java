package com.triton.msa.triton_dashboard.monitoring.client;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import com.triton.msa.triton_dashboard.monitoring.dto.ResourceMetricDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ElasticSearchLogClient {
    private final ElasticsearchClient esClient;

    public List<String> getServices(Long projectId) {
        Instant now = Instant.now();
        Instant past = now.minus(60, ChronoUnit.MINUTES);

        try {
            SearchResponse<Void> response = esClient.search(s -> s
                            .index("project-" + projectId + "-logs-*")
                            .size(0)
                            .query(q -> q
                                    .range(r -> r
                                            .field("@timestamp")
                                            .gte(JsonData.of(past.toString()))
                                    )
                            )
                            .aggregations("services", a -> a
                                    .terms(t -> t
                                            .field("kubernetes.container.name.keyword")
                                            .size(1000)
                                    )
                            ),
                    Void.class
            );

            Aggregate services = response.aggregations().get("services");

            if (services == null || services.sterms() == null) {
                log.warn("Project ID {}에 대한 'services' 집계를 찾을 수 없습니다. 인덱스에 데이터가 없거나 필드 매핑 문제일 수 있습니다.", projectId);
                return Collections.emptyList();
            }
            log.info("Project-{}-logs-* 서비스 조회 완료", projectId);

            return services
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

    public ResourceMetricDto getServiceResourceMetrics(Long projectId, String serviceName, int minutes) {
        Instant now = Instant.now();
        Instant past = now.minus(minutes, ChronoUnit.MINUTES);

        try {
            SearchResponse<Void> response = esClient.search(s -> s
                    .index("project-" + projectId + "-metrics-*")
                    .size(0)
                    .query(q -> q
                            .bool(b -> b
                                    .must(m -> m
                                            .term(t -> t
                                                    .field("kubernetes.deployment.name")
                                                    .value(serviceName)
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
                    )
                    .aggregations("avg_cpu", a -> a.avg(avg -> avg.field("kubernetes.pod.cpu.usage.node.pct")))
                    .aggregations("min_cpu", a -> a.min(min -> min.field("kubernetes.pod.cpu.usage.node.pct")))
                    .aggregations("max_cpu", a -> a.max(max -> max.field("kubernetes.pod.cpu.usage.node.pct")))
                    .aggregations("avg_memory", a -> a.avg(avg -> avg.field("kubernetes.pod.memory.usage.bytes")))
                    .aggregations("min_memory", a -> a.min(min -> min.field("kubernetes.pod.memory.usage.bytes")))
                    .aggregations("max_memory", a -> a.max(max -> max.field("kubernetes.pod.memory.usage.bytes"))),
                    Void.class
            );

            Map<String, Aggregate> aggs = response.aggregations();
            return new ResourceMetricDto(
                    Optional.ofNullable(aggs.get("avg_cpu")).map(a -> a.avg().value()).orElse(0.0),
                    Optional.ofNullable(aggs.get("min_cpu")).map(a -> a.min().value()).orElse(0.0),
                    Optional.ofNullable(aggs.get("max_cpu")).map(a -> a.max().value()).orElse(0.0),
                    Optional.ofNullable(aggs.get("avg_memory")).map(a -> a.avg().value()).orElse(0.0),
                    Optional.ofNullable(aggs.get("min_memory")).map(a -> a.min().value()).orElse(0.0),
                    Optional.ofNullable(aggs.get("max_memory")).map(a -> a.max().value()).orElse(0.0)
            );
        }
        catch (IOException e) {
            log.error("Failed to analyze metrics by Elasticsearch for service: {}", serviceName, e);
            return ResourceMetricDto.getEmpty();
        }
    }
}
