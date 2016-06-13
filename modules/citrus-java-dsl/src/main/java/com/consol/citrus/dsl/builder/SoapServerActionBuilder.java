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

package com.consol.citrus.dsl.builder;

import com.consol.citrus.TestAction;
import com.consol.citrus.dsl.actions.DelegatingTestAction;
import com.consol.citrus.endpoint.Endpoint;
import org.springframework.context.ApplicationContext;

/**
 * Action executes soap server operations such as receiving requests and sending response messsages.
 * 
 * @author Christoph Deppisch
 * @since 2.6
 */
public class SoapServerActionBuilder extends AbstractTestActionBuilder<DelegatingTestAction<TestAction>> {

    /** Spring application context */
    private ApplicationContext applicationContext;

    /** Target soap client instance */
    private final Endpoint soapServer;

    /**
     * Default constructor.
     */
    public SoapServerActionBuilder(DelegatingTestAction<TestAction> action, Endpoint soapServer) {
        super(action);
        this.soapServer = soapServer;
    }

    /**
     * Generic request builder for receiving SOAP messages on server.
     * @return
     */
    public SoapServerRequestActionBuilder receive() {
        SoapServerRequestActionBuilder soapServerRequestActionBuilder = new SoapServerRequestActionBuilder(action, soapServer)
                .withApplicationContext(applicationContext);
        return soapServerRequestActionBuilder;
    }

    /**
     * Generic response builder for sending SOAP response messages to client.
     * @return
     */
    public SoapServerResponseActionBuilder send() {
        SoapServerResponseActionBuilder soapServerResponseActionBuilder = new SoapServerResponseActionBuilder(action, soapServer)
                .withApplicationContext(applicationContext);
        return soapServerResponseActionBuilder;
    }

    /**
     * Generic response builder for sending SOAP fault messages to client.
     * @return
     */
    public SoapServerFaultResponseActionBuilder sendFault() {
        SoapServerFaultResponseActionBuilder soapServerResponseActionBuilder = new SoapServerFaultResponseActionBuilder(action, soapServer)
                .withApplicationContext(applicationContext);
        return soapServerResponseActionBuilder;
    }

    /**
     * Sets the Spring bean application context.
     * @param applicationContext
     */
    public SoapServerActionBuilder withApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        return this;
    }

}
