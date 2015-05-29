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

import com.consol.citrus.admin.exception.CitrusAdminRuntimeException;
import com.consol.citrus.admin.spring.model.*;
import com.consol.citrus.model.config.core.*;
import com.consol.citrus.model.config.core.ObjectFactory;
import com.consol.citrus.variable.dictionary.json.JsonMappingDataDictionary;
import com.consol.citrus.variable.dictionary.xml.NodeMappingDataDictionary;
import com.consol.citrus.variable.dictionary.xml.XpathMappingDataDictionary;
import org.springframework.util.StringUtils;

/**
 * Converter constructs Citrus data dictionary representation form legacy spring bean definition.
 *
 * @author Christoph Deppisch
 * @since 2.0
 */
public class DataDictionarySpringBeanConverter implements SpringBeanConverter<DataDictionaryType> {

    @Override
    public DataDictionaryType convert(SpringBean springBean) {
        DataDictionaryType library;

        if (springBean.getClazz().equals(XpathMappingDataDictionary.class.getName())) {
            library = new ObjectFactory().createXpathDataDictionaryDefinition();
        } else if (springBean.getClazz().equals(NodeMappingDataDictionary.class.getName())) {
            library = new ObjectFactory().createXmlDataDictionaryDefinition();
        } else if (springBean.getClazz().equals(JsonMappingDataDictionary.class.getName())) {
            library = new ObjectFactory().createJsonDataDictionaryDefinition();
        } else {
            throw new CitrusAdminRuntimeException(String.format("Failed to convert Spring bean of type '%s' to data dictionary model object", springBean.getClazz()));
        }

        for (Property property : springBean.getProperties()) {
            if (property.getName().equals("mappings")) {
                for (Entry entry : property.getMap().getEntries()) {
                    DataDictionaryType.Mappings.Mapping mapping = new DataDictionaryType.Mappings.Mapping();
                    mapping.setPath(entry.getKey());
                    mapping.setValue(entry.getValue());
                    library.getMappings().getMappings().add(mapping);
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
