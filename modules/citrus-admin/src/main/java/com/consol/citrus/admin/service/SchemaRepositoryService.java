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

package com.consol.citrus.admin.service;

import com.consol.citrus.admin.converter.spring.SchemaRepositorySpringBeanConverter;
import com.consol.citrus.admin.converter.spring.SchemaSpringBeanConverter;
import com.consol.citrus.admin.spring.model.SpringBean;
import com.consol.citrus.model.config.core.Schema;
import com.consol.citrus.model.config.core.SchemaRepository;
import com.consol.citrus.xml.XsdSchemaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.xml.xsd.SimpleXsdSchema;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Service manages spring application context bean access for schema repositories.
 *
 * @author Christoph Deppisch
 * @since 1.3.1
 */
@Component
public class SchemaRepositoryService {

    @Autowired
    private SpringBeanService springBeanService;

    /** Converter for legacy spring bean definitions */
    private SchemaRepositorySpringBeanConverter schemaRepositorySpringBeanConverter = new SchemaRepositorySpringBeanConverter();
    private SchemaSpringBeanConverter schemaSpringBeanConverter = new SchemaSpringBeanConverter();

    /**
     * Lists all schema repository spring beans available in application context. This includes
     * all Citrus XML schema elements and all usual spring beans of type XsdSchemaRepository.
     * @param projectConfigFile
     * @return
     */
    public List<SchemaRepository> listSchemaRepositories(File projectConfigFile) {
        List<SchemaRepository> schemaRepositories = new ArrayList<SchemaRepository>();

        schemaRepositories.addAll(springBeanService.getBeanDefinitions(projectConfigFile, SchemaRepository.class));

        List<SpringBean> springBeans = springBeanService.getBeanDefinitions(projectConfigFile, SpringBean.class, Collections.singletonMap("class", XsdSchemaRepository.class.getName()));
        for (SpringBean springBean : springBeans) {
            schemaRepositories.add(schemaRepositorySpringBeanConverter.convert(springBean));
        }

        return schemaRepositories;
    }

    /**
     * Gets schema repository bean definition from application context. Method is able to load
     * legacy spring bean definitions in case Citrus schema repository element is not found.
     * @param projectConfigFile
     * @param id
     * @return
     */
    public SchemaRepository getSchemaRepository(File projectConfigFile, String id) {
        SchemaRepository repository = springBeanService.getBeanDefinition(projectConfigFile, id, SchemaRepository.class);

        if (repository == null) {
            SpringBean springBean = springBeanService.getBeanDefinition(projectConfigFile, id, SpringBean.class);
            if (springBean != null) {
                repository = schemaRepositorySpringBeanConverter.convert(springBean);
            }
        }

        return repository;
    }

    /**
     * Gets all available xsd schema bean definitions from application context file. Also
     * reads legacy bean definitions of class SimpleXsdSchema.
     *
     * @param projectConfigFile
     * @return
     */
    public List<Schema> listSchemas(File projectConfigFile) {
        List<Schema> schemas = new ArrayList<Schema>();

        schemas.addAll(springBeanService.getBeanDefinitions(projectConfigFile, Schema.class));

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
    public Schema getSchema(File projectConfigFile, String id) {
        Schema schema = springBeanService.getBeanDefinition(projectConfigFile, id, Schema.class);

        if (schema == null) {
            SpringBean springBean = springBeanService.getBeanDefinition(projectConfigFile, id, SpringBean.class);
            if (springBean != null) {
                schema = schemaSpringBeanConverter.convert(springBean);
            }
        }

        return schema;
    }
}
