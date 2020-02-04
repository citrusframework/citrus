package com.consol.citrus.annotations;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.endpoint.Endpoint;
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
    private static Logger log = LoggerFactory.getLogger(CitrusEndpointAnnotations.class);

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
    public static final void injectEndpoints(final Object target, final TestContext context) {
        ReflectionUtils.doWithFields(target.getClass(), new ReflectionUtils.FieldCallback() {
            @Override
            public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
                log.debug(String.format("Injecting Citrus endpoint on test class field '%s'", field.getName()));
                CitrusEndpoint endpointAnnotation = field.getAnnotation(CitrusEndpoint.class);

                for (Annotation annotation : field.getAnnotations()) {
                    if (annotation.annotationType().getAnnotation(CitrusEndpointConfig.class) != null) {
                        ReflectionUtils.setField(field, target, context.getEndpointFactory().create(getEndpointName(field), annotation, context));
                        return;
                    }
                }

                Endpoint endpoint;
                if (StringUtils.hasText(endpointAnnotation.name())) {
                    endpoint = context.getReferenceResolver().resolve(endpointAnnotation.name(), (Class<Endpoint>) field.getType());
                } else {
                    endpoint = context.getReferenceResolver().resolve((Class<Endpoint>) field.getType());
                }

                ReflectionUtils.setField(field, target, endpoint);
            }
        }, new ReflectionUtils.FieldFilter() {
            @Override
            public boolean matches(Field field) {
                if (field.isAnnotationPresent(CitrusEndpoint.class) &&
                        Endpoint.class.isAssignableFrom(field.getType())) {
                    if (!field.isAccessible()) {
                        ReflectionUtils.makeAccessible(field);
                    }

                    return true;
                }

                return false;
            }
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
