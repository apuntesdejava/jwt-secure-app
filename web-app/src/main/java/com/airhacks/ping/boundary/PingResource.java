package com.airhacks.ping.boundary;

import java.security.Principal;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import org.eclipse.microprofile.jwt.JsonWebToken;

/**
 *
 * @author airhacks.com
 */
@Path("ping")
@Produces(TEXT_PLAIN)
public class PingResource {

    @Inject
    private Principal principal;

    @Inject
    private JsonWebToken jwt;

    @GET
    @PermitAll
    @Path("all")
    public String ping() {
        return principal.getName();
    }

    @GET
    @RolesAllowed("web")
    @Path("web")
    public String pingWeb() {
        return principal.getName();
    }

    @GET
    @RolesAllowed("mobile")
    @Path("mobile")
    public String pingMobile() {
        return principal.getName();
    }

    @GET
    @RolesAllowed("admin")
    @Path("admin")
    public String pingAdmin() {
        return principal.getName();
    }

    @GET
    @RolesAllowed("user")
    @Path("user")
    public String pingUser() {
        return principal.getName() + "->" + jwt.getGroups();
    }

}
