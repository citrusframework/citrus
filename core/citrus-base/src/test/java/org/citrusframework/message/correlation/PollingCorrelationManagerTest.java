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

package org.citrusframework.message.correlation;

import org.citrusframework.endpoint.direct.DirectSyncEndpointConfiguration;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

public class PollingCorrelationManagerTest {

    private ObjectStore objectStore = Mockito.mock(ObjectStore.class);

    @Test
    public void testFind() {
        DirectSyncEndpointConfiguration pollableEndpointConfiguration = new DirectSyncEndpointConfiguration();
        pollableEndpointConfiguration.setPollingInterval(100L);
        pollableEndpointConfiguration.setTimeout(500L);

        PollingCorrelationManager<String> correlationManager = new PollingCorrelationManager<>(pollableEndpointConfiguration, "Try again");
        assertNull(correlationManager.find(""));

        correlationManager.store("foo", "bar");
        assertNull(correlationManager.find("bar"));
        assertEquals(correlationManager.find("foo"), "bar");

        //2nd invocation with same correlation key
        assertNull(correlationManager.find("foo"));

        for (String key : new String[]{"1", "2", "3", "4", "5"}) {
            correlationManager.store(key, "value" + key);
        }

        for (String key : new String[]{"1", "5", "3", "2", "4"}) {
            assertEquals(correlationManager.find(key), "value" + key);
            assertNull(correlationManager.find(key));
        }
    }

    @Test
    public void testFindWithRetry() {
        DirectSyncEndpointConfiguration pollableEndpointConfiguration = new DirectSyncEndpointConfiguration();
        pollableEndpointConfiguration.setPollingInterval(100L);
        pollableEndpointConfiguration.setTimeout(500L);

        PollingCorrelationManager<String> correlationManager = new PollingCorrelationManager<>(pollableEndpointConfiguration, "Try again");
        correlationManager.setObjectStore(objectStore);

        reset(objectStore);
        when(objectStore.remove("foo")).thenReturn(null).thenReturn("bar");
        assertEquals(correlationManager.find("foo"), "bar");

    }

    @Test
    public void testNotFindWithRetry() {
        DirectSyncEndpointConfiguration pollableEndpointConfiguration = new DirectSyncEndpointConfiguration();
        pollableEndpointConfiguration.setPollingInterval(100L);
        pollableEndpointConfiguration.setTimeout(300L);

        PollingCorrelationManager<String> correlationManager = new PollingCorrelationManager<>(pollableEndpointConfiguration, "Try again");
        correlationManager.setObjectStore(objectStore);

        reset(objectStore);
        when(objectStore.remove("foo")).thenReturn(null);
        assertNull(correlationManager.find("foo"));

    }
}
