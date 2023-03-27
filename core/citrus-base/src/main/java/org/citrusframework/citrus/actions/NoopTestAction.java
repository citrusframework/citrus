package org.citrusframework.citrus.actions;

import org.citrusframework.citrus.TestAction;
import org.citrusframework.citrus.context.TestContext;

/**
 * Special test action doing nothing but implementing the test action interface. This is useful during Java dsl fluent API
 * that needs to return a test action but this should not be included or executed during the test run. See test behavior applying
 * test actions.
 *
 * @author Christoph Deppisch
 */
public class NoopTestAction implements TestAction {

    @Override
    public void execute(TestContext context) {
        // do nothing
    }

    @Override
    public String getName() {
        return "noop";
    }
}
