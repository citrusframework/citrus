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

package org.citrusframework.kafka.endpoint.selector;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.citrusframework.kafka.endpoint.selector.KafkaMessageByHeaderSelector.HEADER_FILTER_KEY;
import static org.citrusframework.kafka.endpoint.selector.KafkaMessageByHeaderSelector.HEADER_FILTER_VALUE;

public class KafkaMessageSelectorFactoryTest {

    private KafkaMessageSelectorFactory fixture;

    @BeforeMethod
    public void beforeMethodSetup() {
        fixture = new KafkaMessageSelectorFactory();
    }

    @Test
    public void parseFromSelector_throwsException_whenNoIdentifierPresent() {
        var messageSelectors = new HashMap<String, Object>();

        assertThatThrownBy(() -> fixture.parseFromSelector(messageSelectors))
                .isInstanceOf(CitrusRuntimeException.class)
                .hasMessage("Cannot instantiate Kafka matcher from selectors: " + messageSelectors);
    }

    @Test
    public void parseFromSelector_returnsKafkaMessageByHeaderSelector_ifKeyIsPresent() {
        var messageSelectors = Map.of(
                HEADER_FILTER_KEY, "foo");

        var result = fixture.parseFromSelector(messageSelectors);

        assertThat(result)
                .isInstanceOf(KafkaMessageByHeaderSelector.class)
                .isNotNull();
    }

    @Test
    public void parseFromSelector_returnsKafkaMessageByHeaderSelector_ifValueIsPresent() {
        var messageSelectors = Map.of(
                HEADER_FILTER_VALUE, "bar");

        var result = fixture.parseFromSelector(messageSelectors);

        assertThat(result)
                .isInstanceOf(KafkaMessageByHeaderSelector.class)
                .isNotNull();
    }
}
