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

package org.citrusframework.kafka.message;

import org.testng.annotations.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.MAP;

public class KafkaMessageTest {

    @Test
    public void hasEmptyConstructor() {
        assertThat(new KafkaMessage())
                .isNotNull();
    }

    @Test
    public void hasConstructorWithPayload() {
        var payload = "payload";

        assertThat(new KafkaMessage(payload))
                .hasFieldOrPropertyWithValue("payload", payload);
    }

    @Test
    public void hasConstructorWithPayloadAndHeaders() {
        var payload = "payload";
        Map<String,Object> headers = Map.of("foo", "bar");

        assertThat(new KafkaMessage(payload, headers))
                .hasFieldOrPropertyWithValue("payload", payload)
                .extracting(KafkaMessage::getHeaders)
                .asInstanceOf(MAP)
                .hasEntrySatisfying("foo", e -> assertThat(e).isEqualTo("bar"));
    }
}
