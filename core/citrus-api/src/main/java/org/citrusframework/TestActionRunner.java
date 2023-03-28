package org.citrusframework;

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
     * Runs given test action.
     * @param action
     * @param <T>
     * @return
     */
    default <T extends TestAction> T $(T action) {
        return run((TestActionBuilder<T>) () -> action);
    }

    /**
     * Builds and runs given test action.
     * @param builder
     * @param <T>
     * @return
     */
    default <T extends TestAction> T $(TestActionBuilder<T> builder) {
        return run(builder);
    }

    /**
     * Builds and runs given test action.
     * @param builder
     * @param <T>
     * @return
     */
    <T extends TestAction> T run(TestActionBuilder<T> builder);

    /**
     * Apply test behavior on this test action runner.
     * @param behavior
     * @return
     */
    <T extends TestAction> TestActionBuilder<T> applyBehavior(TestBehavior behavior);
}
