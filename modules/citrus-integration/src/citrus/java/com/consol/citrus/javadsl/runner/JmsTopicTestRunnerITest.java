/*
 * Copyright 2006-2015 the original author or authors.
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

import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.builder.ReceiveMessageBuilder;
import com.consol.citrus.dsl.builder.SendMessageBuilder;
import com.consol.citrus.dsl.builder.BuilderSupport;
import com.consol.citrus.dsl.testng.TestNGCitrusTestRunner;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
@Test
public class JmsTopicTestRunnerITest extends TestNGCitrusTestRunner {
    
    @CitrusTest
    public void jmsTopics() {
        variable("correlationId", "citrus:randomNumber(10)");
        variable("messageId", "citrus:randomNumber(10)");
        variable("user", "Christoph");
        
        parallel().actions(
           receive(new BuilderSupport<ReceiveMessageBuilder>() {
               @Override
               public void configure(ReceiveMessageBuilder builder) {
                   builder.endpoint("helloTopicRequestReceiver")
                           .payload("<HelloRequest xmlns=\"http://www.consol.de/schemas/samples/sayHello.xsd\">" +
                                   "<MessageId>${messageId}</MessageId>" +
                                   "<CorrelationId>${correlationId}</CorrelationId>" +
                                   "<User>${user}</User>" +
                                   "<Text>Hello TestFramework</Text>" +
                                   "</HelloRequest>")
                           .header("Operation", "sayHello")
                           .header("CorrelationId", "${correlationId}")
                           .timeout(5000)
                           .description("Receive asynchronous hello response: HelloService -> TestFramework");
               }
           }),
           receive(new BuilderSupport<ReceiveMessageBuilder>() {
               @Override
               public void configure(ReceiveMessageBuilder builder) {
                   builder.endpoint("helloTopicRequestReceiver")
                           .payload("<HelloRequest xmlns=\"http://www.consol.de/schemas/samples/sayHello.xsd\">" +
                                   "<MessageId>${messageId}</MessageId>" +
                                   "<CorrelationId>${correlationId}</CorrelationId>" +
                                   "<User>${user}</User>" +
                                   "<Text>Hello TestFramework</Text>" +
                                   "</HelloRequest>")
                           .header("Operation", "sayHello")
                           .header("CorrelationId", "${correlationId}")
                           .timeout(5000)
                           .description("Receive asynchronous hello response: HelloService -> TestFramework");
               }
           }),
           sequential().actions(
               sleep(1000L),
               send(new BuilderSupport<SendMessageBuilder>() {
                   @Override
                   public void configure(SendMessageBuilder builder) {
                       builder.endpoint("helloTopicRequestSender")
                               .payload("<HelloRequest xmlns=\"http://www.consol.de/schemas/samples/sayHello.xsd\">" +
                                       "<MessageId>${messageId}</MessageId>" +
                                       "<CorrelationId>${correlationId}</CorrelationId>" +
                                       "<User>${user}</User>" +
                                       "<Text>Hello TestFramework</Text>" +
                                       "</HelloRequest>")
                               .header("Operation", "sayHello")
                               .header("CorrelationId", "${correlationId}")
                               .description("Send asynchronous hello request: TestFramework -> HelloService");
                   }
               })
           )    
        );
    }
}