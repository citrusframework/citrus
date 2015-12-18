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

package com.consol.citrus.rmi;

import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.annotations.CitrusXmlTest;
import com.consol.citrus.dsl.testng.TestNGCitrusTestDesigner;
import com.consol.citrus.rmi.client.RmiClient;
import com.consol.citrus.rmi.message.RmiMessage;
import com.consol.citrus.rmi.server.RmiServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
@Test
public class RmiEndpointIT extends TestNGCitrusTestDesigner {

    @Autowired
    @Qualifier("rmiNewsClient")
    private RmiClient rmiNewsClient;

    @Autowired
    @Qualifier("rmiHelloServer")
    private RmiServer rmiHelloServer;

    @CitrusTest
    public void testClient() {
        send(rmiNewsClient)
                .message(RmiMessage.invocation("getNews"));

        receive(rmiNewsClient)
                .message(RmiMessage.result("This is news from RMI!"));

        send(rmiNewsClient)
                .message(RmiMessage.invocation("setNews")
                                .argument("This is breaking news!"));

        receive(rmiNewsClient)
                .message(RmiMessage.result());

        send(rmiNewsClient)
                .message(RmiMessage.invocation("getNews"));

        receive(rmiNewsClient)
                .message(RmiMessage.result("This is breaking news!"));

        doFinally().actions(
            send(rmiNewsClient)
                    .message(RmiMessage.invocation("setNews")
                            .argument("This is news from RMI!")),
            receive(rmiNewsClient)
                    .message(RmiMessage.result())
        );
    }

    @CitrusTest
    public void testServer() {
        send("camel:direct:hello")
                .payload("Hello RMI this is cool!")
                .fork(true);

        receive(rmiHelloServer)
                .message(RmiMessage.invocation(HelloService.class, "sayHello")
                                    .argument("Hello RMI this is cool!"));

        send(rmiHelloServer)
                .message(RmiMessage.result());

        send("camel:direct:helloCount")
                .fork(true);

        receive(rmiHelloServer)
                .message(RmiMessage.invocation(HelloService.class, "getHelloCount"));

        send(rmiHelloServer)
                .message(RmiMessage.result(100));
    }
}
