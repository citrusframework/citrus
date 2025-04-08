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

package org.citrusframework.openapi.random;

import java.util.HashMap;
import java.util.Map;

import io.apicurio.datamodels.openapi.models.OasSchema;
import io.apicurio.datamodels.openapi.v3.models.Oas30Schema;
import org.citrusframework.openapi.OpenApiSpecification;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertSame;

public class RandomContextTest {

    private OpenApiSpecification specificationMock;

    private RandomContext randomContext;

    private Map<String, OasSchema> schemaDefinitions;

    @BeforeMethod
    public void setUp() {
        RandomModelBuilder randomModelBuilderMock = mock();
        specificationMock = mock();

        schemaDefinitions = new HashMap<>();

        randomContext = spy(new RandomContext(specificationMock, true));
        ReflectionTestUtils.setField(randomContext, "randomModelBuilder", randomModelBuilderMock);

        doReturn(schemaDefinitions).when(randomContext).getSchemaDefinitions();
    }

    @Test
    public void testGenerateWithResolvedSchema() {
        OasSchema oasSchema = new Oas30Schema();
        randomContext.generate(oasSchema);
        verify(randomContext).doGenerate(oasSchema);
    }

    @Test
    public void testGenerateWithReferencedSchema() {
        OasSchema referencedSchema = new Oas30Schema();
        schemaDefinitions.put("reference", referencedSchema);
        OasSchema oasSchema = new Oas30Schema();
        oasSchema.$ref = "reference";

        randomContext.generate(oasSchema);
        verify(randomContext).doGenerate(referencedSchema);
    }

    @Test
    public void testGetRandomModelBuilder() {
        assertNotNull(randomContext.getRandomModelBuilder());
    }

    @Test
    public void testGetSpecification() {
        assertEquals(randomContext.getSpecification(), specificationMock);
    }

    @Test
    public void testCacheVariable() {
        HashMap<String, String> cachedValue1 = randomContext.get("testKey", k -> new HashMap<>());
        HashMap<String, String> cachedValue2 = randomContext.get("testKey", k -> new HashMap<>());

        assertSame(cachedValue1, cachedValue2);
    }
}
