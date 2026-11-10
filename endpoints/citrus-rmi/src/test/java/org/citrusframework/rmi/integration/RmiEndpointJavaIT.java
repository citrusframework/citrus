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

package org.citrusframework.rmi.integration;

import org.citrusframework.GherkinTestActionRunner;
import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.junit.jupiter.spring.CitrusSpringSupport;
import org.citrusframework.rmi.client.RmiClient;
import org.citrusframework.rmi.message.RmiMessage;
import org.citrusframework.rmi.remote.HelloService;
import org.citrusframework.rmi.remote.NewsService;
import org.citrusframework.rmi.server.RmiServer;
import org.citrusframework.spring.config.CitrusSpringConfig;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;

/**
 * @since 2.5
 */
@CitrusSpringSupport
@ContextConfiguration(classes = CitrusSpringConfig.class)
@Disabled("unstable on local environment")
public class RmiEndpointJavaIT implements TestActionSupport {

    @Autowired
    @Qualifier("rmiNewsClient")
    private RmiClient rmiNewsClient;

    @Autowired
    @Qualifier("rmiNewsServer")
    private RmiServer rmiNewsServer;

    @Autowired
    @Qualifier("rmiHelloClient")
    private RmiClient rmiHelloClient;

    @Autowired
    @Qualifier("rmiHelloServer")
    private RmiServer rmiHelloServer;

    @Test
    @CitrusTest
    public void testClient(@CitrusResource GherkinTestActionRunner runner) {
        runner.when(send(rmiNewsClient)
                .fork(true)
                .message(RmiMessage.invocation("getNews")));

        runner.when(receive(rmiNewsServer)
                .message(RmiMessage.invocation(NewsService.class, "getNews")));

        runner.then(send(rmiNewsServer)
                .message(RmiMessage.result("This is news from RMI!")));

        runner.then(receive(rmiNewsClient)
                .message(RmiMessage.result("This is news from RMI!")));

        runner.when(send(rmiNewsClient)
                .fork(true)
                .message(RmiMessage.invocation("setNews")
                                .argument("This is breaking news!")));

        runner.when(receive(rmiNewsServer)
                .message(RmiMessage.invocation(NewsService.class, "setNews")
                        .argument("This is breaking news!")));

        runner.then(send(rmiNewsServer)
                .message(RmiMessage.result()));

        runner.then(receive(rmiNewsClient)
                .message(RmiMessage.result()));
    }

    @Test
    @CitrusTest
    public void testServer(@CitrusResource GherkinTestActionRunner runner) {
        runner.given(send(rmiHelloClient)
                .message(RmiMessage.invocation(HelloService.class, "sayHello")
                        .argument("Hello RMI this is cool!"))
                .fork(true));

        runner.when(receive(rmiHelloServer)
                .message(RmiMessage.invocation(HelloService.class, "sayHello")
                                    .argument("Hello RMI this is cool!")));

        runner.then(send(rmiHelloServer)
                .message(RmiMessage.result()));

        runner.then(receive(rmiHelloClient)
                .message(RmiMessage.result()));

        runner.given(send(rmiHelloClient)
                .message(RmiMessage.invocation(HelloService.class, "getHelloCount"))
                .fork(true));

        runner.when(receive(rmiHelloServer)
                .message(RmiMessage.invocation(HelloService.class, "getHelloCount")));

        runner.then(send(rmiHelloServer)
                .message(RmiMessage.result(100)));

        runner.then(receive(rmiHelloClient)
                .message(RmiMessage.result(100)));
    }
}
