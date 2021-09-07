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

package com.consol.citrus.validation.interceptor;

import com.consol.citrus.UnitTestSupport;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.message.DefaultMessage;
import com.consol.citrus.message.Message;
import com.consol.citrus.message.MessageType;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
@Deprecated
public class AbstractMessageConstructionInterceptorTest extends UnitTestSupport {

    @Test
    public void testInterceptMessageConstruction() throws Exception {
        MessageConstructionInterceptor interceptor = new AbstractMessageConstructionInterceptor() {
            @Override
            public boolean supportsMessageType(String messageType) {
                return MessageType.XML.toString().equalsIgnoreCase(messageType);
            }

            @Override
            protected Message interceptMessage(Message message, String messageType, TestContext context) {
                return new DefaultMessage("Intercepted!");
            }

            @Override
            protected String getName() {
                return "MockInterceptor";
            }
        };

        Message in = new DefaultMessage("Hello Citrus!");
        Message intercepted = interceptor.interceptMessageConstruction(in, MessageType.XML.toString(), context);
        Assert.assertEquals(intercepted.getPayload(String.class), "Intercepted!");

        intercepted = interceptor.interceptMessageConstruction(in, MessageType.PLAINTEXT.toString(), context);
        Assert.assertEquals(intercepted.getPayload(String.class), "Hello Citrus!");
    }
}
