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

import java.io.IOException;

import org.springframework.core.io.Resource;

import com.consol.citrus.actions.SendMessageAction;
import com.consol.citrus.dsl.util.PositionHandle;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.util.FileUtils;
import com.consol.citrus.ws.message.builder.SoapFaultAwareMessageBuilder;

/**
 * Action definition creates a send message action with several message payload and header 
 * constructing build methods.
 * 
 * @author Christoph Deppisch
 */
public class SendSoapFaultActionDefinition extends SendMessageActionDefinition {

    private SoapFaultAwareMessageBuilder soapFaultMessageBuilder = new SoapFaultAwareMessageBuilder();
    
    /**
     * Default constructor with test action.
     * @param action
     * @param positionHandle
     */
    public SendSoapFaultActionDefinition(SendMessageAction action, PositionHandle positionHandle) {
        super(action, positionHandle);
        
        action.setMessageBuilder(soapFaultMessageBuilder);
    }
    
    /**
     * Adds custom SOAP fault code definition.
     * @param code
     * @return
     */
    public SendSoapFaultActionDefinition faultCode(String code) {
        soapFaultMessageBuilder.setFaultCode(code);
        
        return this;
    }
    
    /**
     * Add custom fault string to SOAP fault message.
     * @param faultString
     * @return
     */
    public SendSoapFaultActionDefinition faultString(String faultString) {
        soapFaultMessageBuilder.setFaultString(faultString);
        return this;
    }
    
    /**
     * Add custom fault string to SOAP fault message.
     * @param faultActor
     * @return
     */
    public SendSoapFaultActionDefinition faultActor(String faultActor) {
        soapFaultMessageBuilder.setFaultActor(faultActor);
        return this;
    }
    
    /**
     * Adds a fault detail to SOAP fault message.
     * @param faultDetail
     * @return
     */
    public SendSoapFaultActionDefinition faultDetail(String faultDetail) {
        soapFaultMessageBuilder.getFaultDetails().add(faultDetail);
        return this;
    }
    
    /**
     * Adds a fault detail from file resource.
     * @param resource
     * @return
     */
    public SendSoapFaultActionDefinition faultDetailResource(Resource resource) {
        try {
            soapFaultMessageBuilder.getFaultDetails().add(FileUtils.readToString(resource));
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to read fault detail resource", e);
        }
        return this;
    }
    
    /**
     * Adds message header name value pair.
     * @param name
     * @param value
     */
    public SendMessageActionDefinition header(String name, Object value) {
        soapFaultMessageBuilder.getMessageHeaders().put(name, value);
        return this;
    }
    
    /**
     * Adds message header data. Message header data is used in SOAP
     * messages for instance as header XML fragment.
     * @param data
     */
    public SendMessageActionDefinition header(String data) {
        soapFaultMessageBuilder.setMessageHeaderData(data);
        return this;
    }
    
    /**
     * Adds message header data as file resource. Message header data is used in SOAP
     * messages for instance as header XML fragment.
     * @param resource
     */
    public SendMessageActionDefinition header(Resource resource) {
        try {
            soapFaultMessageBuilder.setMessageHeaderData(FileUtils.readToString(resource));
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to read header resource", e);
        }
        
        return this;
    }
    
}
