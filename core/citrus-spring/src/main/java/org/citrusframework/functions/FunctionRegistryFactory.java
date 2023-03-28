package org.citrusframework.functions;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Registry factory bean automatically adds all function libraries that live in the Spring bean application context.
 * The default function library is also added via Spring bean reference. This is why this registry explicitly doe not use default registry
 * in order to not duplicate the default function library.
 *
 * @author Christoph Deppisch
 */
public class FunctionRegistryFactory implements FactoryBean<FunctionRegistry>, ApplicationContextAware {

    private ApplicationContext applicationContext;

    private final FunctionRegistry registry;

    /**
     * Default constructor.
     */
    public FunctionRegistryFactory() {
        this(new FunctionRegistry());
    }

    /**
     * Constructor initializes with given registry.
     * @param registry
     */
    public FunctionRegistryFactory(FunctionRegistry registry) {
        this.registry = registry;
    }

    @Override
    public FunctionRegistry getObject() throws Exception {
        if (applicationContext != null) {
            applicationContext.getBeansOfType(FunctionLibrary.class)
                    .forEach((key, value) -> registry.addFunctionLibrary(value));
        }

        return registry;
    }

    @Override
    public Class<?> getObjectType() {
        return FunctionRegistry.class;
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
