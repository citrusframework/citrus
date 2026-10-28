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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
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

    private final List<CitrusInstanceProcessor> processors = new ArrayList<>();

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
        processors.forEach(CitrusInstanceManager::removeInstanceProcessor);
        CitrusInstanceManager.mode(CitrusInstanceStrategy.NEW);
        CitrusInstanceManager.reset();
    }

    @Test
    public void shouldShareOneDefaultInstanceBetweenConcurrentCallers() throws Exception {
        AtomicInteger created = new AtomicInteger();
        CitrusContextProvider provider = () -> {
            created.incrementAndGet();
            pause(20);
            return mock(CitrusContext.class);
        };

        Set<Citrus> instances = ConcurrentHashMap.newKeySet();
        runConcurrently(() -> instances.add(CitrusInstanceManager.getOrCreate(() -> provider)));

        assertThat(created).hasValue(1);
        assertThat(instances).hasSize(1);
        assertThat(CitrusInstanceManager.get()).containsSame(instances.iterator().next());
    }

    @Test
    public void shouldPublishInstanceOnlyAfterProcessorsRan() throws Exception {
        CountDownLatch processing = new CountDownLatch(1);
        AtomicBoolean processed = new AtomicBoolean();
        register(instance -> {
            processing.countDown();
            pause(200);
            processed.set(true);
        });

        Future<Citrus> creator = executor.submit(() -> CitrusInstanceManager.getOrCreate(() -> () -> mock(CitrusContext.class)));
        assertThat(processing.await(10, TimeUnit.SECONDS)).isTrue();

        Queue<String> earlyCallers = new ConcurrentLinkedQueue<>();
        runConcurrently(() -> {
            CitrusInstanceManager.getOrDefault();
            if (!processed.get()) {
                earlyCallers.add(Thread.currentThread().getName());
            }
        });

        assertThat(earlyCallers).isEmpty();
        assertThat(creator.get(10, TimeUnit.SECONDS)).isSameAs(CitrusInstanceManager.get().orElseThrow());
    }

    @Test
    public void shouldExposeInstanceToCreatingThreadWhileProcessorsRun() {
        AtomicReference<Citrus> seen = new AtomicReference<>();
        register(instance -> seen.set(CitrusInstanceManager.getOrDefault()));

        Citrus created = CitrusInstanceManager.newInstance(() -> mock(CitrusContext.class));

        assertThat(seen.get()).isSameAs(created);
    }

    @Test
    public void shouldCreateOneSingletonForConcurrentCallers() throws Exception {
        CitrusInstanceManager.mode(CitrusInstanceStrategy.SINGLETON);
        AtomicInteger created = new AtomicInteger();
        CitrusContextProvider provider = () -> {
            created.incrementAndGet();
            pause(20);
            return mock(CitrusContext.class);
        };

        Set<Citrus> instances = ConcurrentHashMap.newKeySet();
        runConcurrently(() -> instances.add(CitrusInstanceManager.newInstance(provider)));

        assertThat(created).hasValue(1);
        assertThat(instances).hasSize(1);
    }

    @Test
    public void shouldRunProcessorsAddedWhileInstancesAreCreated() throws Exception {
        Map<CitrusInstanceProcessor, AtomicInteger> invocations = new ConcurrentHashMap<>();

        runConcurrently(() -> {
            for (int i = 0; i < 20; i++) {
                AtomicInteger counter = new AtomicInteger();
                CitrusInstanceProcessor processor = instance -> counter.incrementAndGet();
                invocations.put(processor, counter);
                CitrusInstanceManager.addInstanceProcessor(processor);
                CitrusInstanceManager.newInstance(() -> mock(CitrusContext.class));
            }
        });
        processors.addAll(invocations.keySet());

        assertThat(invocations).hasSize(THREADS * 20);
        assertThat(invocations.values()).allSatisfy(counter -> assertThat(counter.get()).isPositive());
    }

    @Test
    public void shouldRemoveInstanceProcessor() {
        AtomicInteger invocations = new AtomicInteger();
        CitrusInstanceProcessor processor = instance -> invocations.incrementAndGet();
        CitrusInstanceManager.addInstanceProcessor(processor);
        CitrusInstanceManager.removeInstanceProcessor(processor);

        CitrusInstanceManager.newInstance(() -> mock(CitrusContext.class));

        assertThat(invocations).hasValue(0);
    }

    private void register(CitrusInstanceProcessor processor) {
        processors.add(processor);
        CitrusInstanceManager.addInstanceProcessor(processor);
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

    private static void pause(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
