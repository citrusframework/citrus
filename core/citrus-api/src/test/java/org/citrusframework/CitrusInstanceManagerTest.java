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
package org.citrusframework;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@Isolated("Resets the shared Citrus instance and changes the instance strategy")
public class CitrusInstanceManagerTest {

    private static final int THREADS = 8;

    private ExecutorService executor;

    @BeforeEach
    public void setup() {
        CitrusInstanceManager.reset();
        executor = Executors.newFixedThreadPool(THREADS);
    }

    @AfterEach
    public void cleanup() throws InterruptedException {
        executor.shutdownNow();
        executor.awaitTermination(5, TimeUnit.SECONDS);
        CitrusInstanceManager.mode(CitrusInstanceStrategy.NEW);
        CitrusInstanceManager.reset();
    }

    @Test
    public void shouldCreateOneSingletonForConcurrentCallers() throws Exception {
        CitrusInstanceManager.mode(CitrusInstanceStrategy.SINGLETON);
        AtomicInteger created = new AtomicInteger();
        CitrusContextProvider provider = () -> {
            created.incrementAndGet();
            pause();
            return mock(CitrusContext.class);
        };

        Set<Citrus> instances = ConcurrentHashMap.newKeySet();
        runConcurrently(() -> instances.add(CitrusInstanceManager.newInstance(provider)));

        assertThat(created).hasValue(1);
        assertThat(instances).hasSize(1);
        assertThat(CitrusInstanceManager.get()).containsSame(instances.iterator().next());
    }

    @Test
    public void shouldAddProcessorsWhileInstancesAreCreated() throws Exception {
        CitrusContextProvider provider = () -> mock(CitrusContext.class);
        AtomicInteger processed = new AtomicInteger();

        runConcurrently(() -> {
            for (int i = 0; i < 50; i++) {
                CitrusInstanceManager.addInstanceProcessor(instance -> processed.incrementAndGet());
                CitrusInstanceManager.newInstance(provider);
            }
        });

        assertThat(processed.get()).isPositive();
    }

    private void runConcurrently(Runnable task) throws Exception {
        CountDownLatch start = new CountDownLatch(1);
        List<Future<?>> futures = IntStream.range(0, THREADS)
                .<Future<?>>mapToObj(i -> executor.submit(() -> {
                    start.await();
                    task.run();
                    return null;
                }))
                .toList();

        start.countDown();
        for (Future<?> future : futures) {
            future.get(30, TimeUnit.SECONDS);
        }
    }

    private static void pause() {
        try {
            Thread.sleep(20);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
