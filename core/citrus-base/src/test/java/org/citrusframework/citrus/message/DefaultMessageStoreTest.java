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

package org.citrusframework.citrus.message;

import org.citrusframework.citrus.UnitTestSupport;
import org.citrusframework.citrus.actions.SendMessageAction;
import org.citrusframework.citrus.endpoint.Endpoint;
import org.citrusframework.citrus.endpoint.direct.DirectEndpoint;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 * @since 2.6
 */
public class DefaultMessageStoreTest extends UnitTestSupport {

    private final MessageStore messageStore = new DefaultMessageStore();

    @Test
    public void testStoreAndGetMessage() throws Exception {
        messageStore.storeMessage("request", new DefaultMessage("RequestMessage"));
        Assert.assertEquals(messageStore.getMessage("request").getPayload(String.class), "RequestMessage");
        Assert.assertNull(messageStore.getMessage("unknown"));
    }

    @Test
    public void testConstructMessageName() throws Exception {
        Endpoint endpoint = new DirectEndpoint();
        endpoint.setName("testEndpoint");
        Assert.assertEquals(messageStore.constructMessageName(new SendMessageAction.Builder().build(), endpoint), "send(testEndpoint)");
    }

}
