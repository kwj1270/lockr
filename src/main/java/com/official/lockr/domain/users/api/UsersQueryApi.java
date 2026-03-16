package com.official.lockr.domain.users.api;

import com.official.lockr.domain.auth.signin.domain.SignInSession;
import com.official.lockr.domain.users.api.dto.FindUsersByProviderRequest;
import com.official.lockr.domain.users.api.dto.FindUsersByProviderResponse;
import com.official.lockr.domain.users.api.dto.UserAdditionalInfoResponse;
import com.official.lockr.global.vo.Gender;
import org.jooq.Configuration;
import org.jooq.generated.tables.daos.UserAdditionalInfoDao;
import org.jooq.generated.tables.daos.UsersDao;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RestController;
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
            @RequestAttribute("signInSession") final SignInSession signInSession
    ) {
        final var record = userAdditionalInfoDao.ctx()
                .selectFrom(USER_ADDITIONAL_INFO)
                .where(USER_ADDITIONAL_INFO.USER_ID.eq(signInSession.userId()))
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
}
