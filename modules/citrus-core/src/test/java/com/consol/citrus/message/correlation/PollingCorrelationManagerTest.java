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

package com.consol.citrus.message.correlation;

import com.consol.citrus.channel.ChannelSyncEndpointConfiguration;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;


public class PollingCorrelationManagerTest {

    private ObjectStore<String> objectStore = Mockito.mock(ObjectStore.class);

    @Test
    public void testFind() throws Exception {
        ChannelSyncEndpointConfiguration pollableEndpointConfiguration = new ChannelSyncEndpointConfiguration();
        pollableEndpointConfiguration.setPollingInterval(100L);
        pollableEndpointConfiguration.setTimeout(500L);

        PollingCorrelationManager<String> correlationManager = new PollingCorrelationManager(pollableEndpointConfiguration, "Try again");
        Assert.assertNull(correlationManager.find(""));

        correlationManager.store("foo", "bar");
        Assert.assertNull(correlationManager.find("bar"));
        Assert.assertEquals(correlationManager.find("foo"), "bar");

        //2nd invocation with same correlation key
        Assert.assertNull(correlationManager.find("foo"));

        for (String key : new String[]{"1", "2", "3", "4", "5"}) {
            correlationManager.store(key, "value" + key);
        }

        for (String key : new String[]{"1", "5", "3", "2", "4"}) {
            Assert.assertEquals(correlationManager.find(key), "value" + key);
            Assert.assertNull(correlationManager.find(key));
        }
    }

    @Test
    public void testFindWithRetry() {
        ChannelSyncEndpointConfiguration pollableEndpointConfiguration = new ChannelSyncEndpointConfiguration();
        pollableEndpointConfiguration.setPollingInterval(100L);
        pollableEndpointConfiguration.setTimeout(500L);

        PollingCorrelationManager<String> correlationManager = new PollingCorrelationManager(pollableEndpointConfiguration, "Try again");
        correlationManager.setObjectStore(objectStore);

        reset(objectStore);
        when(objectStore.remove("foo")).thenReturn(null).thenReturn("bar");
        Assert.assertEquals(correlationManager.find("foo"), "bar");

    }

    @Test
    public void testNotFindWithRetry() {
        ChannelSyncEndpointConfiguration pollableEndpointConfiguration = new ChannelSyncEndpointConfiguration();
        pollableEndpointConfiguration.setPollingInterval(100L);
        pollableEndpointConfiguration.setTimeout(300L);

        PollingCorrelationManager<String> correlationManager = new PollingCorrelationManager(pollableEndpointConfiguration, "Try again");
        correlationManager.setObjectStore(objectStore);

        reset(objectStore);
        when(objectStore.remove("foo")).thenReturn(null);
        Assert.assertNull(correlationManager.find("foo"));

    }
}