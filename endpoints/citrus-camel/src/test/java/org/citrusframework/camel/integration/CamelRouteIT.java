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

package org.citrusframework.camel.integration;

import org.citrusframework.TestCaseRunner;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.junit.jupiter.CitrusSupport;
import org.citrusframework.message.MessageType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;

/**
 * @since 2.3
 */
@Isolated("Fresh Spring context per run registers duplicate camelContext JMX MBean under parallel classes")
@CitrusSupport
public class CamelRouteIT implements TestActionSupport {

    @Test
    @CitrusTest(name = "CamelRoute_01_IT")
    public void camelRoute01IT(@CitrusResource TestCaseRunner runner) {
        runner.when(send("inRouteEndpoint")
                .fork(true)
                .message()
                .type(MessageType.PLAINTEXT)
                .body("<News><Message>Citrus rocks!</Message></News>"));

        runner.then(receive("defaultRouteEndpoint")
                .message()
                .type(MessageType.PLAINTEXT)
                .body("<News><Message>Citrus rocks!</Message></News>"));
    }

    @Test
    @CitrusTest(name = "CamelRoute_02_IT")
    public void camelRoute02IT(@CitrusResource TestCaseRunner runner) {
        runner.when(send("inRouteEndpoint")
                .fork(true)
                .message()
                .type(MessageType.PLAINTEXT)
                .body("<News><Message>Citrus rocks!</Message></News>"));

        runner.then(receiveTimeout("outRouteEndpoint").timeout(500));

        runner.and(receive("defaultRouteEndpoint")
                .message()
                .type(MessageType.PLAINTEXT)
                .body("<News><Message>Citrus rocks!</Message></News>"));
    }
}
