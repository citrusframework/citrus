package org.citrusframework;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Instance creation manager creates new Citrus instances or always a singleton based on instance creation strategy.
 */
public class CitrusInstanceManager {

    /** Singleton */
    private static Citrus citrus;

    /** List of instance resolvers capable of taking part in Citrus instance creation process */
    private static final List<CitrusInstanceProcessor> instanceProcessors = new ArrayList<>();

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
        if (strategy.equals(CitrusInstanceStrategy.NEW) || citrus == null) {
            citrus = new Citrus(contextProvider.create());
            instanceProcessors.forEach(processor -> processor.process(citrus));
        }

        return citrus;
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
        return Optional.ofNullable(citrus);
    }


    /**
     * Provide access to the current Citrus instance.
     * Create new instance if it has not been initialized yet.
     * @return
     */
    public static Citrus getOrDefault() {
        if (citrus == null) {
            citrus = newInstance();
        }

        return citrus;
    }

    /**
     * Check if there has already been an instance instantiated using this manager.
     * @return
     */
    public static boolean hasInstance() {
        return citrus != null;
    }

    /**
     * Removes current Citrus instance if any.
     * @return
     */
    public static void reset() {
        citrus = null;
    }
}
