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
import com.consol.citrus.model.config.core.ObjectFactory;
import com.consol.citrus.model.config.core.ValidationMatcherLibraryDefinition;
import org.springframework.util.StringUtils;

/**
 * Converter constructs Citrus validation matcher representation form legacy spring bean definition.
 *
 * @author Christoph Deppisch
 * @since 2.0
 */
public class ValidationMatcherSpringBeanConverter implements SpringBeanConverter<ValidationMatcherLibraryDefinition> {

    @Override
    public ValidationMatcherLibraryDefinition convert(SpringBean springBean) {
        ValidationMatcherLibraryDefinition library = new ObjectFactory().createValidationMatcherLibraryDefinition();

        for (Property property : springBean.getProperties()) {
            if (property.getName().equals("members")) {
                for (Entry entry : property.getMap().getEntries()) {
                    ValidationMatcherLibraryDefinition.Matcher matcher = new ValidationMatcherLibraryDefinition.Matcher();
                    matcher.setName(entry.getKey());
                    matcher.setClazz(entry.getValue());
                    library.getMatchers().add(matcher);
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
