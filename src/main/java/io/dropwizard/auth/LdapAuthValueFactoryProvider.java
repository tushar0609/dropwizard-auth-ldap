package io.dropwizard.auth;

import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.internal.inject.AbstractValueParamProvider;
import org.glassfish.jersey.server.internal.inject.MultivaluedParameterExtractorProvider;
import org.glassfish.jersey.server.model.Parameter;
import org.glassfish.jersey.server.spi.internal.ValueParamProvider;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.lang.reflect.ParameterizedType;
import java.security.Principal;
import java.util.Optional;
import java.util.function.Function;

/**
 * Value factory provider supporting {@link Principal} injection
 * by the {@link Auth} annotation.
 *
 * @param <T> the type of the principal
 */
@Singleton
public class LdapAuthValueFactoryProvider<T extends Principal> extends AbstractValueParamProvider {

    private final Class<T> principalClass;

    @Inject
    public LdapAuthValueFactoryProvider(MultivaluedParameterExtractorProvider mpep,
                                    io.dropwizard.auth.LdapAuthValueFactoryProvider.PrincipalClassProvider<T> principalClassProvider) {
        super(() -> mpep, org.glassfish.jersey.model.Parameter.Source.UNKNOWN);
        this.principalClass = principalClassProvider.clazz;
    }

    @Nullable
    @Override
    protected Function<ContainerRequest, ?> createValueProvider(Parameter parameter) {
        if (!parameter.isAnnotationPresent(LdapAuth.class)) {
            return null;
        } else if (principalClass.equals(parameter.getRawType())) {
            return request -> new PrincipalContainerRequestValueFactory(request).provide();
        } else {
            final boolean isOptionalPrincipal = parameter.getRawType() == Optional.class
                    && ParameterizedType.class.isAssignableFrom(parameter.getType().getClass())
                    && principalClass == ((ParameterizedType) parameter.getType()).getActualTypeArguments()[0];

            return isOptionalPrincipal ? request -> new OptionalPrincipalContainerRequestValueFactory(request).provide() : null;
        }
    }

    @Singleton
    static class PrincipalClassProvider<T extends Principal> {

        private final Class<T> clazz;

        PrincipalClassProvider(Class<T> clazz) {
            this.clazz = clazz;
        }
    }

    public static class Binder<T extends Principal> extends AbstractBinder {

        private final Class<T> principalClass;

        public Binder(Class<T> principalClass) {
            this.principalClass = principalClass;
        }

        @Override
        protected void configure() {
            bind(new io.dropwizard.auth.AuthValueFactoryProvider.PrincipalClassProvider<>(principalClass)).to(io.dropwizard.auth.AuthValueFactoryProvider.PrincipalClassProvider.class);
            bind(io.dropwizard.auth.AuthValueFactoryProvider.class).to(ValueParamProvider.class).in(Singleton.class);
        }
    }
}