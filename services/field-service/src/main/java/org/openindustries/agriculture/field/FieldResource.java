package org.openindustries.agriculture.field;

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

@Path("/api/v1/fields")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Fields", description = "Field registry: land units, crops and seasons (ADAPT-aligned shapes).")
public class FieldResource {

    @Inject
    FieldService service;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Create a field")
    public Response create(@Valid CreateFieldRequest request) {
        return Response.status(Response.Status.CREATED).entity(service.create(request)).build();
    }

    @GET
    @Operation(summary = "List all")
    public List<Field> list() {
        return service.list();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Get one by id")
    public Field get(@PathParam("id") UUID id) {
        return service.get(id);
    }

    @POST
    @Path("/{id}/fallow")
    @Operation(summary = "Fallow this field (lifecycle transition, no body)")
    public Field fallow(@PathParam("id") UUID id) {
        return service.fallow(id);
    }

    @POST
    @Path("/{id}/reactivate")
    @Operation(summary = "Reactivate this field (lifecycle transition, no body)")
    public Field reactivate(@PathParam("id") UUID id) {
        return service.reactivate(id);
    }
}
