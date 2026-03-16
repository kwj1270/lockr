package com.official.lockr.domain.auth.oidc.infrastructure.client;

import com.official.lockr.domain.auth.signup.domain.SignUp;
import com.official.lockr.domain.users.api.dto.SaveUsersRequest;
import com.official.lockr.domain.users.api.dto.SaveUsersResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.URI;

@Component
public class HttpSignUpClient {

    private static final Logger log = LoggerFactory.getLogger(HttpSignUpClient.class);
    private final RestClient signUpAuthorizationRestClient;

    public HttpSignUpClient(final RestClient signUpAuthorizationRestClient) {
        this.signUpAuthorizationRestClient = signUpAuthorizationRestClient;
    }

    public SignUp save() {
        final SaveUsersResponse users = signUpAuthorizationRestClient.post()
                .uri(URI.create("/api/v1/users"))
                .body(new SaveUsersRequest())
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .retrieve()
                .toEntity(SaveUsersResponse.class)
                .getBody();
        return mapToSignUpAuthorization(users);
    }

    public void delete(final String userId) {
        signUpAuthorizationRestClient.delete()
                .uri(URI.create("/api/v1/users/" + userId))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> log.warn("Failed to delete user: userId={}, status={}", userId, response.getStatusCode()))
                .toBodilessEntity();
    }

    private static SignUp mapToSignUpAuthorization(final SaveUsersResponse response) {
        return new SignUp(response.id());
    }
}
