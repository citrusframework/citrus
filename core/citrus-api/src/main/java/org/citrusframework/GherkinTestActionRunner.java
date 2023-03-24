package org.citrusframework;

/**
 * Runner adds default alias methods using Gherkin behavior driven development style (GIVEN, WHEN, THEN).
 * @author Christoph Deppisch
 */
public interface GherkinTestActionRunner extends TestActionRunner {

    /**
     * Behavior driven style alias for run method.
     * @param action
     * @param <T>
     * @return
     */
    default <T extends TestAction> T given(T action) {
        return given((TestActionBuilder<T>) () -> action);
    }

    /**
     * Behavior driven style alias for run method.
     * @param builder
     * @param <T>
     * @return
     */
    default <T extends TestAction> T given(TestActionBuilder<T> builder) {
        return run(builder);
    }

    /**
     * Behavior driven style alias for run method.
     * @param action
     * @param <T>
     * @return
     */
    default <T extends TestAction> T when(T action) {
        return when((TestActionBuilder<T>) () -> action);
    }

    /**
     * Behavior driven style alias for run method.
     * @param builder
     * @param <T>
     * @return
     */
    default <T extends TestAction> T when(TestActionBuilder<T> builder) {
        return run(builder);
    }

    /**
     * Behavior driven style alias for run method.
     * @param action
     * @param <T>
     * @return
     */
    default <T extends TestAction> T then(T action) {
        return then((TestActionBuilder<T>) () -> action);
    }

    /**
     * Behavior driven style alias for run method.
     * @param builder
     * @param <T>
     * @return
     */
    default <T extends TestAction> T then(TestActionBuilder<T> builder) {
        return run(builder);
    }

    /**
     * Behavior driven style alias for run method.
     * @param action
     * @param <T>
     * @return
     */
    default <T extends TestAction> T and(T action) {
        return and((TestActionBuilder<T>) () -> action);
    }

    /**
     * Behavior driven style alias for run method.
     * @param builder
     * @param <T>
     * @return
     */
    default <T extends TestAction> T and(TestActionBuilder<T> builder) {
        return run(builder);
    }
}
