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
import com.consol.citrus.model.config.core.FunctionLibraryDefinition;
import com.consol.citrus.model.config.core.ObjectFactory;
import org.springframework.util.StringUtils;

/**
 * Converter constructs Citrus function library representation form legacy spring bean definition.
 *
 * @author Christoph Deppisch
 * @since 2.0
 */
public class FunctionLibrarySpringBeanConverter implements SpringBeanConverter<FunctionLibraryDefinition> {

    @Override
    public FunctionLibraryDefinition convert(SpringBean springBean) {
        FunctionLibraryDefinition library = new ObjectFactory().createFunctionLibraryDefinition();

        for (Property property : springBean.getProperties()) {
            if (property.getName().equals("members")) {
                for (Entry entry : property.getMap().getEntries()) {
                    FunctionLibraryDefinition.Function function = new FunctionLibraryDefinition.Function();
                    function.setName(entry.getKey());
                    function.setClazz(entry.getValue());
                    library.getFunctions().add(function);
                }
            }
        }

        library.setId(StringUtils.hasText(springBean.getId()) ? springBean.getId() : String.valueOf(System.currentTimeMillis()));

        return library;
    }

    @Override
    public Class<SpringBean> getModelClass() {
        return SpringBean.class;
    }
}
