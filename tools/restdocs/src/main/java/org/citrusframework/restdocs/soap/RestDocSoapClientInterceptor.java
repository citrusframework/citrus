/*
 * Copyright 2006-2016 the original author or authors.
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

package org.citrusframework.restdocs.soap;

import org.springframework.restdocs.RestDocumentationContext;
import org.springframework.restdocs.generate.RestDocumentationGenerator;
import org.springframework.restdocs.snippet.Snippet;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.client.WebServiceClientException;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.ws.context.MessageContext;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Christoph Deppisch
 * @since 2.6
 */
public class RestDocSoapClientInterceptor implements ClientInterceptor {

    private final RestDocumentationGenerator<MessageContext, WebServiceMessage> documentationGenerator;

    public RestDocSoapClientInterceptor(RestDocumentationGenerator<MessageContext, WebServiceMessage> documentationGenerator) {
        this.documentationGenerator = documentationGenerator;
    }

    @Override
    public boolean handleRequest(MessageContext messageContext) throws WebServiceClientException {
        return true;
    }

    @Override
    public boolean handleResponse(MessageContext messageContext) throws WebServiceClientException {
        return true;
    }

    @Override
    public boolean handleFault(MessageContext messageContext) throws WebServiceClientException {
        return true;
    }

    @Override
    public void afterCompletion(MessageContext messageContext, Exception ex) throws WebServiceClientException {
        Map<String, Object> configuration;

        if (messageContext.containsProperty(CitrusRestDocSoapConfigurer.REST_DOC_SOAP_CONFIGURATION)) {
            configuration = (Map<String, Object>) messageContext.getProperty(CitrusRestDocSoapConfigurer.REST_DOC_SOAP_CONFIGURATION);
            configuration.put(RestDocumentationContext.class.getName(), messageContext.getProperty(RestDocumentationContext.class.getName()));

            messageContext.removeProperty(CitrusRestDocSoapConfigurer.REST_DOC_SOAP_CONFIGURATION);
            messageContext.removeProperty(RestDocumentationContext.class.getName());
        } else {
            configuration = new HashMap<>();
        }

        this.documentationGenerator.handle(messageContext, messageContext.getResponse(), configuration);
    }

    /**
     * Adds the given {@code snippets} such that they are documented when this result
     * handler is called.
     *
     * @param snippets the snippets to add
     * @return this {@code RestDocClientInterceptor}
     */
    public RestDocSoapClientInterceptor snippets(Snippet... snippets) {
        this.documentationGenerator.withSnippets(snippets);
        return this;
    }

    /**
     * Gets the value of the documentationGenerator property.
     *
     * @return the documentationGenerator
     */
    public RestDocumentationGenerator<MessageContext, WebServiceMessage> getDocumentationGenerator() {
        return documentationGenerator;
    }
}
