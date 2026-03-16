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

package org.citrusframework.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.citrusframework.TestActionBuilder;
import org.citrusframework.common.TestLoader;
import org.citrusframework.config.annotation.AnnotationConfigParser;
import org.citrusframework.container.TemplateLoader;
import org.citrusframework.context.resolver.TypeAliasResolver;
import org.citrusframework.endpoint.EndpointBuilder;
import org.citrusframework.endpoint.EndpointComponent;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.main.TestEngine;
import org.citrusframework.message.MessageProcessor;
import org.citrusframework.message.MessageSelector;
import org.citrusframework.message.ScriptPayloadBuilder;
import org.citrusframework.validation.HeaderValidator;
import org.citrusframework.validation.MessageValidator;
import org.citrusframework.validation.SchemaValidator;
import org.citrusframework.validation.ValueMatcher;
import org.citrusframework.validation.context.ValidationContext;
import org.citrusframework.validation.script.sql.SqlResultSetScriptValidator;
import org.citrusframework.variable.SegmentVariableExtractorRegistry;
import org.citrusframework.variable.VariableExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.lang.String.format;

/**
 * Class loader helper class that is aware of dynamically loaded Maven artifacts.
 * Helper is able to adapt class loaders with the additional dependencies in the form of artifact URLs.
 * The helper also provides class instantiation utilities that make sure to also honor the additional artifacts in classpath.
 */
public final class ClassLoaderHelper {

    /**
     * Logger
     */
    private static final Logger logger = LoggerFactory.getLogger(ClassLoaderHelper.class);

    /**
     * Supported static instance field in target - used as a fallback to the default constructor
     */
    private static final String INSTANCE = "INSTANCE";

    /**
     * Map of additional artifacts that should be loaded permanently to the classpath for each class loader.
     */
    private static final Map<String, URL> artifacts = new ConcurrentHashMap<>();

    /**
     * Map of additional artifacts that should be loaded temporarily to the classpath for each class loader.
     * When reset these artifacts are cleared.
     */
    private static final Map<String, URL> temporaryArtifacts = new ConcurrentHashMap<>();

    /**
     * Reference to the plain original context class loader. Used to restore class loader to its initial state.
     */
    private static ClassLoader ccl;

    private ClassLoaderHelper() {
        // prevent instantiation of utility class
    }

    /**
     * Instantiate a type by its name. Uses the current thread context class loader to instantiate.
     */
    public static <T> T instantiateType(String type, Object... initargs) {
        return instantiateType(type, getContextClassLoader(), initargs);
    }

    /**
     * Instantiate a type by its name. Use the given caller to resolve the class loader.
     */
    public static <T> T instantiateType(String type, Class<?> caller, Object... initargs) {
        return instantiateType(type, getClassLoader(caller), initargs);
    }

    /**
     * Instantiate a type by its name. Uses given class loader to instantiate.
     */
    public static <T> T instantiateType(String type, ClassLoader cl, Object... initargs) {
        try {
            if (initargs.length == 0) {
                return (T) Class.forName(type, true, cl).getDeclaredConstructor().newInstance();
            } else {
                return (T) getConstructor(Class.forName(type, true, cl), initargs).newInstance(initargs);
            }
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException |
                 NoSuchMethodException | InvocationTargetException e) {
            try {
                if (
                    Arrays.stream(Class.forName(type, true, cl).getFields())
                            .anyMatch(field -> field.getName().equals(INSTANCE)
                                    && Modifier.isStatic(field.getModifiers()))
                ) {
                    return (T) Class.forName(type, true, cl).getField(INSTANCE).get(null);
                }
            } catch (IllegalAccessException | NoSuchFieldException | ClassNotFoundException e1) {
                throw new CitrusRuntimeException(
                        format("Failed to resolve classpath resource of type '%s' - caused by: %s",
                                type, Optional.ofNullable(e1.getMessage()).orElse(e1.getClass().getName())), e1);
            }

            logger.warn(
                    "Neither static instance nor accessible default constructor ({}) is given on type '{}'",
                    Arrays.toString(getParameterTypes(initargs)),
                    type
            );

            throw new CitrusRuntimeException(
                    format("Failed to resolve classpath resource of type '%s' - caused by: %s",
                            type, Optional.ofNullable(e.getMessage()).orElse(e.getClass().getName())), e);
        }
    }

