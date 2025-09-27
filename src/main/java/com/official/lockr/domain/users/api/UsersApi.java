package com.official.lockr.domain.users.api;

import com.official.lockr.domain.users.api.dto.FindUsersByProviderRequest;
import com.official.lockr.domain.users.api.dto.FindUsersByProviderResponse;
import com.official.lockr.domain.users.api.dto.SaveUsersRequest;
import com.official.lockr.domain.users.api.dto.SaveUsersResponse;
import com.official.lockr.domain.users.application.SaveUsersUsecase;
import com.official.lockr.domain.users.domain.Users;
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
public class UsersApi {

    private final UsersDao usersDao;
    private final SaveUsersUsecase saveUsersUsecase;

    public UsersApi(final Configuration configuration, final SaveUsersUsecase saveUsersUsecase) {
        this.usersDao = new UsersDao(configuration);
        this.saveUsersUsecase = saveUsersUsecase;
    }

    @Transactional(readOnly = true)
    @PostMapping("/find/provider")
    public ResponseEntity<FindUsersByProviderResponse> find(
            @RequestBody final FindUsersByProviderRequest request
    ) {
        final UsersEntity usersEntity = usersDao.findAll().stream()
                .filter(it ->
                        it.getProviderId().equals(request.providerId()) &&
                                it.getProviderType().equals(request.providerType())
                ).findFirst()
                .orElseThrow(IllegalArgumentException::new);
        return ResponseEntity.ok().body(
                new FindUsersByProviderResponse(usersEntity.getId(), usersEntity.getProviderId(), usersEntity.getProviderType())
        );
    }

    @PostMapping
    public ResponseEntity<SaveUsersResponse> saveUsers(
            @RequestBody final SaveUsersRequest request
    ) {
        final Users users = saveUsersUsecase.save(request.command());
        return ResponseEntity.ok(new SaveUsersResponse(
                users.getId(),
                users.getProviderId(),
                users.getProviderType()
        ));
    }
}
