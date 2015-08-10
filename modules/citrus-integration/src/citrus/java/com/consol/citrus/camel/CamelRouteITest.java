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

package com.consol.citrus.camel;

import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.testng.TestNGCitrusTestDesigner;
import com.consol.citrus.message.MessageType;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 * @since 2.3
 */
public class CamelRouteITest extends TestNGCitrusTestDesigner {

    @Test
    @CitrusTest(name = "CamelRoute_01_ITest")
    public void camelRoute01ITest() {
        send("inRouteEndpoint")
                .fork(true)
                .messageType(MessageType.PLAINTEXT)
                .payload("<News><Message>Citrus rocks!</Message></News>");

        receive("defaultRouteEndpoint")
                .messageType(MessageType.PLAINTEXT)
                .payload("<News><Message>Citrus rocks!</Message></News>");
    }

    @Test
    @CitrusTest(name = "CamelRoute_02_ITest")
    public void camelRoute02ITest() {
        send("inRouteEndpoint")
                .fork(true)
                .messageType(MessageType.PLAINTEXT)
                .payload("<News><Message>Citrus rocks!</Message></News>");

        receiveTimeout("outRouteEndpoint").timeout(500);

        receive("defaultRouteEndpoint")
                .messageType(MessageType.PLAINTEXT)
                .payload("<News><Message>Citrus rocks!</Message></News>");
    }

}
