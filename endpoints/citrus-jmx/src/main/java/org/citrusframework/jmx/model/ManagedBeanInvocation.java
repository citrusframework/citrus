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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
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
        "mbean",
        "objectDomain",
        "objectName",
        "objectKey",
        "objectValue",
        "attribute",
        "operation"
})
@XmlRootElement(name = "mbean-invocation")
public class ManagedBeanInvocation {

    @XmlElement
    protected String mbean;
    @XmlElement
    protected String objectDomain;
    @XmlElement
    protected String objectName;
    @XmlElement
    protected String objectKey;
    @XmlElement
    protected String objectValue;

    @XmlElement
    protected ManagedBeanInvocation.Attribute attribute;

    @XmlElement
    protected ManagedBeanInvocation.Operation operation;

    /**
     * Gets this service result as object casted to target type if necessary.
     * @return
     */
    public java.lang.Object getAttributeValue(ReferenceResolver referenceResolver) {
        if (attribute == null) {
            return null;
        }

        if (attribute.getValueObject() != null) {
            return attribute.getValueObject();
        }

        try {
            Class argType = Class.forName(attribute.getType());
            java.lang.Object value = null;

            if (attribute.getValue() != null) {
                value = attribute.getValue();
            } else if (StringUtils.hasText(attribute.getRef()) && referenceResolver != null) {
                value = referenceResolver.resolve(attribute.getRef());
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
                    throw new CitrusRuntimeException("Failed to reconstruct attribute object of type map", e);
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
            throw new CitrusRuntimeException("Failed to construct attribute object", e);
        }
    }

    /**
     * Gets the value of the mbean property.
     *
     * @return the mbean
     */
    public String getMbean() {
        return mbean;
    }

    /**
     * Sets the mbean property.
     *
     * @param mbean
     */
    public void setMbean(String mbean) {
        this.mbean = mbean;
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
     * Gets the value of the objectKey property.
     *
     * @return the objectKey
     */
    public String getObjectKey() {
        return objectKey;
    }

    /**
     * Sets the objectKey property.
     *
     * @param objectKey
     */
    public void setObjectKey(String objectKey) {
        this.objectKey = objectKey;
    }

    /**
     * Gets the value of the objectValue property.
     *
     * @return the objectValue
     */
    public String getObjectValue() {
        return objectValue;
    }

    /**
     * Sets the objectValue property.
     *
     * @param objectValue
     */
    public void setObjectValue(String objectValue) {
        this.objectValue = objectValue;
    }

    /**
     * Gets the value of the operation property.
     *
     * @return the operation
     */
    public ManagedBeanInvocation.Operation getOperation() {
        return operation;
    }

    /**
     * Sets the operation property.
     *
     * @param operation
     */
    public void setOperation(ManagedBeanInvocation.Operation operation) {
        this.operation = operation;
    }

    /**
     * Gets the value of the attribute property.
     *
     * @return the attribute
     */
    public ManagedBeanInvocation.Attribute getAttribute() {
        return attribute;
    }

    /**
     * Sets the attribute property.
     *
     * @param attribute
     */
    public void setAttribute(ManagedBeanInvocation.Attribute attribute) {
        this.attribute = attribute;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Attribute {

        @XmlAttribute(name = "name")
        protected String name;
        @XmlAttribute(name = "type")
        protected String type = String.class.getName();
        @XmlAttribute(name = "value")
        protected String value;
        @XmlAttribute(name = "ref")
        protected String ref;
        @XmlAttribute(name = "inner-path")
        protected String innerPath;

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
         * Gets the value of the name property.
         *
         * @return
         *     possible object is
         *     {@link String }
         *
         */
        public String getName() {
            return name;
        }

        /**
         * Sets the value of the name property.
         *
         * @param value
         *     allowed object is
         *     {@link String }
         *
         */
        public void setName(String value) {
            this.name = value;
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

        /**
         * Gets the value of the innerPath property.
         *
         * @return
         *     possible object is
         *     {@link String }
         *
         */
        public String getInnerPath() {
            return innerPath;
        }

        /**
         * Sets the value of the innerPath property.
         *
         * @param value
         *     allowed object is
         *     {@link String }
         *
         */
        public void setInnerPath(String value) {
            this.innerPath = value;
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

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Operation {

        @XmlAttribute(name = "name")
        protected String name;

        @XmlAttribute(name = "return-type")
        protected String returnType;

        protected ManagedBeanInvocation.Parameter parameter;

        /**
         * Gets the argument types from list of parameter.
         * @return
         */
        public String[] getParamTypes() {
            List<String> types = new ArrayList<>();

            if (parameter != null) {
                for (OperationParam arg : parameter.getParameter()) {
                    types.add(arg.getType());
                }
            }

            return types.toArray(new String[types.size()]);
        }

        /**
         * Gets method parameter as objects. Automatically converts simple types and ready referenced beans.
         * @return
         */
        public Object[] getParamValues(ReferenceResolver referenceResolver) {
            List<Object> argValues = new ArrayList<>();

            try {
                if (parameter != null) {
                    for (OperationParam operationParam : parameter.getParameter()) {
                        Class argType = Class.forName(operationParam.getType());
                        Object value = null;

                        if (operationParam.getValueObject() != null) {
                            value = operationParam.getValueObject();
                        } else if (operationParam.getValue() != null) {
                            value = operationParam.getValue();
                        } else if (StringUtils.hasText(operationParam.getRef()) && referenceResolver != null) {
                            value = referenceResolver.resolve(operationParam.getRef());
                        }

                        if (value == null) {
                            argValues.add(null);
                        } else if (argType.isInstance(value) || argType.isAssignableFrom(value.getClass())) {
                            argValues.add(argType.cast(value));
                        } else if (Map.class.equals(argType)) {
                            String mapString = value.toString();

                            Properties props = new Properties();
                            try {
                                props.load(new StringReader(mapString.substring(1, mapString.length() - 1).replace(", ", "\n")));
                            } catch (IOException e) {
                                throw new CitrusRuntimeException("Failed to reconstruct method argument of type map", e);
                            }
                            Map<String, String> map = new LinkedHashMap<>();
                            for (Map.Entry<Object, Object> entry : props.entrySet()) {
                                map.put(entry.getKey().toString(), entry.getValue().toString());
                            }

                            argValues.add(map);
                        } else {
                            argValues.add(TypeConverter.lookupDefault().convertIfNecessary(value, argType));
                        }
                    }
                }
            } catch (ClassNotFoundException e) {
                throw new CitrusRuntimeException("Failed to construct method arg objects", e);
            }

            return argValues.toArray(new Object[argValues.size()]);
        }

        /**
         * Gets the value of the name property.
         *
         * @return
         *     possible object is
         *     {@link String }
         *
         */
        public String getName() {
            return name;
        }

        /**
         * Sets the value of the name property.
         *
         * @param value
         *     allowed object is
         *     {@link String }
         *
         */
        public void setName(String value) {
            this.name = value;
        }

        /**
         * Gets the value of the returnType property.
         *
         * @return the returnType
         */
        public String getReturnType() {
            return returnType;
        }

        /**
         * Sets the returnType property.
         *
         * @param returnType
         */
        public void setReturnType(String returnType) {
            this.returnType = returnType;
        }

        /**
         * Gets the value of the parameter property.
         *
         * @return
         *     possible object is
         *     {@link ManagedBeanInvocation.Parameter }
         *
         */
        public ManagedBeanInvocation.Parameter getParameter() {
            return parameter;
        }

        /**
         * Sets the value of the parameter property.
         *
         * @param value
         *     allowed object is
         *     {@link ManagedBeanInvocation.Parameter }
         *
         */
        public void setParameter(ManagedBeanInvocation.Parameter value) {
            this.parameter = value;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
            "parameter"
    })
    public static class Parameter {

        @XmlElement(name = "param", required = true)
        protected List<OperationParam> parameter;

        public List<OperationParam> getParameter() {
            if (parameter == null) {
                parameter = new ArrayList<OperationParam>();
            }
            return this.parameter;
        }

    }
}
