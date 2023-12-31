package com.yammer.dropwizard.authenticator;

import static com.google.common.base.Preconditions.checkNotNull;

import com.codahale.metrics.annotation.Timed;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.common.collect.ImmutableSet;
import io.dropwizard.auth.basic.BasicCredentials;
import java.util.*;
import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LdapAuthenticator {
    private static final Logger LOG = LoggerFactory.getLogger(LdapAuthenticator.class);
    protected final LdapConfiguration configuration;

    protected final LoadingCache<String, Set<String>> groupCache;

    public LdapAuthenticator(LdapConfiguration configuration) {
        this.configuration = checkNotNull(configuration);
        this.groupCache  = Caffeine.from(configuration.getCachePolicy())
                .build( groupName -> {
                    try {
                        return getGroupData(groupName);
                    } catch (NamingException e) {
                        LOG.error("Error while fetching group data for group {}", groupName, e);
                        return Collections.emptySet();
                    }
                });
    }

    private static String sanitizeEntity(String name) {
        return name.replaceAll("[^A-Za-z0-9-_.]", "");
    }

    public boolean canAuthenticate() {
        try {
            new InitialDirContext(contextConfiguration()).close();
            return true;
        } catch (Exception err) {
            //can't authenticate
        }
        return false;
    }

    private boolean filterByGroup(InitialDirContext context, String sanitizedUsername) throws NamingException {
        final Set<String> restrictedToGroups = configuration.getRestrictToGroups();
        if (restrictedToGroups.isEmpty()) {
            return true;
        }
        final StringBuilder groupFilter = new StringBuilder();
        for (String group : restrictedToGroups) {
            final String sanitizedGroup = sanitizeEntity(group);
            groupFilter.append(String.format("(%s=%s)", configuration.getGroupNameAttribute(), sanitizedGroup));
        }
        final String filter = String.format("(&(%s=%s)(|%s))", configuration.getGroupMembershipAttribute(), sanitizedUsername, groupFilter);
        final NamingEnumeration<SearchResult> result = context.search(configuration.getGroupFilter(), filter, new SearchControls());
        try {
            return result.hasMore();
        } finally {
            result.close();
        }
    }

    private Set<String> getGroupMembershipsIntersectingWithRestrictedGroups(InitialDirContext context, String userName) throws NamingException {
        final String filter = String.format("(&(%s=%s)(objectClass=%s))", configuration.getGroupMembershipAttribute(), userName, configuration.getGroupClassName());
        final NamingEnumeration<SearchResult> result = context.search(configuration.getGroupFilter(), filter, new SearchControls());

        ImmutableSet.Builder<String> overlappingGroups = ImmutableSet.builder();
        try {
            while (result.hasMore()) {
                SearchResult next = result.next();
                if (next.getAttributes() != null && next.getAttributes().get(configuration.getGroupNameAttribute()) != null) {
                    String group = (String) next.getAttributes().get(configuration.getGroupNameAttribute()).get(0);
                    if (configuration.getRestrictToGroups().isEmpty() ||
                            configuration.getRestrictToGroups().contains(group)) {
                        overlappingGroups.add(group);
                    }
                }
            }
            return overlappingGroups.build();
        } finally {
            result.close();
        }
    }

    @Timed
    public boolean authenticate(BasicCredentials credentials) throws io.dropwizard.auth.AuthenticationException {
        final String sanitizedUsername = sanitizeEntity(credentials.getUsername());
        try {
            try (AutoclosingDirContext context = buildContext(sanitizedUsername, credentials.getPassword())) {
                return filterByGroup(context, sanitizedUsername);
            }
        } catch (AuthenticationException ae) {
            LOG.error("{} failed to authenticate.", sanitizedUsername, ae);
        } catch (NamingException err) {
            throw new io.dropwizard.auth.AuthenticationException(String.format("LDAP Authentication failure (username: %s)",
                    sanitizedUsername), err);
        }
        return false;
    }

    public boolean isValidUser(String userName, String role) {
        Set<String> members = groupCache.get(role); // populate cache if not already populated (this is a no-op if already populated
        if(Objects.isNull(members) || members.isEmpty()) {
            return false;
        }
        return members.contains(sanitizeEntity(userName));
    }

    private AutoclosingDirContext buildContext(String sanitizedUsername, String password) throws NamingException {
        final String userDN = String.format("%s=%s,%s", configuration.getUserNameAttribute(), sanitizedUsername, configuration.getUserFilter());
        final var env = contextConfiguration();
        env.put(Context.SECURITY_PRINCIPAL, userDN);
        env.put(Context.SECURITY_CREDENTIALS, password);
        return new AutoclosingDirContext(env);
    }

    @Timed
    public Optional<LdapUser> authenticateAndReturnPermittedGroups(BasicCredentials credentials) throws io.dropwizard.auth.AuthenticationException {
        final String sanitizedUsername = sanitizeEntity(credentials.getUsername());
        try {
            try (AutoclosingDirContext context = buildContext(sanitizedUsername, credentials.getPassword())) {
                Set<String> groupMemberships = getGroupMembershipsIntersectingWithRestrictedGroups(context, sanitizedUsername);
                if (!groupMemberships.isEmpty()) {
                    return Optional.of(new LdapUser(sanitizedUsername, groupMemberships));
                }
            }
        } catch (AuthenticationException ae) {
            LOG.error("{} failed to authenticate.", sanitizedUsername, ae);
        } catch (NamingException err) {
            throw new io.dropwizard.auth.AuthenticationException(String.format("LDAP Authentication failure (username: %s)",
                    sanitizedUsername), err);
        }
        return Optional.empty();
    }

    private Hashtable<String, String> contextConfiguration() {
        final Hashtable<String, String> env = new Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, configuration.getUri().toString());
        env.put("com.sun.jndi.ldap.connect.timeout", String.valueOf(configuration.getConnectTimeout().toMilliseconds()));
        env.put("com.sun.jndi.ldap.read.timeout", String.valueOf(configuration.getReadTimeout().toMilliseconds()));
        env.put("com.sun.jndi.ldap.connect.pool", "true");
        return env;
    }

  private Set<String> getGroupData(String groupName) throws NamingException {
        var env = new Hashtable<String, Object>(11);
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, configuration.getUri().toString());
        env.put(Context.SECURITY_AUTHENTICATION, "none");
        var data = new HashSet<String>();
        var ctx = new InitialDirContext(env);
        final String filter = String.format("(&(cn=%s)(objectClass=%s))", groupName, "posixGroup");
        final NamingEnumeration<SearchResult> result = ctx.search(configuration.getGroupFilter(), filter, new SearchControls());
        try {
            while (result.hasMore()) {
                SearchResult next = result.next();
                if (next.getAttributes() != null && next.getAttributes().get("memberUid") != null) {
                    var members = next.getAttributes().get("memberUid").getAll();
                    while(members.hasMore()) {
                        data.add((String)members.next());
                    }
                }
            }
        } finally {
            result.close();
            ctx.close();
        }
        return data;
    }
}
