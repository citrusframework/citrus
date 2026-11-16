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
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ClassLoaderHelperTest {

    @Test
    void shouldInstantiateWithStringArg() {
        Target result = ClassLoaderHelper.instantiateType(Target.class, "hello");
        Assertions.assertEquals("hello", result.value);
    }

    @Test
    void shouldInstantiateWithPrimitiveArg() {
        Target result = ClassLoaderHelper.instantiateType(Target.class, 42);
        Assertions.assertEquals("42", result.value);
    }

    @Test
    void shouldInstantiateWithAssignableArg() {
        SubParam sub = new SubParam("sub");
        Target result = ClassLoaderHelper.instantiateType(Target.class, sub);
        Assertions.assertEquals("sub", result.value);
    }

    @Test
    void shouldFailWithNoMatchingConstructor() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> ClassLoaderHelper.instantiateType(Target.class, 1.5d));
    }

    @Test
    void shouldFindConstructorByNameAcrossClassLoaders() throws Exception {
        URL[] urls = getClassPathUrls();
        ClassLoader isolated = new ChildFirstClassLoader(urls, getClass().getClassLoader());

        Class<?> targetClass = isolated.loadClass(Target.class.getName());
        Class<?> paramClass = isolated.loadClass(Param.class.getName());

        Assertions.assertNotEquals(Target.class, targetClass);
        Assertions.assertNotEquals(Param.class, paramClass);
        Assertions.assertFalse(
                paramClass.isAssignableFrom(Param.class),
                "Cross-classloader classes should not be assignable"
        );

        Param parentParam = new Param("cross-cl");

        Method getConstructor = ClassLoaderHelper.class.getDeclaredMethod(
                "getConstructor", Class.class, Object[].class);
        getConstructor.setAccessible(true);

        Constructor<?> ctor = (Constructor<?>) getConstructor.invoke(
                null, targetClass, new Object[]{parentParam});

        Assertions.assertNotNull(ctor);
        Assertions.assertEquals(1, ctor.getParameterCount());
        Assertions.assertEquals(
                Param.class.getName(),
                ctor.getParameterTypes()[0].getName()
        );
    }

    private URL[] getClassPathUrls() {
        ClassLoader cl = getClass().getClassLoader();
        if (cl instanceof URLClassLoader ucl) {
            return ucl.getURLs();
        }

        String classpath = System.getProperty("java.class.path", "");
        return java.util.Arrays.stream(classpath.split(System.getProperty("path.separator")))
                .map(entry -> {
                    try {
                        return new java.io.File(entry).toURI().toURL();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .toArray(URL[]::new);
    }

    /**
     * Loads test inner classes from its own URLs first (child-first), breaking
     * standard parent-first delegation. This simulates the cross-classloader
     * scenario where the same class is loaded by two different classloaders.
     */
    private static class ChildFirstClassLoader extends URLClassLoader {

        private static final String TEST_CLASS_PREFIX =
                "org.citrusframework.util.ClassLoaderHelperTest$";

        private final ClassLoader parentFallback;

        ChildFirstClassLoader(URL[] urls, ClassLoader parent) {
            super(urls, null);
            this.parentFallback = parent;
        }

        @Override
        protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            Class<?> loaded = findLoadedClass(name);
            if (loaded != null) {
                return loaded;
            }

            if (name.startsWith(TEST_CLASS_PREFIX)) {
                try {
                    Class<?> c = findClass(name);
                    if (resolve) {
                        resolveClass(c);
                    }
                    return c;
                } catch (ClassNotFoundException ignored) {
                }
            }

            return parentFallback.loadClass(name);
        }
    }

    public static class Param {
        public final String data;

        public Param(String data) {
            this.data = data;
        }
    }

    public static class SubParam extends Param {
        public SubParam(String data) {
            super(data);
        }
    }

    public static class Target {
        public final String value;

        public Target(String value) {
            this.value = value;
        }

        public Target(int number) {
            this.value = String.valueOf(number);
        }

        public Target(Param param) {
            this.value = param.data;
        }
    }
}
