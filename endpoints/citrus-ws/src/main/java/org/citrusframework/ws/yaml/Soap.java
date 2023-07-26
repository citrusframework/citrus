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

package org.citrusframework.ws.yaml;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
import org.citrusframework.yaml.TestActions;
import org.citrusframework.yaml.actions.Message;
import org.citrusframework.yaml.actions.Receive;
import org.citrusframework.yaml.actions.Send;

/**
 * @author Christoph Deppisch
 */
public class Soap implements TestActionBuilder<TestAction>, ReferenceResolverAware {

    private TestActionBuilder<?> builder;

    private Receive receive;
    private Send send;

    private String description;
    private String actor;

    private ReferenceResolver referenceResolver;

    public void setDescription(String value) {
        this.description = value;
    }

    public void setActor(String actor) {
        this.actor = actor;
    }

    public void setClient(String soapClient) {
        builder = new SoapActionBuilder().client(soapClient);
    }

    public void setServer(String soapServer) {
        builder = new SoapActionBuilder().server(soapServer);
    }

    public void setSendRequest(ClientRequest request) {
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
                soapAttachment.setContentResourcePath(attachment.getResource());
                if (attachment.getCharset() != null) {
                    soapAttachment.setCharsetName(attachment.getCharset());
                }
                soapAttachment.setContent(attachment.getContent());
                requestBuilder.message().attachment(soapAttachment);
            }
        }

        builder = requestBuilder;
    }

    public void setReceiveResponse(ClientResponse response) {
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
                soapAttachment.setContentResourcePath(attachment.getResource());
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

        response.getValidate().forEach(receive.getValidate()::add);

        if (response.extract != null) {
            receive.setExtract(response.extract);
        }

        builder = responseBuilder;
    }

    public void setReceiveRequest(ServerRequest request) {
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
                soapAttachment.setContentResourcePath(attachment.getResource());
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

        request.getValidates().forEach(receive.getValidate()::add);

        if (request.extract != null) {
            receive.setExtract(request.extract);
        }

        builder = requestBuilder;
    }

    public void setSendResponse(ServerResponse response) {
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
                soapAttachment.setContentResourcePath(attachment.getResource());
                if (attachment.getCharset() != null) {
                    soapAttachment.setCharsetName(attachment.getCharset());
                }
                soapAttachment.setContent(attachment.getContent());
                responseBuilder.message().attachment(soapAttachment);
            }
        }

        builder = responseBuilder;
    }

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
                    responseBuilder.message().faultDetail(faultDetail.content);
                }

                if (faultDetail.resource != null) {
                    responseBuilder.message().faultDetailResource(faultDetail.resource);
                }
            }

            for (SoapMessage.Attachment attachment : response.getMessage().getAttachments()) {
                SoapAttachment soapAttachment = new SoapAttachment();
                soapAttachment.setContentId(attachment.getContentId());
                soapAttachment.setContentType(attachment.getContentType());
                soapAttachment.setContentResourcePath(attachment.getResource());
                if (attachment.getCharset() != null) {
                    soapAttachment.setCharsetName(attachment.getCharset());
                }
                soapAttachment.setContent(attachment.getContent());
                responseBuilder.message().attachment(soapAttachment);
            }
        }

        builder = responseBuilder;
    }

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
                assertFault.faultDetail(faultDetail.content);
            }

            if (faultDetail.resource != null) {
                assertFault.faultDetailResource(faultDetail.resource);
            }
        }

        if (soapFault.validator != null) {
            assertFault.validator(soapFault.validator);
        }

        if (!soapFault.getActions().isEmpty()) {
            assertFault.actions(soapFault.getActions().toArray(TestActionBuilder<?>[]::new));
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

    public static class ClientRequest {
        protected String uri;
        protected Boolean fork;

        protected SoapRequest message;

        protected Message.Extract extract;

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

        public SoapRequest getMessage() {
            return message;
        }

        public void setMessage(SoapRequest message) {
            this.message = message;
        }

        public Message.Extract getExtract() {
            return extract;
        }

        public void setExtract(Message.Extract extract) {
            this.extract = extract;
        }
    }

    public static class ServerRequest {
        protected Integer timeout;
        protected String select;
        protected String validator;
        protected String validators;
        protected String headerValidator;
        protected String headerValidators;

        protected Receive.Selector selector;

        protected String attachmentValidator;
        protected SoapRequest message;

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

        protected List<Receive.Validate> validates;

        protected Message.Extract extract;

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

    public static class ServerResponse {
        protected Message.Extract extract;

        protected SoapResponse message;

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

    public static class ClientResponse {
        protected Integer timeout;
        protected String select;
        protected String validator;
        protected String validators;

        protected String attachmentValidator;
        protected SoapResponse message;

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

        protected String headerValidator;
        protected String headerValidators;

        protected Receive.Selector selector;

        protected List<Receive.Validate> validate;

        protected Message.Extract extract;

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

        public List<Receive.Validate> getValidate() {
            if (validate == null) {
                validate = new ArrayList<>();
            }

            return validate;
        }

        public void setValidate(List<Receive.Validate> validate) {
            this.validate = validate;
        }

        public Message.Extract getExtract() {
            return extract;
        }

        public void setExtract(Message.Extract extract) {
            this.extract = extract;
        }
    }

    public static class ClientAssertFault extends SoapFault {
        protected String validator;

        protected List<TestActionBuilder<?>> actions;

        public String getValidator() {
            return validator;
        }

        public void setValidator(String validator) {
            this.validator = validator;
        }

        public void setWhen(List<TestActions> actions) {
            getActions().addAll(actions.stream().map(TestActions::get).toList());
        }

        public void setActions(List<TestActions> actions) {
            getActions().addAll(actions.stream().map(TestActions::get).toList());
        }

        public List<TestActionBuilder<?>> getActions() {
            if (actions == null) {
                actions = new ArrayList<>();
            }

            return actions;
        }
    }

    public static class ServerFaultResponse {
        protected Message.Extract extract;

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

    public static class SoapMessage extends Message {

        private List<Attachment> attachments;

        public List<Attachment> getAttachments() {
            if (attachments == null) {
                attachments = new ArrayList<>();
            }

            return attachments;
        }

        public void setAttachments(List<Attachment> attachments) {
            this.attachments = attachments;
        }

        public static class Attachment {
            private String contentId;
            private String contentType;
            private String content;
            private String resource;
            private String charset;

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

            public String getResource() {
                return resource;
            }

            public void setResource(String resource) {
                this.resource = resource;
            }
        }
    }

    public static class SoapRequest extends SoapMessage {
        protected String path;
        protected String contentType;
        protected String accept;
        protected String version;

        protected String soapAction;
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

    public static class SoapResponse extends SoapMessage {
        protected String status;
        protected String reasonPhrase;
        protected String version;
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

    public static class SoapFault extends SoapResponse {
        protected String faultCode;
        protected String faultString;
        protected String faultActor;

        protected List<SoapFaultDetail> faultDetails;

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

        public void setFaultDetails(List<SoapFaultDetail> faultDetails) {
            this.faultDetails = faultDetails;
        }

        public List<SoapFaultDetail> getFaultDetails() {
            if (faultDetails == null) {
                faultDetails = new ArrayList<>();
            }

            return faultDetails;
        }

        public static class SoapFaultDetail {
            protected String content;
            protected String resource;

            public void setContent(String content) {
                this.content = content;
            }

            public String getContent() {
                return content;
            }

            public void setResource(String resource) {
                this.resource = resource;
            }

            public String getResource() {
                return resource;
            }
        }
    }
}
