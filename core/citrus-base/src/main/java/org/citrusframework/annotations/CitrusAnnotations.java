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

package org.citrusframework.annotations;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import org.citrusframework.Citrus;
import org.citrusframework.CitrusContext;
import org.citrusframework.GherkinTestActionRunner;
import org.citrusframework.TestActionRunner;
import org.citrusframework.TestCaseRunner;
import org.citrusframework.common.Named;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.BindToRegistry;
import org.citrusframework.spi.ReferenceRegistry;
import org.citrusframework.util.ReflectionHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dependency injection support for {@link CitrusFramework}, {@link CitrusResource} and {@link CitrusEndpoint} annotations.
 *
 * @author Christoph Deppisch
 * @since 2.5
 */
public abstract class CitrusAnnotations {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(CitrusAnnotations.class);

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

        CitrusContext citrusContext = citrusFramework.getCitrusContext();
        injectCitrusContext(target, citrusContext);

        citrusContext.parseConfiguration(target);

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
        ReflectionHelper.doWithFields(testCase.getClass(), field -> {
            if (!field.isAnnotationPresent(CitrusFramework.class) || !Citrus.class.isAssignableFrom(field.getType())) {
                return;
            }

            logger.trace(String.format("Injecting Citrus framework instance on test class field '%s'", field.getName()));
            ReflectionHelper.setField(field, testCase, citrusFramework);
        });
    }

    /**
     * Inject Citrus context instance to the test class fields with {@link CitrusResource} annotation.
     * @param target
     * @param context
     */
    public static void injectCitrusContext(final Object target, final CitrusContext context) {
        ReflectionHelper.doWithFields(target.getClass(), field -> {
            if (!field.isAnnotationPresent(CitrusResource.class) || !CitrusContext.class.isAssignableFrom(field.getType())) {
                return;
            }

            logger.trace(String.format("Injecting Citrus context instance on test class field '%s'", field.getName()));
            ReflectionHelper.setField(field, target, context);
        });
    }

    /**
     * Inject test context instance to the test class fields with {@link CitrusResource} annotation.
     * @param target
     * @param context
     */
    public static void injectTestContext(final Object target, final TestContext context) {
        ReflectionHelper.doWithFields(target.getClass(), field -> {
            if (!field.isAnnotationPresent(CitrusResource.class) || !TestContext.class.isAssignableFrom(field.getType())) {
                return;
            }

            Class<?> type = field.getType();
            if (TestContext.class.isAssignableFrom(type)) {
                logger.trace(String.format("Injecting test context instance on test class field '%s'", field.getName()));
                ReflectionHelper.setField(field, target, context);
            } else {
                throw new CitrusRuntimeException("Not able to provide a Citrus resource injection for type " + type);
            }
        });
    }

    /**
     * Inject test runner instance to the test class fields with {@link CitrusResource} annotation.
     * @param target
     * @param runner
     */
    public static void injectTestRunner(final Object target, final TestCaseRunner runner) {
        ReflectionHelper.doWithFields(target.getClass(), field -> {
            if (!field.isAnnotationPresent(CitrusResource.class) || !TestCaseRunner.class.isAssignableFrom(field.getType())) {
                return;
            }

            Class<?> type = field.getType();
            if (TestCaseRunner.class.isAssignableFrom(type)) {
                logger.trace(String.format("Injecting test runner instance on test class field '%s'", field.getName()));
                ReflectionHelper.setField(field, target, runner);
            } else {
                throw new CitrusRuntimeException("Not able to provide a Citrus resource injection for type " + type);
            }
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
        ReflectionHelper.doWithFields(target.getClass(), field -> {
            if (!field.isAnnotationPresent(CitrusResource.class) || !TestActionRunner.class.isAssignableFrom(field.getType())) {
                return;
            }

            Class<?> type = field.getType();
            if (TestActionRunner.class.isAssignableFrom(type)) {
                logger.trace(String.format("Injecting test action runner instance on test class field '%s'", field.getName()));
                ReflectionHelper.setField(field, target, runner);
            } else {
                throw new CitrusRuntimeException("Not able to provide a Citrus resource injection for type " + type);
            }
        });
    }

    /**
     * Inject test action runner instance to the test class fields with {@link CitrusResource} annotation.
     * @param target
     * @param runner
     */
    private static void injectGherkinTestActionRunner(final Object target, final GherkinTestActionRunner runner) {
        ReflectionHelper.doWithFields(target.getClass(), field -> {
            if (!field.isAnnotationPresent(CitrusResource.class) || !GherkinTestActionRunner.class.isAssignableFrom(field.getType())) {
                return;
            }

            Class<?> type = field.getType();
            if (GherkinTestActionRunner.class.isAssignableFrom(type)) {
                logger.trace(String.format("Injecting test action runner instance on test class field '%s'", field.getName()));
                ReflectionHelper.setField(field, target, runner);
            } else {
                throw new CitrusRuntimeException("Not able to provide a Citrus resource injection for type " + type);
            }
        });
    }

    /**
     * Parse given configuration class and bind annotated fields, methods to reference registry.
     * @param configClass
     * @param citrusContext
     */
    public static void parseConfiguration(Class<?> configClass, CitrusContext citrusContext) {
        try {
            parseConfiguration(configClass.getConstructor().newInstance(), citrusContext);
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
            throw new CitrusRuntimeException("Missing or non-accessible default constructor on custom configuration class", e);
        }
    }

    /**
     * Parse given configuration class and bind annotated fields, methods to reference registry.
     * @param configuration
     * @param citrusContext
     */
    public static void parseConfiguration(Object configuration, CitrusContext citrusContext) {
        Class<?> configClass = configuration.getClass();

        if (configClass.isAnnotationPresent(CitrusConfiguration.class)) {
            for (Class<?> type : configClass.getAnnotation(CitrusConfiguration.class).classes()) {
                citrusContext.parseConfiguration(type);
            }
        }

        Arrays.stream(configClass.getDeclaredMethods())
                .filter(m -> m.getAnnotation(BindToRegistry.class) != null)
                .forEach(m -> {
                    try {
                        String name = ReferenceRegistry.getName(m.getAnnotation(BindToRegistry.class), m.getName());
                        Object component = m.invoke(configuration);

                        if (component instanceof Named named) {
                            named.setName(name);
                        }

                        citrusContext.addComponent(name, component);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new CitrusRuntimeException("Failed to invoke configuration method", e);
                    }
                });

        Arrays.stream(configClass.getDeclaredFields())
                .filter(f -> f.getAnnotation(BindToRegistry.class) != null)
                .peek(f -> {
                    if ((!Modifier.isPublic(f.getModifiers()) ||
                            !Modifier.isPublic(f.getDeclaringClass().getModifiers()) ||
                            Modifier.isFinal(f.getModifiers())) && !f.isAccessible()) {
                        f.setAccessible(true);
                    }
                })
                .forEach(f -> {
                    try {
                        String name = ReferenceRegistry.getName(f.getAnnotation(BindToRegistry.class), f.getName());
                        Object component = f.get(configuration);

                        if (component instanceof Named named) {
                            named.setName(name);
                        }

                        citrusContext.addComponent(name, component);
                    } catch (IllegalAccessException e) {
                        throw new CitrusRuntimeException("Failed to access configuration field", e);
                    }
                });
    }
}
