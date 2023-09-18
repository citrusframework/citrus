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

package org.citrusframework.jmx.server;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.NotCompliantMBeanException;
import javax.management.ReflectionException;
import javax.xml.transform.Source;
import java.util.List;
import java.util.Map;

import org.citrusframework.endpoint.EndpointAdapter;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.jmx.endpoint.JmxEndpointConfiguration;
import org.citrusframework.jmx.model.ManagedBeanDefinition;
import org.citrusframework.jmx.model.ManagedBeanInvocation;
import org.citrusframework.jmx.model.ManagedBeanResult;
import org.citrusframework.jmx.model.OperationParam;
import org.citrusframework.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Managed bean implementation based on standard mbean implementation. This managed bean delegates incoming requests for operation calls and
 * attribute access to endpoint adapter. The endpoint adapter is capable of returning a invocation result which is converted to a proper operation
 * result.
 *
 * This class supports managed bean operation invocation as well as read and write access to managed bean attributes.
 *
 * @author Christoph Deppisch
 * @since 2.5
 */
public class JmxEndpointMBean implements DynamicMBean {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(JmxEndpointMBean.class);

    /** Endpoint adapter delegate */
    private final EndpointAdapter endpointAdapter;

    /** Endpoint configuration */
    private final JmxEndpointConfiguration endpointConfiguration;

    /** Managed bean interface type */
    private final ManagedBeanDefinition mbean;

    /**
     * Constructor using the managed bean type.
     * @param mbean
     */
    public JmxEndpointMBean(ManagedBeanDefinition mbean, JmxEndpointConfiguration endpointConfiguration, EndpointAdapter endpointAdapter) throws NotCompliantMBeanException {
        this.mbean = mbean;
        this.endpointConfiguration = endpointConfiguration;
        this.endpointAdapter = endpointAdapter;
    }

    @Override
    public Object getAttribute(String name) throws AttributeNotFoundException, MBeanException, ReflectionException {
        ManagedBeanInvocation mbeanInvocation = new ManagedBeanInvocation();
        mbeanInvocation.setMbean(mbean.createObjectName().toString());
        ManagedBeanInvocation.Attribute attribute = new ManagedBeanInvocation.Attribute();
        attribute.setName(name);
        mbeanInvocation.setAttribute(attribute);

        return handleInvocation(mbeanInvocation);
    }

    @Override
    public void setAttribute(Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
        ManagedBeanInvocation mbeanInvocation = new ManagedBeanInvocation();
        mbeanInvocation.setMbean(mbean.createObjectName().toString());
        ManagedBeanInvocation.Attribute mbeanAttribute = new ManagedBeanInvocation.Attribute();
        mbeanAttribute.setName(attribute.getName());
        mbeanAttribute.setValueObject(attribute.getValue());
        mbeanInvocation.setAttribute(mbeanAttribute);

        handleInvocation(mbeanInvocation);
    }

    @Override
    public AttributeList getAttributes(String[] attributes) {
        AttributeList list = new AttributeList();
        try {
            for (String attribute : attributes) {
                list.add(new Attribute(attribute, getAttribute(attribute)));
            }
        } catch (AttributeNotFoundException | ReflectionException | MBeanException e) {
            throw new CitrusRuntimeException("Failed to get managed bean attribute", e);
        }

        return list;
    }

    @Override
    public AttributeList setAttributes(AttributeList attributes) {
        AttributeList list = new AttributeList();

        try {
            for (Object attribute : attributes) {
                setAttribute((Attribute) attribute);
                list.add(attribute);
            }
        } catch (AttributeNotFoundException | ReflectionException | MBeanException | InvalidAttributeValueException e) {
            throw new CitrusRuntimeException("Failed to get managed bean attribute", e);
        }

        return list;
    }

    @Override
    public Object invoke(String actionName, Object[] params, String[] signature) throws MBeanException, ReflectionException {
        if (logger.isDebugEnabled()) {
            logger.debug("Received message on JMX server: '" + endpointConfiguration.getServerUrl() + "'");
        }

        ManagedBeanInvocation mbeanInvocation = new ManagedBeanInvocation();
        mbeanInvocation.setMbean(mbean.createObjectName().toString());

        ManagedBeanInvocation.Operation operation = new ManagedBeanInvocation.Operation();
        operation.setName(actionName);

        if (params != null && params.length > 0) {
            operation.setParameter(new ManagedBeanInvocation.Parameter());

            for (Object arg : params) {
                OperationParam operationParam = new OperationParam();

                operationParam.setValueObject(arg);
                if (Map.class.isAssignableFrom(arg.getClass())) {
                    operationParam.setType(Map.class.getName());
                } else if (List.class.isAssignableFrom(arg.getClass())) {
                    operationParam.setType(List.class.getName());
                } else {
                    operationParam.setType(arg.getClass().getName());
                }

                operation.getParameter().getParameter().add(operationParam);
            }
        }
        mbeanInvocation.setOperation(operation);

        return handleInvocation(mbeanInvocation);
    }

    @Override
    public MBeanInfo getMBeanInfo() {
        return mbean.createMBeanInfo();
    }

    /**
     * Handle managed bean invocation by delegating to endpoint adapter. Response is converted to proper method return result.
     * @param mbeanInvocation
     * @return
     */
    private Object handleInvocation(ManagedBeanInvocation mbeanInvocation) {
        Message response = endpointAdapter.handleMessage(endpointConfiguration.getMessageConverter()
                .convertInbound(mbeanInvocation, endpointConfiguration, null));

        ManagedBeanResult serviceResult = null;
        if (response != null && response.getPayload() != null) {
            if (response.getPayload() instanceof ManagedBeanResult) {
                serviceResult = (ManagedBeanResult) response.getPayload();
            } else if (response.getPayload() instanceof String) {
                serviceResult = (ManagedBeanResult) endpointConfiguration.getMarshaller().unmarshal(response.getPayload(Source.class));
            }
        }

        if (serviceResult != null) {
            return serviceResult.getResultObject(endpointConfiguration.getReferenceResolver());
        } else {
            return null;
        }
    }
}
