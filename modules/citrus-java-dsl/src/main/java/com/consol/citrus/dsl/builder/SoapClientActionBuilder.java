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
 * Action executes soap client operations such as sending requests and receiving responses.
 * 
 * @author Christoph Deppisch
 * @since 2.6
 */
public class SoapClientActionBuilder extends AbstractTestActionBuilder<DelegatingTestAction<TestAction>> {

    /** Spring application context */
    private ApplicationContext applicationContext;

    /** Target soap client instance */
    private Endpoint soapClient;
    private String soapClientUri;

    /**
     * Default constructor.
     */
    public SoapClientActionBuilder(DelegatingTestAction<TestAction> action, Endpoint soapClient) {
        super(action);
        this.soapClient = soapClient;
    }

    /**
     * Default constructor.
     */
    public SoapClientActionBuilder(DelegatingTestAction<TestAction> action, String soapClientUri) {
        super(action);
        this.soapClientUri = soapClientUri;
    }

    /**
     * Generic response builder for expecting response messages on client.
     * @return
     */
    public SoapClientResponseActionBuilder receive() {
        SoapClientResponseActionBuilder soapClientResponseActionBuilder;
        if (soapClient != null) {
            soapClientResponseActionBuilder = new SoapClientResponseActionBuilder(action, soapClient);
        } else {
            soapClientResponseActionBuilder = new SoapClientResponseActionBuilder(action, soapClientUri);
        }

        soapClientResponseActionBuilder.withApplicationContext(applicationContext);

        return soapClientResponseActionBuilder;
    }

    /**
     * Generic request builder with request method and path.
     * @return
     */
    public SoapClientRequestActionBuilder send() {
        SoapClientRequestActionBuilder soapClientRequestActionBuilder;
        if (soapClient != null) {
            soapClientRequestActionBuilder = new SoapClientRequestActionBuilder(action, soapClient);
        } else {
            soapClientRequestActionBuilder = new SoapClientRequestActionBuilder(action, soapClientUri);
        }

        soapClientRequestActionBuilder.withApplicationContext(applicationContext);

        return soapClientRequestActionBuilder;
    }

    /**
     * Sets the Spring bean application context.
     * @param applicationContext
     */
    public SoapClientActionBuilder withApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        return this;
    }

}