    /**
     * Adapt given class loader to also use additional artifacts if any.
     * Wraps given class loader if additional artifacts are present.
     */
    public static ClassLoader adapt(ClassLoader cl) {
        if (cl == null) {
            return null;
        }

        if (artifacts.isEmpty() && temporaryArtifacts.isEmpty()) {
            return cl;
        }

        OpenURLClassLoader openURLClassLoader;
        if (cl instanceof OpenURLClassLoader existing) {
            openURLClassLoader = existing;
        } else {
            openURLClassLoader = new OpenURLClassLoader(cl);
        }

        artifacts.forEach(openURLClassLoader::addArtifact);
        temporaryArtifacts.forEach(openURLClassLoader::addArtifact);
        return openURLClassLoader;
    }

    public static synchronized ClassLoader getContextClassLoader() {
        if (artifacts.isEmpty() && temporaryArtifacts.isEmpty()) {
            return Thread.currentThread().getContextClassLoader();
        }

        if (ccl == null) {
            // save original context class loader for later restore option
            ccl = Thread.currentThread().getContextClassLoader();
        }

        return adapt(Thread.currentThread().getContextClassLoader());
    }

    public static ClassLoader getClassLoader() {
        ClassLoader classLoader = getContextClassLoader();
        if (classLoader != null) {
            return classLoader;
        }

        return getClassLoader(ClassLoaderHelper.class);
    }

    public static ClassLoader getClassLoader(Class<?> type) {
        return adapt(type.getClassLoader());
    }

    /**
     * Gets the constructor best matching the given parameter types.
     */
    private static Constructor<?> getConstructor(Class<?> type, Object[] initargs) {
        final Class<?>[] parameterTypes = getParameterTypes(initargs);

        Optional<Constructor<?>> exactMatch = Arrays.stream(type.getDeclaredConstructors())
                .filter(
                        constructor -> Arrays.equals(replacePrimitiveTypes(constructor), parameterTypes))
                .findFirst();

        if (exactMatch.isPresent()) {
            return exactMatch.get();
        }

        Optional<Constructor<?>> match = Arrays.stream(type.getDeclaredConstructors())
                .filter(constructor -> {
                    if (constructor.getParameterCount() != parameterTypes.length) {
                        return false;
                    }

                    for (int i = 0; i < parameterTypes.length; i++) {
                        if (!constructor.getParameterTypes()[i].isAssignableFrom(parameterTypes[i])) {
                            return false;
                        }
                    }

                    return true;
                })
                .findFirst();

        if (match.isPresent()) {
            return match.get();
        }

        throw new IllegalArgumentException(
                format(
                        "No matching constructor found for type %s and parameters %s",
                        type.getName(),
                        Arrays.toString(parameterTypes)
                )
        );
    }

    /**
     * Get types of init arguments.
     */
    private static Class<?>[] getParameterTypes(Object... initargs) {
        return Arrays.stream(initargs)
                .map(Object::getClass)
                .toArray(Class[]::new);
    }

