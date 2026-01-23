package com.official.lockr.domain.users.api;

import com.official.lockr.domain.auth.signin.domain.SignInSession;
import com.official.lockr.domain.users.api.dto.FindUsersByProviderRequest;
import com.official.lockr.domain.users.api.dto.FindUsersByProviderResponse;
import com.official.lockr.domain.users.api.dto.UserAdditionalInfoResponse;
import com.official.lockr.global.vo.Gender;
import jakarta.servlet.http.HttpSession;
import org.jooq.Configuration;
import org.jooq.generated.tables.daos.UserAdditionalInfoDao;
import org.jooq.generated.tables.daos.UsersDao;
import org.jooq.generated.tables.pojos.UsersEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import static java.util.Objects.isNull;
import static org.jooq.generated.tables.UserAdditionalInfoJOOQEntity.USER_ADDITIONAL_INFO;

@RequestMapping(value = "/api/v1/users")
@RestController
public class UsersQueryApi {

    private final UsersDao usersDao;
    private final UserAdditionalInfoDao userAdditionalInfoDao;

    public UsersQueryApi(final Configuration configuration) {
        this.usersDao = new UsersDao(configuration);
        this.userAdditionalInfoDao = new UserAdditionalInfoDao(configuration);
    }

    @Transactional(readOnly = true)
    @GetMapping("/additional-info")
    public ResponseEntity<UserAdditionalInfoResponse> getMyAdditionalInfo(
            final HttpSession httpSession
    ) {
        final SignInSession signIn = session(httpSession);
        final var record = userAdditionalInfoDao.ctx()
                .selectFrom(USER_ADDITIONAL_INFO)
                .where(USER_ADDITIONAL_INFO.USER_ID.eq(signIn.userId()))
                .and(USER_ADDITIONAL_INFO.DELETED_AT.isNull())
                .fetchOne();

        if (record == null) {
            return ResponseEntity.notFound().build();
        }

        final UserAdditionalInfoResponse response = UserAdditionalInfoResponse.of(
                record.getId(),
                record.getUserId(),
                record.getName(),
                record.getBirthDate(),
                record.getPhone(),
                Gender.fromDbValue(record.getGender())
        );

        return ResponseEntity.ok(response);
    }

    private SignInSession session(final HttpSession httpSession) {
        final SignInSession signIn = (SignInSession) httpSession.getAttribute("signIn");
        if (isNull(signIn)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }
        return signIn;
    }
}
