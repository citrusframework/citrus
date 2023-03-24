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

package org.citrusframework.camel.integration;

import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.message.MessageType;
import org.citrusframework.testng.TestNGCitrusSupport;
import org.testng.annotations.Test;

import static org.citrusframework.actions.ReceiveMessageAction.Builder.receive;
import static org.citrusframework.actions.ReceiveTimeoutAction.Builder.receiveTimeout;
import static org.citrusframework.actions.SendMessageAction.Builder.send;

/**
 * @author Christoph Deppisch
 * @since 2.3
 */
public class CamelRouteIT extends TestNGCitrusSupport {

    @Test
    @CitrusTest(name = "CamelRoute_01_IT")
    public void camelRoute01IT() {
        when(send("inRouteEndpoint")
                .fork(true)
                .message()
                .type(MessageType.PLAINTEXT)
                .body("<News><Message>Citrus rocks!</Message></News>"));

        then(receive("defaultRouteEndpoint")
                .message()
                .type(MessageType.PLAINTEXT)
                .body("<News><Message>Citrus rocks!</Message></News>"));
    }

    @Test
    @CitrusTest(name = "CamelRoute_02_IT")
    public void camelRoute02IT() {
        when(send("inRouteEndpoint")
                .fork(true)
                .message()
                .type(MessageType.PLAINTEXT)
                .body("<News><Message>Citrus rocks!</Message></News>"));

        then(receiveTimeout("outRouteEndpoint").timeout(500));

        and(receive("defaultRouteEndpoint")
                .message()
                .type(MessageType.PLAINTEXT)
                .body("<News><Message>Citrus rocks!</Message></News>"));
    }

}
