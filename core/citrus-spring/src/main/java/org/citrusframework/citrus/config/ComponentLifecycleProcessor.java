/*
 * Copyright 2020 the original author or authors.
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

package org.citrusframework.citrus.config;

import org.citrusframework.citrus.common.InitializingPhase;
import org.citrusframework.citrus.common.Named;
import org.citrusframework.citrus.common.ShutdownPhase;
import org.citrusframework.citrus.context.SpringBeanReferenceResolver;
import org.citrusframework.citrus.spi.ReferenceResolver;
import org.citrusframework.citrus.spi.ReferenceResolverAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * @author Christoph Deppisch
 */
public class ComponentLifecycleProcessor implements DestructionAwareBeanPostProcessor, ApplicationContextAware {

    private ReferenceResolver referenceResolver;

    /** Logger */
    private final static Logger LOG = LoggerFactory.getLogger(ComponentLifecycleProcessor.class);

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof Named) {
            ((Named) bean).setName(beanName);
        }

        if (bean instanceof ReferenceResolverAware) {
            ((ReferenceResolverAware) bean).setReferenceResolver(referenceResolver);
        }

        if (bean instanceof InitializingPhase) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("Initializing component '%s'", beanName));
            }
            ((InitializingPhase) bean).initialize();
        }

        return bean;
    }

    @Override
    public void postProcessBeforeDestruction(Object bean, String beanName) throws BeansException {
        if (requiresDestruction(bean)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("Destroying component '%s'", beanName));
            }
            ((ShutdownPhase) bean).destroy();
        }
    }

    @Override
    public boolean requiresDestruction(Object bean) {
        return bean instanceof ShutdownPhase;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (applicationContext.getBeansOfType(ReferenceResolver.class).size() == 1) {
            this.referenceResolver = applicationContext.getBean(ReferenceResolver.class);
        } else {
            this.referenceResolver = new SpringBeanReferenceResolver(applicationContext);
        }
    }
}
