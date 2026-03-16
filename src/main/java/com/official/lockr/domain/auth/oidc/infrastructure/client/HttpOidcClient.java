package com.official.lockr.domain.auth.oidc.infrastructure.client;

import com.official.lockr.domain.auth.oidc.domain.vo.OidcPublicKeys;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

@Component
public class HttpOidcClient {

    private final Map<String, RestClient> oidcRestClients;

    public HttpOidcClient(@Qualifier("oidcRestClients") final Map<String, RestClient> oidcRestClients) {
        this.oidcRestClients = oidcRestClients;
    }

    public OidcPublicKeys authKeys(final String providerType) {
        final RestClient restClient = oidcRestClients.get(providerType);
        if (restClient == null) {
            throw new IllegalArgumentException("Unsupported OIDC provider: " + providerType);
        }
        return Optional.ofNullable(restClient.get()
                .retrieve()
                .toEntity(OidcPublicKeys.class)
                .getBody())
                .orElseGet(() -> new OidcPublicKeys(new ArrayList<>()));
    }
}
