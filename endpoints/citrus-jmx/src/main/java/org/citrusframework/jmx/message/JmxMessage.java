/*
 * Copyright 2006-2015 the original author or authors.
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

package org.citrusframework.jmx.message;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.jmx.model.JmxMarshaller;
import org.citrusframework.jmx.model.ManagedBeanInvocation;
import org.citrusframework.jmx.model.ManagedBeanResult;
import org.citrusframework.jmx.model.OperationParam;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.xml.StringResult;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class JmxMessage extends DefaultMessage {

    /** Model objects */
    private ManagedBeanInvocation mbeanInvocation;
    private ManagedBeanResult mbeanResult;

    private JmxMarshaller marshaller = new JmxMarshaller();

    /**
     * Prevent traditional instantiation.
     */
    private JmxMessage() { super(); }

    /**
     * Constructor initializes new service invocation message.
     * @param mbeanInvocation
     */
    private JmxMessage(ManagedBeanInvocation mbeanInvocation) {
        super(mbeanInvocation);
        this.mbeanInvocation = mbeanInvocation;
    }

    /**
     * Constructor initializes new service result message.
     * @param mbeanResult
     */
    private JmxMessage(ManagedBeanResult mbeanResult) {
        super(mbeanResult);
        this.mbeanResult = mbeanResult;
    }

    public static JmxMessage invocation(String mbean) {
        ManagedBeanInvocation invocation = new ManagedBeanInvocation();
        invocation.setMbean(mbean);

        return new JmxMessage(invocation);
    }

    public static JmxMessage invocation(String objectDomain, String objectName) {
        ManagedBeanInvocation invocation = new ManagedBeanInvocation();
        invocation.setObjectDomain(objectDomain);
        invocation.setObjectName(objectName);

        return new JmxMessage(invocation);
    }

    public static JmxMessage invocation(String objectDomain, String objectKey, String objectValue) {
        ManagedBeanInvocation invocation = new ManagedBeanInvocation();
        invocation.setObjectDomain(objectDomain);
        invocation.setObjectKey(objectKey);
        invocation.setObjectValue(objectValue);

        return new JmxMessage(invocation);
    }

    /**
     * Sets attribute for read operation.
     * @param name
     * @return
     */
    public JmxMessage attribute(String name) {
        return attribute(name, null, null);
    }

    /**
     * Sets attribute for write operation.
     * @param name
     * @param value
     * @return
     */
    public JmxMessage attribute(String name, Object value) {
        return attribute(name, value, value.getClass());
    }

    /**
     * Sets attribute for write operation with custom value type.
     * @param name
     * @param value
     * @param valueType
     * @return
     */
    public JmxMessage attribute(String name, Object value, Class<?> valueType) {
        if (mbeanInvocation == null) {
            throw new CitrusRuntimeException("Invalid access to attribute for JMX message");
        }

        ManagedBeanInvocation.Attribute attribute = new ManagedBeanInvocation.Attribute();
        attribute.setName(name);
        if (value != null) {
            attribute.setValueObject(value);
            attribute.setType(valueType.getName());
        }

        mbeanInvocation.setAttribute(attribute);
        return this;
    }

    /**
     * Sets operation for read write access.
     * @param name
     * @return
     */
    public JmxMessage operation(String name) {
        if (mbeanInvocation == null) {
            throw new CitrusRuntimeException("Invalid access to operation for JMX message");
        }

        ManagedBeanInvocation.Operation operation = new ManagedBeanInvocation.Operation();
        operation.setName(name);
        mbeanInvocation.setOperation(operation);
        return this;
    }

    /**
     * Adds operation parameter.
     * @param arg
     * @return
     */
    public JmxMessage parameter(Object arg) {
        return parameter(arg, arg.getClass());
    }

    /**
     * Adds operation parameter with custom parameter type.
     * @param arg
     * @param argType
     * @return
     */
    public JmxMessage parameter(Object arg, Class<?> argType) {
        if (mbeanInvocation == null) {
            throw new CitrusRuntimeException("Invalid access to operation parameter for JMX message");
        }

        if (mbeanInvocation.getOperation() == null) {
            throw new CitrusRuntimeException("Invalid access to operation parameter before operation was set for JMX message");
        }

        if (mbeanInvocation.getOperation().getParameter() == null) {
            mbeanInvocation.getOperation().setParameter(new ManagedBeanInvocation.Parameter());
        }

        OperationParam operationParam = new OperationParam();
        operationParam.setValueObject(arg);
        operationParam.setType(argType.getName());
        mbeanInvocation.getOperation().getParameter().getParameter().add(operationParam);
        return this;
    }

    public static JmxMessage result(Object value) {
        ManagedBeanResult mbeanResult = new ManagedBeanResult();
        ManagedBeanResult.Object mbeanResultObject = new ManagedBeanResult.Object();
        mbeanResultObject.setValueObject(value);
        mbeanResult.setObject(mbeanResultObject);

        return new JmxMessage(mbeanResult);
    }

    public static JmxMessage result() {
        return new JmxMessage(new ManagedBeanResult());
    }

    @Override
    public <T> T getPayload(Class<T> type) {
        if (String.class.equals(type)) {
            return (T) getPayload();
        } else {
            return super.getPayload(type);
        }
    }

    @Override
    public Object getPayload() {
        StringResult payloadResult = new StringResult();
        if (mbeanInvocation != null) {
            marshaller.marshal(mbeanInvocation, payloadResult);
            return payloadResult.toString();
        } else if (mbeanResult != null) {
            marshaller.marshal(mbeanResult, payloadResult);
            return payloadResult.toString();
        }

        return super.getPayload();
    }
}
