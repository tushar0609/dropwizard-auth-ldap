package com.yammer.dropwizard.authenticator;

import java.security.Principal;
import java.util.Set;

public class LdapUser implements Principal {

    private final String name;
    private final Set<String> roles;

    public LdapUser(String name, Set<String> roles) {
        this.name = name;
        this.roles = roles;
    }

    public String getName() {
        return name;
    }

    public Set<String> getRoles() {
        return roles;
    }
}
