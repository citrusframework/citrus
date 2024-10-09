/*
 * Copyright the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
