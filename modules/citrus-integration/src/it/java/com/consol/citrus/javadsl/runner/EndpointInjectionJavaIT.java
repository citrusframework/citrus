/*
 * Copyright 2006-2016 the original author or authors.
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

package com.consol.citrus.javadsl.runner;

import com.consol.citrus.Citrus;
import com.consol.citrus.annotations.*;
import com.consol.citrus.dsl.builder.*;
import com.consol.citrus.dsl.testng.TestNGCitrusTestRunner;
import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.jms.config.annotation.JmsEndpointConfig;
import com.consol.citrus.message.MessageType;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class EndpointInjectionJavaIT extends TestNGCitrusTestRunner {

    @CitrusFramework
    private Citrus citrus;

    @CitrusEndpoint
    @JmsEndpointConfig(destinationName = "FOO.test.queue")
    private Endpoint jmsEndpoint;

    @Test
    @CitrusTest
    public void injectEndpoint() {
        send(new BuilderSupport<SendMessageBuilder>() {
            @Override
            public void configure(SendMessageBuilder builder) {
                builder.endpoint(jmsEndpoint)
                        .messageType(MessageType.PLAINTEXT)
                        .payload("Hello!");
            }
        });

        receive(new BuilderSupport<ReceiveMessageBuilder>() {
            @Override
            public void configure(ReceiveMessageBuilder builder) {
                builder.endpoint(jmsEndpoint)
                        .messageType(MessageType.PLAINTEXT)
                        .payload("Hello!");
            }
        });

        Assert.assertNotNull(citrus);
    }
}
