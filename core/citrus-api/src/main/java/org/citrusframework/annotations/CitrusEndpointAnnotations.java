/*
 * Copyright the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.annotations;

import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.spi.BindToRegistry;
import org.citrusframework.spi.ReferenceRegistry;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.util.ReflectionHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

/**
 * Dependency injection support for {@link CitrusEndpoint} endpoint annotations.
 *
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

            logger.debug("Injecting Citrus endpoint on test class field '{}'", field.getName());
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
