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

import com.consol.citrus.admin.converter.spring.SchemaSpringBeanConverter;
import com.consol.citrus.admin.spring.model.SpringBean;
import com.consol.citrus.model.config.core.SchemaDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.xml.xsd.SimpleXsdSchema;

import java.io.File;
import java.util.*;

/**
 * @author Christoph Deppisch
 * @since 2.0
 */
@Component
public class SchemaService {

    @Autowired
    private SpringBeanService springBeanService;

    private SchemaSpringBeanConverter schemaSpringBeanConverter = new SchemaSpringBeanConverter();

    /**
     * Gets all available xsd schema bean definitions from application context file. Also
     * reads legacy bean definitions of class SimpleXsdSchema.
     *
     * @param projectConfigFile
     * @return
     */
    public List<SchemaDefinition> listSchemas(File projectConfigFile) {
        List<SchemaDefinition> schemas = new ArrayList<SchemaDefinition>();

        schemas.addAll(springBeanService.getBeanDefinitions(projectConfigFile, SchemaDefinition.class));

        List<SpringBean> springBeans = springBeanService.getBeanDefinitions(projectConfigFile, SpringBean.class, Collections.singletonMap("class", SimpleXsdSchema.class.getName()));
        for (SpringBean springBean : springBeans) {
            schemas.add(schemaSpringBeanConverter.convert(springBean));
        }

        return schemas;
    }

    /**
     * Gets the schema definition from application context. In case of legacy bean definition convert
     * and construct proper schema element.
     * @param projectConfigFile
     * @param id
     * @return
     */
    public SchemaDefinition getSchema(File projectConfigFile, String id) {
        SchemaDefinition schema = springBeanService.getBeanDefinition(projectConfigFile, id, SchemaDefinition.class);

        if (schema == null) {
            SpringBean springBean = springBeanService.getBeanDefinition(projectConfigFile, id, SpringBean.class);
            if (springBean != null) {
                schema = schemaSpringBeanConverter.convert(springBean);
            }
        }

        return schema;
    }

}
