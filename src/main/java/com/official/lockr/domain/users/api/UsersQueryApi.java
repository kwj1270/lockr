package com.official.lockr.domain.users.api;

import com.official.lockr.domain.users.api.dto.FindUsersByProviderRequest;
import com.official.lockr.domain.users.api.dto.FindUsersByProviderResponse;
import org.jooq.Configuration;
import org.jooq.generated.tables.daos.UsersDao;
import org.jooq.generated.tables.pojos.UsersEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping(value = "/api/v1/users")
@RestController
public class UsersQueryApi {

    private final UsersDao usersDao;

    public UsersQueryApi(final Configuration configuration) {
        this.usersDao = new UsersDao(configuration);
    }

    @Transactional(readOnly = true)
    @PostMapping("/find/provider")
    public ResponseEntity<FindUsersByProviderResponse> find(
            @RequestBody final FindUsersByProviderRequest request
    ) {
        // TODO: 작업 중 - OIDC 테이블과 조인 필요
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
