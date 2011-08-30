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

package com.consol.citrus.ws.interceptor;

import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.soap.SoapMessage;

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
        if (log.isDebugEnabled()) {
            if (messageContext.getRequest() instanceof SoapMessage) {
                logSoapMessage("Endpoint handling SOAP request:\n", 
                        ((SoapMessage) messageContext.getRequest()).getEnvelope().getSource());
            } else {
                logWebServiceMessage("Endppint handling request:\n", messageContext.getRequest());
            }
        }
        
        return true;
    }

    /**
     * Write response message to logger.
     */
    public boolean handleResponse(MessageContext messageContext, Object endpoint) throws Exception {
        if (messageContext.hasResponse() && log.isDebugEnabled()) {
            if (messageContext.getResponse() instanceof SoapMessage) {
                logSoapMessage("Endpoint sending SOAP response:\n", 
                        ((SoapMessage) messageContext.getResponse()).getEnvelope().getSource());
            } else {
                logWebServiceMessage("Endpoint sending response:\n", messageContext.getResponse());
            }
        }
        
        return true;
    }

    /**
     * Write fault message to logger.
     */
    public boolean handleFault(MessageContext messageContext, Object endpoint) throws Exception {
        if (messageContext.hasResponse() && log.isDebugEnabled()) {
            if (messageContext.getResponse() instanceof SoapMessage) {
                logSoapMessage("Endpoint handling SOAP fault:\n", 
                        ((SoapMessage) messageContext.getResponse()).getEnvelope().getSource());
            } else {
                logWebServiceMessage("Endpoint handling fault:\n", messageContext.getResponse());
            }
        }
        
        return true;
    }
}
