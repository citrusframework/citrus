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

package com.consol.citrus.ws.message.builder;

import java.io.IOException;
import java.util.*;

import org.springframework.util.StringUtils;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.util.FileUtils;
import com.consol.citrus.validation.builder.PayloadTemplateMessageBuilder;
import com.consol.citrus.ws.message.CitrusSoapMessageHeaders;
import com.consol.citrus.ws.util.SoapFaultDefinitionHolder;

/**
 * Message builder implementation adding SOAP faults to the message.
 * @author Christoph Deppisch
 */
public class SoapFaultAwareMessageBuilder extends PayloadTemplateMessageBuilder {
    
    /** Fault code as QName string */
    private String faultCode;
    
    /** Fault reason string describing the fault */
    private String faultString;
    
    /** Optional fault actor */
    private String faultActor;
    
    /** List of fault detail elements */
    private List<String> faultDetails = new ArrayList<String>();
    
    @Override
    protected Map<String, Object> buildMessageHeaders(TestContext context) {
        Map<String, Object> headers = super.buildMessageHeaders(context);
        
        if (!StringUtils.hasText(faultCode)) {
            throw new CitrusRuntimeException("Missing fault code definition for SOAP fault generation. Please specify a proper SOAP fault code!");
        }
        
        SoapFaultDefinitionHolder soapFaultDefinitionHolder = new SoapFaultDefinitionHolder();
        soapFaultDefinitionHolder.setFaultCode(context.replaceDynamicContentInString(faultCode));
        
        if (StringUtils.hasText(faultString)) {
            soapFaultDefinitionHolder.setFaultStringOrReason(context.replaceDynamicContentInString(faultString));
        }

        if (StringUtils.hasText(faultActor)) {
            soapFaultDefinitionHolder.setFaultActor(context.replaceDynamicContentInString(faultActor));
        }
        
        headers.put(CitrusSoapMessageHeaders.SOAP_FAULT, soapFaultDefinitionHolder.toString());
        
        for (int i = 0; i < faultDetails.size(); i++) {
            String faultDetail = faultDetails.get(i);
            if (faultDetail.startsWith(CitrusSoapMessageHeaders.SOAP_FAULT_DETAIL_RESOURCE)) {
                String resourcePath = faultDetail.substring(CitrusSoapMessageHeaders.SOAP_FAULT_DETAIL_RESOURCE.length() + 1, faultDetail.length() - 1);
                try {
                    headers.put(CitrusSoapMessageHeaders.SOAP_FAULT_DETAIL + "_" + (i+1), context.replaceDynamicContentInString(
                            FileUtils.readToString(FileUtils.getFileResource(resourcePath, context))));
                } catch (IOException e) {
                    throw new CitrusRuntimeException("Failed to build soap fault detail from resource", e);
                }
            } else {
                headers.put(CitrusSoapMessageHeaders.SOAP_FAULT_DETAIL + "_" + (i+1), context.replaceDynamicContentInString(faultDetail));
            }
        }
        
        return headers;
    }
    
    /**
     * Set the fault code QName string. This can be either
     * a fault code in {@link org.springframework.ws.soap.server.endpoint.SoapFaultDefinition} 
     * or a custom QName like {http://www.consol.de/citrus}citrus:TEC-1000
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
    
    /**
     * Adds a fault detail element.
     * @param faultDetail
     */
    public void addFaultDetail(String faultDetail) {
        faultDetails.add(faultDetail);
    }
    
    /**
     * Delegates to payload resource setter.
     * @param faultDetailResource
     */
    public void addFaultDetailResource(String faultDetailResourcePath) {
        faultDetails.add(CitrusSoapMessageHeaders.SOAP_FAULT_DETAIL_RESOURCE + "(" +  faultDetailResourcePath + ")");
    }
    
    /**
     * Sets the faultActor.
     * @param faultActor the faultActor to set
     */
    public void setFaultActor(String faultActor) {
        this.faultActor = faultActor;
    }
    
    /**
     * Gets the faultActor.
     * @return the faultActor the faultActor to get.
     */
    public String getFaultActor() {
        return faultActor;
    }

    /**
     * Gets the faultCode.
     * @return the faultCode
     */
    public String getFaultCode() {
        return faultCode;
    }

    /**
     * Gets the faultString.
     * @return the faultString
     */
    public String getFaultString() {
        return faultString;
    }

    /**
     * Gets the faultDetails.
     * @return the faultDetails the faultDetails to get.
     */
    public List<String> getFaultDetails() {
        return faultDetails;
    }

    /**
     * Sets the faultDetails.
     * @param faultDetails the faultDetails to set
     */
    public void setFaultDetails(List<String> faultDetails) {
        this.faultDetails = faultDetails;
    }
}
