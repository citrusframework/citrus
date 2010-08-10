/*
 * Copyright 2006-2010 the original author or authors.
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

package com.consol.citrus.ws.actions;

import java.io.IOException;
import java.text.ParseException;
import java.util.Map;

import org.springframework.core.io.Resource;
import org.springframework.integration.core.Message;
import org.springframework.integration.message.MessageBuilder;
import org.springframework.util.StringUtils;
import org.springframework.ws.soap.server.endpoint.SoapFaultDefinition;

import com.consol.citrus.actions.SendMessageAction;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.util.FileUtils;
import com.consol.citrus.ws.WebServiceEndpoint;
import com.consol.citrus.ws.message.CitrusSoapMessageHeaders;

/**
 * Message sender implementation sending SOAP responses with SOAP fault element.
 * 
 * The sender will only add a specific header entry to the message header (see {@link CitrusSoapMessageHeaders}).
 * The Citrus {@link WebServiceEndpoint} implementation will read this entry and generate the SOAP fault for us.
 * Fault detail is sent as normal message payload.
 *  
 * @author Christoph Deppisch
 */
public class SendSoapFaultAction extends SendMessageAction {

    /** SOAP fault detail as inline CDATA data */
    private String faultDetail;
    
    /** SOAP fault detail as external file resource */
    private Resource faultDetailResource;

    /** Fault code as QName string */
    private String faultCode;
    
    /** Fault reason string describing the fault */
    private String faultString;
    
    /** (non-Javadoc)
     * @see com.consol.citrus.actions.SendMessageAction#createMessage(com.consol.citrus.context.TestContext)
     */
    @Override
    protected Message<?> createMessage(TestContext context) {
        try {
            /* Set message header */
            Map<String, Object> headers = context.replaceVariablesInMap(getHeaderValues());
    
            if(!StringUtils.hasText(faultCode)) {
                throw new CitrusRuntimeException("Missing fault code definition for SOAP fault generation. Please specify a proper SOAP fault code!");
            }
            
            String soapFaultString = context.replaceDynamicContentInString(faultCode);
            
            if(StringUtils.hasText(faultString)) {
                soapFaultString += "," + context.replaceDynamicContentInString(faultString);
            }

            //put special SOAP fault QName string to message headers. Citrus SOAP ws endpoint will
            //take read the entry an generate the SOAP fauflt for us
            headers.put(CitrusSoapMessageHeaders.SOAP_FAULT, soapFaultString);

            String messagePayload = null;
            
            if (faultDetailResource != null) {
                messagePayload = context.replaceDynamicContentInString(FileUtils.readToString(faultDetailResource));
            } else if (faultDetail != null){
                messagePayload = context.replaceDynamicContentInString(faultDetail);
            } else {
                //no fault detail specified therefore just send empty message body
                messagePayload = "";
            }
            
            return MessageBuilder.withPayload(messagePayload).copyHeaders(headers).build();
        } catch (IOException e) {
            throw new CitrusRuntimeException(e);
        } catch (ParseException e) {
            throw new CitrusRuntimeException(e);
        }
    }

    /**
     * Set the fault detail coming from inline XML CDATA element
     * @param faultDetail the faultDetail to set
     */
    public void setFaultDetail(String faultDetail) {
        this.faultDetail = faultDetail;
    }

    /**
     * Set the fault detail from external file resource
     * @param faultDetailResource the faultDetailResource to set
     */
    public void setFaultDetailResource(Resource faultDetailResource) {
        this.faultDetailResource = faultDetailResource;
    }

    /**
     * Set the fault code QName string. This can be either
     * a fault code in {@link SoapFaultDefinition} or a custom QName like
     * {http://www.consol.de/citrus}citrus:TEC-1000
     * 
     * @param faultCode the faultCode to set
     */
    public void setFaultCode(String faultCode) {
        this.faultCode = faultCode;
    }

    /**
     * Set the fault reason string describing the fault.
     * @param faultString the faultString to set
     */
    public void setFaultString(String faultString) {
        this.faultString = faultString;
    }
}
