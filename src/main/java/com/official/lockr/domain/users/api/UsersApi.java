package com.official.lockr.domain.users.api;

import com.official.lockr.domain.users.api.dto.FindUsersByProviderRequest;
import com.official.lockr.domain.users.api.dto.FindUsersByProviderResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RequestMapping(value = "/api/v1/users")
@RestController
public class UsersApi {

    @PostMapping("/find/provider")
    public ResponseEntity<FindUsersByProviderResponse> findProvider(
            @RequestBody final FindUsersByProviderRequest request
    ) {
        final FindUsersByProviderResponse response = new FindUsersByProviderResponse(
                UUID.randomUUID().toString(),
                request.providerId(),
                request.providerType()
        );
        return ResponseEntity.ok().body(response);
    }
}
