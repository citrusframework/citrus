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

package com.consol.citrus.admin.converter.spring;

import com.consol.citrus.admin.spring.model.*;
import com.consol.citrus.model.config.core.GlobalVariablesDefinition;
import com.consol.citrus.model.config.core.ObjectFactory;

/**
 * Converter constructs Citrus global variables representation form legacy spring bean definition.
 *
 * @author Christoph Deppisch
 * @since 2.0
 */
public class GlobalVariablesSpringBeanConverter implements SpringBeanConverter<GlobalVariablesDefinition> {

    @Override
    public GlobalVariablesDefinition convert(SpringBean springBean) {
        GlobalVariablesDefinition variables = new ObjectFactory().createGlobalVariablesDefinition();

        for (Property property : springBean.getProperties()) {
            if (property.getName().equals("variables")) {
                for (Entry entry : property.getMap().getEntries()) {
                    GlobalVariablesDefinition.Variable variable = new GlobalVariablesDefinition.Variable();
                    variable.setName(entry.getKey());
                    variable.setValue(entry.getValue());
                    variables.getVariables().add(variable);
                }
            }
        }

        return variables;
    }

    @Override
    public Class<SpringBean> getModelClass() {
        return SpringBean.class;
    }
}
