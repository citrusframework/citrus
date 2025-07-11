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

import io.apicurio.datamodels.openapi.models.OasSchema;
import io.apicurio.datamodels.openapi.v3.models.Oas30Schema;
import org.citrusframework.openapi.OpenApiConstants;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RandomArrayGeneratorTest {

    private RandomArrayGenerator generator;
    private RandomContext mockContext;
    private RandomModelBuilder builderSpy;

    @BeforeMethod
    public void setUp() {
        generator = new RandomArrayGenerator();
        mockContext = mock();

        builderSpy = spy(new RandomModelBuilder(true));

        when(mockContext.getRandomModelBuilder()).thenReturn(builderSpy);
    }

    @Test
    public void testGenerateArrayWithDefaultItems() {
        Oas30Schema schema = new Oas30Schema();
        schema.type = OpenApiConstants.TYPE_ARRAY;

        Oas30Schema itemsSchema = new Oas30Schema();
        itemsSchema.type = OpenApiConstants.TYPE_STRING;
        schema.items = itemsSchema;

        generator.generateIntoContext(mockContext, schema);

        verify(builderSpy, atLeastOnce()).array(any());
        verify(mockContext, atLeastOnce()).generate(any(OasSchema.class));
    }

    @Test
    public void testGenerateArrayWithMinItems() {
        Oas30Schema schema = new Oas30Schema();
        schema.type = OpenApiConstants.TYPE_ARRAY;
        schema.minItems = 5;

        Oas30Schema itemsSchema = new Oas30Schema();
        itemsSchema.type = OpenApiConstants.TYPE_STRING;
        schema.items = itemsSchema;

        generator.generateIntoContext(mockContext, schema);

        verify(builderSpy, atLeastOnce()).array(any());
        verify(mockContext, atLeast(5)).generate(any(OasSchema.class));
    }

    @Test
    public void testGenerateArrayWithMaxItems() {
        Oas30Schema schema = new Oas30Schema();
        schema.type = OpenApiConstants.TYPE_ARRAY;
        schema.maxItems = 3;

        Oas30Schema itemsSchema = new Oas30Schema();
        itemsSchema.type = OpenApiConstants.TYPE_STRING;
        schema.items = itemsSchema;

        generator.generateIntoContext(mockContext, schema);

        verify(builderSpy, atLeastOnce()).array(any());
        verify(mockContext, atMost(3)).generate(any(OasSchema.class));
    }

    @Test
    public void testGenerateArrayWithMinMaxItems() {
        Oas30Schema schema = new Oas30Schema();
        schema.type = OpenApiConstants.TYPE_ARRAY;
        schema.minItems = 2;
        schema.maxItems = 5;

        Oas30Schema itemsSchema = new Oas30Schema();
        itemsSchema.type = OpenApiConstants.TYPE_STRING;
        schema.items = itemsSchema;

        generator.generateIntoContext(mockContext, schema);

        verify(builderSpy, atLeastOnce()).array(any());
        verify(mockContext, atLeast(2)).generate(any(OasSchema.class));
        verify(mockContext, atMost(5)).generate(any(OasSchema.class));
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testGenerateArrayWithUnsupportedItems() {
        Oas30Schema schema = new Oas30Schema();
        schema.items = new Object(); // Unsupported items type

        generator.generateIntoContext(mockContext, schema);
    }
}
