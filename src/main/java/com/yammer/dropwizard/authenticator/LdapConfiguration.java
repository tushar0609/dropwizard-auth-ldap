package com.yammer.dropwizard.authenticator;

import com.github.benmanes.caffeine.cache.CaffeineSpec;
import com.google.common.collect.Sets;
import io.dropwizard.util.Duration;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.net.URI;
import java.util.Set;

@SuppressWarnings("unused")
public class LdapConfiguration {
    @NotNull
    @Valid
    private URI uri = URI.create("ldaps://www.example.com:636");

    @NotNull
    @Valid
    private CaffeineSpec cachePolicy = CaffeineSpec.parse("maximumSize=0");

    @NotNull
    @NotEmpty
    private String userFilter = "ou=people,dc=example,dc=com";

    @NotNull
    @NotEmpty
    private String groupFilter = "ou=groups,dc=example,dc=com";

    @NotNull
    @NotEmpty
    private String userNameAttribute = "cn";

    @NotNull
    @NotEmpty
    private String groupNameAttribute = "cn";

    @NotNull
    @NotEmpty
    private String groupMembershipAttribute = "memberUid";

    @NotNull
    @NotEmpty
    private String groupClassName = "posixGroup";

    @NotNull
    @Valid
    private Duration connectTimeout = Duration.milliseconds(500);

    @NotNull
    @Valid
    private Duration readTimeout = Duration.milliseconds(500);

    @NotNull
    @Valid
    private Set<String> restrictToGroups = Sets.newHashSet();

    public URI getUri() {
        return uri;
    }

    public LdapConfiguration setUri(URI uri) {
        this.uri = uri;
        return this;
    }

    public CaffeineSpec getCachePolicy() {
        return cachePolicy;
    }

    public LdapConfiguration setCachePolicy(CaffeineSpec cachePolicy) {
        this.cachePolicy = cachePolicy;
        return this;
    }

    public String getUserFilter() {
        return userFilter;
    }

    public LdapConfiguration setUserFilter(String userFilter) {
        this.userFilter = userFilter;
        return this;
    }

    public String getGroupFilter() {
        return groupFilter;
    }

    public LdapConfiguration setGroupFilter(String groupFilter) {
        this.groupFilter = groupFilter;
        return this;
    }

    public String getUserNameAttribute() {
        return userNameAttribute;
    }

    public LdapConfiguration setUserNameAttribute(String userNameAttribute) {
        this.userNameAttribute = userNameAttribute;
        return this;
    }

    public String getGroupNameAttribute() {
        return groupNameAttribute;
    }

    public LdapConfiguration setGroupNameAttribute(String groupNameAttribute) {
        this.groupNameAttribute = groupNameAttribute;
        return this;
    }

    public String getGroupMembershipAttribute() {
        return groupMembershipAttribute;
    }

    public LdapConfiguration setGroupMembershipAttribute(String groupMembershipAttribute) {
        this.groupMembershipAttribute = groupMembershipAttribute;
        return this;
    }

    public String getGroupClassName() {
        return groupClassName;
    }

    public LdapConfiguration setGroupClassName(String groupClassName) {
        this.groupClassName = groupClassName;
        return this;
    }

    public Duration getConnectTimeout() {
        return connectTimeout;
    }

    public LdapConfiguration setConnectTimeout(Duration connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    public Duration getReadTimeout() {
        return readTimeout;
    }

    public LdapConfiguration setReadTimeout(Duration readTimeout) {
        this.readTimeout = readTimeout;
        return this;
    }

    public Set<String> getRestrictToGroups() {
        return restrictToGroups;
    }

    public LdapConfiguration addRestrictedGroup(String group) {
        restrictToGroups.add(group);
        return this;
    }
}