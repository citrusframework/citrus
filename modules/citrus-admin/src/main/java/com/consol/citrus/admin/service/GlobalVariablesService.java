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

import com.consol.citrus.admin.converter.spring.GlobalVariablesSpringBeanConverter;
import com.consol.citrus.admin.spring.model.SpringBean;
import com.consol.citrus.model.config.core.GlobalVariablesDefinition;
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
public class GlobalVariablesService {

    @Autowired
    private SpringBeanService springBeanService;

    private GlobalVariablesSpringBeanConverter springBeanConverter = new GlobalVariablesSpringBeanConverter();

    /**
     * Gets the global variables definition from application context. In case of legacy bean definition convert
     * and construct proper global variables element.
     * @param projectConfigFile
     * @return
     */
    public GlobalVariablesDefinition getGlobalVariables(File projectConfigFile) {
        List<GlobalVariablesDefinition> contexts = springBeanService.getBeanDefinitions(projectConfigFile, GlobalVariablesDefinition.class);

        if (CollectionUtils.isEmpty(contexts)) {
            List<SpringBean> springBeans = springBeanService.getBeanDefinitions(projectConfigFile, SpringBean.class, Collections.singletonMap("class", com.consol.citrus.variable.GlobalVariables.class.getName()));
            if (!CollectionUtils.isEmpty(springBeans)) {
                return springBeanConverter.convert(springBeans.get(0));
            }
        }

        if (CollectionUtils.isEmpty(contexts)) {
            return new GlobalVariablesDefinition();
        } else {
            return contexts.get(0);
        }
    }

    /**
     * Overwrites global variables bean definition with given jax object. Searches for all global variables beans and overwrites
     * them with new content.
     *
     * @param projectConfigFile
     * @param context
     */
    public void updateGlobalVariables(File projectConfigFile, GlobalVariablesDefinition context) {
        if (context.getVariables().isEmpty()) {
            springBeanService.removeBeanDefinitions(projectConfigFile, GlobalVariablesDefinition.class);
        } else if (getGlobalVariables(projectConfigFile).getVariables().isEmpty()) {
            springBeanService.addBeanDefinition(projectConfigFile, context);
        } else {
            springBeanService.updateBeanDefinitions(projectConfigFile, GlobalVariablesDefinition.class, context);
        }
    }
}
