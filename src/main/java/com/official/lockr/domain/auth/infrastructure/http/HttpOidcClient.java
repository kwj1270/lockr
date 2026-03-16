package com.official.lockr.domain.auth.infrastructure.http;

import com.official.lockr.domain.auth.domain.oidc.OidcPublicKeys;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.Optional;

@Component
public class HttpOidcClient {

    private final RestClient oidcRestClient;

    public HttpOidcClient(final RestClient oidcRestClient) {
        this.oidcRestClient = oidcRestClient;
    }

    public OidcPublicKeys authKeys() {
        return Optional.ofNullable(oidcRestClient.get()
                .uri("/auth/keys")
                .retrieve()
                .toEntity(OidcPublicKeys.class)
                .getBody())
                .orElseGet(() -> new OidcPublicKeys(new ArrayList<>()));
    }
}
