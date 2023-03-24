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

import org.springframework.restdocs.config.SnippetConfigurer;
import org.springframework.ws.client.WebServiceClientException;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.ws.context.MessageContext;

/**
 * @author Christoph Deppisch
 * @since 2.6
 */
public class CitrusSnippetConfigurer extends SnippetConfigurer<CitrusRestDocSoapConfigurer, CitrusSnippetConfigurer>
        implements ClientInterceptor {

    public CitrusSnippetConfigurer(CitrusRestDocSoapConfigurer parent) {
        super(parent);
    }

    @Override
    public boolean handleRequest(MessageContext messageContext) throws WebServiceClientException {
        and().handleRequest(messageContext);
        return true;
    }

    @Override
    public boolean handleResponse(MessageContext messageContext) throws WebServiceClientException {
        and().handleResponse(messageContext);
        return true;
    }

    @Override
    public boolean handleFault(MessageContext messageContext) throws WebServiceClientException {
        and().handleFault(messageContext);
        return true;
    }

    @Override
    public void afterCompletion(MessageContext messageContext, Exception ex) throws WebServiceClientException {
        and().afterCompletion(messageContext, ex);
    }
}
