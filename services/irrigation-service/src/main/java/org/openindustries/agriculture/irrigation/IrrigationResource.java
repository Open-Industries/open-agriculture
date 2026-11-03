package org.openindustries.agriculture.irrigation;

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

@Path("/api/v1/water-budgets")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Water Budgets", description = "Seasonal water budgets per field and the irrigation draws against them.")
public class IrrigationResource {

    @Inject
    IrrigationService service;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Open a waterbudget")
    public Response open(@Valid OpenWaterBudgetRequest request) {
        return Response.status(Response.Status.CREATED).entity(service.open(request)).build();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Get one by id")
    public WaterBudget get(@PathParam("id") UUID id) {
        return service.get(id);
    }

    @POST
    @Path("/{id}/uses")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Draw from the account (rejects if it would overdraw, 422)")
    public WaterUse consume(@PathParam("id") UUID id, @Valid CreateWaterUseRequest request) {
        return service.consume(id, request);
    }

    @GET
    @Path("/{id}/uses")
    @Operation(summary = "List consumption records of an account")
    public List<WaterUse> records(@PathParam("id") UUID id) {
        return service.recordsOf(id);
    }
}