    /**
     * Get the types of a constructor. Primitive types are converted to their respective object
     * type.
     */
    private static Class<?>[] replacePrimitiveTypes(Constructor<?> constructor) {
        Class<?>[] constructorParameters = constructor.getParameterTypes();
        for (int i = 0; i < constructorParameters.length; i++) {
            if (constructorParameters[i] == int.class) {
                constructorParameters[i] = Integer.class;
            } else if (constructorParameters[i] == short.class) {
                constructorParameters[i] = Short.class;
            } else if (constructorParameters[i] == double.class) {
                constructorParameters[i] = Double.class;
            } else if (constructorParameters[i] == float.class) {
                constructorParameters[i] = Float.class;
            } else if (constructorParameters[i] == char.class) {
                constructorParameters[i] = Character.class;
            } else if (constructorParameters[i] == boolean.class) {
                constructorParameters[i] = Boolean.class;
            }
        }

        return constructorParameters;
    }

    /**
     * Adds a permanent dynamic class loader entry in the form of a URL.
     * When this helper resolves class loaders the dynamic entry will be part of the class loader.
     */
    public static synchronized void addArtifact(String gav, URL url) {
        addArtifact(gav, url, true);
    }

    /**
     * Adds a dynamic class loader entry in the form of a URL.
     * When this helper resolves class loaders the dynamic entry will be part of the class loader.
     */
    public static synchronized void addArtifact(String gav, URL url, boolean permanent) {
        if (permanent) {
            artifacts.putIfAbsent(gav, url);
        } else {
            temporaryArtifacts.putIfAbsent(gav, url);
        }
    }

    /**
     * Clears additional artifacts and restores context class loader to its initial state before adapting.
     */
    public static synchronized void restore() {
        if (!artifacts.isEmpty() || !temporaryArtifacts.isEmpty()) {
            reset();

            try {
                if (ccl != null) {
                    Thread.currentThread().setContextClassLoader(ccl);
                    ccl = null;
                }
            } catch (Throwable e) {
                logger.warn("Failed to restore context class loader", e);
            }
        }
    }

    /**
     * Reset temporary artifacts only.
     */
    public static synchronized void reset() {
        temporaryArtifacts.clear();
    }

    /**
     * Set context class loader for current thread with adapted instance.
     */
    public static synchronized boolean updateContextClassloader() {
        return updateContextClassloader(false);
    }

    /**
     * Set context class loader for current thread with adapted instance.
     */
    public static synchronized boolean updateContextClassloader(boolean clearCache) {
        try {
            // Adapt and set context class loader for current thread
            Thread.currentThread().setContextClassLoader(ClassLoaderHelper.getContextClassLoader());
        } catch (Throwable e) {
            logger.warn("Failed to update context class loader due to '%s'".formatted(e.getMessage()));
            return false;
        }

        if (clearCache) {
            clearCache();
        }

        return true;
    }

    /**
     * Clear cache for resource path lookup. Required after the classpath has been adapted with additional artifacts.
     */
    public static void clearCache() {
        TestActionBuilder.clearCache();
        TestLoader.clearCache();
        TypeAliasResolver.clearCache();
        HeaderValidator.clearCache();
        MessageValidator.clearCache();
        SchemaValidator.clearCache();
        EndpointBuilder.clearCache();
        EndpointComponent.clearCache();
        MessageSelector.clearCache();
        ScriptPayloadBuilder.clearCache();
        ValueMatcher.clearCache();
        AnnotationConfigParser.clearCache();
        ValidationContext.clearCache();
        SqlResultSetScriptValidator.clearCache();
        SegmentVariableExtractorRegistry.clearCache();

        TestEngine.clearCache();
        TemplateLoader.clearCache();
        MessageProcessor.clearCache();
        VariableExtractor.clearCache();
    }

    /**
     * Special URL class loader allows to add new file URLs.
     */
    public static class OpenURLClassLoader extends URLClassLoader {

        private final Set<String> addedArtifacts =  new HashSet<>();

        OpenURLClassLoader() {
            super(new URL[0]);
        }

        OpenURLClassLoader(ClassLoader parent) {
            super(new URL[0], parent);
        }

        /**
         * Adds the given URL if artifact has not been added before.
         */
        protected void addArtifact(String gav, URL url) {
            if (addedArtifacts.add(gav)) {
                addURL(url);
            }
        }
    }
}
