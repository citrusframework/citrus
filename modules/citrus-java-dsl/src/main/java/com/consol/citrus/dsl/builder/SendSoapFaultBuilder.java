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

package com.consol.citrus.dsl.builder;

import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.util.FileUtils;
import com.consol.citrus.ws.actions.SendSoapFaultAction;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;

import java.io.IOException;

/**
 * Action builder creates a send message action with several message payload and header
 * constructing build methods.
 * 
 * @author Christoph Deppisch
 * @since 2.3
 */
public class SendSoapFaultBuilder extends SendSoapMessageBuilder {

    private SendSoapFaultAction action;

    /**
     * Default constructor with test action.
     * @param action
     */
    public SendSoapFaultBuilder(SendSoapFaultAction action) {
        super(action);
        this.action = action;
    }

    /**
     * Default constructor.
     */
    public SendSoapFaultBuilder() {
        this(new SendSoapFaultAction());
    }

    @Override
    public SendSoapFaultBuilder endpoint(Endpoint messageEndpoint) {
        return (SendSoapFaultBuilder) super.endpoint(messageEndpoint);
    }

    @Override
    public SendSoapFaultBuilder endpoint(String messageEndpointUri) {
        return (SendSoapFaultBuilder) super.endpoint(messageEndpointUri);
    }

    /**
     * Adds custom SOAP fault code.
     * @param code
     * @return
     */
    public SendSoapFaultBuilder faultCode(String code) {
        action.setFaultCode(code);
        return this;
    }
    
    /**
     * Add custom fault string to SOAP fault message.
     * @param faultString
     * @return
     */
    public SendSoapFaultBuilder faultString(String faultString) {
        action.setFaultString(faultString);
        return this;
    }
    
    /**
     * Add custom fault string to SOAP fault message.
     * @param faultActor
     * @return
     */
    public SendSoapFaultBuilder faultActor(String faultActor) {
        action.setFaultActor(faultActor);
        return this;
    }
    
    /**
     * Adds a fault detail to SOAP fault message.
     * @param faultDetail
     * @return
     */
    public SendSoapFaultBuilder faultDetail(String faultDetail) {
        action.getFaultDetails().add(faultDetail);
        return this;
    }
    
    /**
     * Adds a fault detail from file resource.
     * @param resource
     * @return
     */
    public SendSoapFaultBuilder faultDetailResource(Resource resource) {
        try {
            action.getFaultDetails().add(FileUtils.readToString(resource));
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to read fault detail resource", e);
        }
        return this;
    }

    /**
     * Adds a fault detail from file resource path.
     * @param filePath
     * @return
     */
    public SendSoapFaultBuilder faultDetailResource(String filePath) {
        action.getFaultDetailResourcePaths().add(filePath);
        return this;
    }

    @Override
    public SendSoapFaultBuilder withApplicationContext(ApplicationContext applicationContext) {
        return (SendSoapFaultBuilder) super.withApplicationContext(applicationContext);
    }

    @Override
    public SendSoapFaultAction build() {
        return (SendSoapFaultAction) super.build();
    }
}
