package org.citrusframework.report;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Factory bean automatically adds all test action listeners that live in the Spring bean application context.
 *
 * @author Christoph Deppisch
 */
public class TestActionListenersFactory implements FactoryBean<TestActionListeners>, ApplicationContextAware {

    private ApplicationContext applicationContext;

    private final TestActionListeners listeners;

    /**
     * Default constructor.
     */
    public TestActionListenersFactory() {
        this(new TestActionListeners());
    }

    /**
     * Constructor initializes with given listeners.
     * @param listeners
     */
    public TestActionListenersFactory(TestActionListeners listeners) {
        this.listeners = listeners;
    }

    @Override
    public TestActionListeners getObject() throws Exception {
        if (applicationContext != null) {
            applicationContext.getBeansOfType(TestActionListener.class)
                    .forEach((key, value) -> listeners.addTestActionListener(value));
        }

        return listeners;
    }

    @Override
    public Class<?> getObjectType() {
        return TestActionListeners.class;
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
