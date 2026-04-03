package com.official.lockr.domain.auth.oidc.infrastructure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.official.lockr.domain.auth.oidc.domain.OidcProviders;
import com.official.lockr.domain.auth.oidc.domain.vo.OidcPublicKey;
import com.official.lockr.domain.auth.oidc.domain.vo.OidcPublicKeyId;
import com.official.lockr.domain.auth.oidc.domain.vo.OidcPublicKeys;
import com.official.lockr.domain.auth.oidc.infrastructure.client.HttpOidcClient;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;

@Component
public class HttpOidcProviders implements OidcProviders {

    private final HttpOidcClient httpOidcClient;
    private final ObjectMapper objectMapper;

    public HttpOidcProviders(final HttpOidcClient httpOidcClient, final ObjectMapper objectMapper) {
        this.httpOidcClient = httpOidcClient;
        this.objectMapper = objectMapper;
    }

    public String identifier(final String idToken, String providerType) {
        final OidcPublicKeyId oidcPublicKeyId = oidcPublicKeyId(idToken);
        final OidcPublicKeys oidcPublicKeys = httpOidcClient.authKeys(providerType);
        final OidcPublicKey oidcPublicKey = oidcPublicKeys.findByOidcPublicKeyId(oidcPublicKeyId);
        final PublicKey publicKey = publicKey(oidcPublicKey);
        final Claims claims = parseClaims(idToken, publicKey);
        return claims.getSubject();
    }

    private OidcPublicKeyId oidcPublicKeyId(final String idToken) {
        try {
            final String headerString = new String(Base64.getUrlDecoder().decode(idToken.split("\\.")[0]));
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
        return Jwts.parser()
                .verifyWith((java.security.interfaces.RSAPublicKey) publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
