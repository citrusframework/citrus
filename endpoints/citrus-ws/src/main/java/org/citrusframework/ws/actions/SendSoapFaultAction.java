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

package org.citrusframework.ws.actions;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.message.MessageHeaders;
import org.citrusframework.spi.Resource;
import org.citrusframework.util.FileUtils;
import org.citrusframework.util.StringUtils;
import org.citrusframework.validation.builder.StaticMessageBuilder;
import org.citrusframework.ws.message.SoapAttachment;
import org.citrusframework.ws.message.SoapFault;
import org.citrusframework.ws.message.SoapMessage;
import org.springframework.http.HttpStatus;

/**
 * @author Christoph Deppisch
 * @since 2.0
 */
public class SendSoapFaultAction extends SendSoapMessageAction {

    /**
     * Fault code as QName string
     */
    private final String faultCode;

    /**
     * Fault reason string describing the fault
     */
    private final String faultString;

    /**
     * Optional fault actor
     */
    private final String faultActor;

    /**
     * List of fault detail contents
     */
    private final List<String> faultDetails;

    /**
     * List of fault detail resource paths
     */
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
     *
     * @return the faultActor the faultActor to get.
     */
    public String getFaultActor() {
        return faultActor;
    }

    /**
     * Gets the faultCode.
     *
     * @return the faultCode
     */
    public String getFaultCode() {
        return faultCode;
    }

    /**
     * Gets the faultString.
     *
     * @return the faultString
     */
    public String getFaultString() {
        return faultString;
    }

    /**
     * Gets the faultDetails.
     *
     * @return the faultDetails the faultDetails to get.
     */
    public List<String> getFaultDetails() {
        return faultDetails;
    }

    /**
     * Gets the fault detail resource paths.
     *
     * @return
     */
    public List<String> getFaultDetailResourcePaths() {
        return faultDetailResourcePaths;
    }

    /**
     * Action builder.
     */
    public static final class Builder extends SendSoapMessageBuilder<SendSoapFaultAction, Builder.SoapFaultMessageBuilderSupport, Builder> {

        private String faultCode;
        private String faultString;
        private String faultActor;
        private final List<String> faultDetails = new ArrayList<>();
        private final List<String> faultDetailResourcePaths = new ArrayList<>();

        public Builder() {
            message(new StaticMessageBuilder(soapMessage));
        }

        @Override
        public SoapFaultMessageBuilderSupport getMessageBuilderSupport() {
            if (messageBuilderSupport == null) {
                messageBuilderSupport = new SoapFaultMessageBuilderSupport(soapMessage, this);
            }
            return super.getMessageBuilderSupport();
        }

        public static class SoapFaultMessageBuilderSupport extends SoapMessageBuilderSupport<SendSoapFaultAction, Builder, SoapFaultMessageBuilderSupport> {

            protected SoapFaultMessageBuilderSupport(SoapMessage soapMessage, Builder delegate) {
                super(soapMessage, delegate);
            }

            /**
             * Adds custom SOAP fault code.
             *
             * @param code
             * @return
             */
            public SoapFaultMessageBuilderSupport faultCode(String code) {
                delegate.faultCode = code;
                return this;
            }

            /**
             * Add custom fault string to SOAP fault message.
             *
             * @param faultString
             * @return
             */
            public SoapFaultMessageBuilderSupport faultString(String faultString) {
                delegate.faultString = faultString;
                return this;
            }

            /**
             * Add custom fault string to SOAP fault message.
             *
             * @param faultActor
             * @return
             */
            public SoapFaultMessageBuilderSupport faultActor(String faultActor) {
                delegate.faultActor = faultActor;
                return this;
            }

            /**
             * Adds a fault detail to SOAP fault message.
             *
             * @param faultDetail
             * @return
             */
            public SoapFaultMessageBuilderSupport faultDetail(String faultDetail) {
                delegate.faultDetails.add(faultDetail);
                return this;
            }

            /**
             * Adds a fault detail from file resource.
             *
             * @param resource
             * @return
             */
            public SoapFaultMessageBuilderSupport faultDetailResource(Resource resource) {
                return faultDetailResource(resource, FileUtils.getDefaultCharset());
            }

            /**
             * Adds a fault detail from file resource.
             *
             * @param resource
             * @param charset
             * @return
             */
            public SoapFaultMessageBuilderSupport faultDetailResource(Resource resource, Charset charset) {
                try {
                    delegate.faultDetails.add(FileUtils.readToString(resource, charset));
                } catch (IOException e) {
                    throw new CitrusRuntimeException("Failed to read fault detail resource", e);
                }
                return this;
            }

            /**
             * Adds a fault detail from file resource path.
             *
             * @param filePath
             * @return
             */
            public SoapFaultMessageBuilderSupport faultDetailResource(String filePath) {
                delegate.faultDetailResourcePaths.add(filePath);
                return this;
            }

            /**
             * Sets the response status.
             *
             * @param status
             * @return
             */
            public SoapFaultMessageBuilderSupport status(HttpStatus status) {
                soapMessage.status(status);
                return this;
            }

            /**
             * Sets the response status code.
             *
             * @param statusCode
             * @return
             */
            public SoapFaultMessageBuilderSupport statusCode(Integer statusCode) {
                soapMessage.statusCode(statusCode);
                return this;
            }

            /**
             * Sets the response status reason phrase.
             * @param reasonPhrase
             * @return
             */
            public SoapFaultMessageBuilderSupport reasonPhrase(String reasonPhrase) {
                soapMessage.reasonPhrase(reasonPhrase);
                return this;
            }
        }

        @Override
        public SendSoapFaultAction doBuild() {
            return new SendSoapFaultAction(this);
        }
    }
}
