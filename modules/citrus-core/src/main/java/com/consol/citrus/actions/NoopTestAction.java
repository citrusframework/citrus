package com.consol.citrus.actions;

import com.consol.citrus.TestAction;
import com.consol.citrus.TestActor;
import com.consol.citrus.context.TestContext;

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
    }

    @Override
    public String getName() {
        return "noop";
    }

    @Override
    public TestAction setName(String name) {
        return null;
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public TestAction setDescription(String description) {
        return null;
    }

    @Override
    public boolean isDisabled(TestContext context) {
        return false;
    }

    @Override
    public TestActor getActor() {
        return null;
    }

    @Override
    public TestAction setActor(TestActor actor) {
        return null;
    }
}
