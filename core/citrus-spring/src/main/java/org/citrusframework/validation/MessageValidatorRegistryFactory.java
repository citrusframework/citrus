package org.citrusframework.validation;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Registry factory bean automatically adds all message validators that live in the Spring bean application context.
 * The validators that are located in the Spring bean application context may overwrite existing default message validators that
 * have the same name.
 *
 * @author Christoph Deppisch
 */
public class MessageValidatorRegistryFactory implements FactoryBean<MessageValidatorRegistry>, ApplicationContextAware {

    private ApplicationContext applicationContext;

    private final MessageValidatorRegistry registry;

    public MessageValidatorRegistryFactory() {
        this(new DefaultMessageValidatorRegistry());
    }

    public MessageValidatorRegistryFactory(MessageValidatorRegistry messageValidatorRegistry) {
        this.registry = messageValidatorRegistry;
    }

    @Override
    public MessageValidatorRegistry getObject() throws Exception {
        if (applicationContext != null) {
            applicationContext.getBeansOfType(MessageValidator.class)
                    .forEach(registry::addMessageValidator);
        }

        return registry;
    }

    @Override
    public Class<?> getObjectType() {
        return MessageValidatorRegistry.class;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
