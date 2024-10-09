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

package org.citrusframework.openapi.actions;

import org.citrusframework.TestActionBuilder;
import org.citrusframework.spi.AbstractReferenceResolverAwareTestActionBuilder;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Map;

import static org.testng.Assert.assertTrue;

public class OpenApiActionBuilderTest {

    private OpenApiActionBuilder fixture;

    @BeforeMethod
    public void beforeMethod() {
        fixture = new OpenApiActionBuilder();
    }

    @Test
    public void shouldLookupTestActionBuilder() {
        Map<String, TestActionBuilder<?>> endpointBuilders = TestActionBuilder.lookup();
        Assert.assertTrue(endpointBuilders.containsKey("openapi"));

        Assert.assertTrue(TestActionBuilder.lookup("openapi").isPresent());
        Assert.assertEquals(TestActionBuilder.lookup("openapi").get().getClass(), OpenApiActionBuilder.class);
    }

    @Test
    public void isReferenceResolverAwareTestActionBuilder() {
        assertTrue(fixture instanceof AbstractReferenceResolverAwareTestActionBuilder<?>, "Is instanceof AbstractReferenceResolverAwareTestActionBuilder");
    }
}
