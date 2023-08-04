package io.dropwizard.auth;

import java.lang.annotation.Annotation;
import java.util.Optional;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;
import org.glassfish.jersey.server.model.AnnotatedMethod;

public class LdapAuthDynamicFeature implements DynamicFeature {
    private final ContainerRequestFilter authFilter;

    private final Class<? extends ContainerRequestFilter> authFilterClass;

    // We suppress the null away checks, as adding `@Nullable` to the auth
    // filter fields, causes Jersey to try and resolve the fields to a concrete
    // type (which subsequently fails).
    @SuppressWarnings("NullAway")
    public LdapAuthDynamicFeature(ContainerRequestFilter authFilter) {
        this.authFilter = authFilter;
        this.authFilterClass = null;
    }

    @SuppressWarnings("NullAway")
    public LdapAuthDynamicFeature(Class<? extends ContainerRequestFilter> authFilterClass) {
        this.authFilter = null;
        this.authFilterClass = authFilterClass;
    }

    @Override
    public void configure(ResourceInfo resourceInfo, FeatureContext context) {
        final AnnotatedMethod am = new AnnotatedMethod(resourceInfo.getResourceMethod());
        final Annotation[][] parameterAnnotations = am.getParameterAnnotations();
        final Class<?>[] parameterTypes = am.getParameterTypes();

        // First, check for any @Auth annotations on the method.
        for (int i = 0; i < parameterAnnotations.length; i++) {
            if (containsAuthAnnotation(parameterAnnotations[i])) {
                // Optional auth requires that a concrete AuthFilter be provided.
                if (parameterTypes[i].equals(Optional.class) && authFilter != null) {
                    context.register(new WebApplicationExceptionCatchingFilter(authFilter));
                } else {
                    registerAuthFilter(context);
                }
                return;
            }
        }
        // Second, check for any authorization annotations on the class or method.
        // Note that @DenyAll shouldn't be attached to classes.
        final boolean annotationOnClass = (resourceInfo.getResourceClass().getAnnotation(LdapRolesAllowed.class) != null) ||
                (resourceInfo.getResourceClass().getAnnotation(LdapPermitAll.class) != null);
        final boolean annotationOnMethod = am.isAnnotationPresent(LdapRolesAllowed.class) || am.isAnnotationPresent(LdapDenyAll.class) ||
                am.isAnnotationPresent(LdapPermitAll.class);

        if (annotationOnClass || annotationOnMethod) {
            registerAuthFilter(context);
        }
    }
    private boolean containsAuthAnnotation(final Annotation[] annotations) {
        for (final Annotation annotation : annotations) {
            if (annotation instanceof LdapAuth) {
                return true;
            }
        }
        return false;
    }

    private void registerAuthFilter(FeatureContext context) {
        if (authFilter != null) {
            context.register(authFilter);
        } else if (authFilterClass != null) {
            context.register(authFilterClass);
        }
    }
}