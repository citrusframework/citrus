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

import com.consol.citrus.admin.model.Property;
import com.consol.citrus.admin.service.ProjectService;
import com.consol.citrus.variable.VariableUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import javax.xml.bind.annotation.XmlAttribute;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @author Christoph Deppisch
 * @since 2.0
 */
public abstract class AbstractObjectConverter<T, S> implements ObjectConverter<T, S> {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(AbstractObjectConverter.class);

    @Autowired
    private ProjectService projectService;

    /**
     * Adds new endpoint property.
     * @param fieldName
     * @param definition
     */
    protected Property property(String fieldName, S definition) {
        return property(fieldName, definition, null);
    }

    /**
     * Adds new endpoint property.
     * @param fieldName
     * @param definition
     * @param defaultValue
     */
    protected Property property(String fieldName, S definition, String defaultValue) {
        return property(fieldName, StringUtils.capitalize(fieldName), definition, defaultValue);
    }

    /**
     * Adds new endpoint property.
     * @param fieldName
     * @param displayName
     * @param definition
     */
    protected Property property(String fieldName, String displayName, S definition) {
        return property(fieldName, displayName, definition, null);
    }

    /**
     * Adds new endpoint property.
     * @param fieldName
     * @param displayName
     * @param definition
     * @param defaultValue
     */
    protected Property property(String fieldName, String displayName, S definition, String defaultValue) {
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
                    return new Property(field.getAnnotation(XmlAttribute.class).name(), fieldName, displayName, resolvePropertyExpression(value));
                } else {
                    return new Property(fieldName, fieldName, displayName, resolvePropertyExpression(value));
                }
            } else {
                return new Property(fieldName, fieldName, displayName, null);
            }
        } else {
            log.warn(String.format("Unknown field %s on endpoint type %s", fieldName, definition.getClass()));
            return null;
        }
    }

    /**
     * Resolves property value with project properties in case value is a property expression.
     * @param value
     * @return
     */
    protected String resolvePropertyExpression(String value) {
        if (VariableUtils.isVariableName(value)) {
            return projectService.getProjectProperties().getProperty(VariableUtils.cutOffVariablesPrefix(value));
        } else {
            return value;
        }
    }

    /**
     * Construct default Java bean property getter for field name.
     * @param fieldName
     * @return
     */
    private String getMethodName(String fieldName) {
        return "get" + StringUtils.capitalize(fieldName);
    }
}
