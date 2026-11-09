/*
 * Copyright the original author or authors.
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

import org.citrusframework.TestCaseRunner;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.jmx.client.JmxClient;
import org.citrusframework.jmx.message.JmxMessage;
import org.citrusframework.jmx.server.JmxServer;
import org.citrusframework.junit.jupiter.spring.CitrusSpringSupport;
import org.citrusframework.spring.config.CitrusSpringConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;

/**
 * @since 2.5
 */
@CitrusSpringSupport
@ContextConfiguration(classes = CitrusSpringConfig.class)
@Isolated("Shares JVM-global platform MBeanServer with other JMX ITs; parallel classes risk InstanceAlreadyExistsException and message cross-talk")
public class JmxEndpointIT implements TestActionSupport {

    @Autowired
    @Qualifier("jmxHelloClient")
    private JmxClient jmxClient;

    @Autowired
    @Qualifier("jmxHelloServer")
    private JmxServer jmxServer;

    @Test
    @CitrusTest
    public void testClient(@CitrusResource TestCaseRunner runner) {
        runner.when(send(jmxClient)
                .message(JmxMessage.invocation("java.lang:type=Memory")
                        .attribute("Verbose")));

        runner.then(receive(jmxClient)
                .message(JmxMessage.result(false)));
    }

    @Test
    @CitrusTest
    public void testServer(@CitrusResource TestCaseRunner runner) {
        runner.when(send(jmxClient)
                .message(JmxMessage.invocation("org.citrusframework.jmx.mbean:type=HelloBean")
                            .operation("hello")
                            .parameter("Hello JMX this is cool!"))
                .fork(true));

        runner.then(receive(jmxServer)
                .message(JmxMessage.invocation("org.citrusframework.jmx.mbean:type=HelloBean")
                            .operation("hello")
                            .parameter("Hello JMX this is cool!")));

        runner.when(send(jmxServer)
                .message(JmxMessage.result("Hello from JMX!")));

        runner.then(receive(jmxClient)
                .message(JmxMessage.result("Hello from JMX!")));

        runner.when(send(jmxClient)
                .message(JmxMessage.invocation("news:name=NewsBean")
                                .attribute("newsCount"))
                .fork(true));

        runner.then(receive(jmxServer)
                .message(JmxMessage.invocation("news:name=NewsBean")
                                .attribute("newsCount")));

        runner.when(send(jmxServer)
                .message(JmxMessage.result(100)));

        runner.then(receive(jmxClient)
                .message(JmxMessage.result(100)));
    }
}
