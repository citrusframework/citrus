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

package org.citrusframework.spring;

import java.util.ArrayList;
import java.util.Optional;

import org.citrusframework.api.container.AfterSuite;
import org.citrusframework.api.container.BeforeSuite;
import org.citrusframework.base.DefaultCitrusContext;
import org.citrusframework.functions.FunctionRegistry;
import org.citrusframework.log.LogModifier;
import org.citrusframework.report.MessageListeners;
import org.citrusframework.report.TestActionListeners;
import org.citrusframework.report.TestListeners;
import org.citrusframework.report.TestReporters;
import org.citrusframework.report.TestSuiteListeners;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spring.config.CitrusSpringConfig;
import org.citrusframework.spring.context.TestContextFactoryBean;
import org.citrusframework.util.TypeConverter;
import org.citrusframework.validation.MessageValidatorRegistry;
import org.citrusframework.validation.matcher.ValidationMatcherRegistry;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class CitrusSpringContext extends DefaultCitrusContext {

    /** Basic Spring application context */
    private final ApplicationContext applicationContext;

    /**
     * Protected constructor using given builder to construct this instance.
     * @param builder
     */
    protected CitrusSpringContext(Builder builder) {
        super(builder);

        this.applicationContext = builder.applicationContext;
    }

    /**
     * Gets the basic Citrus Spring bean application context.
     * @return
     */
    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    /**
     * Closing Citrus and its application context.
     */
    public void close() {
        super.close();
        if (applicationContext instanceof ConfigurableApplicationContext configurableApplicationContext && configurableApplicationContext.isActive()) {
            configurableApplicationContext.close();
        }
    }

    /**
     * Initializing method loads Spring application context and reads bean definitions
     * such as test listeners and test context factory.
     * @return
     */
    public static CitrusSpringContext create() {
        return create(new AnnotationConfigApplicationContext(CitrusSpringConfig.class));
    }

    /**
     * Initializing method with Spring application context Java configuration class
     * that gets loaded as application context.
     * @param configClass
     * @return
     */
    public static CitrusSpringContext create(Class<? extends CitrusSpringConfig> configClass) {
        return create(new AnnotationConfigApplicationContext(configClass));
    }

    /**
     * Create new Citrus context with given Spring bean application context.
     * @param applicationContext
     * @return
     */
    public static CitrusSpringContext create(ApplicationContext applicationContext) {
        return new Builder()
                .withApplicationContext(applicationContext)
                .build();
    }

    /**
     * Spring aware Citrus context builder.
     */
    public static final class Builder extends DefaultCitrusContext.Builder {

        private ApplicationContext applicationContext;

        public Builder withApplicationContext(ApplicationContext applicationContext) {
            this.applicationContext = applicationContext;

            findBean(FunctionRegistry.class).ifPresent(this::functionRegistry);
            findBean(ValidationMatcherRegistry.class).ifPresent(this::validationMatcherRegistry);
            findBean(MessageValidatorRegistry.class).ifPresent(this::messageValidatorRegistry);
            findBean(MessageListeners.class).ifPresent(this::messageListeners);
            findBean(TestListeners.class).ifPresent(this::testListeners);
            findBean(TestActionListeners.class).ifPresent(this::testActionListeners);
            findBean(TestReporters.class).ifPresent(this::testReporters);
            findBean(TestSuiteListeners.class).ifPresent(this::testSuiteListeners);
            findBean(TestContextFactoryBean.class).ifPresent(this::testContextFactory);
            findBean(ReferenceResolver.class).ifPresent(this::referenceResolver);
            findBean(TypeConverter.class).ifPresent(this::typeConverter);
            findBean(LogModifier.class).ifPresent(this::logModifier);

            beforeSuite(new ArrayList<>(applicationContext.getBeansOfType(BeforeSuite.class).values()));
            afterSuite(new ArrayList<>(applicationContext.getBeansOfType(AfterSuite.class).values()));

            return this;
        }

        /**
         * Gets bean by given type from application context.
         * Handles no such bean exception to returns empty.
         * @param beanType
         * @return optional bean or empty
         * @param <T> bean type
         */
        private <T> Optional<T> findBean(Class<T> beanType) {
            try {
                return Optional.of(applicationContext.getBean(beanType));
            } catch (NoSuchBeanDefinitionException e) {
                return Optional.empty();
            }
        }

        public CitrusSpringContext build() {
            return new CitrusSpringContext(this);
        }
    }
}
