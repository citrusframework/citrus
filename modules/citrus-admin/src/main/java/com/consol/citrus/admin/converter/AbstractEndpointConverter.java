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

package com.consol.citrus.admin.converter;

import com.consol.citrus.admin.model.EndpointData;
import org.springframework.util.ReflectionUtils;

import javax.xml.bind.annotation.XmlAttribute;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @author Christoph Deppisch
 * @since 1.4.1
 */
public abstract class AbstractEndpointConverter<T> implements EndpointConverter<T> {

    /**
     * Adds basic endpoint properties using reflection on definition objects.
     * @param endpointData
     * @param definition
     */
    protected void addEndpointProperties(EndpointData endpointData, Object definition) {
        add("timeout", endpointData, definition, "5000");
        add("actor", endpointData, definition);
    }

    protected void add(String fieldName, EndpointData endpointData, Object definition) {
        add(fieldName, endpointData, definition, null);
    }

    protected void add(String fieldName, EndpointData endpointData, Object definition, String defaultValue) {
        Field field = ReflectionUtils.findField(definition.getClass(), fieldName);

        if (field != null) {
            Method getter = ReflectionUtils.findMethod(definition.getClass(), getMethodName(fieldName));

            String value = defaultValue;
            if (getter != null) {
                Object getterResult = ReflectionUtils.invokeMethod(getter, definition);
                if (getterResult != null) {
                    value = getterResult.toString();
                }
            }

            if (value != null) {
                if (field.isAnnotationPresent(XmlAttribute.class)) {
                    endpointData.add(field.getAnnotation(XmlAttribute.class).name(), value);
                } else {
                    endpointData.add(fieldName, value);
                }
            }
        }
    }

    private String getMethodName(String fieldName) {
        return "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
    }

}
