package com.consol.citrus;

import java.util.ArrayList;
import java.util.List;

/**
 * Instance creation manager creates new Citrus instances or always a singleton based on instance creation strategy.
 */
public class CitrusInstanceManager {

    /** Singleton */
    private static Citrus citrus;

    /** List of instance resolvers capable of taking part in Citrus instance creation process */
    private static List<CitrusInstanceProcessor> instanceProcessors = new ArrayList<>();

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
        if (strategy.equals(CitrusInstanceStrategy.NEW)) {
            return newInstance(CitrusContext.create());
        } else if (citrus == null) {
            citrus = newInstance(CitrusContext.create());
        }

        return citrus;
    }

    /**
     * Create new Citrus instance with given context.
     * @param citrusContext
     * @return
     */
    public static Citrus newInstance(CitrusContext citrusContext) {
        if (strategy.equals(CitrusInstanceStrategy.NEW)) {
            Citrus instance = new Citrus(citrusContext);
            instanceProcessors.forEach(processor -> processor.process(instance));
            return instance;
        } else if (citrus == null) {
            citrus = new Citrus(citrusContext);
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
     * Gets the singleton instance of Citrus.
     * @return
     */
    public static Citrus getSingleton() {
        return citrus;
    }
}
