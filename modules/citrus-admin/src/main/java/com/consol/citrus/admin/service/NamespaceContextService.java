/*
 * Copyright 2006-2014 the original author or authors.
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

package com.consol.citrus.admin.service;

import com.consol.citrus.admin.converter.spring.NamespaceContextSpringBeanConverter;
import com.consol.citrus.admin.spring.model.SpringBean;
import com.consol.citrus.model.config.core.NamespaceContextDefinition;
import com.consol.citrus.xml.namespace.NamespaceContextBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.util.Collections;
import java.util.List;

/**
 * @author Christoph Deppisch
 * @since 2.0
 */
@Component
public class NamespaceContextService {

    @Autowired
    private SpringBeanService springBeanService;

    private NamespaceContextSpringBeanConverter springBeanConverter = new NamespaceContextSpringBeanConverter();

    /**
     * Gets the namespace context definition from application context. In case of legacy bean definition convert
     * and construct proper namespace context element.
     * @param projectConfigFile
     * @return
     */
    public NamespaceContextDefinition getNamespaceContext(File projectConfigFile) {
        List<NamespaceContextDefinition> contexts = springBeanService.getBeanDefinitions(projectConfigFile, NamespaceContextDefinition.class);

        if (CollectionUtils.isEmpty(contexts)) {
            List<SpringBean> springBeans = springBeanService.getBeanDefinitions(projectConfigFile, SpringBean.class, Collections.singletonMap("class", NamespaceContextBuilder.class.getName()));
            if (!CollectionUtils.isEmpty(springBeans)) {
                return springBeanConverter.convert(springBeans.get(0));
            }
        }

        if (CollectionUtils.isEmpty(contexts)) {
            return new NamespaceContextDefinition();
        } else {
            return contexts.get(0);
        }
    }

    /**
     * Overwrites namespace context bean definition with given jax object. Searches for all namespace context beans and overwrites
     * them with new content.
     *
     * @param projectConfigFile
     * @param context
     */
    public void updateNamespaceContext(File projectConfigFile, NamespaceContextDefinition context) {
        if (context.getNamespaces().isEmpty()) {
            springBeanService.removeBeanDefinitions(projectConfigFile, NamespaceContextDefinition.class);
        } else if (getNamespaceContext(projectConfigFile).getNamespaces().isEmpty()) {
            springBeanService.addBeanDefinition(projectConfigFile, context);
        } else {
            springBeanService.updateBeanDefinitions(projectConfigFile, NamespaceContextDefinition.class, context);
        }
    }
}
