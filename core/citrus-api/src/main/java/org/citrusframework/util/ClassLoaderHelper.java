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
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.citrusframework.exceptions.CitrusRuntimeException;
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

    private static final Map<String, URL> additionalArtifacts = new ConcurrentHashMap<>();

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

        if (additionalArtifacts.isEmpty() || cl instanceof OpenURLClassLoader) {
            return cl;
        }

        OpenURLClassLoader openURLClassLoader = new OpenURLClassLoader(cl);
        additionalArtifacts.values().forEach(openURLClassLoader::addURL);
        return openURLClassLoader;
    }

    public static synchronized ClassLoader getContextClassLoader() {
        if (additionalArtifacts.isEmpty()) {
            return Thread.currentThread().getContextClassLoader();
        }

        if (ccl == null) {
            // save original context class loader for later restore option
            ccl = Thread.currentThread().getContextClassLoader();
        }

        return adapt(Thread.currentThread().getContextClassLoader());
    }

    public static ClassLoader getClassLoader() {
        return adapt(ClassLoaderHelper.class.getClassLoader());
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
     * Adds a dynamic class loader entry in the form of a URL.
     * When this helper resolves class loaders the dynamic entry will be part of the class loader.
     */
    public static synchronized void addArtifact(String gav, URL url) {
        if (ccl != null) {
            throw new IllegalStateException("Not allowed to add additional artifacts, " +
                    "because the context class loader has been adapted already!");
        }

        additionalArtifacts.putIfAbsent(gav, url);
    }

    /**
     * Clears additional artifacts and restores context class loader to its initial state before adapting.
     */
    public static synchronized void restore() {
        if (!additionalArtifacts.isEmpty()) {
            additionalArtifacts.clear();
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
     * Special URL class loader allows to add new file URLs.
     */
    public static class OpenURLClassLoader extends URLClassLoader {

        OpenURLClassLoader() {
            super(new URL[0]);
        }

        OpenURLClassLoader(ClassLoader parent) {
            super(new URL[0], parent);
        }

        @Override
        protected void addURL(URL url) {
            super.addURL(url);
        }
    }
}
