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

package com.consol.citrus.adapter.common.endpoint;

import org.springframework.integration.Message;
import org.springframework.integration.support.MessageBuilder;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.exceptions.CitrusRuntimeException;

/**
 * @author Christoph Deppisch
 */
public class MessageHeaderEndpointUriResolverTest {

    @Test
    public void testEndpointMapping() {
        MessageHeaderEndpointUriResolver endpointUriResolver = new MessageHeaderEndpointUriResolver();
        
        Message<?> testMessage;
        
        testMessage = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                .setHeader(MessageHeaderEndpointUriResolver.ENDPOINT_URI_HEADER_NAME, "http://localhost:8080/request")
                .build();
        
        Assert.assertEquals(endpointUriResolver.resolveEndpointUri(testMessage), "http://localhost:8080/request");
        Assert.assertEquals(endpointUriResolver.resolveEndpointUri(testMessage, "http://localhost:8080/default"), "http://localhost:8080/request");
    }
    
    @Test
    public void testDefaultEndpoint() {
        MessageHeaderEndpointUriResolver endpointUriResolver = new MessageHeaderEndpointUriResolver();
        
        Message<?> testMessage;
        
        testMessage = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>").build();
        
        Assert.assertEquals(endpointUriResolver.resolveEndpointUri(testMessage, "http://localhost:8080/default"), "http://localhost:8080/default");
    }
    
    @Test
    public void testResolveException() {
        MessageHeaderEndpointUriResolver endpointUriResolver = new MessageHeaderEndpointUriResolver();
        
        Message<?> testMessage;
        
        testMessage = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>").build();
        
        try {
            endpointUriResolver.resolveEndpointUri(testMessage);
        } catch (CitrusRuntimeException e) {
            Assert.assertTrue(e.getMessage().startsWith("Unable to resolve dynamic endpoint uri"));
            Assert.assertTrue(e.getMessage().contains(MessageHeaderEndpointUriResolver.ENDPOINT_URI_HEADER_NAME));
            return;
        }
        
        Assert.fail("Missing CitrusRuntimeException caused by unresolvable endpoint uri");
    }
}
