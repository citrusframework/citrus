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
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Instance creation manager creates new Citrus instances or always a singleton based on instance creation strategy.
 * <p>
 * Instance creation is thread-safe: concurrent callers of {@link #getOrDefault()} share one instance.
 * A new instance is published to other threads only after all instance processors have run on it,
 * so the creating thread sees the instance while the processors run and every other thread waits for them to finish.
 * The instance is published even when a processor fails, so a failing processor does not trigger another creation.
 * <p>
 * Instance processors run while the manager holds its creation lock. A processor must not wait for another thread
 * that accesses the manager, such as a thread calling {@link #getOrDefault()}, otherwise both threads block forever.
 */
public class CitrusInstanceManager {

    /** Published instance, visible to all threads once its instance processors have run */
    private static volatile Citrus citrus;

    /** Instance under creation, visible only to the creating thread while its instance processors run */
    private static final ThreadLocal<Citrus> initializing = new ThreadLocal<>();

    /** Guards instance creation, replacement and removal */
    private static final ReentrantLock lock = new ReentrantLock();

    /** List of instance resolvers capable of taking part in Citrus instance creation process */
    private static final List<CitrusInstanceProcessor> instanceProcessors = new CopyOnWriteArrayList<>();

    /** Strategy decides which instances are created */
    protected static volatile CitrusInstanceStrategy strategy = CitrusInstanceStrategy.NEW;

    /**
     * Add instance processor.
     * @param processor
     */
    public static void addInstanceProcessor(CitrusInstanceProcessor processor) {
        instanceProcessors.add(processor);
    }

    /**
     * Remove instance processor.
     * @param processor
     */
    public static void removeInstanceProcessor(CitrusInstanceProcessor processor) {
        instanceProcessors.remove(processor);
    }

    /**
     * Initializing method loads Citrus context and reads bean definitions
     * such as test listeners and test context factory.
     * @return
     */
    public static Citrus newInstance() {
        return newInstance(CitrusContextProvider.lookup());
    }

    /**
     * Create new Citrus instance with given context.
     * @param contextProvider
     * @return
     */
    public static Citrus newInstance(CitrusContextProvider contextProvider) {
        return locked(() -> strategy == CitrusInstanceStrategy.NEW || citrus == null ? create(contextProvider) : citrus);
    }

    /**
     * Sets the instance creation strategy.
     * @param mode
     */
    public static void mode(CitrusInstanceStrategy mode) {
        strategy = mode;
    }

    /**
     * Gets the actual instance that has been created with this manager.
     * @return
     */
    public static Optional<Citrus> get() {
        return Optional.ofNullable(current());
    }

    /**
     * Provide access to the current Citrus instance.
     * Create new instance if it has not been initialized yet.
     */
    public static Citrus getOrDefault() {
        return getOrCreate(CitrusContextProvider::lookup);
    }

    /**
     * Atomically reuses the current Citrus instance when it is reusable, otherwise closes it
     * and creates a new instance with the given context provider.
     * @param reusable decides whether the current instance can be reused.
     * @param contextProvider supplies the context provider for a new instance, only called when an instance is created.
     * @return the reused or the newly created instance.
     */
    public static Citrus getOrReplace(Predicate<? super Citrus> reusable,
                                      Supplier<? extends CitrusContextProvider> contextProvider) {
        return locked(() -> {
            Citrus instance = current();
            if (instance != null) {
                if (reusable.test(instance)) {
                    return instance;
                }

                instance.close();
            }

            return create(contextProvider.get());
        });
    }

    /**
     * Check if there has already been an instance instantiated using this manager.
     */
    public static boolean hasInstance() {
        return current() != null;
    }

    /**
     * Removes current Citrus instance if any.
     */
    public static void reset() {
        locked(() -> {
            if (citrus != null) {
                citrus.close();
                DefaultCitrusContextProvider.reset();
            }

            citrus = null;
            return null;
        });
    }

    /**
     * Current instance or a new one created with the given context provider.
     * Skips the creation lock once an instance has been published.
     */
    static Citrus getOrCreate(Supplier<? extends CitrusContextProvider> contextProvider) {
        Citrus instance = current();
        return instance != null ? instance : getOrReplace(existing -> true, contextProvider);
    }

    /**
     * Creates a new instance, runs the instance processors on it and publishes it.
     * Supports nested creation from within an instance processor.
     */
    private static Citrus create(CitrusContextProvider contextProvider) {
        Citrus created = new Citrus(contextProvider.create());
        Citrus enclosing = initializing.get();

        initializing.set(created);
        try {
            instanceProcessors.forEach(processor -> processor.process(created));
        } finally {
            if (enclosing != null) {
                initializing.set(enclosing);
            } else {
                initializing.remove();
            }

            citrus = created;
        }

        return created;
    }

    /**
     * The instance under creation for the creating thread, the published instance for all other threads.
     */
    private static Citrus current() {
        Citrus instance = initializing.get();
        return instance != null ? instance : citrus;
    }

    private static <T> T locked(Supplier<T> action) {
        lock.lock();
        try {
            return action.get();
        } finally {
            lock.unlock();
        }
    }
}
