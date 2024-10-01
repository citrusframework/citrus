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

package org.citrusframework.camel.actions;

import org.citrusframework.TestActionBuilder;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class CamelActionBuilderTest {

    private CamelActionBuilder fixture;

    @BeforeMethod
    public void beforeMethod() {
        fixture = new CamelActionBuilder();
    }

    @Test
    public void shouldLookupTestActionBuilder() {
        Map<String, TestActionBuilder<?>> endpointBuilders = TestActionBuilder.lookup();
        assertTrue(endpointBuilders.containsKey("camel"));

        assertTrue(TestActionBuilder.lookup("camel").isPresent());
        assertEquals(TestActionBuilder.lookup("camel").get().getClass(), CamelActionBuilder.class);
    }

    @Test
    public void passReferenceResolverToDelegate() {
        var referenceResolverAware = mock(TestReferenceResolver.class);
        setField(fixture, "delegate", referenceResolverAware);

        var referenceResolver = mock(ReferenceResolver.class);
        fixture.setReferenceResolver(referenceResolver);

        verify(referenceResolverAware).setReferenceResolver(referenceResolver);
    }

    @Test
    public void setReferenceResolver_ignoresNonReferenceResolverAware() {
        var referenceResolverAware = mock(TestActionBuilder.class);
        setField(fixture, "delegate", referenceResolverAware);

        fixture.setReferenceResolver(mock(ReferenceResolver.class));

        verifyNoInteractions(referenceResolverAware);
    }

    @Test
    public void setReferenceResolver_ignoresNullReferenceResolver() {
        var referenceResolverAware = mock(TestReferenceResolver.class);
        setField(fixture, "delegate", referenceResolverAware);

        fixture.setReferenceResolver(null);

        verifyNoInteractions(referenceResolverAware);
    }

    private static abstract class TestReferenceResolver implements TestActionBuilder, ReferenceResolverAware {
    }
}
