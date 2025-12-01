package com.official.lockr.domain.users.api;

import com.official.lockr.domain.users.api.dto.SaveUsersRequest;
import com.official.lockr.domain.users.api.dto.SaveUsersResponse;
import com.official.lockr.domain.users.application.RegisterUsersUsecase;
import com.official.lockr.domain.users.application.command.SaveUsersCommand;
import com.official.lockr.domain.users.domain.Users;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping(value = "/api/v1/users")
@RestController
public class UsersApi {

    private final RegisterUsersUsecase registerUsersUsecase;

    public UsersApi(final RegisterUsersUsecase registerUsersUsecase) {
        this.registerUsersUsecase = registerUsersUsecase;
    }

    @PostMapping
    public ResponseEntity<SaveUsersResponse> registerUsers(@RequestBody final SaveUsersRequest request) {
        final Users users = registerUsersUsecase.register(new SaveUsersCommand());
        return ResponseEntity.ok(new SaveUsersResponse(users.getId()));
    }
}
