/*
 * Copyright 2006-2018 the original author or authors.
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

package org.citrusframework.generate.javadsl;

import java.util.List;
import java.util.Optional;

import org.citrusframework.actions.ReceiveMessageAction;
import org.citrusframework.actions.SendMessageAction;
import org.citrusframework.generate.provider.CodeProvider;
import org.citrusframework.generate.provider.ReceiveCodeProvider;
import org.citrusframework.generate.provider.SendCodeProvider;
import org.citrusframework.generate.provider.http.ReceiveHttpRequestCodeProvider;
import org.citrusframework.generate.provider.http.ReceiveHttpResponseCodeProvider;
import org.citrusframework.generate.provider.http.SendHttpRequestCodeProvider;
import org.citrusframework.generate.provider.http.SendHttpResponseCodeProvider;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.message.Message;
import org.citrusframework.ws.message.SoapMessage;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

/**
 * @author Christoph Deppisch
 * @since 2.7.4
 */
public class MessagingJavaTestGenerator<T extends MessagingJavaTestGenerator<T>> extends JavaDslTestGenerator<T> {

    /** Endpoint name to use */
    private String endpoint;

    /** Sample request */
    private Message request;

    /** Sample response */
    private Message response;

    @Override
    protected JavaFile.Builder createJavaFileBuilder(TypeSpec.Builder testTypeBuilder) {
        return super.createJavaFileBuilder(testTypeBuilder)
                .addStaticImport(SendMessageAction.Builder.class, "send")
                .addStaticImport(ReceiveMessageAction.Builder.class, "receive");
    }

    @Override
    protected List<CodeBlock> getActions() {
        List<CodeBlock> codeBlocks = super.getActions();

        if (getMode().equals(GeneratorMode.CLIENT)) {
            codeBlocks.add(getSendRequestCodeProvider(request).getCode(Optional.ofNullable(endpoint).orElseGet(() -> getMode().name().toLowerCase()), generateOutboundMessage(request)));

            if (response != null) {
                codeBlocks.add(getReceiveResponseCodeProvider(response).getCode(Optional.ofNullable(endpoint).orElseGet(() -> getMode().name().toLowerCase()), generateInboundMessage(response)));
            }
        } else if (getMode().equals(GeneratorMode.SERVER)) {
            codeBlocks.add(getReceiveRequestCodeProvider(request).getCode(Optional.ofNullable(endpoint).orElseGet(() -> getMode().name().toLowerCase()), generateInboundMessage(request)));

            if (response != null) {
                codeBlocks.add(getSendResponseCodeProvider(response).getCode(Optional.ofNullable(endpoint).orElseGet(() -> getMode().name().toLowerCase()), generateOutboundMessage(response)));
            }
        }

        return codeBlocks;
    }

    /**
     * Inbound message generation hook for subclasses.
     * @param message
     * @return
     */
    protected Message generateInboundMessage(Message message) {
        return message;
    }

    /**
     * Outbound message generation hook for subclasses.
     * @param message
     * @return
     */
    protected Message generateOutboundMessage(Message message) {
        return message;
    }

    protected <M extends Message> CodeProvider<M> getSendRequestCodeProvider(M message) {
        if (message instanceof HttpMessage) {
            return (CodeProvider<M>) new SendHttpRequestCodeProvider();
        } else if (message instanceof SoapMessage) {
            return (CodeProvider<M>) new SendCodeProvider();
        } else {
            return (CodeProvider<M>) new SendCodeProvider();
        }
    }

    protected <M extends Message> CodeProvider<M> getReceiveResponseCodeProvider(M message) {
        if (message instanceof HttpMessage) {
            return (CodeProvider<M>) new ReceiveHttpResponseCodeProvider();
        } else if (message instanceof SoapMessage) {
            return (CodeProvider<M>) new ReceiveCodeProvider();
        } else {
            return (CodeProvider<M>) new ReceiveCodeProvider();
        }
    }

    protected <M extends Message> CodeProvider<M> getSendResponseCodeProvider(M message) {
        if (message instanceof HttpMessage) {
            return (CodeProvider<M>) new SendHttpResponseCodeProvider();
        } else if (message instanceof SoapMessage) {
            return (CodeProvider<M>) new SendCodeProvider();
        } else {
            return (CodeProvider<M>) new SendCodeProvider();
        }
    }

    protected <M extends Message> CodeProvider<M> getReceiveRequestCodeProvider(M message) {
        if (message instanceof HttpMessage) {
            return (CodeProvider<M>) new ReceiveHttpRequestCodeProvider();
        } else if (message instanceof SoapMessage) {
            return (CodeProvider<M>) new ReceiveCodeProvider();
        } else {
            return (CodeProvider<M>) new ReceiveCodeProvider();
        }
    }

    /**
     * Set the endpoint to use.
     * @param endpoint
     * @return
     */
    public T withEndpoint(String endpoint) {
        this.endpoint = endpoint;
        return self;
    }

    /**
     * Set the request to use.
     * @param request
     * @return
     */
    public T withRequest(Message request) {
        this.request = request;
        return self;
    }

    /**
     * Set the response to use.
     * @param response
     * @return
     */
    public T withResponse(Message response) {
        this.response = response;
        return self;
    }

    /**
     * Adds a request header to use.
     * @param name
     * @param value
     * @return
     */
    public T addRequestHeader(String name , Object value) {
        this.request.setHeader(name, value);
        return self;
    }

    /**
     * Adds a response header to use.
     * @param name
     * @param value
     * @return
     */
    public T addResponseHeader(String name, Object value) {
        this.request.setHeader(name, value);
        return self;
    }

    /**
     * Sets the endpoint.
     *
     * @param endpoint
     */
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    /**
     * Gets the endpoint.
     *
     * @return
     */
    public String getEndpoint() {
        return endpoint;
    }

}
