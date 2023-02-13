package io.dropwizard.auth;


import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Specifies that no security roles are allowed to invoke the specified
 * method(s).
 *
 * @see javax.annotation.security.RolesAllowed
 * @see javax.annotation.security.PermitAll
 * @since Common Annotations 1.0
 */
@Documented
@Retention(RUNTIME)
@Target({TYPE, METHOD})
public @interface LdapPermitAll {
}
