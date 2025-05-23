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

package org.citrusframework.message;

import org.citrusframework.UnitTestSupport;
import org.citrusframework.actions.SendMessageAction;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.endpoint.direct.DirectEndpoint;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @since 2.6
 */
public class DefaultMessageStoreTest extends UnitTestSupport {

    private final MessageStore messageStore = new DefaultMessageStore();

    @Test
    public void testStoreAndGetMessage() {
        messageStore.storeMessage("request", new DefaultMessage("RequestMessage"));
        Assert.assertEquals(messageStore.getMessage("request").getPayload(String.class), "RequestMessage");
        Assert.assertNull(messageStore.getMessage("unknown"));
    }

    @Test
    public void testConstructMessageName() {
        Endpoint endpoint = new DirectEndpoint();
        endpoint.setName("testEndpoint");
        Assert.assertEquals(messageStore.constructMessageName(new SendMessageAction.Builder().build(), endpoint), "send(testEndpoint)");
    }
}
