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

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.util.ReflectionHelper;
import org.citrusframework.util.StringUtils;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class ManagedBeanDefinition {

    public static final String OPERATION_DESCRIPTION = "Operation exposed for management";
    public static final String ATTRIBUTE_DESCRIPTION = "Attribute exposed for management";
    private Class<?> type;
    private String objectDomain;
    private String objectName;

    private String name = "org.citrusframework.CitrusMBean";
    private String description;

    private List<ManagedBeanInvocation.Operation> operations = new ArrayList<>();
    private List<ManagedBeanInvocation.Attribute> attributes = new ArrayList<>();

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

            if (type != null) {
                if (StringUtils.hasText(objectDomain)) {
                    return new ObjectName(objectDomain, "type", type.getSimpleName());
                }

                return new ObjectName(type.getPackage().getName(), "type", type.getSimpleName());
            }

            return new ObjectName(objectDomain, "name", name);
        } catch (NullPointerException | MalformedObjectNameException e) {
            throw new CitrusRuntimeException("Failed to create proper object name for managed bean", e);
        }
    }

    /**
     * Create managed bean info with all constructors, operations, notifications and attributes.
     * @return
     */
    public MBeanInfo createMBeanInfo() {
        if (type != null) {
            return new MBeanInfo(type.getName(), description, getAttributeInfo(), getConstructorInfo(), getOperationInfo(), getNotificationInfo());
        } else {
            return new MBeanInfo(name, description, getAttributeInfo(), getConstructorInfo(), getOperationInfo(), getNotificationInfo());
        }
    }

    /**
     * Create notification info.
     * @return
     */
    private MBeanNotificationInfo[] getNotificationInfo() {
        return new MBeanNotificationInfo[0];
    }

    /**
     * Create this managed bean operations info.
     * @return
     */
    private MBeanOperationInfo[] getOperationInfo() {
        final List<MBeanOperationInfo> infoList = new ArrayList<>();

        if (type != null) {
            ReflectionHelper.doWithMethods(type, method -> {
                if (!method.getDeclaringClass().equals(type)
                        || method.getName().startsWith("set")
                        || method.getName().startsWith("get")
                        || method.getName().startsWith("is")
                        || method.getName().startsWith("$jacoco")) { // Fix for code coverage
                    return;
                }

                infoList.add(new MBeanOperationInfo(OPERATION_DESCRIPTION, method));
            });
        } else {
            for (ManagedBeanInvocation.Operation operation : operations) {
                List<MBeanParameterInfo> parameterInfo = new ArrayList<>();

                int i = 1;
                for (OperationParam parameter : operation.getParameter().getParameter()) {
                    parameterInfo.add(new MBeanParameterInfo("p" + i++, parameter.getType(), "Parameter #" + i));
                }

                infoList.add(new MBeanOperationInfo(operation.getName(), OPERATION_DESCRIPTION, parameterInfo.toArray(new MBeanParameterInfo[operation.getParameter().getParameter().size()]), operation.getReturnType(), MBeanOperationInfo.UNKNOWN));
            }
        }

        return infoList.toArray(new MBeanOperationInfo[infoList.size()]);
    }

    /**
     * Create this managed bean constructor info.
     * @return
     */
    private MBeanConstructorInfo[] getConstructorInfo() {
        final List<MBeanConstructorInfo> infoList = new ArrayList<>();

        if (type != null) {
            for (Constructor<?> constructor : type.getConstructors()) {
                infoList.add(new MBeanConstructorInfo(constructor.toGenericString(), constructor));
            }
        }

        return infoList.toArray(new MBeanConstructorInfo[infoList.size()]);
    }

    /**
     * Create this managed bean attributes info.
     * @return
     */
    private MBeanAttributeInfo[] getAttributeInfo() {
        final List<MBeanAttributeInfo> infoList = new ArrayList<>();

        if (type != null) {
            final List<String> attributes = new ArrayList<>();

            if (type.isInterface()) {
                ReflectionHelper.doWithMethods(type, method -> {
                    if (!method.getDeclaringClass().equals(type) ||
                            !(method.getName().startsWith("get") || method.getName().startsWith("is"))) {
                        return;
                    }

                    String attributeName;

                    if (method.getName().startsWith("get")) {
                        attributeName = method.getName().substring(3);
                    } else if (method.getName().startsWith("is")) {
                        attributeName = method.getName().substring(2);
                    } else {
                        attributeName = method.getName();
                    }

                    if (!attributes.contains(attributeName)) {
                        infoList.add(new MBeanAttributeInfo(attributeName, method.getReturnType().getName(), ATTRIBUTE_DESCRIPTION, true, true, method.getName().startsWith("is")));
                        attributes.add(attributeName);
                    }
                });
            } else {
                ReflectionHelper.doWithFields(type, field -> {
                    if (Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers())) {
                        return;
                    }

                    infoList.add(new MBeanAttributeInfo(field.getName(), field.getType().getName(), ATTRIBUTE_DESCRIPTION, true, true, field.getType().equals(Boolean.class)));
                });
            }
        } else {
            int i = 1;
            for (ManagedBeanInvocation.Attribute attribute : attributes) {
                infoList.add(new MBeanAttributeInfo(attribute.getName(), attribute.getType(), ATTRIBUTE_DESCRIPTION, true, true, attribute.getType().equals(Boolean.class.getName())));
            }
        }

        return infoList.toArray(new MBeanAttributeInfo[infoList.size()]);
    }

    /**
     * Gets the value of the operations property.
     *
     * @return the operations
     */
    public List<ManagedBeanInvocation.Operation> getOperations() {
        return operations;
    }

    /**
     * Sets the operations property.
     *
     * @param operations
     */
    public void setOperations(List<ManagedBeanInvocation.Operation> operations) {
        this.operations = operations;
    }

    /**
     * Gets the value of the attributes property.
     *
     * @return the attributes
     */
    public List<ManagedBeanInvocation.Attribute> getAttributes() {
        return attributes;
    }

    /**
     * Sets the attributes property.
     *
     * @param attributes
     */
    public void setAttributes(List<ManagedBeanInvocation.Attribute> attributes) {
        this.attributes = attributes;
    }

    /**
     * Gets the value of the description property.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description property.
     *
     * @param description
     */
    public void setDescription(String description) {
        this.description = description;
    }

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
     * Gets the value of the name property.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name property.
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
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

}
