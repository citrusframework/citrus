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
import java.util.function.Supplier;

/**
 * Instance creation manager creates new Citrus instances or always a singleton based on instance creation strategy.
 * Instance creation is thread-safe, so concurrent callers of {@link #getOrDefault()} share one instance.
 * A new instance is published to other threads only after all instance processors have run on it.
 */
public class CitrusInstanceManager {

    /** Singleton */
    private static volatile Citrus citrus;

    /** Instance being created, visible only to the creating thread while the instance processors run */
    private static Citrus initializing;

    /** List of instance resolvers capable of taking part in Citrus instance creation process */
    private static final List<CitrusInstanceProcessor> instanceProcessors = new CopyOnWriteArrayList<>();

    /** Strategy decides which instances are created */
    protected static CitrusInstanceStrategy strategy = CitrusInstanceStrategy.NEW;

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
    public static synchronized Citrus newInstance() {
        return newInstance(CitrusContextProvider.lookup());
    }

    /**
     * Create new Citrus instance with given context.
     * @param contextProvider
     * @return
     */
    public static synchronized Citrus newInstance(CitrusContextProvider contextProvider) {
        if (strategy.equals(CitrusInstanceStrategy.NEW) || citrus == null) {
            Citrus created = new Citrus(contextProvider.create());
            initializing = created;
            try {
                instanceProcessors.forEach(processor -> processor.process(created));
            } finally {
                initializing = null;
                citrus = created;
            }
        }

        return citrus;
    }

    /**
     * Sets the instance creation strategy.
     * @param mode
     */
    public static synchronized void mode(CitrusInstanceStrategy mode) {
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
     * Provide access to the current Citrus instance or create one with the given context provider.
     * Compound check-then-act sequences on the instance must run via
     * {@link #runSynchronized(Runnable)} or {@link #supplySynchronized(Supplier)}
     * instead of synchronizing on {@code CitrusInstanceManager.class} from outside.
     */
    public static Citrus getOrCreate(Supplier<CitrusContextProvider> contextProvider) {
        Citrus instance = current();
        if (instance != null) {
            return instance;
        }

        synchronized (CitrusInstanceManager.class) {
            if (citrus == null) {
                newInstance(contextProvider.get());
            }

            return citrus;
        }
    }

    /**
     * Runs the given action while holding the manager's instance creation lock.
     * Use this for check-then-act sequences that must stay atomic with instance creation,
     * instead of synchronizing on {@code CitrusInstanceManager.class} from outside.
     */
    public static synchronized void runSynchronized(Runnable action) {
        action.run();
    }

    /**
     * Calls the given action while holding the manager's instance creation lock and returns its result.
     * Use this for check-then-act sequences that must stay atomic with instance creation,
     * instead of synchronizing on {@code CitrusInstanceManager.class} from outside.
     */
    public static synchronized <T> T supplySynchronized(Supplier<T> action) {
        return action.get();
    }

    /**
     * Check if there has already been an instance instantiated using this manager.
     */
    public static boolean hasInstance() {
        return current() != null;
    }

    /**
     * Current instance. The thread creating a new instance sees it while the instance processors run,
     * all other threads see it once the processors have finished.
     */
    private static Citrus current() {
        if (Thread.holdsLock(CitrusInstanceManager.class) && initializing != null) {
            return initializing;
        }

        return citrus;
    }

    /**
     * Removes current Citrus instance if any.
     */
    public static synchronized void reset() {
        if (citrus != null) {
            citrus.close();
            DefaultCitrusContextProvider.reset();
        }

        citrus = null;
    }
}
