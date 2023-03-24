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

package org.citrusframework.jmx.integration;

import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.jmx.client.JmxClient;
import org.citrusframework.jmx.message.JmxMessage;
import org.citrusframework.jmx.server.JmxServer;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.testng.annotations.Test;

import static org.citrusframework.actions.ReceiveMessageAction.Builder.receive;
import static org.citrusframework.actions.SendMessageAction.Builder.send;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
@Test
public class JmxEndpointIT extends TestNGCitrusSpringSupport {

    @Autowired
    @Qualifier("jmxHelloClient")
    private JmxClient jmxClient;

    @Autowired
    @Qualifier("jmxHelloServer")
    private JmxServer jmxServer;

    @CitrusTest
    public void testClient() {
        when(send(jmxClient)
                .message(JmxMessage.invocation("java.lang:type=Memory")
                        .attribute("Verbose")));

        then(receive(jmxClient)
                .message(JmxMessage.result(false)));
    }

    @CitrusTest
    public void testServer() {
        when(send(jmxClient)
                .message(JmxMessage.invocation("org.citrusframework.jmx.mbean:type=HelloBean")
                            .operation("hello")
                            .parameter("Hello JMX this is cool!"))
                .fork(true));

        then(receive(jmxServer)
                .message(JmxMessage.invocation("org.citrusframework.jmx.mbean:type=HelloBean")
                            .operation("hello")
                            .parameter("Hello JMX this is cool!")));

        when(send(jmxServer)
                .message(JmxMessage.result("Hello from JMX!")));

        then(receive(jmxClient)
                .message(JmxMessage.result("Hello from JMX!")));

        when(send(jmxClient)
                .message(JmxMessage.invocation("news:name=NewsBean")
                                .attribute("newsCount"))
                .fork(true));

        then(receive(jmxServer)
                .message(JmxMessage.invocation("news:name=NewsBean")
                                .attribute("newsCount")));

        when(send(jmxServer)
                .message(JmxMessage.result(100)));

        then(receive(jmxClient)
                .message(JmxMessage.result(100)));
    }
}
