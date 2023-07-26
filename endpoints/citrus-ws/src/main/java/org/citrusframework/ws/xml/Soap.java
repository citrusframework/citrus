/*
 * Copyright 2022 the original author or authors.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.ws.xml;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.TestActionContainerBuilder;
import org.citrusframework.actions.ReceiveMessageAction;
import org.citrusframework.actions.SendMessageAction;
import org.citrusframework.endpoint.resolver.EndpointUriResolver;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;
import org.citrusframework.ws.actions.AssertSoapFault;
import org.citrusframework.ws.actions.ReceiveSoapMessageAction;
import org.citrusframework.ws.actions.SendSoapFaultAction;
import org.citrusframework.ws.actions.SendSoapMessageAction;
import org.citrusframework.ws.actions.SoapActionBuilder;
import org.citrusframework.ws.actions.SoapClientActionBuilder;
import org.citrusframework.ws.actions.SoapServerActionBuilder;
import org.citrusframework.ws.message.SoapAttachment;
import org.citrusframework.ws.message.SoapMessageHeaders;
import org.citrusframework.xml.TestActions;
import org.citrusframework.xml.actions.Message;
import org.citrusframework.xml.actions.Receive;
import org.citrusframework.xml.actions.Send;

/**
 * @author Christoph Deppisch
 */
@XmlRootElement(name = "soap")
public class Soap implements TestActionBuilder<TestAction>, ReferenceResolverAware {

    private TestActionBuilder<?> builder;

    private Receive receive;
    private Send send;

    private String description;
    private String actor;

    private ReferenceResolver referenceResolver;

    @XmlElement
    public Soap setDescription(String value) {
        this.description = value;
        return this;
    }

    @XmlAttribute(name = "actor")
    public Soap setActor(String actor) {
        this.actor = actor;
        return this;
    }

    @XmlAttribute(name = "client")
    public Soap setSoapClient(String soapClient) {
        builder = new SoapActionBuilder().client(soapClient);
        return this;
    }

    @XmlAttribute(name = "server")
    public Soap setSoapServer(String soapServer) {
        builder = new SoapActionBuilder().server(soapServer);
        return this;
    }

    @XmlElement(name = "send-request")
    public Soap setSendRequest(ClientRequest request) {
        SendSoapMessageAction.Builder requestBuilder = asClientBuilder().send();
        requestBuilder.name("soap:send-request");
        requestBuilder.description(description);

        send = new Send(requestBuilder) {
            @Override
            protected SendMessageAction doBuild() {
                // do not build inside delegate. the actual build is called directly on the builder.
                return null;
            }
        };

        if (request.fork != null) {
            send.setFork(request.fork);
        }

        if (request.extract != null) {
            send.setExtract(request.extract);
        }

        if (request.uri != null) {
            requestBuilder.message().header(EndpointUriResolver.ENDPOINT_URI_HEADER_NAME, request.uri);
        }

        if (request.getMessage() != null) {
            send.setMessage(request.getMessage());

            if (request.getMessage().contentType != null) {
                requestBuilder.message().contentType(request.getMessage().contentType);
            }

            if (request.getMessage().accept != null) {
                requestBuilder.message().accept(request.getMessage().accept);
            }

            if (request.getMessage().soapAction != null) {
                requestBuilder.message().soapAction(request.getMessage().soapAction);
            }

            if (request.getMessage().mtomEnabled != null) {
                requestBuilder.message().mtomEnabled(request.getMessage().mtomEnabled);
            }

            for (SoapMessage.Attachment attachment : request.getMessage().getAttachments()) {
                SoapAttachment soapAttachment = new SoapAttachment();
                soapAttachment.setContentId(attachment.getContentId());
                soapAttachment.setContentType(attachment.getContentType());
                if (attachment.getResource() != null) {
                    soapAttachment.setContentResourcePath(attachment.getResource().getFile());
                }
                if (attachment.getCharset() != null) {
                    soapAttachment.setCharsetName(attachment.getCharset());
                }
                soapAttachment.setContent(attachment.getContent());
                requestBuilder.message().attachment(soapAttachment);
            }
        }

        builder = requestBuilder;
        return this;
    }

