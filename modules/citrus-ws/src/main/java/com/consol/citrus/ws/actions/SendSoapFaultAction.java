/*
 * Copyright 2006-2014 the original author or authors.
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

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.MessageHeaders;
import com.consol.citrus.util.FileUtils;
import com.consol.citrus.ws.message.SoapAttachment;
import com.consol.citrus.ws.message.*;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.*;

/**
 * @author Christoph Deppisch
 * @since 2.0
 */
public class SendSoapFaultAction extends SendSoapMessageAction {

    /** Fault code as QName string */
    private String faultCode;

    /** Fault reason string describing the fault */
    private String faultString;

    /** Optional fault actor */
    private String faultActor;

    /** List of fault detail contents */
    private List<String> faultDetails = new ArrayList<String>();

    /** List of fault detail resource paths */
    private List<String> faultDetailResourcePaths = new ArrayList<String>();

    @Override
    protected SoapMessage createMessage(TestContext context, String messageType) {
        SoapMessage soapMessage = super.createMessage(context, messageType);

        SoapFault soapFault = new SoapFault();
        soapFault.setPayload(soapMessage.getPayload());

        if (!StringUtils.hasText(faultCode)) {
            throw new CitrusRuntimeException("Missing fault code definition for SOAP fault generation. Please specify a proper SOAP fault code!");
        }
        soapFault.faultCode(context.replaceDynamicContentInString(faultCode));

        for (Map.Entry<String, Object> header : soapMessage.copyHeaders().entrySet()) {
            if (!header.getKey().equals(MessageHeaders.ID)) {
                soapFault.setHeader(header.getKey(), header.getValue());
            }
        }

        for (String headerData : soapMessage.getHeaderData()) {
            soapFault.addHeaderData(headerData);
        }

        for (SoapAttachment soapAttachment : soapMessage.getAttachments()) {
            soapFault.addAttachment(soapAttachment);
        }

        if (StringUtils.hasText(faultActor)) {
            soapFault.faultActor(context.replaceDynamicContentInString(faultActor));
        }

        if (faultString != null) {
            soapFault.faultString(context.replaceDynamicContentInString(faultString));
        }

        for (String faultDetail : faultDetails) {
            soapFault.addFaultDetail(context.replaceDynamicContentInString(faultDetail));
        }

        try {
            for (String faultDetailPath : faultDetailResourcePaths) {
                String resourcePath = context.replaceDynamicContentInString(faultDetailPath);
                soapFault.addFaultDetail(context.replaceDynamicContentInString(FileUtils.readToString(FileUtils.getFileResource(resourcePath, context))));
            }
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to create SOAP fault detail from file resource", e);
        }

        return soapFault;
    }

    /**
     * Set the fault code QName string. This can be either
     * a fault code in {@link org.springframework.ws.soap.server.endpoint.SoapFaultDefinition}
     * or a custom QName like {http://www.consol.de/citrus}citrus:TEC-1000
     *
     * @param faultCode the faultCode to set
     */
    public SendSoapFaultAction setFaultCode(String faultCode) {
        this.faultCode = faultCode;
        return this;
    }

    /**
     * Set the fault reason string describing the fault.
     * @param faultString the faultString to set
     */
    public SendSoapFaultAction setFaultString(String faultString) {
        this.faultString = faultString;
        return this;
    }

    /**
     * Sets the faultActor.
     * @param faultActor the faultActor to set
     */
    public SendSoapFaultAction setFaultActor(String faultActor) {
        this.faultActor = faultActor;
        return this;
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
    public SendSoapFaultAction setFaultDetails(List<String> faultDetails) {
        this.faultDetails = faultDetails;
        return this;
    }

    /**
     * Gets the fault detail resource paths.
     * @return
     */
    public List<String> getFaultDetailResourcePaths() {
        return faultDetailResourcePaths;
    }

    /**
     * Sets the fault detail resource paths.
     * @param faultDetailResourcePaths
     */
    public SendSoapFaultAction setFaultDetailResourcePaths(List<String> faultDetailResourcePaths) {
        this.faultDetailResourcePaths = faultDetailResourcePaths;
        return this;
    }
}
