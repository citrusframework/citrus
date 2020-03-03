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

package com.consol.citrus.rmi.integration;

import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.rmi.client.RmiClient;
import com.consol.citrus.rmi.message.RmiMessage;
import com.consol.citrus.rmi.remote.HelloService;
import com.consol.citrus.rmi.server.RmiServer;
import com.consol.citrus.testng.TestNGCitrusSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.testng.annotations.Test;

import static com.consol.citrus.actions.ReceiveMessageAction.Builder.receive;
import static com.consol.citrus.actions.SendMessageAction.Builder.send;
import static com.consol.citrus.container.FinallySequence.Builder.doFinally;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
@Test
public class RmiEndpointJavaIT extends TestNGCitrusSupport {

    @Autowired
    @Qualifier("rmiNewsClient")
    private RmiClient rmiNewsClient;

    @Autowired
    @Qualifier("rmiHelloServer")
    private RmiServer rmiHelloServer;

    @CitrusTest
    public void testClient() {
        when(send(rmiNewsClient)
                .message(RmiMessage.invocation("getNews")));

        then(receive(rmiNewsClient)
                .message(RmiMessage.result("This is news from RMI!")));

        when(send(rmiNewsClient)
                .message(RmiMessage.invocation("setNews")
                                .argument("This is breaking news!")));

        then(receive(rmiNewsClient)
                .message(RmiMessage.result()));

        when(send(rmiNewsClient)
                .message(RmiMessage.invocation("getNews")));

        then(receive(rmiNewsClient)
                .message(RmiMessage.result("This is breaking news!")));

        then(doFinally().actions(
            send(rmiNewsClient)
                    .message(RmiMessage.invocation("setNews")
                            .argument("This is news from RMI!")),
            receive(rmiNewsClient)
                    .message(RmiMessage.result())
        ));
    }

    @CitrusTest
    public void testServer() {
        given(send("camel:direct:hello")
                .payload("Hello RMI this is cool!")
                .fork(true));

        when(receive(rmiHelloServer)
                .message(RmiMessage.invocation(HelloService.class, "sayHello")
                                    .argument("Hello RMI this is cool!")));

        then(send(rmiHelloServer)
                .message(RmiMessage.result()));

        given(send("camel:direct:helloCount")
                .fork(true));

        when(receive(rmiHelloServer)
                .message(RmiMessage.invocation(HelloService.class, "getHelloCount")));

        then(send(rmiHelloServer)
                .message(RmiMessage.result(100)));
    }
}
