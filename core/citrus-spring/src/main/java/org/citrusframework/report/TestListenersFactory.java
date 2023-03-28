package org.citrusframework.report;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Factory bean automatically adds all test listeners that live in the Spring bean application context.
 *
 * @author Christoph Deppisch
 */
public class TestListenersFactory implements FactoryBean<TestListeners>, ApplicationContextAware {

    private ApplicationContext applicationContext;

    private final TestListeners listeners;

    /**
     * Default constructor.
     */
    public TestListenersFactory() {
        this(new TestListeners());
    }

    /**
     * Constructor initializes with given listeners.
     * @param listeners
     */
    public TestListenersFactory(TestListeners listeners) {
        this.listeners = listeners;
    }

    @Override
    public TestListeners getObject() throws Exception {
        if (applicationContext != null) {
            applicationContext.getBeansOfType(TestListener.class)
                    .forEach((key, value) -> listeners.addTestListener(value));
        }

        return listeners;
    }

    @Override
    public Class<?> getObjectType() {
        return TestListeners.class;
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
