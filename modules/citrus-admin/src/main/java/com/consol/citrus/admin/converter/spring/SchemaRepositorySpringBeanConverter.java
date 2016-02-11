/*
 * Copyright 2006-2013 the original author or authors.
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

import com.consol.citrus.admin.exception.CitrusAdminRuntimeException;
import com.consol.citrus.admin.spring.model.*;
import com.consol.citrus.model.config.core.ObjectFactory;
import com.consol.citrus.model.config.core.*;
import com.consol.citrus.xml.schema.WsdlXsdSchema;
import org.springframework.util.StringUtils;
import org.springframework.xml.xsd.SimpleXsdSchema;

/**
 * Converter capable to create proper schema repository from legacy spring bean definition.
 *
 * @author Christoph Deppisch
 * @since 1.3.1
 */
public class SchemaRepositorySpringBeanConverter implements SpringBeanConverter<SchemaRepositoryDefinition> {

    @Override
    public SchemaRepositoryDefinition convert(SpringBean springBean) {
        SchemaRepositoryDefinition repository = new ObjectFactory().createSchemaRepositoryDefinition();
        repository.setId(springBean.getId());
        repository.setSchemas(new SchemaRepositoryDefinition.Schemas());

        for (Property property : springBean.getProperties()) {
            if (property.getName().equals("schemas")) {
                for (Object item : property.getList().getListItems()) {
                    if (item instanceof SpringBean) {
                        SpringBean bean = (SpringBean)item;

                        SchemaDefinition schema = new SchemaDefinition();

                        if (StringUtils.hasText(bean.getId())) {
                            schema.setId(bean.getId());
                        } else {
                            schema.setId(String.valueOf(System.currentTimeMillis()));
                        }

                        Class beanClass;
                        try {
                            beanClass = Class.forName(springBean.getClazz());
                        } catch (ClassNotFoundException e) {
                            throw new CitrusAdminRuntimeException(String.format("Failed to access Spring bean of type '%s'", springBean.getClazz()));
                        }

                        if (SimpleXsdSchema.class.isAssignableFrom(beanClass)) {
                            for (Property beanProperty : bean.getProperties()) {
                                if (beanProperty.getName().equals("xsd")) {
                                    schema.setLocation(beanProperty.getValue());
                                }
                            }
                        } else if (WsdlXsdSchema.class.isAssignableFrom(beanClass)) {
                            for (Property beanProperty : bean.getProperties()) {
                                if (beanProperty.getName().equals("wsdl")) {
                                    schema.setLocation(beanProperty.getValue());
                                }
                            }
                        }

                        repository.getSchemas().getSchemas().add(schema);
                    } else if (item instanceof Ref) {
                        Ref ref = (Ref) item;
                        SchemaRepositoryDefinition.Schemas.Reference schema = new SchemaRepositoryDefinition.Schemas.Reference();
                        schema.setSchema(ref.getBean());
                        repository.getSchemas().getReferences().add(schema);
                    }
                }
            }
        }

        return repository;
    }

    @Override
    public Class<SpringBean> getModelClass() {
        return SpringBean.class;
    }
}
