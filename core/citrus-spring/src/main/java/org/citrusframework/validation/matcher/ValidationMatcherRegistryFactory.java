package org.citrusframework.validation.matcher;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Registry factory bean automatically adds all validation matcher libraries that live in the Spring bean application context.
 * The default validation matcher library is also added via Spring bean reference. This is why this registry explicitly doe not use default registry
 * in order to not duplicate the default validation matcher library.
 *
 * @author Christoph Deppisch
 */
public class ValidationMatcherRegistryFactory implements FactoryBean<ValidationMatcherRegistry>, ApplicationContextAware {

    private ApplicationContext applicationContext;

    private final ValidationMatcherRegistry registry;

    /**
     * Default constructor.
     */
    public ValidationMatcherRegistryFactory() {
        this(new ValidationMatcherRegistry());
    }

    /**
     * Constructor initializes with given registry.
     * @param registry
     */
    public ValidationMatcherRegistryFactory(ValidationMatcherRegistry registry) {
        this.registry = registry;
    }

    @Override
    public ValidationMatcherRegistry getObject() throws Exception {
        if (applicationContext != null) {
            applicationContext.getBeansOfType(ValidationMatcherLibrary.class)
                    .forEach((key, value) -> registry.addValidationMatcherLibrary(value));
        }

        return registry;
    }

    @Override
    public Class<?> getObjectType() {
        return ValidationMatcherRegistry.class;
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
