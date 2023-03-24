package org.citrusframework.report;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Factory bean automatically adds all test suite listeners that live in the Spring bean application context.
 *
 * @author Christoph Deppisch
 */
public class TestSuiteListenersFactory implements FactoryBean<TestSuiteListeners>, ApplicationContextAware {

    private ApplicationContext applicationContext;

    private final TestSuiteListeners listeners;

    /**
     * Default constructor.
     */
    public TestSuiteListenersFactory() {
        this(new TestSuiteListeners());
    }

    /**
     * Constructor initializes with given listeners.
     * @param listeners
     */
    public TestSuiteListenersFactory(TestSuiteListeners listeners) {
        this.listeners = listeners;
    }

    @Override
    public TestSuiteListeners getObject() throws Exception {
        if (applicationContext != null) {
            applicationContext.getBeansOfType(TestSuiteListener.class)
                    .forEach((key, value) -> listeners.addTestSuiteListener(value));
        }

        return listeners;
    }

    @Override
    public Class<?> getObjectType() {
        return TestSuiteListeners.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
