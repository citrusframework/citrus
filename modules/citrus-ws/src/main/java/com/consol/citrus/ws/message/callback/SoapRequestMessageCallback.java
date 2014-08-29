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

package com.consol.citrus.ws.message.callback;

import com.consol.citrus.ws.message.converter.SoapMessageConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.Message;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.client.core.WebServiceMessageCallback;

import javax.xml.transform.TransformerException;
import java.io.IOException;

/**
 * Sender callback invoked by framework with actual web service request before message is sent.
 * Web service message is filled with content from internal message representation.
 * 
 * @author Christoph Deppisch
 */
public class SoapRequestMessageCallback implements WebServiceMessageCallback {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(SoapRequestMessageCallback.class);
    
    /** The internal message content source */
    private Message<?> message;
    
    /** Soap message converter */
    private SoapMessageConverter soapMessageConverter;

    /**
     * Constructor uses default message converter implementation.
     *
     * @param message
     */
    public SoapRequestMessageCallback(Message<?> message) {
        this(message, new SoapMessageConverter());
    }

    /**
     * Constructor using message converter implementation.
     *
     * @param message
     * @param soapMessageConverter
     */
    public SoapRequestMessageCallback(Message<?> message, SoapMessageConverter soapMessageConverter) {
        this.message = message;
        this.soapMessageConverter = soapMessageConverter;
    }
    
    /**
     * Callback method called before request message  is sent.
     */
    public void doWithMessage(WebServiceMessage requestMessage) throws IOException, TransformerException {
        soapMessageConverter.convertOutbound(requestMessage, message);
    }
}
