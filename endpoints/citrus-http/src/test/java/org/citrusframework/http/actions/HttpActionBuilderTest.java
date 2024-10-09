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

package org.citrusframework.http.actions;

import org.citrusframework.TestActionBuilder;
import org.citrusframework.spi.AbstractReferenceResolverAwareTestActionBuilder;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class HttpActionBuilderTest {

    private HttpActionBuilder fixture;

    @BeforeMethod
    public void beforeMethod() {
        fixture = new HttpActionBuilder();
    }

    @Test
    public void shouldLookupTestActionBuilder() {
        Map<String, TestActionBuilder<?>> endpointBuilders = TestActionBuilder.lookup();
        assertTrue(endpointBuilders.containsKey("http"));

        assertTrue(TestActionBuilder.lookup("http").isPresent());
        assertEquals(TestActionBuilder.lookup("http").get().getClass(), HttpActionBuilder.class);
    }

    @Test
    public void isReferenceResolverAwareTestActionBuilder() {
        assertTrue(fixture instanceof AbstractReferenceResolverAwareTestActionBuilder<?>, "Is instanceof AbstractReferenceResolverAwareTestActionBuilder");
    }
}
