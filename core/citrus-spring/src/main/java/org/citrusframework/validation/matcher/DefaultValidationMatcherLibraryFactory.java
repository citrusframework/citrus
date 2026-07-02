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

import org.citrusframework.CitrusSettings;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

public class DefaultValidationMatcherLibraryFactory implements FactoryBean<DefaultValidationMatcherLibrary>, ApplicationContextAware, EnvironmentAware {

    private ApplicationContext applicationContext;
    private Environment environment;

    private final DefaultValidationMatcherLibrary library = new DefaultValidationMatcherLibrary();

    @Override
    public DefaultValidationMatcherLibrary getObject() {
        library.getMembers().forEach((key, member) -> {
            if (member instanceof ApplicationContextAware applicationContextAware) {
                applicationContextAware.setApplicationContext(applicationContext);
            }

            if (member instanceof EnvironmentAware environmentAware) {
                environmentAware.setEnvironment(environment);
            }
        });

        boolean allowOverride = CitrusSettings.isAllowValidationMatcherOverride();
        applicationContext.getBeansOfType(ValidationMatcher.class)
                .forEach((key, value) -> {
                    if (allowOverride) {
                        library.getMembers().put(key, value);
                    } else {
                        library.addMember(key, value);
                    }
                });

        return library;
    }

    @Override
    public Class<?> getObjectType() {
        return DefaultValidationMatcherLibrary.class;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}