    @XmlElement(name = "receive-response")
    public Soap setReceiveResponse(ClientResponse response) {
        ReceiveSoapMessageAction.Builder responseBuilder = asClientBuilder().receive();

        responseBuilder.name("soap:receive-response");
        responseBuilder.description(description);

        receive = new Receive(responseBuilder) {
            @Override
            protected ReceiveMessageAction doBuild() {
                // do not build inside delegate. the actual build is called directly on the builder.
                return null;
            }
        };

        if (response.getMessage() != null) {
            if (response.getMessage().status != null) {
                responseBuilder.message().statusCode(Integer.parseInt(response.getMessage().status));
            }

            if (response.getMessage().reasonPhrase != null) {
                responseBuilder.message().reasonPhrase(response.getMessage().reasonPhrase);
            }

            if (response.getMessage().contentType != null) {
                responseBuilder.message().contentType(response.getMessage().contentType);
            }

            for (SoapMessage.Attachment attachment : response.getMessage().getAttachments()) {
                SoapAttachment soapAttachment = new SoapAttachment();
                soapAttachment.setContentId(attachment.getContentId());
                soapAttachment.setContentType(attachment.getContentType());
                if (attachment.getResource() != null) {
                    soapAttachment.setContentResourcePath(attachment.getResource().getFile());
                }
                if (attachment.getCharset() != null) {
                    soapAttachment.setCharsetName(attachment.getCharset());
                }
                soapAttachment.setContent(attachment.getContent());
                responseBuilder.message().attachment(soapAttachment);
            }

            receive.setMessage(response.getMessage());
        }

        if (response.attachmentValidator != null) {
            responseBuilder.message().attachmentValidatorName(response.attachmentValidator);
        }

        if (response.timeout != null) {
            receive.setTimeout(response.timeout);
        }

        receive.setSelect(response.select);
        receive.setValidator(response.validator);
        receive.setValidators(response.validators);
        receive.setHeaderValidator(response.headerValidator);
        receive.setHeaderValidators(response.headerValidators);

        if (response.selector != null) {
            receive.setSelector(response.selector);
        }

        receive.setSelect(response.select);

        response.getValidates().forEach(receive.getValidates()::add);

        if (response.extract != null) {
            receive.setExtract(response.extract);
        }

        builder = responseBuilder;
        return this;
    }

    @XmlElement(name = "receive-request")
    public Soap setReceiveRequest(ServerRequest request) {
        ReceiveSoapMessageAction.Builder requestBuilder = asServerBuilder().receive();

        requestBuilder.name("soap:receive-request");
        requestBuilder.description(description);

        receive = new Receive(requestBuilder) {
            @Override
            protected ReceiveMessageAction doBuild() {
                // do not build inside delegate. the actual build is called directly on the builder.
                return null;
            }
        };

        if (request.getMessage() != null) {
            receive.setMessage(request.getMessage());

            if (request.getMessage().contentType != null) {
                requestBuilder.message().contentType(request.getMessage().contentType);
            }

            if (request.getMessage().accept != null) {
                requestBuilder.message().accept(request.getMessage().accept);
            }

            if (request.getMessage().soapAction != null) {
                requestBuilder.message().soapAction(request.getMessage().soapAction);
            }

            for (SoapMessage.Attachment attachment : request.getMessage().getAttachments()) {
                SoapAttachment soapAttachment = new SoapAttachment();
                soapAttachment.setContentId(attachment.getContentId());
                soapAttachment.setContentType(attachment.getContentType());
                if (attachment.getResource() != null) {
                    soapAttachment.setContentResourcePath(attachment.getResource().getFile());
                }
                if (attachment.getCharset() != null) {
                    soapAttachment.setCharsetName(attachment.getCharset());
                }
                soapAttachment.setContent(attachment.getContent());
                requestBuilder.message().attachment(soapAttachment);
            }
        }

        if (request.attachmentValidator != null) {
            requestBuilder.message().attachmentValidatorName(request.attachmentValidator);
        }

        if (request.selector != null) {
            receive.setSelector(request.selector);
        }

        receive.setSelect(request.select);
        receive.setValidator(request.validator);
        receive.setValidators(request.validators);
        receive.setHeaderValidator(request.headerValidator);
        receive.setHeaderValidators(request.headerValidators);

        if (request.timeout != null) {
            receive.setTimeout(request.timeout);
        }

        request.getValidates().forEach(receive.getValidates()::add);

        if (request.extract != null) {
            receive.setExtract(request.extract);
        }

        builder = requestBuilder;
        return this;
    }

