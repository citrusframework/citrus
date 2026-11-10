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

package org.citrusframework.ws.actions;

import org.citrusframework.TestActionBuilder;
import org.citrusframework.spi.AbstractReferenceResolverAwareTestActionBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class SoapActionBuilderTest {

    private SoapActionBuilder fixture;

    @BeforeEach
    void beforeMethod() {
        fixture = new SoapActionBuilder();
    }

    @Test
    public void shouldLookupTestActionBuilder() {
        Map<String, TestActionBuilder<?>> endpointBuilders = TestActionBuilder.lookup();
        Assertions.assertTrue(endpointBuilders.containsKey("soap"));

        Assertions.assertTrue(TestActionBuilder.lookup("soap").isPresent());
        Assertions.assertEquals(SoapActionBuilder.class, TestActionBuilder.lookup("soap").get().getClass());
    }

    @Test
    public void isReferenceResolverAwareTestActionBuilder() {
        assertTrue(fixture instanceof AbstractReferenceResolverAwareTestActionBuilder<?>, "Is instanceof AbstractReferenceResolverAwareTestActionBuilder");
    }
}
