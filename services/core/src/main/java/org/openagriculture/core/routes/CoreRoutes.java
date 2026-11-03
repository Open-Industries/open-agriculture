package org.openagriculture.core.routes;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.openagriculture.core.model.CoreResource;
import org.openagriculture.core.service.BadRequestException;
import org.openagriculture.core.service.CoreService;
import org.openagriculture.core.service.JsonSupport;
import org.openagriculture.core.service.NotFoundException;

import java.util.List;

/**
 * Thin HTTP adapter over {@link CoreService}. One generic processor serves
 * all seven entity types so the contract surface stays uniform:
 *
 * <ul>
 *   <li>POST /v1/{entity} -&gt; 201 with the created resource</li>
 *   <li>GET /v1/{entity}/{id} -&gt; 200, or 404 when missing or owned by
 *       another tenant</li>
 *   <li>GET /v1/{entity}?status=X -&gt; 200 with the tenant's list</li>
 * </ul>
 *
 * Required headers: X-Correlation-ID (UUID), X-Tenant-ID.
 * Optional headers: Idempotency-Key (POST), X-Causation-ID, X-Purpose,
 * X-Data-Classification. Errors are JSON bodies, never stack traces.
 *
 * <p>Quarkus runtime: this RouteBuilder is a CDI bean discovered by
 * camel-quarkus, and the REST DSL binds to the Quarkus HTTP server via
 * platform-http (backed by Vert.x). Quarkus owns the port
 * (see src/main/resources/application.properties); the routes do not
 * resolve ports themselves.</p>
 */
@ApplicationScoped
public class CoreRoutes extends RouteBuilder {

    private final CoreService service = new CoreService();

    public CoreService service() {
        return service;
    }

    @Override
    public void configure() {
        restConfiguration()
                .component("platform-http");

        rest("/v1")
                .post("/{entity}").to("direct:create")
                .get("/{entity}/{id}").to("direct:get")
                .get("/{entity}").to("direct:list");

        // Unauthenticated health endpoint for container probes (Knative,
        // Kubernetes). It carries no domain data and requires no envelope
        // headers, unlike every /v1 operation.
        rest("/health")
                .get().to("direct:health");

        from("direct:create").process(new CreateProcessor());
        from("direct:get").process(new GetProcessor());
        from("direct:list").process(new ListProcessor());
        from("direct:health").process(exchange -> writeJson(exchange, 200,
                JsonSupport.MAPPER.createObjectNode().put("status", "UP")));
    }

    private String header(Exchange exchange, String name) {
        return exchange.getMessage().getHeader(name, String.class);
    }

    private void writeJson(Exchange exchange, int status, Object body) throws Exception {
        ObjectMapper mapper = JsonSupport.MAPPER;
        exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, status);
        exchange.getMessage().setHeader(Exchange.CONTENT_TYPE, "application/json");
        exchange.getMessage().setBody(mapper.writeValueAsString(body));
    }

    private class CreateProcessor implements Processor {
        @Override
        public void process(Exchange exchange) throws Exception {
            try {
                String entity = header(exchange, "entity");
                String body = exchange.getMessage().getBody(String.class);
                JsonNode node = body == null || body.isBlank()
                        ? JsonSupport.MAPPER.createObjectNode()
                        : JsonSupport.MAPPER.readTree(body);
                CoreResource created = service.create(
                        entity,
                        header(exchange, "X-Tenant-ID"),
                        header(exchange, "X-Correlation-ID"),
                        header(exchange, "X-Causation-ID"),
                        header(exchange, "X-Purpose"),
                        header(exchange, "X-Data-Classification"),
                        header(exchange, "Idempotency-Key"),
                        node);
                writeJson(exchange, 201, created);
            } catch (BadRequestException e) {
                writeErrorNode(exchange, 400, e.getMessage());
            }
        }
    }

    private class GetProcessor implements Processor {
        @Override
        public void process(Exchange exchange) throws Exception {
            try {
                CoreResource resource = service.get(
                        header(exchange, "entity"),
                        header(exchange, "X-Tenant-ID"),
                        header(exchange, "id"));
                writeJson(exchange, 200, resource);
            } catch (BadRequestException e) {
                writeErrorNode(exchange, 400, e.getMessage());
            } catch (NotFoundException e) {
                writeErrorNode(exchange, 404, e.getMessage());
            }
        }
    }

    private class ListProcessor implements Processor {
        @Override
        public void process(Exchange exchange) throws Exception {
            try {
                String query = header(exchange, Exchange.HTTP_QUERY);
                String status = null;
                if (query != null) {
                    for (String part : query.split("&")) {
                        if (part.startsWith("status=")) {
                            status = part.substring("status=".length());
                        }
                    }
                }
                List<CoreResource> resources = service.list(
                        header(exchange, "entity"),
                        header(exchange, "X-Tenant-ID"),
                        status);
                writeJson(exchange, 200, resources);
            } catch (BadRequestException e) {
                writeErrorNode(exchange, 400, e.getMessage());
            }
        }
    }

    private void writeErrorNode(Exchange exchange, int status, String message) throws Exception {
        ObjectMapper mapper = JsonSupport.MAPPER;
        exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, status);
        exchange.getMessage().setHeader(Exchange.CONTENT_TYPE, "application/json");
        exchange.getMessage().setBody(mapper.writeValueAsString(
                mapper.createObjectNode().put("error", message)));
    }
}
