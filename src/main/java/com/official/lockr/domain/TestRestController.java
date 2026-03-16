package com.official.lockr.domain;

import jakarta.annotation.Nonnull;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Profile("local")
@RestController
public class TestRestController {

    @GetMapping("/test")
    public ResponseEntity<TestHttpResponse> test() {
        return ResponseEntity.ok(new TestHttpResponse("success"));
    }

    record TestHttpResponse(
            @Nonnull String value
    ) {
    }
}
