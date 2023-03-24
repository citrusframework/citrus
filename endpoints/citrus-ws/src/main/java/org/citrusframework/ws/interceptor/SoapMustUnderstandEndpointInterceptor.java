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

package org.citrusframework.ws.interceptor;

import java.util.ArrayList;
import java.util.List;

import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.server.SoapEndpointInterceptor;

/**
 * Handles and accepts must understand header entries in SOAP requests. The supported header entries are configured
 * via simple HashMap where key is the namespace uri and value the header name (local part).
 * 
 * @author Christoph Deppisch
 */
public class SoapMustUnderstandEndpointInterceptor implements SoapEndpointInterceptor {

    private List<String> acceptedHeaders = new ArrayList<String>();
    
    /**
     * (non-Javadoc)
     * @see org.springframework.ws.soap.server.SoapEndpointInterceptor#understands(org.springframework.ws.soap.SoapHeaderElement)
     */
    public boolean understands(SoapHeaderElement header) {
        //see if header is accepted
        if (header.getName() != null && acceptedHeaders.contains(header.getName().toString())) {
            return true;
        }
        
        return false;
    }
    
    /**
     * (non-Javadoc)
     * @see org.springframework.ws.server.EndpointInterceptor#handleFault(org.springframework.ws.context.MessageContext, java.lang.Object)
     */
    public boolean handleFault(MessageContext messageContext, Object endpoint)
            throws Exception {
        return true;
    }

    /**
     * (non-Javadoc)
     * @see org.springframework.ws.server.EndpointInterceptor#handleRequest(org.springframework.ws.context.MessageContext, java.lang.Object)
     */
    public boolean handleRequest(MessageContext messageContext, Object endpoint)
            throws Exception {
        return true;
    }

    /**
     * (non-Javadoc)
     * @see org.springframework.ws.server.EndpointInterceptor#handleResponse(org.springframework.ws.context.MessageContext, java.lang.Object)
     */
    public boolean handleResponse(MessageContext messageContext, Object endpoint)
            throws Exception {
        return true;
    }
    
    /**
     * {@inheritDoc}
     */
    public void afterCompletion(MessageContext messageContext, Object endpoint, Exception ex) 
            throws Exception {
    }
    
    /**
     * @param acceptedHeaders the acceptedHeaders to set
     */
    public void setAcceptedHeaders(List<String> acceptedHeaders) {
        this.acceptedHeaders = acceptedHeaders;
    }
    
}
