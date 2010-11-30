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

package com.consol.citrus.samples.bookregistry.endpoint.interceptor;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.interceptor.EndpointInterceptorAdapter;
import org.springframework.ws.soap.SoapMessage;

/**
 * Adds an image attachment to the SOAP response message.
 * 
 * @author Christoph Deppisch
 */
public class BookCoverAttachmentEndpointInterceptor extends EndpointInterceptorAdapter {

    private Object bookCoverInboundGateway;
    
    @Override
    public boolean handleResponse(MessageContext messageContext, Object endpoint) throws Exception {
        SoapMessage response = (SoapMessage)messageContext.getResponse();
        
        if (endpoint.equals(bookCoverInboundGateway)) {
            Resource cover = new ClassPathResource("com/consol/citrus/samples/bookregistry/covers/citrus.png");
            response.addAttachment("bookCoverImage", cover.getFile());
        }
        
        return true;
    }

    /**
     * @param bookCoverInboundGateway the bookCoverInboundGateway to set
     */
    public void setBookCoverInboundGateway(Object bookCoverInboundGateway) {
        this.bookCoverInboundGateway = bookCoverInboundGateway;
    }
}
