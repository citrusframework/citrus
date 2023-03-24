package org.citrusframework;

/**
 * Citrus instance processor takes part in instance creation process.
 */
@FunctionalInterface
public interface CitrusInstanceProcessor {

    /**
     * Process Citrus instance after this has been instantiated.
     * @param instance
     */
    void process(Citrus instance);
}
