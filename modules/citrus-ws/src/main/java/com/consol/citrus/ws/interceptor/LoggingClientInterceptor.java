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

import javax.xml.transform.TransformerException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ws.client.WebServiceClientException;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapEnvelopeException;
import org.springframework.ws.soap.SoapMessage;

/**
 * Client interceptor implementation logging SOAP requests and responses as well as SOAP faults
 * with logging framework.
 * 
 * @author Christoph Deppisch
 */
public class LoggingClientInterceptor extends LoggingInterceptorSupport implements ClientInterceptor {
    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(LoggingClientInterceptor.class);

    /**
     * Write SOAP request to logger before sending.
     */
    public boolean handleRequest(MessageContext messageContext)
            throws WebServiceClientException {
        if (log.isDebugEnabled()) {
            if (messageContext.getRequest() instanceof SoapMessage) {
                try {
                    logSoapMessage("Sending SOAP request:\n", 
                            ((SoapMessage) messageContext.getRequest()).getEnvelope().getSource());
                } catch (SoapEnvelopeException e) {
                    log.warn("Unable to write SOAP request to logger", e);
                } catch (TransformerException e) {
                    log.warn("Unable to write SOAP request to logger", e);
                }
            } else {
                logWebServiceMessage("Sending WebService request:\n", messageContext.getRequest());
            }
        }
        
        return true;
    }

    /**
     * Write SOAP response to logger.
     */
    public boolean handleResponse(MessageContext messageContext)
            throws WebServiceClientException {
        if (messageContext.hasResponse() && log.isDebugEnabled()) {
            if (messageContext.getResponse() instanceof SoapMessage) {
                try {
                    logSoapMessage("Received SOAP response:\n", 
                            ((SoapMessage) messageContext.getResponse()).getEnvelope().getSource());
                } catch (SoapEnvelopeException e) {
                    log.warn("Unable to write SOAP response to logger", e);
                } catch (TransformerException e) {
                    log.warn("Unable to write SOAP response to logger", e);
                }
            } else {
                logWebServiceMessage("Received WebService response:\n", messageContext.getResponse());
            }
        }
        
        return true;
    }
    
    /**
     * Write SOAP fault to logger.
     */
    public boolean handleFault(MessageContext messageContext)
            throws WebServiceClientException {
        return handleResponse(messageContext);
    }
}
