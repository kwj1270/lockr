package com.official.lockr.domain.auth.infrastructure;

import com.official.lockr.domain.auth.domain.vo.ProviderType;
import com.official.lockr.domain.auth.domain.signin.SignIn;
import com.official.lockr.domain.auth.domain.signin.SignInRepository;
import org.jooq.Configuration;
import org.jooq.generated.tables.daos.SignInDao;
import org.jooq.generated.tables.pojos.SignInEntity;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;

@Repository
public class JOOQSignInRepository implements SignInRepository {

    private final SignInDao signInsDao;

    public JOOQSignInRepository(final Configuration configuration) {
        this.signInsDao = new SignInDao(configuration);
    }

    @Transactional
    @Override
    public SignIn save(final SignIn signIn) {
        final SignInEntity entity = new SignInEntity(
                signIn.getId(),
                signIn.getUserId(),
                signIn.getProviderId(),
                signIn.getProviderTypeValue(),
                signIn.getDeviceId(),
                signIn.getDeviceInfo(),
                signIn.getIpAddress(),
                signIn.getUserAgent(),
                signIn.getCreatedAt().toLocalDateTime()
        );
        signInsDao.insert(entity);
        return new SignIn(
                entity.getId(),
                entity.getUserId(),
                entity.getProviderId(),
                ProviderType.valueOf(entity.getProviderType()),
                entity.getDeviceId(),
                entity.getDeviceInfo(),
                entity.getIpAddress(),
                entity.getUserAgent(),
                entity.getCreatedAt().atZone(ZoneId.of("Asia/Seoul"))
        );
    }
}
