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

package com.consol.citrus.jmx;

import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.testng.TestNGCitrusTestDesigner;
import com.consol.citrus.jmx.client.JmxClient;
import com.consol.citrus.jmx.message.JmxMessage;
import com.consol.citrus.jmx.server.JmxServer;
import com.consol.citrus.rmi.message.RmiMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
@Test
public class JmxEndpointIT extends TestNGCitrusTestDesigner {

    @Autowired
    @Qualifier("jmxClient")
    private JmxClient jmxClient;

    @Autowired
    @Qualifier("jmxServer")
    private JmxServer jmxServer;

    @CitrusTest
    public void testClient() {
        send(jmxClient)
                .message(JmxMessage.invocation("java.lang:type=Memory")
                        .attribute("Verbose"));

        receive(jmxClient)
                .message(JmxMessage.result(false));
    }

    @CitrusTest
    public void testServer() {
        send(jmxClient)
                .message(JmxMessage.invocation("com.consol.citrus.jmx:type=HelloBean")
                            .operation("hello")
                            .parameter("Hello JMX this is cool!"))
                .fork(true);

        receive(jmxServer)
                .message(JmxMessage.invocation("com.consol.citrus.jmx:type=HelloBean")
                            .operation("hello")
                            .parameter("Hello JMX this is cool!"));

        send(jmxServer)
                .message(JmxMessage.result("Hello from JMX!"));

        receive(jmxClient)
                .message(JmxMessage.result("Hello from JMX!"));

        send(jmxClient)
                .message(JmxMessage.invocation("com.consol.citrus.news:name=News")
                                .attribute("newsCount"))
                .fork(true);

        receive(jmxServer)
                .message(JmxMessage.invocation("com.consol.citrus.news:name=News")
                                .attribute("newsCount"));

        send(jmxServer)
                .message(JmxMessage.result(100));

        receive(jmxClient)
                .message(JmxMessage.result(100));
    }
}
