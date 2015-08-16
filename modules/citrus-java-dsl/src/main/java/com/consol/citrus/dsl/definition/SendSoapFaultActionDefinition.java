/*
 * Copyright 2006-2012 the original author or authors.
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

package com.consol.citrus.dsl.definition;

import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.util.FileUtils;
import com.consol.citrus.ws.actions.SendSoapFaultAction;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;

import java.io.IOException;

/**
 * Action definition creates a send message action with several message payload and header 
 * constructing build methods.
 * 
 * @author Christoph Deppisch
 * @deprecated since 2.3 in favor of using {@link com.consol.citrus.dsl.builder.SendSoapFaultBuilder}
 */
public class SendSoapFaultActionDefinition extends SendSoapMessageActionDefinition {

    /**
     * Default constructor with test action.
     * @param action
     */
    public SendSoapFaultActionDefinition(SendSoapFaultAction action) {
        super(action);
    }

    /**
     * Default constructor.
     */
    public SendSoapFaultActionDefinition() {
        super(new SendSoapFaultAction());
    }

    @Override
    public SendSoapFaultActionDefinition endpoint(Endpoint messageEndpoint) {
        return (SendSoapFaultActionDefinition) super.endpoint(messageEndpoint);
    }

    @Override
    public SendSoapFaultActionDefinition endpoint(String messageEndpointUri) {
        return (SendSoapFaultActionDefinition) super.endpoint(messageEndpointUri);
    }

    /**
     * Adds custom SOAP fault code definition.
     * @param code
     * @return
     */
    public SendSoapFaultActionDefinition faultCode(String code) {
        getAction().setFaultCode(code);
        return this;
    }

    /**
     * Add custom fault string to SOAP fault message.
     * @param faultString
     * @return
     */
    public SendSoapFaultActionDefinition faultString(String faultString) {
        getAction().setFaultString(faultString);
        return this;
    }

    /**
     * Add custom fault string to SOAP fault message.
     * @param faultActor
     * @return
     */
    public SendSoapFaultActionDefinition faultActor(String faultActor) {
        getAction().setFaultActor(faultActor);
        return this;
    }

    /**
     * Adds a fault detail to SOAP fault message.
     * @param faultDetail
     * @return
     */
    public SendSoapFaultActionDefinition faultDetail(String faultDetail) {
        getAction().getFaultDetails().add(faultDetail);
        return this;
    }

    /**
     * Adds a fault detail from file resource.
     * @param resource
     * @return
     */
    public SendSoapFaultActionDefinition faultDetailResource(Resource resource) {
        try {
            getAction().getFaultDetails().add(FileUtils.readToString(resource));
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
    public SendSoapFaultActionDefinition faultDetailResource(String filePath) {
        getAction().getFaultDetailResourcePaths().add(filePath);
        return this;
    }

    @Override
    public SendSoapFaultActionDefinition withApplicationContext(ApplicationContext applicationContext) {
        return (SendSoapFaultActionDefinition) super.withApplicationContext(applicationContext);
    }

    @Override
    public SendSoapFaultAction getAction() {
        return (SendSoapFaultAction) super.getAction();
    }
}
