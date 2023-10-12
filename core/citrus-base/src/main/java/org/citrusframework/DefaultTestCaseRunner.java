package org.citrusframework;

import java.util.Date;

import org.citrusframework.actions.ApplyTestBehaviorAction;
import org.citrusframework.container.FinallySequence;
import org.citrusframework.context.TestContext;
import org.citrusframework.spi.ReferenceResolverAware;

/**
 * @author Christoph Deppisch
 */
public class DefaultTestCaseRunner implements TestCaseRunner {

    /** The test case */
    private TestCase testCase;

    /** The test context */
    private final TestContext context;

    /**
     * Constructor initializes a default test case with given test context.
     * @param context
     */
    public DefaultTestCaseRunner(TestContext context) {
        this(new DefaultTestCase(), context);
    }

    /**
     * Constructor initializes with given test case and context.
     * @param testCase
     * @param context
     */
    public DefaultTestCaseRunner(TestCase testCase, TestContext context) {
        this.testCase = testCase;
        this.context = context;

        this.testCase.setIncremental(true);
    }

    public TestContext getContext() {
        return context;
    }

    @Override
    public void start() {
        testCase.start(context);
    }

    @Override
    public void stop() {
        testCase.finish(context);
    }

    @Override
    public <T> T variable(String name, T value) {
        testCase.getVariableDefinitions().put(name, value);

        if (value instanceof String) {
            String resolved = context.replaceDynamicContentInString(value.toString());
            context.setVariable(name, resolved);
            return (T) resolved;
        } else {
            context.setVariable(name, value);
            return value;
        }
    }

    @Override
    public void testClass(Class<?> type) {
        testCase.setTestClass(type);
    }

    @Override
    public void name(String name) {
        testCase.setName(name);
    }

    @Override
    public void description(String description) {
        testCase.setDescription(description);
    }

    @Override
    public void author(String author) {
        testCase.getMetaInfo().setAuthor(author);
    }

    @Override
    public void packageName(String packageName) {
        testCase.setPackageName(packageName);
    }

    @Override
    public void status(TestCaseMetaInfo.Status status) {
        testCase.getMetaInfo().setStatus(status);
    }

    @Override
    public void creationDate(Date date) {
        testCase.getMetaInfo().setCreationDate(date);
    }

    @Override
    public void groups(String[] groups) {
        if (testCase instanceof TestGroupAware) {
            ((TestGroupAware) testCase).setGroups(groups);
        }
    }

    @Override
    public <T extends TestAction> T run(TestActionBuilder<T> builder) {
        if (builder instanceof ReferenceResolverAware) {
            ((ReferenceResolverAware) builder).setReferenceResolver(context.getReferenceResolver());
        }

        if (builder instanceof ApplyTestBehaviorAction.Builder) {
            ((ApplyTestBehaviorAction.Builder) builder).on(this);
        }

        T action = builder.build();

        if (builder instanceof FinallySequence.Builder) {
            ((FinallySequence.Builder) builder).getActions().forEach(testCase::addFinalAction);
            return action;
        }

        testCase.addTestAction(action);
        testCase.executeAction(action, context);
        return action;
    }

    @Override
    @SuppressWarnings("unchecked")
    public ApplyTestBehaviorAction.Builder applyBehavior(TestBehavior behavior) {
        return new ApplyTestBehaviorAction.Builder()
                .behavior(behavior)
                .on(this);
    }

    /**
     * Obtains the testCase.
     * @return
     */
    @Override
    public TestCase getTestCase() {
        return testCase;
    }

    public void setTestCase(TestCase testCase) {
        this.testCase = testCase;
        this.testCase.setIncremental(true);
    }

    public static class DefaultTestCaseRunnerProvider implements TestCaseRunnerProvider {

        @Override
        public TestCaseRunner createTestCaseRunner(TestContext context) {
            return new DefaultTestCaseRunner(context);
        }

        @Override
        public TestCaseRunner createTestCaseRunner(TestCase testCase, TestContext context) {
            return new DefaultTestCaseRunner(testCase, context);
        }

    }
}
