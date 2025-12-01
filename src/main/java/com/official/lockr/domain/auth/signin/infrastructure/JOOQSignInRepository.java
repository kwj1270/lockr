package com.official.lockr.domain.auth.signin.infrastructure;

import com.official.lockr.domain.auth.signin.domain.SignIn;
import com.official.lockr.domain.auth.signin.domain.SignInRepository;
import com.official.lockr.global.ddd.DomainEventPublisher;
import org.jooq.Configuration;
import org.jooq.generated.tables.daos.SignInDao;
import org.jooq.generated.tables.pojos.SignInEntity;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class JOOQSignInRepository implements SignInRepository {

    private final SignInDao signInsDao;
    private final DomainEventPublisher domainEventPublisher;

    public JOOQSignInRepository(final Configuration configuration,
                                final DomainEventPublisher domainEventPublisher
    ) {
        this.signInsDao = new SignInDao(configuration);
        this.domainEventPublisher = domainEventPublisher;
    }

    @Transactional
    @Override
    public SignIn save(final SignIn signIn) {
        final SignInEntity entity = new SignInEntity(
                signIn.getId(),
                signIn.getUserId(),
                signIn.getDeviceId(),
                signIn.getDeviceName(),
                signIn.getDeviceOS(),
                signIn.getIpAddress(),
                signIn.getUserAgent(),
                signIn.getCreatedAt()
        );
        signInsDao.insert(entity);
        signIn.publish(domainEventPublisher);
        return signIn;
    }
}
