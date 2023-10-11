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

package org.citrusframework.jmx.model;

import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.XmlType;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.util.StringUtils;
import org.citrusframework.util.TypeConverter;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "object"
})
@XmlRootElement(name = "mbean-result")
public class ManagedBeanResult {

    @XmlElement
    protected ManagedBeanResult.Object object;

    /**
     * Gets the value of the object property.
     *
     * @return
     *     possible object is
     *     {@link ManagedBeanResult.Object }
     *
     */
    public ManagedBeanResult.Object getObject() {
        return object;
    }

    /**
     * Sets the value of the object property.
     *
     * @param value
     *     allowed object is
     *     {@link ManagedBeanResult.Object }
     *
     */
    public void setObject(ManagedBeanResult.Object value) {
        this.object = value;
    }

    /**
     * Gets this service result as object casted to target type if necessary.
     * @return
     */
    public java.lang.Object getResultObject(ReferenceResolver referenceResolver) {
        if (object == null) {
            return null;
        }

        if (object.getValueObject() != null) {
            return object.getValueObject();
        }

        try {
            Class<?> argType = Class.forName(object.getType());
            java.lang.Object value = null;

            if (object.getValue() != null) {
                value = object.getValue();
            } else if (StringUtils.hasText(object.getRef()) && referenceResolver != null) {
                value = referenceResolver.resolve(object.getRef());
            }

            if (value == null) {
                return null;
            } else if (argType.isInstance(value) || argType.isAssignableFrom(value.getClass())) {
                return argType.cast(value);
            } else if (Map.class.equals(argType)) {
                String mapString = value.toString();

                Properties props = new Properties();
                try {
                    props.load(new StringReader(mapString.substring(1, mapString.length() - 1).replace(", ", "\n")));
                } catch (IOException e) {
                    throw new CitrusRuntimeException("Failed to reconstruct service result object of type map", e);
                }
                Map<String, String> map = new LinkedHashMap<>();
                for (Map.Entry<java.lang.Object, java.lang.Object> entry : props.entrySet()) {
                    map.put(entry.getKey().toString(), entry.getValue().toString());
                }

                return map;
            } else {
                return TypeConverter.lookupDefault().convertIfNecessary(value, argType);
            }
        } catch (ClassNotFoundException e) {
            throw new CitrusRuntimeException("Failed to construct service result object", e);
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Object {

        @XmlAttribute(name = "type")
        protected String type = String.class.getName();
        @XmlAttribute(name = "value")
        protected String value;
        @XmlAttribute(name = "ref")
        protected String ref;

        @XmlTransient
        private java.lang.Object valueObject;

        /**
         * Gets the value of the type property.
         *
         * @return
         *     possible object is
         *     {@link String }
         *
         */
        public String getType() {
            return type;
        }

        /**
         * Sets the value of the type property.
         *
         * @param value
         *     allowed object is
         *     {@link String }
         *
         */
        public void setType(String value) {
            this.type = value;
        }

        /**
         * Gets the value of the value property.
         *
         * @return
         *     possible object is
         *     {@link String }
         *
         */
        public String getValue() {
            return value;
        }

        /**
         * Sets the value of the value property.
         *
         * @param value
         *     allowed object is
         *     {@link String }
         *
         */
        public void setValue(String value) {
            this.value = value;
        }

        /**
         * Gets the value of the ref property.
         *
         * @return
         *     possible object is
         *     {@link String }
         *
         */
        public String getRef() {
            return ref;
        }

        /**
         * Sets the value of the ref property.
         *
         * @param value
         *     allowed object is
         *     {@link String }
         *
         */
        public void setRef(String value) {
            this.ref = value;
        }

        public java.lang.Object getValueObject() {
            return valueObject;
        }

        public void setValueObject(java.lang.Object valueObject) {
            setType(valueObject.getClass().getName());
            setValue(valueObject.toString());
            this.valueObject = valueObject;
        }
    }
}
