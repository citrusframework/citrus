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
import com.consol.citrus.model.config.core.*;
import com.consol.citrus.model.config.core.ObjectFactory;

/**
 * Converter constructs Citrus namespace context representation form legacy spring bean definition.
 *
 * @author Christoph Deppisch
 * @since 2.0
 */
public class NamespaceContextSpringBeanConverter implements SpringBeanConverter<NamespaceContextDefinition> {

    @Override
    public NamespaceContextDefinition convert(SpringBean springBean) {
        NamespaceContextDefinition context = new ObjectFactory().createNamespaceContextDefinition();

        for (Property property : springBean.getProperties()) {
            if (property.getName().equals("namespaceMappings")) {
                for (Entry entry : property.getMap().getEntries()) {
                    NamespaceContextDefinition.Namespace namespace = new NamespaceContextDefinition.Namespace();
                    namespace.setPrefix(entry.getKey());
                    namespace.setUri(entry.getValue());
                    context.getNamespaces().add(namespace);
                }
            }
        }

        return context;
    }

    @Override
    public Class<SpringBean> getModelClass() {
        return SpringBean.class;
    }
}
