package org.citrusframework;

/**
 * Behavior applies logic to given test action runner.
 * @author Christoph Deppisch
 */
@FunctionalInterface
public interface TestBehavior {

    /**
     * Behavior building method.
     */
    void apply(TestActionRunner runner);

}
