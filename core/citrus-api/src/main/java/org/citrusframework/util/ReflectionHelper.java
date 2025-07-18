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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import jakarta.annotation.Nonnull;
import org.citrusframework.exceptions.CitrusRuntimeException;

import static java.lang.String.format;

/**
 * Helper for working with reflection on classes.
 *  <p/>
 *  This code is based on org.apache.camel.util.ReflectionHelper class.
 */
public class ReflectionHelper {

    private ReflectionHelper() {
        // utility class
    }

    /**
     * Callback interface invoked on each field in the hierarchy.
     */
    @FunctionalInterface
    public interface FieldCallback {

        /**
         * Perform an operation using the given field.
         *
         * @param field the field to operate on
         */
        void doWith(Field field) throws IllegalArgumentException, IllegalAccessException;
    }

    /**
     * Action to take on each method.
     */
    @FunctionalInterface
    public interface MethodCallback {

        /**
         * Perform an operation using the given method.
         *
         * @param method the method to operate on
         */
        void doWith(Method method) throws IllegalArgumentException, IllegalAccessException;
    }

    /**
     * Action to take on each class.
     */
    @FunctionalInterface
    public interface ClassCallback {

        /**
         * Perform an operation using the given class.
         *
         * @param clazz the class to operate on
         */
        void doWith(Class<?> clazz) throws IllegalArgumentException, IllegalAccessException;
    }

    /**
     * Perform the given callback operation on the nested (inner) classes.
     *
     * @param clazz class to start looking at
     * @param cc    the callback to invoke for each inner class (excluding the class itself)
     */
    public static void doWithClasses(Class<?> clazz, ClassCallback cc) throws IllegalArgumentException {
        // and then nested classes
        Class<?>[] classes = clazz.getDeclaredClasses();
        for (Class<?> aClazz : classes) {
            try {
                cc.doWith(aClazz);
            } catch (IllegalAccessException ex) {
                throw new IllegalStateException("Shouldn't be illegal to access class '" + aClazz.getName() + "': " + ex);
            }
        }
    }

    /**
     * Invoke the given callback on all fields in the target class, going up the class hierarchy to get all declared
     * fields.
     *
     * @param clazz the target class to analyze
     * @param fc    the callback to invoke for each field
     */
    public static void doWithFields(Class<?> clazz, FieldCallback fc) throws IllegalArgumentException {
        // Keep backing up the inheritance hierarchy.
        Class<?> targetClass = clazz;
        do {
            Field[] fields = targetClass.getDeclaredFields();
            for (Field field : fields) {
                try {
                    fc.doWith(field);
                } catch (IllegalAccessException ex) {
                    throw new IllegalStateException("Shouldn't be illegal to access field '" + field.getName() + "': " + ex);
                }
            }
            targetClass = targetClass.getSuperclass();
        } while (targetClass != null && targetClass != Object.class);
    }

