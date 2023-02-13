package io.dropwizard.auth;
import java.lang.annotation.*;
import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

/**
 * Specifies the list of security roles permitted to access method(s) in an
 * application.  The value of the <code>RolesAllowed</code> annotation
 * is a list of security role names.
 * This annotation can be specified on a class or on method(s). Specifying it
 * at a class level means that it applies to all the methods in the class.
 * Specifying it on a method means that it is applicable to that method only.
 * If applied at both the class and methods level, the method value overrides
 * the class value if the two conflict.
 *
 * @since Common Annotations 1.0
 */
@Documented
@Retention (RUNTIME)
@Target({TYPE, METHOD})
public @interface LdapRolesAllowed {
    /**
     * List of roles that are permitted access.
     */
    String[] value();
}
