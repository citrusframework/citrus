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

package org.citrusframework.rmi.integration;

import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.rmi.client.RmiClient;
import org.citrusframework.rmi.message.RmiMessage;
import org.citrusframework.rmi.remote.HelloService;
import org.citrusframework.rmi.remote.NewsService;
import org.citrusframework.rmi.server.RmiServer;
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
public class RmiEndpointJavaIT extends TestNGCitrusSpringSupport {

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

    @CitrusTest
    public void testClient() {
        when(send(rmiNewsClient)
                .fork(true)
                .message(RmiMessage.invocation("getNews")));

        when(receive(rmiNewsServer)
                .message(RmiMessage.invocation(NewsService.class, "getNews")));

        then(send(rmiNewsServer)
                .message(RmiMessage.result("This is news from RMI!")));

        then(receive(rmiNewsClient)
                .message(RmiMessage.result("This is news from RMI!")));

        when(send(rmiNewsClient)
                .fork(true)
                .message(RmiMessage.invocation("setNews")
                                .argument("This is breaking news!")));

        when(receive(rmiNewsServer)
                .message(RmiMessage.invocation(NewsService.class, "setNews")
                        .argument("This is breaking news!")));

        then(send(rmiNewsServer)
                .message(RmiMessage.result()));

        then(receive(rmiNewsClient)
                .message(RmiMessage.result()));
    }

    @CitrusTest
    public void testServer() {
        given(send(rmiHelloClient)
                .message(RmiMessage.invocation(HelloService.class, "sayHello")
                        .argument("Hello RMI this is cool!"))
                .fork(true));

        when(receive(rmiHelloServer)
                .message(RmiMessage.invocation(HelloService.class, "sayHello")
                                    .argument("Hello RMI this is cool!")));

        then(send(rmiHelloServer)
                .message(RmiMessage.result()));

        then(receive(rmiHelloClient)
                .message(RmiMessage.result()));

        given(send(rmiHelloClient)
                .message(RmiMessage.invocation(HelloService.class, "getHelloCount"))
                .fork(true));

        when(receive(rmiHelloServer)
                .message(RmiMessage.invocation(HelloService.class, "getHelloCount")));

        then(send(rmiHelloServer)
                .message(RmiMessage.result(100)));

        then(receive(rmiHelloClient)
                .message(RmiMessage.result(100)));
    }
}
