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

package org.citrusframework.ws.message.callback;

import org.citrusframework.context.TestContext;
import org.citrusframework.message.Message;
import org.citrusframework.ws.client.WebServiceEndpointConfiguration;
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

    /** The internal message content source */
    private final Message message;

    /** Endpoint configuration */
    private final WebServiceEndpointConfiguration endpointConfiguration;

    /** Test context */
    private final TestContext context;
    
    /**
     * Constructor using internal message and endpoint configuration as fields.
     *
     * @param message
     * @param endpointConfiguration
     * @param context
     */
    public SoapRequestMessageCallback(Message message, WebServiceEndpointConfiguration endpointConfiguration, TestContext context) {
        this.message = message;
        this.endpointConfiguration = endpointConfiguration;
        this.context = context;
    }
    
    /**
     * Callback method called before request message  is sent.
     */
    public void doWithMessage(WebServiceMessage requestMessage) throws IOException, TransformerException {
        endpointConfiguration.getMessageConverter().convertOutbound(requestMessage, message, endpointConfiguration, context);
    }
}
