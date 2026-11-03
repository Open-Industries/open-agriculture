package org.openindustries.agriculture.agronomy;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;
import java.util.UUID;

@Path("/api/v1/agronomy-rules")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Agronomy Rules", description = "Decision rules binding crops to water and weather envelopes, with reasoned decisions.")
public class AgronomyRuleResource {

    @Inject
    AgronomyRuleService service;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Create a rule (starts as DRAFT)")
    public Response create(@Valid CreateAgronomyRuleRequest request) {
        return Response.status(Response.Status.CREATED).entity(service.create(request)).build();
    }

    @GET
    @Operation(summary = "List rules")
    public List<AgronomyRule> list() {
        return service.list();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Get one rule")
    public AgronomyRule get(@PathParam("id") UUID id) {
        return service.get(id);
    }

    @POST
    @Path("/{id}/activate")
    @Operation(summary = "Activate a rule (lifecycle transition, no body)")
    public AgronomyRule activate(@PathParam("id") UUID id) {
        return service.activate(id);
    }

    @POST
    @Path("/evaluate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Evaluate whether an active rule covers a request")
    public Evaluation evaluate(@Valid EvaluateRequest request) {
        return service.evaluate(request);
    }
}
