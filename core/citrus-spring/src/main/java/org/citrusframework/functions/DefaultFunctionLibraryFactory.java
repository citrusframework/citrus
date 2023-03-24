/*
 * Copyright 2022 the original author or authors.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.functions;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

/**
 * @author Christoph Deppisch
 */
public class DefaultFunctionLibraryFactory implements FactoryBean<DefaultFunctionLibrary>, ApplicationContextAware, EnvironmentAware {

    private ApplicationContext applicationContext;
    private Environment environment;

    private final DefaultFunctionLibrary library = new DefaultFunctionLibrary();

    @Override
    public DefaultFunctionLibrary getObject() throws Exception {
        library.getMembers().forEach((key, member) -> {
            if (member instanceof ApplicationContextAware) {
                ((ApplicationContextAware) member).setApplicationContext(applicationContext);
            }

            if (member instanceof EnvironmentAware) {
                ((EnvironmentAware) member).setEnvironment(environment);
            }
        });

        applicationContext.getBeansOfType(Function.class)
                .forEach((key, value) -> library.getMembers().put(key, value));

        return library;
    }

    @Override
    public Class<?> getObjectType() {
        return DefaultFunctionLibrary.class;
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