    /**
     * Perform the given callback operation on all matching methods of the given class and superclasses (or given
     * interface and super-interfaces).
     * <p/>
     * <b>Important:</b> This method does not take the {@link java.lang.reflect.Method#isBridge() bridge methods} into
     * account.
     *
     * @param clazz class to start looking at
     * @param mc    the callback to invoke for each method
     */
    public static void doWithMethods(Class<?> clazz, MethodCallback mc) throws IllegalArgumentException {
        // Keep backing up the inheritance hierarchy.
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            if (method.isBridge()) {
                // skip the bridge methods which in Java 8 leads to problems with inheritance
                // see https://bugs.openjdk.java.net/browse/JDK-6695379
                continue;
            }
            try {
                mc.doWith(method);
            } catch (IllegalAccessException ex) {
                throw new IllegalStateException("Shouldn't be illegal to access method '" + method.getName() + "': " + ex);
            }
        }
        if (clazz.getSuperclass() != null) {
            doWithMethods(clazz.getSuperclass(), mc);
        } else if (clazz.isInterface()) {
            for (Class<?> superIfc : clazz.getInterfaces()) {
                doWithMethods(superIfc, mc);
            }
        }
    }

    /**
     * Attempt to find a {@link Field field} on the supplied {@link Class} with the
     * supplied {@code name} and/or {@link Class type}. Searches all superclasses
     * up to {@link Object}.
     * @param clazz the class to introspect
     * @param name the name of the field (maybe {@code null} if type is specified)
     * @return the corresponding Field object, or {@code null} if not found
     */
    public static Field findField(Class<?> clazz, String name) {
        ObjectHelper.assertNotNull(clazz, "Class must not be null");
        Class<?> searchType = clazz;
        while (Object.class != searchType && searchType != null) {
            Field[] fields = searchType.getDeclaredFields();
            for (Field field : fields) {
                if (name.equals(field.getName())) {
                    return field;
                }
            }
            searchType = searchType.getSuperclass();
        }
        return null;
    }

    /**
     * Attempt to find a {@link Method} on the supplied class with the supplied name and parameter types. Searches all
     * superclasses up to {@code Object}.
     * <p>
     * Returns {@code null} if no {@link Method} can be found.
     *
     * @param  clazz      the class to introspect
     * @param  name       the name of the method
     * @param  paramTypes the parameter types of the method (may be {@code null} to indicate any signature)
     * @return            the Method object, or {@code null} if none found
     */
    public static Method findMethod(Class<?> clazz, String name, Class<?>... paramTypes) {
        ObjectHelper.assertNotNull(clazz, "Class must not be null");
        ObjectHelper.assertNotNull(name, "Method name must not be null");
        Class<?> searchType = clazz;
        while (searchType != null) {
            Method[] methods = searchType.isInterface() ? searchType.getMethods() : searchType.getDeclaredMethods();
            for (Method method : methods) {
                if (name.equals(method.getName())
                        && (paramTypes == null || Arrays.equals(paramTypes, method.getParameterTypes()))) {
                    return method;
                }
            }
            searchType = searchType.getSuperclass();
        }
        return null;
    }

    /**
     * Invoke the specified {@link Method} against the supplied target object with the
     * supplied arguments. The target object can be {@code null} when invoking a
     * static {@link Method}.
     * @param method the method to invoke
     * @param target the target object to invoke the method on
     * @param args the invocation arguments (maybe {@code null})
     * @return the invocation result, if any
     */
    public static Object invokeMethod(Method method, Object target, Object... args) {
        try {
            return method.invoke(target, args);
        } catch (InvocationTargetException | IllegalAccessException e) {
            if (e.getCause() instanceof CitrusRuntimeException runtimeException) {
                throw runtimeException;
            }

            throw new CitrusRuntimeException("Failed to invoke method", e);
        }
    }

    @SuppressWarnings("java:S3011")
    public static void setField(Field f, Object instance, Object value) {
        try {
            if (!Modifier.isPublic(f.getModifiers()) && !f.canAccess(instance)) {
                f.setAccessible(true);
            }
            f.set(instance, value);
        } catch (IllegalAccessException e) {
            throw new UnsupportedOperationException("Cannot set value of type: " + value.getClass() + " into field: " + f, e);
        }
    }

    @SuppressWarnings("java:S3011")
    public static Object getField(Field f, Object instance) {
        try {
            if ((!Modifier.isPublic(f.getModifiers()) ||
                    !Modifier.isPublic(f.getDeclaringClass().getModifiers()) ||
                    Modifier.isFinal(f.getModifiers())) &&
                    (Modifier.isStatic(f.getModifiers()) ? !f.canAccess(null) : !f.canAccess(instance))) {
                f.setAccessible(true);
            }
        } catch (Exception e) {
            // ignore
        }

        try {
            return f.get(instance);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Copies the values of all declared fields from a source object to a target object for the specified class.
     */
    @SuppressWarnings("java:S3011")
    public static void copyFields(@Nonnull Class<?> clazz, @Nonnull Object source, @Nonnull Object target) {
        Class<?> currentClass = clazz;

        while (currentClass != null) {
            Field[] fields = currentClass.getDeclaredFields();

            for (Field field : fields) {
                try {
                    field.setAccessible(true);
                    field.set(target, field.get(source));
                } catch (IllegalAccessException e) {
                    throw new CitrusRuntimeException(format(
                        "Unable to reflectively copy fields from source to target. clazz=%s sourceClass=%s targetClass=%s",
                        clazz, source.getClass(), target.getClass()));
                }
            }

            currentClass = currentClass.getSuperclass();
        }
    }
}
