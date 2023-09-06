package com.yammer.dropwizard.authenticator;

import io.dropwizard.auth.AuthFilter;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.basic.BasicCredentials;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.Base64;
import javax.annotation.Nullable;
import javax.annotation.Priority;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.SecurityContext;

@Priority(1001)
public class CustomBasicCredentialAuthFilter<P extends Principal> extends AuthFilter<BasicCredentials, P> {

  public CustomBasicCredentialAuthFilter() {
  }

  @Override
  public void filter(ContainerRequestContext requestContext) throws IOException {
    final BasicCredentials credentials =
        getCredentials(requestContext.getHeaders().getFirst(HttpHeaders.AUTHORIZATION));
    if (!requestContext.getHeaders().getFirst(HttpHeaders.AUTHORIZATION).startsWith("Basic")) {
      return;
    }
    if (!authenticate(requestContext, credentials, SecurityContext.BASIC_AUTH)) {
      throw new WebApplicationException(unauthorizedHandler.buildResponse(prefix, realm));
    }
  }

  /**
   * Parses a Base64-encoded value of the `Authorization` header
   * in the form of `Basic dXNlcm5hbWU6cGFzc3dvcmQ=`.
   *
   * @param header the value of the `Authorization` header
   * @return a username and a password as {@link BasicCredentials}
   */
  @Nullable
  private BasicCredentials getCredentials(String header) {
    if (header == null) {
      return null;
    }

    final int space = header.indexOf(' ');
    if (space <= 0) {
      return null;
    }

    final String method = header.substring(0, space);
    if (!prefix.equalsIgnoreCase(method)) {
      return null;
    }

    final String decoded;
    try {
      decoded = new String(Base64.getDecoder().decode(header.substring(space + 1)), StandardCharsets.UTF_8);
    } catch (IllegalArgumentException e) {
      logger.warn("Error decoding credentials", e);
      return null;
    }

    // Decoded credentials is 'username:password'
    final int i = decoded.indexOf(':');
    if (i <= 0) {
      return null;
    }

    final String username = decoded.substring(0, i);
    final String password = decoded.substring(i + 1);
    return new BasicCredentials(username, password);
  }

  /**
   * Builder for {@link io.dropwizard.auth.basic.BasicCredentialAuthFilter}.
   * <p>An {@link Authenticator} must be provided during the building process.</p>
   *
   * @param <P> the principal
   */
  public static class Builder<P extends Principal> extends
      AuthFilterBuilder<BasicCredentials, P, CustomBasicCredentialAuthFilter<P>> {

    @Override
    protected CustomBasicCredentialAuthFilter<P> newInstance() {
      return new CustomBasicCredentialAuthFilter<>();
    }
  }
}