/*
 * Copyright 2006-2010 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 * Citrus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Citrus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Citrus. If not, see <http://www.gnu.org/licenses/>.
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
            String messagePayload = null;
            
            if (faultDetailResource != null) {
                messagePayload = context.replaceDynamicContentInString(FileUtils.readToString(faultDetailResource));
            } else if (faultDetail != null){
                messagePayload = context.replaceDynamicContentInString(faultDetail);
            } else {
                //no fault detail specified therefore just send empty message body
                messagePayload = "";
            }
    
            /* Set message header */
            Map<String, Object> headers = context.replaceVariablesInMap(getHeaderValues());
    
            String soapFaultString = faultCode;
            
            if(StringUtils.hasText(faultString)) {
                soapFaultString += "," + faultString;
            }

            //put special SOAP fault QName string to message headers. Citrus SOAP ws endpoint will
            //take read the entry an generate the SOAP fauflt for us
            headers.put(CitrusSoapMessageHeaders.SOAP_FAULT, soapFaultString);
            
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
