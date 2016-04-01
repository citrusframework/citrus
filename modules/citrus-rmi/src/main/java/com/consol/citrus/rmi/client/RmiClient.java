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

package com.consol.citrus.rmi.client;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.endpoint.AbstractEndpoint;
import com.consol.citrus.exceptions.ActionTimeoutException;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.DefaultMessage;
import com.consol.citrus.message.Message;
import com.consol.citrus.message.correlation.CorrelationManager;
import com.consol.citrus.message.correlation.PollingCorrelationManager;
import com.consol.citrus.messaging.*;
import com.consol.citrus.rmi.endpoint.RmiEndpointConfiguration;
import com.consol.citrus.rmi.message.RmiMessageHeaders;
import com.consol.citrus.rmi.model.RmiServiceInvocation;
import com.consol.citrus.rmi.model.RmiServiceResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.*;
import org.springframework.xml.transform.StringResult;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.*;
import java.rmi.registry.Registry;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class RmiClient extends AbstractEndpoint implements Producer, ReplyConsumer {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(RmiClient.class);

    /** Store of reply messages */
    private CorrelationManager<Message> correlationManager;

    /**
     * Default constructor initializing endpoint configuration.
     */
    public RmiClient() {
        this(new RmiEndpointConfiguration());
    }

    /**
     * Default constructor using endpoint configuration.
     * @param endpointConfiguration
     */
    public RmiClient(RmiEndpointConfiguration endpointConfiguration) {
        super(endpointConfiguration);
        this.correlationManager = new PollingCorrelationManager(endpointConfiguration, "Reply message did not arrive yet");
    }

    @Override
    public RmiEndpointConfiguration getEndpointConfiguration() {
        return (RmiEndpointConfiguration) super.getEndpointConfiguration();
    }

    @Override
    public void send(final Message message, TestContext context) {
        String correlationKeyName = getEndpointConfiguration().getCorrelator().getCorrelationKeyName(getName());
        String correlationKey = getEndpointConfiguration().getCorrelator().getCorrelationKey(message);
        correlationManager.saveCorrelationKey(correlationKeyName, correlationKey, context);

        String binding = message.getHeader(RmiMessageHeaders.RMI_BINDING) != null ? message.getHeader(RmiMessageHeaders.RMI_BINDING).toString() : getEndpointConfiguration().getBinding();
        try {
            RmiServiceInvocation invocation = getEndpointConfiguration().getMessageConverter().convertOutbound(message, getEndpointConfiguration(), context);
            Registry registry = getEndpointConfiguration().getRegistry();
            final Remote remoteTarget = registry.lookup(binding);

            final Method[] method = new Method[1];
            if (StringUtils.hasText(invocation.getMethod())) {
                method[0] = ReflectionUtils.findMethod(remoteTarget.getClass(), invocation.getMethod(), invocation.getArgTypes());
            } else {
                ReflectionUtils.doWithMethods(remoteTarget.getClass(), new ReflectionUtils.MethodCallback() {
                    @Override
                    public void doWith(Method declaredMethod) throws IllegalArgumentException, IllegalAccessException {
                        if (method[0] == null) {
                            method[0] = declaredMethod;
                        }
                    }
                }, new ReflectionUtils.MethodFilter() {
                    @Override
                    public boolean matches(Method declaredMethod) {
                        return CollectionUtils.arrayToList(declaredMethod.getExceptionTypes()).contains(RemoteException.class) &&
                                declaredMethod.getDeclaringClass().equals(remoteTarget.getClass());
                    }
                });
            }

            if (method[0] == null) {
                throw new CitrusRuntimeException("Unable to find proper method declaration on remote target object");
            }

            if (log.isDebugEnabled()) {
                log.debug("Sending message to RMI server: '" + binding + "'");
                log.debug("Message to send:\n" + message.getPayload(String.class));
            }
            context.onOutboundMessage(message);

            Object result = method[0].invoke(remoteTarget, invocation.getArgValues(context.getApplicationContext()));
            RmiServiceResult serviceResult = new RmiServiceResult();

            if (result != null) {
                RmiServiceResult.Object serviceResultObject = new RmiServiceResult.Object();
                serviceResultObject.setType(result.getClass().getName());
                serviceResultObject.setValueObject(result);
                serviceResult.setObject(serviceResultObject);
            }

            StringResult payload = new StringResult();
            getEndpointConfiguration().getMarshaller().marshal(serviceResult, payload);
            Message response = new DefaultMessage(payload.toString());
            correlationManager.store(correlationKey, response);

            log.info("Message was sent to RMI server: '" + binding + "'");
            if (result != null) {
                context.onInboundMessage(response);
            }
        } catch (RemoteException e) {
            throw new CitrusRuntimeException("Failed to connect to RMI server", e);
        } catch (NotBoundException e) {
            throw new CitrusRuntimeException("Failed to find service binding on RMI server", e);
        } catch (InvocationTargetException e) {
            throw new CitrusRuntimeException("Failed to invoke method on remote target", e);
        } catch (IllegalAccessException e) {
            throw new CitrusRuntimeException("Failed to invoke method on remote target, because remote method not accessible", e);
        }

        log.info("Message was sent to RMI server: '" + binding + "'");
    }

    @Override
    public Message receive(TestContext context) {
        return receive(correlationManager.getCorrelationKey(
                getEndpointConfiguration().getCorrelator().getCorrelationKeyName(getName()), context), context);
    }

    @Override
    public Message receive(String selector, TestContext context) {
        return receive(selector, context, getEndpointConfiguration().getTimeout());
    }

    @Override
    public Message receive(TestContext context, long timeout) {
        return receive(correlationManager.getCorrelationKey(
                getEndpointConfiguration().getCorrelator().getCorrelationKeyName(getName()), context), context, timeout);
    }

    @Override
    public Message receive(String selector, TestContext context, long timeout) {
        Message message = correlationManager.find(selector, timeout);

        if (message == null) {
            throw new ActionTimeoutException("Action timeout while receiving synchronous reply message from RMI server");
        }

        return message;
    }

    /**
     * Creates a message producer for this endpoint for sending messages
     * to this endpoint.
     */
    @Override
    public Producer createProducer() {
        return this;
    }

    /**
     * Creates a message consumer for this endpoint. Consumer receives
     * messages on this endpoint.
     *
     * @return
     */
    @Override
    public SelectiveConsumer createConsumer() {
        return this;
    }

    /**
     * Sets the correlation manager.
     * @param correlationManager
     */
    public void setCorrelationManager(CorrelationManager<Message> correlationManager) {
        this.correlationManager = correlationManager;
    }
}