    @XmlElement(name = "send-response")
    public Soap setSendResponse(ServerResponse response) {
        SendSoapMessageAction.Builder responseBuilder = asServerBuilder().send();

        responseBuilder.name("soap:send-response");
        responseBuilder.description(description);

        send = new Send(responseBuilder) {
            @Override
            protected SendMessageAction doBuild() {
                // do not build inside delegate. the actual build is called directly on the builder.
                return null;
            }
        };

        if (response.getMessage() != null) {
            send.setMessage(response.getMessage());

            if (response.extract != null) {
                send.setExtract(response.extract);
            }

            if (response.getMessage().status != null) {
                responseBuilder.message().header(SoapMessageHeaders.HTTP_STATUS_CODE, response.getMessage().status);
            }

            if (response.getMessage().reasonPhrase != null) {
                responseBuilder.message().header(SoapMessageHeaders.HTTP_REASON_PHRASE, response.getMessage().reasonPhrase);
            }

            if (response.getMessage().contentType != null) {
                responseBuilder.message().contentType(response.getMessage().contentType);
            }

            for (SoapMessage.Attachment attachment : response.getMessage().getAttachments()) {
                SoapAttachment soapAttachment = new SoapAttachment();
                soapAttachment.setContentId(attachment.getContentId());
                soapAttachment.setContentType(attachment.getContentType());
                if (attachment.getResource() != null) {
                    soapAttachment.setContentResourcePath(attachment.getResource().getFile());
                }
                if (attachment.getCharset() != null) {
                    soapAttachment.setCharsetName(attachment.getCharset());
                }
                soapAttachment.setContent(attachment.getContent());
                responseBuilder.message().attachment(soapAttachment);
            }
        }

        builder = responseBuilder;
        return this;
    }

    @XmlElement(name = "send-fault")
    public void setSendFault(ServerFaultResponse response) {
        SendSoapFaultAction.Builder responseBuilder = asServerBuilder().sendFault();
        responseBuilder.name("soap:send-fault");
        responseBuilder.description(description);

        send = new Send(responseBuilder) {
            @Override
            protected SendMessageAction doBuild() {
                // do not build inside delegate. the actual build is called directly on the builder.
                return null;
            }
        };

        if (response.getMessage() != null) {
            send.setMessage(response.getMessage());

            if (response.extract != null) {
                send.setExtract(response.extract);
            }

            if (response.getMessage().status != null) {
                responseBuilder.message().statusCode(Integer.parseInt(response.getMessage().status));
            }

            if (response.getMessage().reasonPhrase != null) {
                responseBuilder.message().reasonPhrase(response.getMessage().reasonPhrase);
            }

            if (response.getMessage().faultCode != null) {
                responseBuilder.message().faultCode(response.getMessage().faultCode);
            }

            if (response.getMessage().faultString != null) {
                responseBuilder.message().faultString(response.getMessage().faultString);
            }

            if (response.getMessage().faultActor != null) {
                responseBuilder.message().faultActor(response.getMessage().faultActor);
            }

            for (SoapFault.SoapFaultDetail faultDetail : response.getMessage().getFaultDetails()) {
                if (faultDetail.content != null) {
                    responseBuilder.message().faultDetail(faultDetail.getContent().trim());
                }

                if (faultDetail.getResource() != null) {
                    responseBuilder.message().faultDetailResource(faultDetail.getResource().getFile());
                }
            }

            for (SoapMessage.Attachment attachment : response.getMessage().getAttachments()) {
                SoapAttachment soapAttachment = new SoapAttachment();
                soapAttachment.setContentId(attachment.getContentId());
                soapAttachment.setContentType(attachment.getContentType());
                if (attachment.getResource() != null) {
                    soapAttachment.setContentResourcePath(attachment.getResource().getFile());
                }
                if (attachment.getCharset() != null) {
                    soapAttachment.setCharsetName(attachment.getCharset());
                }
                soapAttachment.setContent(attachment.getContent());
                responseBuilder.message().attachment(soapAttachment);
            }
        }

        builder = responseBuilder;
    }

