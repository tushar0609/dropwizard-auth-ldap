package io.dropwizard.auth;
import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.*;

@Documented
@Retention (RUNTIME)
@Target({TYPE, METHOD})
public @interface LdapRolesAllowed {
    String[] value();
}
