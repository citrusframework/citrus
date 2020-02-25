package com.consol.citrus.validation;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * @author Christoph Deppisch
 */
public class MessageValidatorRegistryFactory implements FactoryBean<MessageValidatorRegistry>, ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public MessageValidatorRegistry getObject() throws Exception {
        MessageValidatorRegistry messageValidatorRegistry = new DefaultMessageValidatorRegistry();

        if (applicationContext != null) {
            applicationContext.getBeansOfType(MessageValidator.class)
                    .forEach((key, value) -> messageValidatorRegistry.getMessageValidators().put(key, value));
        }

        return messageValidatorRegistry;
    }

    @Override
    public Class<?> getObjectType() {
        return MessageValidatorRegistry.class;
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
