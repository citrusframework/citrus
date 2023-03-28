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

import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.EndpointInterceptor;

/**
 * Endpoint interceptor implementation logging incoming WebService requests and respective responses to
 * the logger in their complete nature (e.g. complete SOAP envelope).
 * 
 * For SOAP messages the SOAP envelope content is logged with transformer instance. For 
 * WebService requests (other than SOAP) the content is logged via {@link java.io.ByteArrayOutputStream} 
 * which is much more expensive as whole message is loaded to internal memory.
 * 
 * @author Christoph Deppisch
 */
public class LoggingEndpointInterceptor extends LoggingInterceptorSupport implements EndpointInterceptor {

    /**
     * Write request message to logger.
     */
    public boolean handleRequest(MessageContext messageContext, Object endpoint) throws Exception {
        logRequest("Received SOAP request", messageContext, true);
        
        return true;
    }

    /**
     * Write response message to logger.
     */
    public boolean handleResponse(MessageContext messageContext, Object endpoint) throws Exception {
        logResponse("Sending SOAP response", messageContext, false);
        
        return true;
    }

    /**
     * Write fault message to logger.
     */
    public boolean handleFault(MessageContext messageContext, Object endpoint) throws Exception {
        logResponse("Endpoint sending SOAP fault", messageContext, false);
        
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public void afterCompletion(MessageContext messageContext, Object endpoint, Exception ex) throws Exception {
    }
}
