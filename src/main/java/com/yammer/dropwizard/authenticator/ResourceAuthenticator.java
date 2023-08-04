package com.yammer.dropwizard.authenticator;

import static com.google.common.base.Preconditions.checkNotNull;

import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.basic.BasicCredentials;
import java.util.Collections;
import java.util.Optional;

public class ResourceAuthenticator implements Authenticator<BasicCredentials, LdapUser> {

    private final LdapAuthenticator ldapAuthenticator;

    public ResourceAuthenticator(LdapAuthenticator ldapAuthenticator) {
        this.ldapAuthenticator = checkNotNull(ldapAuthenticator);
    }

    @Override
    public Optional<LdapUser> authenticate(BasicCredentials credentials) throws AuthenticationException {
        if (ldapAuthenticator.authenticate(credentials)) {
            return Optional.of(new LdapUser(credentials.getUsername(), Collections.<String>emptySet()));
        } else {
            return Optional.empty();
        }
    }
}