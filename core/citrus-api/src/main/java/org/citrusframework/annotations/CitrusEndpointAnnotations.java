package org.citrusframework.annotations;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.spi.BindToRegistry;
import org.citrusframework.spi.ReferenceRegistry;
import org.citrusframework.spi.ReferenceResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

/**
 * Dependency injection support for {@link CitrusEndpoint} endpoint annotations.
 *
 * @author Christoph Deppisch
 */
public abstract class CitrusEndpointAnnotations {

    /** Logger */
    private static final Logger LOG = LoggerFactory.getLogger(CitrusEndpointAnnotations.class);

    /**
     * Prevent instantiation.
     */
    private CitrusEndpointAnnotations() {
        super();
    }

    /**
     * Reads all {@link CitrusEndpoint} and {@link CitrusEndpointConfig} related annotations on target object field declarations and
     * injects proper endpoint instances.
     *
     * @param target
     * @param context
     */
    public static void injectEndpoints(final Object target, final TestContext context) {
        ReflectionUtils.doWithFields(target.getClass(), field -> {
            LOG.debug(String.format("Injecting Citrus endpoint on test class field '%s'", field.getName()));
            CitrusEndpoint endpointAnnotation = field.getAnnotation(CitrusEndpoint.class);

            for (Annotation annotation : field.getAnnotations()) {
                if (annotation.annotationType().getAnnotation(CitrusEndpointConfig.class) != null) {
                    Endpoint endpoint = context.getEndpointFactory().create(getEndpointName(field), annotation, context);
                    ReflectionUtils.setField(field, target, endpoint);

                    if (field.isAnnotationPresent(BindToRegistry.class)) {
                        context.getReferenceResolver().bind(ReferenceRegistry.getName(field.getAnnotation(BindToRegistry.class), endpoint.getName()), endpoint);
                    }
                    return;
                }
            }

            ReferenceResolver referenceResolver = context.getReferenceResolver();
            if (endpointAnnotation.properties().length > 0) {
                ReflectionUtils.setField(field, target, context.getEndpointFactory().create(getEndpointName(field), endpointAnnotation, field.getType(), context));
            } else if (StringUtils.hasText(endpointAnnotation.name()) && referenceResolver.isResolvable(endpointAnnotation.name())) {
                ReflectionUtils.setField(field, target, referenceResolver.resolve(endpointAnnotation.name(), field.getType()));
            } else if (referenceResolver.isResolvable(field.getName())) {
                ReflectionUtils.setField(field, target, referenceResolver.resolve(field.getName(), field.getType()));
            } else {
                ReflectionUtils.setField(field, target, referenceResolver.resolve(field.getType()));
            }
        }, field -> {
            if (field.isAnnotationPresent(CitrusEndpoint.class) &&
                    Endpoint.class.isAssignableFrom(field.getType())) {
                if (!field.canAccess(target)) {
                    ReflectionUtils.makeAccessible(field);
                }

                return true;
            }

            return false;
        });
    }

    /**
     * Either reads {@link CitrusEndpoint} name property or constructs endpoint name from field name.
     * @param field
     * @return
     */
    private static String getEndpointName(Field field) {
        if (field.getAnnotation(CitrusEndpoint.class) != null && StringUtils.hasText(field.getAnnotation(CitrusEndpoint.class).name())) {
            return field.getAnnotation(CitrusEndpoint.class).name();
        }

        return field.getName();
    }
}
