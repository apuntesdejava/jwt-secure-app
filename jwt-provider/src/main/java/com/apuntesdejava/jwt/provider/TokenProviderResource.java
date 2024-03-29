package com.apuntesdejava.jwt.provider;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author Diego Silva Limaco <diego.silva at apuntesdejava.com>
 */
@ApplicationScoped
@Path("/auth")
@ApplicationPath("/")

public class TokenProviderResource extends Application {

    private static final Logger LOG = Logger.getLogger(TokenProviderResource.class.getName());
    @Inject
    private CypherService cypherService;
    private PrivateKey key;

    @PostConstruct
    public void init() {
        try {
            key = cypherService.readPrivateKey();
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    @POST
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response login(
            @FormParam("username") String username,
            @FormParam("password") String password,
            @Context HttpServletRequest request) {
        if (key == null) {
            LOG.severe("Credentials not loaded");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

        List<String> target = new ArrayList<>();
        try {
            request.login(username, password);
            cypherService.getRoles()
                    .stream()
                    .filter((role) -> (request.isUserInRole(role)))
                    .forEachOrdered((role)
                            -> target.add(role)
                    );

        } catch (ServletException ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            return Response.status(Response.Status.UNAUTHORIZED)
                    .build();
        }

        String token = cypherService.createToken(key, username, target);

        return Response.status(Response.Status.OK)
                .header(AUTHORIZATION, "Bearer ".concat(token))
                .entity(token)
                .build();

    }
}
