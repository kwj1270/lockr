package com.official.lockr.domain.auth.infrastructure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.official.lockr.domain.auth.domain.oidc.OidcProviders;
import com.official.lockr.domain.auth.domain.oidc.OidcPublicKey;
import com.official.lockr.domain.auth.domain.oidc.OidcPublicKeyId;
import com.official.lockr.domain.auth.domain.oidc.OidcPublicKeys;
import com.official.lockr.domain.auth.infrastructure.http.HttpOidcClient;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Arrays;
import java.util.Base64;

@Component
public class HttpOidcProviders implements OidcProviders {

    private final HttpOidcClient httpOidcClient;
    private final ObjectMapper objectMapper;

    public HttpOidcProviders(final HttpOidcClient httpOidcClient, final ObjectMapper objectMapper) {
        this.httpOidcClient = httpOidcClient;
        this.objectMapper = objectMapper;
    }

    public String providerId(final String idToken, String providerType) {
        final OidcPublicKeyId oidcPublicKeyId = oidcPublicKeyId(idToken);
        final OidcPublicKeys oidcPublicKeys = oidcPublicKeys();
        final OidcPublicKey oidcPublicKey = oidcPublicKeys.findByOidcPublicKeyId(oidcPublicKeyId);
        return providerId(idToken, oidcPublicKey);
    }

    private String providerId(final String idToken, final OidcPublicKey oidcPublicKey) {
        final PublicKey publicKey = publicKey(oidcPublicKey);
        final Claims claims = parseClaims(idToken, publicKey);
        return claims.getSubject();
    }

    private OidcPublicKeys oidcPublicKeys() {
        return httpOidcClient.authKeys();
    }

    private OidcPublicKeyId oidcPublicKeyId(final String idToken) {
        try {
            final String headerString = Arrays.toString(Base64.getUrlDecoder().decode(idToken.split("\\.")[0]));
            return objectMapper.readValue(headerString, OidcPublicKeyId.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private PublicKey publicKey(OidcPublicKey publicKey) {
        final byte[] nBytes = Base64.getUrlDecoder().decode(publicKey.n());
        final byte[] eBytes = Base64.getUrlDecoder().decode(publicKey.e());
        final RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(new BigInteger(1, nBytes), new BigInteger(1, eBytes));
        try {
            return KeyFactory.getInstance(publicKey.kty()).generatePublic(publicKeySpec);
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private Claims parseClaims(final String token, final PublicKey publicKey) {
        return Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
