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

import javax.xml.transform.TransformerException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ws.client.WebServiceClientException;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapEnvelopeException;

/**
 * Client interceptor implementation logging SOAP requests and responses as well as SOAP faults
 * with logging framework.
 *
 * @author Christoph Deppisch
 */
public class LoggingClientInterceptor extends LoggingInterceptorSupport implements ClientInterceptor {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(LoggingClientInterceptor.class);

    /**
     * Write SOAP request to logger before sending.
     */
    public boolean handleRequest(MessageContext messageContext) throws WebServiceClientException {
        try {
            logRequest("Sending SOAP request", messageContext, false);
        } catch (SoapEnvelopeException | TransformerException e) {
            logger.warn("Unable to write SOAP request to logger", e);
        }

        return true;
    }

    /**
     * Write SOAP response to logger.
     */
    public boolean handleResponse(MessageContext messageContext) throws WebServiceClientException {
        try {
            logResponse("Received SOAP response", messageContext, true);
        } catch (SoapEnvelopeException | TransformerException e) {
            logger.warn("Unable to write SOAP response to logger", e);
        }

        return true;
    }

    /**
     * Write SOAP fault to logger.
     */
    public boolean handleFault(MessageContext messageContext) throws WebServiceClientException {
        try {
            logResponse("Received SOAP fault", messageContext, true);
        } catch (SoapEnvelopeException | TransformerException e) {
            logger.warn("Unable to write SOAP fault to logger", e);
        }

        return true;
    }

    @Override
    public void afterCompletion(MessageContext messageContext, Exception ex) throws WebServiceClientException {
        //TODO make something
    }
}
