package com.yammer.dropwizard.authenticator;

import com.yammer.dropwizard.authenticator.healthchecks.LdapHealthCheck;
import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.auth.Authorizer;
import io.dropwizard.auth.CachingAuthenticator;
import io.dropwizard.auth.LdapAuthDynamicFeature;
import io.dropwizard.auth.LdapAuthValueFactoryProvider;
import io.dropwizard.auth.basic.BasicCredentialAuthFilter;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public abstract class LdapAuthenticatorBundle<T extends Configuration> implements ConfiguredBundle<T> {

    public abstract LdapConfiguration getConfiguration(final T configuration);

    @Override
    public void initialize(Bootstrap<?> bootstrap) {

    }

    @Override
    public void run(final T configuration, final Environment environment) {
        LdapConfiguration ldapConfiguration = getConfiguration(configuration);
        CachingAuthenticator ldapAuthenticator = new CachingAuthenticator(environment.metrics(),
                new UserResourceAuthenticator(new LdapAuthenticator(ldapConfiguration)),
                ldapConfiguration.getCachePolicy());
        environment.jersey().register(new LdapAuthDynamicFeature(
                new BasicCredentialAuthFilter.Builder<LdapUser>()
                        .setAuthenticator(ldapAuthenticator)
                        .setAuthorizer((Authorizer<LdapUser>) (user, role) -> user.getRoles().contains(role))
                        .setRealm("realm")
                        .buildAuthFilter()));
        environment.jersey().register(LdapRolesAllowedDynamicFeature.class);
        //If you want to use @Auth to inject a custom Principal type into your resource
        environment.jersey().register(new LdapAuthValueFactoryProvider.Binder<>(LdapUser.class));
        environment.healthChecks().register("ldap",
                new LdapHealthCheck<>(new ResourceAuthenticator(new LdapCanAuthenticate(ldapConfiguration))));
    }
}
