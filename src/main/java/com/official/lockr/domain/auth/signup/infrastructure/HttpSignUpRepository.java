package com.official.lockr.domain.auth.signup.infrastructure;

import com.official.lockr.domain.auth.signup.domain.SignUp;
import com.official.lockr.domain.auth.signup.domain.SignUpRepository;
import com.official.lockr.domain.auth.oidc.infrastructure.client.HttpSignUpClient;
import org.springframework.stereotype.Repository;

@Repository
public class HttpSignUpRepository implements SignUpRepository {

    private final HttpSignUpClient httpSignUpClient;

    public HttpSignUpRepository(final HttpSignUpClient httpSignUpClient) {
        this.httpSignUpClient = httpSignUpClient;
    }

    @Override
    public SignUp save() {
        return httpSignUpClient.save();
    }

    @Override
    public void delete(final String userId) {
        httpSignUpClient.delete(userId);
    }
}
