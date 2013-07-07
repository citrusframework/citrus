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

package com.consol.citrus.admin.converter;

import com.consol.citrus.admin.spring.model.Property;
import com.consol.citrus.admin.spring.model.Ref;
import com.consol.citrus.admin.spring.model.SpringBean;
import com.consol.citrus.model.config.core.ObjectFactory;
import com.consol.citrus.model.config.core.SchemaRepository;
import org.springframework.util.StringUtils;
import org.springframework.xml.xsd.SimpleXsdSchema;

/**
 * Converter capable to create proper schema repository from legacy spring bean definition.
 *
 * @author Christoph Deppisch
 * @since 1.3.1
 */
public class SchemaRepositorySpringBeanConverter implements SpringBeanConverter<SchemaRepository> {

    @Override
    public SchemaRepository convert(SpringBean springBean) {
        SchemaRepository repository = new ObjectFactory().createSchemaRepository();
        repository.setId(springBean.getId());
        repository.setSchemas(new SchemaRepository.Schemas());

        for (Property property : springBean.getProperties()) {
            if (property.getName().equals("schemas")) {
                for (Object item : property.getList().getListItems()) {
                    if (item instanceof SpringBean) {
                        SpringBean bean = (SpringBean)item;

                        if (bean.getClazz().equals(SimpleXsdSchema.class.getName())) {
                            SchemaRepository.Schemas.Schema schema = new SchemaRepository.Schemas.Schema();

                            if (StringUtils.hasText(bean.getId())) {
                                schema.setRef(bean.getId());
                                repository.getSchemas().getSchemas().add(schema);
                                continue;
                            }

                            for (Property beanProperty : bean.getProperties()) {
                                if (beanProperty.getName().equals("xsd")) {
                                    schema.setRef(beanProperty.getValue());
                                    repository.getSchemas().getSchemas().add(schema);
                                }
                            }
                        }
                    } else if (item instanceof Ref) {
                        Ref ref = (Ref) item;
                        SchemaRepository.Schemas.Schema schema = new SchemaRepository.Schemas.Schema();
                        schema.setRef(ref.getBean());
                        repository.getSchemas().getSchemas().add(schema);
                    }
                }
            }
        }

        return repository;
    }
}
