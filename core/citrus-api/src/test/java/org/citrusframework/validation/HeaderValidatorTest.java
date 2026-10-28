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

package org.citrusframework.validation;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.citrusframework.util.ConcurrencyTestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class HeaderValidatorTest {

    @Test
    public void testLookup() {
        Map<String, HeaderValidator> validators = HeaderValidator.lookup();
        Assertions.assertEquals(1L, validators.size());
        Assertions.assertNotNull(validators.get("defaultHeaderValidator"));
        Assertions.assertEquals(DefaultHeaderValidator.class, validators.get("defaultHeaderValidator").getClass());
    }

    @Test
    public void testTestLookup() {
        Assertions.assertTrue(HeaderValidator.lookup("default").isPresent());
    }

    @Test
    public void shouldLookupValidatorsOnceWhenCalledConcurrently() throws Exception {
        ConcurrencyTestUtils.raceRepeatedly(10_000, 16, HeaderValidator.validators::clear, HeaderValidator::lookup,
                () -> Assertions.assertEquals(Set.of("defaultHeaderValidator"), HeaderValidator.validators.keySet()));
    }

    @Test
    public void shouldNotDeadlockWhenValidatorClassesInitializeConcurrently() throws Exception {
        // Both validators call lookup() from their static initializer, and lookup() instantiates DefaultHeaderValidator.
        // A class is initialized once per class loader, so every round races the two initializers in a fresh loader.
        ExecutorService executor = Executors.newFixedThreadPool(2, runnable -> {
            Thread thread = new Thread(runnable);
            thread.setDaemon(true);
            return thread;
        });
        try {
            for (int round = 0; round < 50; round++) {
                ClassLoader loader = new CitrusClassesFirstClassLoader(HeaderValidatorTest.class.getClassLoader());
                CyclicBarrier start = new CyclicBarrier(2);
                List<Future<?>> results = new ArrayList<>();
                for (String type : List.of(DefaultHeaderValidator.class.getName(), DefaultMessageHeaderValidator.class.getName())) {
                    results.add(executor.submit(() -> {
                        Thread.currentThread().setContextClassLoader(loader);
                        start.await();
                        return Class.forName(type, true, loader);
                    }));
                }

                for (Future<?> result : results) {
                    result.get(10, TimeUnit.SECONDS);
                }
            }
        } finally {
            executor.shutdownNow();
        }
    }

    /**
     * Defines its own copy of every org.citrusframework class, so their static initializers run again.
     */
    private static final class CitrusClassesFirstClassLoader extends ClassLoader {

        CitrusClassesFirstClassLoader(ClassLoader parent) {
            super(parent);
        }

        @Override
        protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            if (!name.startsWith("org.citrusframework.")) {
                return super.loadClass(name, resolve);
            }

            synchronized (getClassLoadingLock(name)) {
                Class<?> type = findLoadedClass(name);
                if (type == null) {
                    try (InputStream in = getParent().getResourceAsStream(name.replace('.', '/') + ".class")) {
                        if (in == null) {
                            throw new ClassNotFoundException(name);
                        }
                        byte[] bytes = in.readAllBytes();
                        type = defineClass(name, bytes, 0, bytes.length);
                    } catch (IOException e) {
                        throw new ClassNotFoundException(name, e);
                    }
                }
                return type;
            }
        }
    }
}
