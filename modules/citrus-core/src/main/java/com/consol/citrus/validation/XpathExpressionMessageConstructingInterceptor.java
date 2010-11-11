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

package com.consol.citrus.validation;

import java.util.HashMap;
import java.util.Map;

import org.springframework.integration.core.Message;
import org.springframework.integration.message.MessageBuilder;

import com.consol.citrus.context.TestContext;

/**
 * Interceptor implementation evaluating XPath expressions on message payload during message construction.
 * Class identifies XML elements inside the message payload via XPath expressions in order to overwrite their value.
 * 
 * @author Christoph Deppisch
 */
public class XpathExpressionMessageConstructingInterceptor implements MessageConstructingInterceptor<String> {

    /** Overwrites message elements before validating (via XPath expressions) */
    private Map<String, String> xPathExpressions = new HashMap<String, String>();
    
    
    /**
     * Default constructor using fields.
     * @param xPathExpressions
     */
    public XpathExpressionMessageConstructingInterceptor(Map<String, String> xPathExpressions) {
        super();
        this.xPathExpressions = xPathExpressions;
    }

    /**
     * Intercept the message payload construction.
     */
    public String interceptMessageConstruction(String messagePayload, TestContext context) {
        return context.replaceMessageValues(xPathExpressions, messagePayload);
    }

    /**
     * Intercept the message and modify the message payload.
     */
    public Message<String> interceptMessageConstruction(Message<String> message, TestContext context) {
       return MessageBuilder.withPayload(interceptMessageConstruction(message.getPayload(), context))
                            .copyHeaders(message.getHeaders()).build();
    }

    /**
     * @param xPathExpressions the xPathExpressions to set
     */
    public void setxPathExpressions(Map<String, String> xPathExpressions) {
        this.xPathExpressions = xPathExpressions;
    }

}
