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

import java.lang.reflect.Field;

import com.consol.citrus.Citrus;
import com.consol.citrus.GherkinTestActionRunner;
import com.consol.citrus.TestActionRunner;
import com.consol.citrus.TestCaseRunner;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;

/**
 * Dependency injection support for {@link CitrusFramework}, {@link CitrusResource} and {@link CitrusEndpoint} annotations.
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
    public static void injectAll(final Object target) {
        injectAll(target, Citrus.newInstance());
    }

    /**
     * Creates new Citrus test context and injects all supported components and endpoints to target object using annotations.
     * @param target
     */
    public static void injectAll(final Object target, final Citrus citrusFramework) {
        injectAll(target, citrusFramework, citrusFramework.getCitrusContext().createTestContext());
    }

    /**
     * Injects all supported components and endpoints to target object using annotations.
     * @param target
     */
    public static void injectAll(final Object target, final Citrus citrusFramework, final TestContext context) {
        injectCitrusFramework(target, citrusFramework);

        citrusFramework.getCitrusContext().parseConfiguration(target);

        injectEndpoints(target, context);
        injectTestContext(target, context);
    }

    /**
     * Reads all {@link CitrusEndpoint} and {@link CitrusEndpointConfig} related annotations on target object field declarations and
     * injects proper endpoint instances.
     *
     * @param target
     * @param context
     */
    public static void injectEndpoints(final Object target, final TestContext context) {
        CitrusEndpointAnnotations.injectEndpoints(target, context);
    }

    /**
     * Inject Citrus framework instance to the test class fields with {@link CitrusFramework} annotation.
     * @param testCase
     * @param citrusFramework
     */
    public static void injectCitrusFramework(final Object testCase, final Citrus citrusFramework) {
        ReflectionUtils.doWithFields(testCase.getClass(), new ReflectionUtils.FieldCallback() {
            @Override
            public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
                log.trace(String.format("Injecting Citrus framework instance on test class field '%s'", field.getName()));
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
     * Inject test context instance to the test class fields with {@link CitrusResource} annotation.
     * @param target
     * @param context
     */
    public static void injectTestContext(final Object target, final TestContext context) {
        ReflectionUtils.doWithFields(target.getClass(), field -> {
            Class<?> type = field.getType();
            if (TestContext.class.isAssignableFrom(type)) {
                log.trace(String.format("Injecting test context instance on test class field '%s'", field.getName()));
                ReflectionUtils.setField(field, target, context);
            } else {
                throw new CitrusRuntimeException("Not able to provide a Citrus resource injection for type " + type);
            }
        }, field -> {
            if (field.isAnnotationPresent(CitrusResource.class) && TestContext.class.isAssignableFrom(field.getType())) {
                if (!field.isAccessible()) {
                    ReflectionUtils.makeAccessible(field);
                }

                return true;
            }

            return false;
        });
    }

    /**
     * Inject test runner instance to the test class fields with {@link CitrusResource} annotation.
     * @param target
     * @param runner
     */
    public static void injectTestRunner(final Object target, final TestCaseRunner runner) {
        ReflectionUtils.doWithFields(target.getClass(), field -> {
            Class<?> type = field.getType();
            if (TestCaseRunner.class.isAssignableFrom(type)) {
                log.trace(String.format("Injecting test runner instance on test class field '%s'", field.getName()));
                ReflectionUtils.setField(field, target, runner);
            } else {
                throw new CitrusRuntimeException("Not able to provide a Citrus resource injection for type " + type);
            }
        }, field -> {
            if (field.isAnnotationPresent(CitrusResource.class) && TestCaseRunner.class.isAssignableFrom(field.getType())) {
                if (!field.isAccessible()) {
                    ReflectionUtils.makeAccessible(field);
                }

                return true;
            }

            return false;
        });

        injectTestActionRunner(target, runner);
        injectGherkinTestActionRunner(target, runner);
    }

    /**
     * Inject test action runner instance to the test class fields with {@link CitrusResource} annotation.
     * @param target
     * @param runner
     */
    private static void injectTestActionRunner(final Object target, final TestActionRunner runner) {
        ReflectionUtils.doWithFields(target.getClass(), field -> {
            Class<?> type = field.getType();
            if (TestActionRunner.class.isAssignableFrom(type)) {
                log.trace(String.format("Injecting test action runner instance on test class field '%s'", field.getName()));
                ReflectionUtils.setField(field, target, runner);
            } else {
                throw new CitrusRuntimeException("Not able to provide a Citrus resource injection for type " + type);
            }
        }, field -> {
            if (field.isAnnotationPresent(CitrusResource.class) && TestActionRunner.class.isAssignableFrom(field.getType())) {
                if (!field.isAccessible()) {
                    ReflectionUtils.makeAccessible(field);
                }

                return true;
            }

            return false;
        });
    }

    /**
     * Inject test action runner instance to the test class fields with {@link CitrusResource} annotation.
     * @param target
     * @param runner
     */
    private static void injectGherkinTestActionRunner(final Object target, final GherkinTestActionRunner runner) {
        ReflectionUtils.doWithFields(target.getClass(), field -> {
            Class<?> type = field.getType();
            if (GherkinTestActionRunner.class.isAssignableFrom(type)) {
                log.trace(String.format("Injecting test action runner instance on test class field '%s'", field.getName()));
                ReflectionUtils.setField(field, target, runner);
            } else {
                throw new CitrusRuntimeException("Not able to provide a Citrus resource injection for type " + type);
            }
        }, field -> {
            if (field.isAnnotationPresent(CitrusResource.class) && GherkinTestActionRunner.class.isAssignableFrom(field.getType())) {
                if (!field.isAccessible()) {
                    ReflectionUtils.makeAccessible(field);
                }

                return true;
            }

            return false;
        });
    }
}
