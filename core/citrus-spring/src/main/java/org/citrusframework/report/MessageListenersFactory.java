package org.citrusframework.report;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Factory bean automatically adds all message listeners that live in the Spring bean application context.
 *
 * @author Christoph Deppisch
 */
public class MessageListenersFactory implements FactoryBean<MessageListeners>, ApplicationContextAware {

    private ApplicationContext applicationContext;

    private final MessageListeners listeners;

    /**
     * Default constructor.
     */
    public MessageListenersFactory() {
        this(new MessageListeners());
    }

    /**
     * Constructor initializes with given listeners.
     * @param listeners
     */
    public MessageListenersFactory(MessageListeners listeners) {
        this.listeners = listeners;
    }

    @Override
    public MessageListeners getObject() throws Exception {
        if (applicationContext != null) {
            applicationContext.getBeansOfType(MessageListener.class)
                    .forEach((key, value) -> listeners.addMessageListener(value));
        }

        return listeners;
    }

    @Override
    public Class<?> getObjectType() {
        return MessageListeners.class;
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
