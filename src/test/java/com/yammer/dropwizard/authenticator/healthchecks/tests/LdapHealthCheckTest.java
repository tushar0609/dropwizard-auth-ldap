package com.yammer.dropwizard.authenticator.healthchecks.tests;

import com.yammer.dropwizard.authenticator.LdapAuthenticator;
import com.yammer.dropwizard.authenticator.LdapCanAuthenticate;
import com.yammer.dropwizard.authenticator.LdapConfiguration;
import com.yammer.dropwizard.authenticator.ResourceAuthenticator;
import com.yammer.dropwizard.authenticator.healthchecks.LdapHealthCheck;
import io.dropwizard.auth.basic.BasicCredentials;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LdapHealthCheckTest {

    @Test
    public void healthy() throws Exception {
        LdapAuthenticator ldapAuthenticator = mock(LdapAuthenticator.class);
        when(ldapAuthenticator.authenticate(any(BasicCredentials.class))).thenReturn(true);
        LdapHealthCheck healthCheck = new LdapHealthCheck<>(new ResourceAuthenticator(ldapAuthenticator));
        assertTrue(healthCheck.check().isHealthy());
    }

    @Test
    public void unhealthy() throws Exception {
        final LdapAuthenticator badLdapAuthenticator = new LdapCanAuthenticate(new LdapConfiguration());
        final LdapHealthCheck badHealthCheck = new LdapHealthCheck<>(new ResourceAuthenticator(badLdapAuthenticator));
        assertFalse(badHealthCheck.check().isHealthy());
    }
}
