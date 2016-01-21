/*
 * Copyright 2006-2016 the original author or authors.
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

package com.consol.citrus.jmx.model;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import org.springframework.util.StringUtils;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class ManagedBeanDefinition {

    private Class type;
    private String objectDomain;
    private String objectName;

    /**
     * Gets the value of the type property.
     *
     * @return the type
     */
    public Class getType() {
        return type;
    }

    /**
     * Sets the type property.
     *
     * @param type
     */
    public void setType(Class type) {
        this.type = type;
    }

    /**
     * Gets the value of the objectDomain property.
     *
     * @return the objectDomain
     */
    public String getObjectDomain() {
        return objectDomain;
    }

    /**
     * Sets the objectDomain property.
     *
     * @param objectDomain
     */
    public void setObjectDomain(String objectDomain) {
        this.objectDomain = objectDomain;
    }

    /**
     * Gets the value of the objectName property.
     *
     * @return the objectName
     */
    public String getObjectName() {
        return objectName;
    }

    /**
     * Sets the objectName property.
     *
     * @param objectName
     */
    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    /**
     * Constructs proper object name either from given domain and name property or
     * by evaluating the mbean type class information.
     *
     * @return
     */
    public ObjectName createObjectName() {
        try {
            if (StringUtils.hasText(objectName)) {
                return new ObjectName(objectDomain + ":" + objectName);
            }

            if (StringUtils.hasText(objectDomain)) {
                return new ObjectName(objectDomain, "type", type.getSimpleName());
            }

            return new ObjectName(type.getPackage().getName(), "type", type.getSimpleName());
        } catch (MalformedObjectNameException e) {
            throw new CitrusRuntimeException("Failed to create proper object name for managed bean", e);
        }
    }
}
