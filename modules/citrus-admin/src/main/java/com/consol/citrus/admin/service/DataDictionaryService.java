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

import com.consol.citrus.admin.converter.spring.DataDictionarySpringBeanConverter;
import com.consol.citrus.admin.exception.CitrusAdminRuntimeException;
import com.consol.citrus.admin.spring.model.SpringBean;
import com.consol.citrus.model.config.core.*;
import com.consol.citrus.variable.dictionary.json.JsonMappingDataDictionary;
import com.consol.citrus.variable.dictionary.xml.NodeMappingDataDictionary;
import com.consol.citrus.variable.dictionary.xml.XpathMappingDataDictionary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @author Christoph Deppisch
 * @since 2.0
 */
@Component
public class DataDictionaryService {

    @Autowired
    private SpringBeanService springBeanService;

    @Autowired
    private ProjectService projectService;

    private DataDictionarySpringBeanConverter dictionarySpringBeanConverter = new DataDictionarySpringBeanConverter();

    /**
     * Gets the data dictionary object from bean in application context. Bean is identified by its id.
     * @param id
     * @return
     */
    public DataDictionaryType getDataDictionary(String id) {
        DataDictionaryType library = springBeanService.getBeanDefinition(projectService.getProjectContextConfigFile(), id, XpathDataDictionaryDefinition.class);

        if (library == null) {
            library = springBeanService.getBeanDefinition(projectService.getProjectContextConfigFile(), id, XmlDataDictionaryDefinition.class);
        }

        if (library == null) {
            library = springBeanService.getBeanDefinition(projectService.getProjectContextConfigFile(), id, JsonDataDictionaryDefinition.class);
        }

        if (library == null) {
            SpringBean springBean = springBeanService.getBeanDefinition(projectService.getProjectContextConfigFile(), id, SpringBean.class);
            if (springBean != null) {
                library = dictionarySpringBeanConverter.convert(springBean);
            }
        }

        if (library == null) {
            throw new CitrusAdminRuntimeException(String.format("Unable to find data dictionary definition for id '%s'", id));
        }

        return library;
    }

    /**
     * List all data dictionaries in application context.
     * @return
     */
    public List<DataDictionaryType> listDataDictionaries() {
        List<DataDictionaryType> libraries = new ArrayList<DataDictionaryType>();

        libraries.addAll(springBeanService.getBeanDefinitions(projectService.getProjectContextConfigFile(), XpathDataDictionaryDefinition.class));
        libraries.addAll(springBeanService.getBeanDefinitions(projectService.getProjectContextConfigFile(), XmlDataDictionaryDefinition.class));
        libraries.addAll(springBeanService.getBeanDefinitions(projectService.getProjectContextConfigFile(), JsonDataDictionaryDefinition.class));

        List<SpringBean> springBeans = new ArrayList<SpringBean>();
        springBeans.addAll(springBeanService.getBeanDefinitions(projectService.getProjectContextConfigFile(), SpringBean.class, Collections.singletonMap("class", XpathMappingDataDictionary.class.getName())));
        springBeans.addAll(springBeanService.getBeanDefinitions(projectService.getProjectContextConfigFile(), SpringBean.class, Collections.singletonMap("class", NodeMappingDataDictionary.class.getName())));
        springBeans.addAll(springBeanService.getBeanDefinitions(projectService.getProjectContextConfigFile(), SpringBean.class, Collections.singletonMap("class", JsonMappingDataDictionary.class.getName())));
        for (SpringBean springBean : springBeans) {
            libraries.add(dictionarySpringBeanConverter.convert(springBean));
        }

        return libraries;
    }

}
