package com.official.lockr.domain.auth.infrastructure.http;

import com.official.lockr.domain.auth.domain.auth.SignUpAuthorization;
import com.official.lockr.domain.auth.infrastructure.http.dto.FindUsersByProviderResponse;
import com.official.lockr.domain.auth.infrastructure.http.dto.SaveUsersResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.util.Map;
import java.util.Objects;

@Component
public class HttpSignUpAuthorizationClient {

    private final RestClient signUpAuthorizationRestClient;

    public HttpSignUpAuthorizationClient(final RestClient signUpAuthorizationRestClient) {
        this.signUpAuthorizationRestClient = signUpAuthorizationRestClient;
    }

    public SignUpAuthorization find(final String providerId, final String providerType) {
        final ResponseEntity<FindUsersByProviderResponse> entity = signUpAuthorizationRestClient.post()
                .uri(URI.create("/api/v1/users/find/provider"))
                .body(Map.of("providerId", providerId, "providerType", providerType))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .retrieve()
                .toEntity(FindUsersByProviderResponse.class);

        final FindUsersByProviderResponse users = entity.getBody();
        if (Objects.isNull(users)) {
            return null;
        }
        return mapToSignUpAuthorization(users);
    }

    public SignUpAuthorization save(final String providerId, final String providerType) {
        final SaveUsersResponse users = signUpAuthorizationRestClient.post()
                .uri(URI.create("/api/v1/save/users"))
                .body(Map.of("providerId", providerId, "providerType", providerType))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .retrieve()
                .toEntity(SaveUsersResponse.class)
                .getBody();
        return mapToSignUpAuthorization(users);
    }

    private static SignUpAuthorization mapToSignUpAuthorization(final FindUsersByProviderResponse response) {
        return new SignUpAuthorization(
                response.id(),
                response.providerId(),
                response.providerType()
        );
    }

    private static SignUpAuthorization mapToSignUpAuthorization(final SaveUsersResponse response) {
        return new SignUpAuthorization(
                response.id(),
                response.providerId(),
                response.providerType()
        );
    }
}