    @XmlElement(name = "assert-fault")
    public void setAssertFault(ClientAssertFault soapFault) {
        AssertSoapFault.Builder assertFault = asClientBuilder().assertFault();

        assertFault.name("soap:assert-fault");
        assertFault.description(description);

        if (soapFault.faultCode != null) {
            assertFault.faultCode(soapFault.faultCode);
        }

        if (soapFault.faultString != null) {
            assertFault.faultString(soapFault.faultString);
        }

        if (soapFault.faultActor != null) {
            assertFault.faultActor(soapFault.faultActor);
        }

        for (SoapFault.SoapFaultDetail faultDetail : soapFault.getFaultDetails()) {
            if (faultDetail.content != null) {
                assertFault.faultDetail(faultDetail.getContent().trim());
            }

            if (faultDetail.getResource() != null) {
                assertFault.faultDetailResource(faultDetail.getResource().getFile());
            }
        }

        if (soapFault.validator != null) {
            assertFault.validator(soapFault.validator);
        }

        if (soapFault.getActions() != null) {
            assertFault.actions(soapFault.getActions().getActionBuilders().toArray(TestActionBuilder<?>[]::new));
        }

        builder = assertFault;
    }

    @Override
    public TestAction build() {
        if (builder == null) {
            throw new CitrusRuntimeException("Missing client or server Soap action - please provide proper action details");
        }

        if (send != null) {
            send.setReferenceResolver(referenceResolver);
            send.setActor(actor);
            send.build();
        }

        if (receive != null) {
            receive.setReferenceResolver(referenceResolver);
            receive.setActor(actor);
            receive.build();
        }

        if (builder instanceof TestActionContainerBuilder<?,?>) {
            ((TestActionContainerBuilder<?,?>) builder).getActions().stream()
                    .filter(action -> action instanceof ReferenceResolverAware)
                    .forEach(action -> ((ReferenceResolverAware) action).setReferenceResolver(referenceResolver));
        }

        if (builder instanceof ReferenceResolverAware) {
            ((ReferenceResolverAware) builder).setReferenceResolver(referenceResolver);
        }

        return builder.build();
    }

    @Override
    public void setReferenceResolver(ReferenceResolver referenceResolver) {
        this.referenceResolver = referenceResolver;
    }

    /**
     * Converts current builder to client builder.
     * @return
     */
    private SoapClientActionBuilder asClientBuilder() {
        if (builder instanceof SoapClientActionBuilder) {
            return (SoapClientActionBuilder) builder;
        }

        throw new CitrusRuntimeException(String.format("Failed to convert '%s' to soap client action builder",
                Optional.ofNullable(builder).map(Object::getClass).map(Class::getName).orElse("null")));
    }

