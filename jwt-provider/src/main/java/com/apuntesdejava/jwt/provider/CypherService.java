package com.apuntesdejava.jwt.provider;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.UUID;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonString;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

/**
 *
 * @author Diego Silva Limaco <diego.silva at apuntesdejava.com>
 */
@ApplicationScoped
public class CypherService {

    private static final Logger LOG = Logger.getLogger(CypherService.class.getName());
    private String issuer;
    private int tokenValid;
    private String audience;
    private String headerKey;
    private final Collection<String> roles = new LinkedHashSet<>();

    @PostConstruct
    public void init() {
        Security.addProvider(new BouncyCastleProvider());
        LOG.info("Credentials loaded");
    }

    public String createToken(PrivateKey key, String username, Collection<String> groups) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiration = now.plus(tokenValid, ChronoUnit.SECONDS);
        return Jwts.builder()
                .setHeaderParam("typ", "JWT")
                .setIssuedAt(Date.from(now.atZone(ZoneId.systemDefault())
                        .toInstant()))
                .setExpiration(Date.from(expiration.atZone(ZoneId.systemDefault())
                        .toInstant()))
                .signWith(SignatureAlgorithm.RS256, key)
                .setSubject(username)
                .setAudience(audience)
                .setHeaderParam("kid", headerKey)
                .claim("groups", groups)
                .setId(UUID.randomUUID().toString())
                .setIssuer(issuer).compact();
    }

    public PrivateKey readPrivateKey() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        try ( InputStream is = CypherService.class.getResourceAsStream("/jwt-config.json")) {
            JsonObject json = Json.createReader(is).readObject();
            String keyString = json.getString("private-key");
            this.issuer = json.getString("issuer");
            this.audience = json.getString("audience", "audience");
            json.getJsonArray("roles").stream()
                    .map((v) -> (JsonString) v).forEachOrdered((s)
                    -> roles.add(s.getString()));
            this.tokenValid = json.getInt("token-valid");
            this.headerKey = json.getString("header-key");
            byte[] encoded = Base64.getDecoder().decode(keyString);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
            KeyFactory factory = KeyFactory.getInstance("RSA");
            return factory.generatePrivate(keySpec);
        }
    }

    public Collection<String> getRoles() {
        return roles;
    }

}
