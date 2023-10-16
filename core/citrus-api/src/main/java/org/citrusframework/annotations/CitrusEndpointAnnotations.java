package org.citrusframework.annotations;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.spi.BindToRegistry;
import org.citrusframework.spi.ReferenceRegistry;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.util.ReflectionHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dependency injection support for {@link CitrusEndpoint} endpoint annotations.
 *
 * @author Christoph Deppisch
 */
public abstract class CitrusEndpointAnnotations {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(CitrusEndpointAnnotations.class);

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
        ReflectionHelper.doWithFields(target.getClass(), field -> {
            if (!field.isAnnotationPresent(CitrusEndpoint.class) || !Endpoint.class.isAssignableFrom(field.getType())) {
                return;
            }

            logger.debug(String.format("Injecting Citrus endpoint on test class field '%s'", field.getName()));
            CitrusEndpoint endpointAnnotation = field.getAnnotation(CitrusEndpoint.class);

            for (Annotation annotation : field.getAnnotations()) {
                if (annotation.annotationType().getAnnotation(CitrusEndpointConfig.class) != null) {
                    Endpoint endpoint = context.getEndpointFactory().create(getEndpointName(field), annotation, context);
                    ReflectionHelper.setField(field, target, endpoint);

                    if (field.isAnnotationPresent(BindToRegistry.class)) {
                        context.getReferenceResolver().bind(ReferenceRegistry.getName(field.getAnnotation(BindToRegistry.class), endpoint.getName()), endpoint);
                    }
                    return;
                }
            }

            ReferenceResolver referenceResolver = context.getReferenceResolver();
            if (endpointAnnotation.properties().length > 0) {
                ReflectionHelper.setField(field, target, context.getEndpointFactory().create(getEndpointName(field), endpointAnnotation, field.getType(), context));
            } else if (endpointAnnotation.name() != null && !endpointAnnotation.name().isBlank() &&
                    referenceResolver.isResolvable(endpointAnnotation.name())) {
                ReflectionHelper.setField(field, target, referenceResolver.resolve(endpointAnnotation.name(), field.getType()));
            } else if (referenceResolver.isResolvable(field.getName())) {
                ReflectionHelper.setField(field, target, referenceResolver.resolve(field.getName(), field.getType()));
            } else {
                ReflectionHelper.setField(field, target, referenceResolver.resolve(field.getType()));
            }
        });
    }

    /**
     * Either reads {@link CitrusEndpoint} name property or constructs endpoint name from field name.
     * @param field
     * @return
     */
    private static String getEndpointName(Field field) {
        if (field.getAnnotation(CitrusEndpoint.class) != null &&
                field.getAnnotation(CitrusEndpoint.class).name() != null &&
                !field.getAnnotation(CitrusEndpoint.class).name().isBlank()) {
            return field.getAnnotation(CitrusEndpoint.class).name();
        }

        return field.getName();
    }
}
