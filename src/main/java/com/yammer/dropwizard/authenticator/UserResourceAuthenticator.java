package com.yammer.dropwizard.authenticator;

import static com.google.common.base.Preconditions.checkNotNull;

import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.basic.BasicCredentials;
import java.util.Optional;

public class UserResourceAuthenticator implements Authenticator<BasicCredentials, LdapUser> {

    private final LdapAuthenticator ldapAuthenticator;

    public UserResourceAuthenticator(LdapAuthenticator ldapAuthenticator) {
        this.ldapAuthenticator = checkNotNull(ldapAuthenticator);
    }

    @Override
    public Optional<LdapUser> authenticate(BasicCredentials credentials) throws AuthenticationException {
        return ldapAuthenticator.authenticateAndReturnPermittedGroups(credentials);
    }
}