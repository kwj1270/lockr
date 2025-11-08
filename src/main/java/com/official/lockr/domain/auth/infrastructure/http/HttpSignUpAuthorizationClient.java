package com.official.lockr.domain.auth.infrastructure.http;

import com.official.lockr.domain.auth.domain.signup.SignUpAuthorization;
import com.official.lockr.domain.auth.infrastructure.http.dto.FindUsersByProviderResponse;
import com.official.lockr.domain.auth.infrastructure.http.dto.SaveUsersResponse;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.util.Map;
import java.util.Objects;

@Component
public class HttpSignUpAuthorizationClient {

    private static final Logger log = LoggerFactory.getLogger(HttpSignUpAuthorizationClient.class);
    private final RestClient signUpAuthorizationRestClient;

    public HttpSignUpAuthorizationClient(final RestClient signUpAuthorizationRestClient) {
        this.signUpAuthorizationRestClient = signUpAuthorizationRestClient;
    }

    public SignUpAuthorization find(final String providerId, final String providerType) {
        final FindUsersByProviderResponse users = signUpAuthorizationRestClient.post()
                .uri(URI.create("/api/v1/users/find/provider"))
                .body(Map.of("providerId", providerId, "providerType", providerType))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> log.debug("{}", response))
                .toEntity(FindUsersByProviderResponse.class)
                .getBody();

        if (Objects.isNull(users) || Strings.isBlank(users.id())) {
            return null;
        }
        return mapToSignUpAuthorization(users);
    }

    public SignUpAuthorization save(final String providerId, final String providerType) {
        final SaveUsersResponse users = signUpAuthorizationRestClient.post()
                .uri(URI.create("/api/v1/users"))
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
