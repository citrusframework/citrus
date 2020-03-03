package com.consol.citrus;

/**
 * @author Christoph Deppisch
 */
public interface TestActionRunner {

    /**
     * Runs given test action.
     * @param action
     * @param <T>
     * @return
     */
    default <T extends TestAction> T run(T action) {
        return run((TestActionBuilder<T>) () -> action);
    }

    /**
     * Builds and runs given test action.
     * @param builder
     * @param <T>
     * @return
     */
    <T extends TestAction> T run(TestActionBuilder<T> builder);
}
