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

package org.citrusframework.rmi.message;

import java.rmi.Remote;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.rmi.model.MethodArg;
import org.citrusframework.rmi.model.RmiMarshaller;
import org.citrusframework.rmi.model.RmiServiceInvocation;
import org.citrusframework.rmi.model.RmiServiceResult;
import org.citrusframework.xml.StringResult;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class RmiMessage extends DefaultMessage {

    /** Model objects */
    private RmiServiceInvocation serviceInvocation;
    private RmiServiceResult serviceResult;

    private RmiMarshaller marshaller = new RmiMarshaller();

    /**
     * Prevent traditional instantiation.
     */
    private RmiMessage() { super(); }

    /**
     * Constructor initializes new service invocation message.
     * @param serviceInvocation
     */
    private RmiMessage(RmiServiceInvocation serviceInvocation) {
        super(serviceInvocation);
        this.serviceInvocation = serviceInvocation;
    }

    /**
     * Constructor initializes new service result message.
     * @param serviceResult
     */
    private RmiMessage(RmiServiceResult serviceResult) {
        super(serviceResult);
        this.serviceResult = serviceResult;
    }

    public static RmiMessage invocation(String method) {
        RmiServiceInvocation invocation = new RmiServiceInvocation();
        invocation.setMethod(method);

        return new RmiMessage(invocation);
    }

    public static RmiMessage invocation(Class<? extends Remote> remoteTarget, String method) {
        RmiServiceInvocation invocation = new RmiServiceInvocation();
        invocation.setRemote(remoteTarget.getName());
        invocation.setMethod(method);

        return new RmiMessage(invocation);
    }

    public static RmiMessage result(Object resultObject) {
        RmiServiceResult serviceResult = new RmiServiceResult();
        RmiServiceResult.Object serviceResultObject = new RmiServiceResult.Object();
        serviceResultObject.setValueObject(resultObject);
        serviceResult.setObject(serviceResultObject);

        return new RmiMessage(serviceResult);
    }

    public static RmiMessage result() {
        return new RmiMessage(new RmiServiceResult());
    }

    public RmiMessage argument(Object arg) {
        return argument(arg, arg.getClass());
    }

    public RmiMessage argument(Object arg, Class<?> argType) {
        if (serviceInvocation == null) {
            throw new CitrusRuntimeException("Invalid access to method argument for RMI message");
        }

        if (serviceInvocation.getArgs() == null) {
            serviceInvocation.setArgs(new RmiServiceInvocation.Args());
        }

        MethodArg methodArg = new MethodArg();
        methodArg.setValueObject(arg);
        methodArg.setType(argType.getName());
        serviceInvocation.getArgs().getArgs().add(methodArg);
        return this;
    }

    public RmiMessage exception(String message) {
        if (serviceResult == null) {
            throw new CitrusRuntimeException("Invalid access to result exception for RMI message");
        }

        serviceResult.setException(message);

        return this;
    }

    @Override
    public <T> T getPayload(Class<T> type) {
        if (RmiServiceInvocation.class.equals(type) && serviceInvocation != null) {
            return (T) serviceInvocation;
        } else if (RmiServiceResult.class.equals(type) && serviceResult != null) {
            return (T) serviceResult;
        } else if (String.class.equals(type)) {
            return (T) getPayload();
        } else {
            return super.getPayload(type);
        }
    }

    @Override
    public Object getPayload() {
        StringResult payloadResult = new StringResult();
        if (serviceInvocation != null) {
            marshaller.marshal(serviceInvocation, payloadResult);
            return payloadResult.toString();
        } else if (serviceResult != null) {
            marshaller.marshal(serviceResult, payloadResult);
            return payloadResult.toString();
        }

        return super.getPayload();
    }
}
