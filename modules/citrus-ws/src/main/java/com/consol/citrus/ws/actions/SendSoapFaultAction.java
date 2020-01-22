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

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.MessageHeaders;
import com.consol.citrus.util.FileUtils;
import com.consol.citrus.validation.builder.StaticMessageContentBuilder;
import com.consol.citrus.ws.message.SoapAttachment;
import com.consol.citrus.ws.message.SoapFault;
import com.consol.citrus.ws.message.SoapMessage;
import com.consol.citrus.ws.message.SoapMessageHeaders;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;

/**
 * @author Christoph Deppisch
 * @since 2.0
 */
public class SendSoapFaultAction extends SendSoapMessageAction {

    /** Fault code as QName string */
    private final String faultCode;

    /** Fault reason string describing the fault */
    private final String faultString;

    /** Optional fault actor */
    private final String faultActor;

    /** List of fault detail contents */
    private final List<String> faultDetails;

    /** List of fault detail resource paths */
    private final List<String> faultDetailResourcePaths;

    public SendSoapFaultAction(Builder builder) {
        super(builder);

        this.faultCode = builder.faultCode;
        this.faultString = builder.faultString;
        this.faultActor = builder.faultActor;
        this.faultDetails = builder.faultDetails;
        this.faultDetailResourcePaths = builder.faultDetailResourcePaths;
    }

    @Override
    protected SoapMessage createMessage(TestContext context, String messageType) {
        SoapMessage soapMessage = super.createMessage(context, messageType);

        SoapFault soapFault = new SoapFault();
        soapFault.setPayload(soapMessage.getPayload());

        if (!StringUtils.hasText(faultCode)) {
            throw new CitrusRuntimeException("Missing fault code definition for SOAP fault generation. Please specify a proper SOAP fault code!");
        }
        soapFault.faultCode(context.replaceDynamicContentInString(faultCode));

        for (Map.Entry<String, Object> header : soapMessage.getHeaders().entrySet()) {
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
                soapFault.addFaultDetail(context.replaceDynamicContentInString(FileUtils.readToString(FileUtils.getFileResource(resourcePath, context), FileUtils.getCharset(resourcePath))));
            }
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to create SOAP fault detail from file resource", e);
        }

        return soapFault;
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
     * Gets the fault detail resource paths.
     * @return
     */
    public List<String> getFaultDetailResourcePaths() {
        return faultDetailResourcePaths;
    }

    /**
     * Action builder.
     */
    public static final class Builder extends SendSoapMessageBuilder<SendSoapFaultAction, Builder> {

        private String faultCode;
        private String faultString;
        private String faultActor;
        private List<String> faultDetails = new ArrayList<>();
        private List<String> faultDetailResourcePaths = new ArrayList<>();

        public Builder() {
            messageBuilder(new StaticMessageContentBuilder(soapMessage));
        }

        /**
         * Adds custom SOAP fault code.
         * @param code
         * @return
         */
        public Builder faultCode(String code) {
            this.faultCode = code;
            return this;
        }

        /**
         * Add custom fault string to SOAP fault message.
         * @param faultString
         * @return
         */
        public Builder faultString(String faultString) {
            this.faultString = faultString;
            return this;
        }

        /**
         * Add custom fault string to SOAP fault message.
         * @param faultActor
         * @return
         */
        public Builder faultActor(String faultActor) {
            this.faultActor = faultActor;
            return this;
        }

        /**
         * Adds a fault detail to SOAP fault message.
         * @param faultDetail
         * @return
         */
        public Builder faultDetail(String faultDetail) {
            this.faultDetails.add(faultDetail);
            return this;
        }

        /**
         * Adds a fault detail from file resource.
         * @param resource
         * @return
         */
        public Builder faultDetailResource(Resource resource) {
            return faultDetailResource(resource, FileUtils.getDefaultCharset());
        }

        /**
         * Adds a fault detail from file resource.
         * @param resource
         * @param charset
         * @return
         */
        public Builder faultDetailResource(Resource resource, Charset charset) {
            try {
                this.faultDetails.add(FileUtils.readToString(resource, charset));
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
        public Builder faultDetailResource(String filePath) {
            this.faultDetailResourcePaths.add(filePath);
            return this;
        }

        /**
         * Sets the response status.
         * @param status
         * @return
         */
        public Builder status(HttpStatus status) {
            soapMessage.header(SoapMessageHeaders.HTTP_STATUS_CODE, status.value());
            return this;
        }

        /**
         * Sets the response status code.
         * @param statusCode
         * @return
         */
        public Builder statusCode(Integer statusCode) {
            soapMessage.header(SoapMessageHeaders.HTTP_STATUS_CODE, statusCode);
            return this;
        }

        @Override
        public SendSoapFaultAction build() {
            return new SendSoapFaultAction(this);
        }
    }
}
