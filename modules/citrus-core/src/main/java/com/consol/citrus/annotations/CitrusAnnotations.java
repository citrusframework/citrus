/*
 * Copyright 2006-2016 the original author or authors.
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

package com.consol.citrus.annotations;

import com.consol.citrus.Citrus;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.endpoint.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

/**
 * Dependency injection support for {@link CitrusEndpoint} endpoint annotations.
 *
 * @author Christoph Deppisch
 * @since 2.5
 */
public abstract class CitrusAnnotations {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(CitrusAnnotations.class);

    /**
     * Prevent instantiation.
     */
    private CitrusAnnotations() {
        super();
    }

    /**
     * Creates new Citrus instance and injects all supported components and endpoints to target object using annotations.
     * @param target
     */
    public static final void injectAll(final Object target) {
        injectAll(target, Citrus.newInstance());
    }

    /**
     * Creates new Citrus test context and injects all supported components and endpoints to target object using annotations.
     * @param target
     */
    public static final void injectAll(final Object target, final Citrus citrusFramework) {
        injectAll(target, citrusFramework, citrusFramework.createTestContext());
    }

    /**
     * Injects all supported components and endpoints to target object using annotations.
     * @param target
     */
    public static final void injectAll(final Object target, final Citrus citrusFramework, final TestContext context) {
        injectCitrusFramework(target, citrusFramework);
        injectEndpoints(target, context);
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
     * Inject Citrus framework instance to the test class fields with {@link CitrusFramework} annotation.
     * @param testCase
     * @param citrusFramework
     */
    public static final void injectCitrusFramework(final Object testCase, final Citrus citrusFramework) {
        ReflectionUtils.doWithFields(testCase.getClass(), new ReflectionUtils.FieldCallback() {
            @Override
            public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
                log.debug(String.format("Injecting Citrus framework instance on test class field '%s'", field.getName()));
                ReflectionUtils.setField(field, testCase, citrusFramework);
            }
        }, new ReflectionUtils.FieldFilter() {
            @Override
            public boolean matches(Field field) {
                if (field.isAnnotationPresent(CitrusFramework.class) &&
                        Citrus.class.isAssignableFrom(field.getType())) {
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