    /**
     * Converts current builder to client builder.
     * @return
     */
    private SoapServerActionBuilder asServerBuilder() {
        if (builder instanceof SoapServerActionBuilder) {
            return (SoapServerActionBuilder) builder;
        }

        throw new CitrusRuntimeException(String.format("Failed to convert '%s' to soap client action builder",
                Optional.ofNullable(builder).map(Object::getClass).map(Class::getName).orElse("null")));
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
            "message",
            "extract"
    })
    public static class ClientRequest {
        @XmlAttribute(name = "uri")
        protected String uri;
        @XmlAttribute(name = "fork")
        protected Boolean fork;

        @XmlElement(name = "message")
        protected SoapRequest message;

        @XmlElement
        protected Message.Extract extract;

        public SoapRequest getMessage() {
            return message;
        }

        public void setMessage(SoapRequest message) {
            this.message = message;
        }

        public String getUri() {
            return uri;
        }

        public void setUri(String uri) {
            this.uri = uri;
        }

        public Boolean getFork() {
            return fork;
        }

        public void setFork(Boolean fork) {
            this.fork = fork;
        }

        public Message.Extract getExtract() {
            return extract;
        }

        public void setExtract(Message.Extract extract) {
            this.extract = extract;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
            "selector",
            "message",
            "validates",
            "extract"
    })
    public static class ServerRequest {
        @XmlAttribute
        protected Integer timeout;

        @XmlAttribute
        protected String select;

        @XmlAttribute
        protected String validator;

        @XmlAttribute
        protected String validators;

        @XmlAttribute(name = "header-validator")
        protected String headerValidator;

        @XmlAttribute(name = "header-validators")
        protected String headerValidators;

        @XmlAttribute(name= "attachment-validator")
        protected String attachmentValidator;

        @XmlElement
        protected SoapRequest message;

        @XmlElement
        protected Receive.Selector selector;

        @XmlElement(name = "validate")
        protected List<Receive.Validate> validates;

        @XmlElement
        protected Message.Extract extract;

        public SoapRequest getMessage() {
            return message;
        }

        public void setMessage(SoapRequest message) {
            this.message = message;
        }

        public String getAttachmentValidator() {
            return attachmentValidator;
        }

        public void setAttachmentValidator(String attachmentValidator) {
            this.attachmentValidator = attachmentValidator;
        }

        public Integer getTimeout() {
            return timeout;
        }

        public void setTimeout(Integer timeout) {
            this.timeout = timeout;
        }

        public String getSelect() {
            return select;
        }

        public void setSelect(String select) {
            this.select = select;
        }

        public String getValidator() {
            return validator;
        }

        public void setValidator(String validator) {
            this.validator = validator;
        }

        public String getValidators() {
            return validators;
        }

        public void setValidators(String validators) {
            this.validators = validators;
        }

        public String getHeaderValidator() {
            return headerValidator;
        }

        public void setHeaderValidator(String headerValidator) {
            this.headerValidator = headerValidator;
        }

        public String getHeaderValidators() {
            return headerValidators;
        }

        public void setHeaderValidators(String headerValidators) {
            this.headerValidators = headerValidators;
        }

        public Receive.Selector getSelector() {
            return selector;
        }

        public void setSelector(Receive.Selector selector) {
            this.selector = selector;
        }

        public List<Receive.Validate> getValidates() {
            if (validates == null) {
                validates = new ArrayList<>();
            }

            return validates;
        }

        public Message.Extract getExtract() {
            return extract;
        }

        public void setExtract(Message.Extract extract) {
            this.extract = extract;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
            "message",
            "extract"
    })
    public static class ServerResponse {
        @XmlElement
        protected SoapResponse message;

        @XmlElement
        protected Message.Extract extract;

        public SoapResponse getMessage() {
            return message;
        }

        public void setMessage(SoapResponse message) {
            this.message = message;
        }

        public Message.Extract getExtract() {
            return extract;
        }

        public void setExtract(Message.Extract extract) {
            this.extract = extract;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
            "selector",
            "message",
            "validates",
            "extract"
    })
    public static class ClientResponse {
        @XmlAttribute
        protected Integer timeout;

        @XmlAttribute
        protected String select;

        @XmlAttribute
        protected String validator;

        @XmlAttribute
        protected String validators;

        @XmlAttribute(name = "header-validator")
        protected String headerValidator;

        @XmlAttribute(name = "header-validators")
        protected String headerValidators;

        @XmlElement
        protected Receive.Selector selector;

        @XmlElement
        protected SoapResponse message;

        @XmlAttribute(name = "attachment-validator")
        protected String attachmentValidator;

        @XmlElement(name = "validate")
        protected List<Receive.Validate> validates;

        @XmlElement
        protected Message.Extract extract;

        public SoapResponse getMessage() {
            return message;
        }

        public void setMessage(SoapResponse message) {
            this.message = message;
        }

        public String getAttachmentValidator() {
            return attachmentValidator;
        }

        public void setAttachmentValidator(String attachmentValidator) {
            this.attachmentValidator = attachmentValidator;
        }

        public Integer getTimeout() {
            return timeout;
        }

        public void setTimeout(Integer timeout) {
            this.timeout = timeout;
        }

        public String getSelect() {
            return select;
        }

        public void setSelect(String select) {
            this.select = select;
        }

        public Receive.Selector getSelector() {
            return selector;
        }

        public void setSelector(Receive.Selector selector) {
            this.selector = selector;
        }

        public String getValidator() {
            return validator;
        }

        public void setValidator(String validator) {
            this.validator = validator;
        }

        public String getValidators() {
            return validators;
        }

        public void setValidators(String validators) {
            this.validators = validators;
        }

        public String getHeaderValidator() {
            return headerValidator;
        }

        public void setHeaderValidator(String headerValidator) {
            this.headerValidator = headerValidator;
        }

        public String getHeaderValidators() {
            return headerValidators;
        }

        public void setHeaderValidators(String headerValidators) {
            this.headerValidators = headerValidators;
        }

        public List<Receive.Validate> getValidates() {
            if (validates == null) {
                validates = new ArrayList<>();
            }

            return validates;
        }

        public void setValidates(List<Receive.Validate> validates) {
            this.validates = validates;
        }

        public Message.Extract getExtract() {
            return extract;
        }

        public void setExtract(Message.Extract extract) {
            this.extract = extract;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {})
    public static class ClientAssertFault extends SoapFault {
        @XmlAttribute
        protected String validator;

        @XmlElement(name = "when")
        protected TestActions actions;

        public String getValidator() {
            return validator;
        }

        public void setValidator(String validator) {
            this.validator = validator;
        }

        public void setWhen(TestActions actions) {
            this.actions = actions;
        }

        public void setActions(TestActions actions) {
            this.actions = actions;
        }

        public TestActions getActions() {
            return actions;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
            "message",
            "extract"
    })
    public static class ServerFaultResponse {
        @XmlElement
        protected Message.Extract extract;

        @XmlElement
        protected SoapFault message;

        public SoapFault getMessage() {
            return message;
        }

        public void setMessage(SoapFault message) {
            this.message = message;
        }

        public Message.Extract getExtract() {
            return extract;
        }

        public void setExtract(Message.Extract extract) {
            this.extract = extract;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
            "attachments",
    })
    public static class SoapMessage extends Message {

        @XmlElement(name = "attachment")
        private List<SoapMessage.Attachment> attachments;

        public List<SoapMessage.Attachment> getAttachments() {
            if (attachments == null) {
                attachments = new ArrayList<>();
            }

            return attachments;
        }

        public void setAttachments(List<SoapMessage.Attachment> attachments) {
            this.attachments = attachments;
        }

        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
                "content",
                "resource"
        })
        public static class Attachment {
            @XmlAttribute(name = "content-id")
            private String contentId;
            @XmlAttribute(name = "content-type")
            private String contentType;
            @XmlAttribute(name = "charset")
            private String charset;
            @XmlElement
            private String content;
            @XmlElement
            private Resource resource;

            public String getContentId() {
                return contentId;
            }

            public void setContentId(String contentId) {
                this.contentId = contentId;
            }

            public String getContentType() {
                return contentType;
            }

            public void setContentType(String contentType) {
                this.contentType = contentType;
            }

            public String getContent() {
                return content;
            }

            public void setContent(String content) {
                this.content = content;
            }

            public String getCharset() {
                return charset;
            }

            public void setCharset(String charset) {
                this.charset = charset;
            }

            public Resource getResource() {
                return resource;
            }

            public void setResource(Resource resource) {
                this.resource = resource;
            }

            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class Resource {
                @XmlAttribute(name = "file")
                protected String file;

                public String getFile() {
                    return file;
                }

                public void setFile(String file) {
                    this.file = file;
                }
            }
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class SoapRequest extends SoapMessage {
        @XmlAttribute
        protected String path;
        @XmlAttribute(name = "content-type")
        protected String contentType;
        @XmlAttribute(name = "accept")
        protected String accept;
        @XmlAttribute(name = "version")
        protected String version;

        @XmlAttribute(name = "soap-action")
        protected String soapAction;

        @XmlAttribute(name = "mtom-enabled")
        protected Boolean mtomEnabled;

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getContentType() {
            return contentType;
        }

        public void setContentType(String contentType) {
            this.contentType = contentType;
        }

        public String getAccept() {
            return accept;
        }

        public void setAccept(String accept) {
            this.accept = accept;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getSoapAction() {
            return soapAction;
        }

        public void setSoapAction(String soapAction) {
            this.soapAction = soapAction;
        }

        public Boolean getMtomEnabled() {
            return mtomEnabled;
        }

        public void setMtomEnabled(Boolean mtomEnabled) {
            this.mtomEnabled = mtomEnabled;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class SoapResponse extends SoapMessage {
        @XmlAttribute(name = "status")
        protected String status;
        @XmlAttribute(name = "reason-phrase")
        protected String reasonPhrase;
        @XmlAttribute(name = "version")
        protected String version;
        @XmlAttribute(name = "content-type")
        protected String contentType;

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getReasonPhrase() {
            return reasonPhrase;
        }

        public void setReasonPhrase(String reasonPhrase) {
            this.reasonPhrase = reasonPhrase;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getContentType() {
            return contentType;
        }

        public void setContentType(String contentType) {
            this.contentType = contentType;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
            "faultDetails"
    })
    public static class SoapFault extends SoapResponse {
        @XmlAttribute(name = "fault-code")
        protected String faultCode;
        @XmlAttribute(name = "fault-string")
        protected String faultString;
        @XmlAttribute(name = "fault-actor")
        protected String faultActor;

        @XmlElement(name = "fault-detail")
        protected List<SoapFault.SoapFaultDetail> faultDetails;

        public void setFaultCode(String faultCode) {
            this.faultCode = faultCode;
        }

        public String getFaultCode() {
            return faultCode;
        }

        public void setFaultString(String faultString) {
            this.faultString = faultString;
        }

        public String getFaultString() {
            return faultString;
        }

        public void setFaultActor(String faultActor) {
            this.faultActor = faultActor;
        }

        public String getFaultActor() {
            return faultActor;
        }

        public void setFaultDetails(List<SoapFault.SoapFaultDetail> faultDetails) {
            this.faultDetails = faultDetails;
        }

        public List<SoapFault.SoapFaultDetail> getFaultDetails() {
            if (faultDetails == null) {
                faultDetails = new ArrayList<>();
            }

            return faultDetails;
        }

        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
                "content",
                "resource"
        })
        public static class SoapFaultDetail {
            @XmlElement
            protected String content;
            @XmlElement
            protected Resource resource;

            public void setContent(String content) {
                this.content = content;
            }

            public String getContent() {
                return content;
            }

            public void setResource(Resource resource) {
                this.resource = resource;
            }

            public Resource getResource() {
                return resource;
            }

            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class Resource {
                @XmlAttribute(name = "file", required = true)
                protected String file;

                public String getFile() {
                    return file;
                }

                public void setFile(String file) {
                    this.file = file;
                }
            }
        }
    }

}
