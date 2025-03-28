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

import java.util.List;

import io.apicurio.datamodels.openapi.models.OasSchema;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static java.util.Collections.emptyList;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class RandomEnumGeneratorTest {

    private RandomEnumGenerator generator;
    private RandomContext mockContext;
    private RandomModelBuilder mockBuilder;
    private OasSchema mockSchema;

    @BeforeMethod
    public void setUp() {
        generator = new RandomEnumGenerator();
        mockContext = mock(RandomContext.class);
        mockBuilder = mock(RandomModelBuilder.class);
        mockSchema = mock(OasSchema.class);

        when(mockContext.getRandomModelBuilder()).thenReturn(mockBuilder);
    }

    @Test
    public void testHandlesWithEnum() {
        mockSchema.enum_ = List.of("value1", "value2", "value3");

        boolean result = generator.handles(mockSchema);

        assertTrue(result);
    }

    @Test
    public void testHandlesWithoutEnum() {
        mockSchema.enum_ = null;

        boolean result = generator.handles(mockSchema);

        assertFalse(result);
    }

    @Test
    public void testGenerateWithEnum() {
        mockSchema.enum_ = List.of("value1", "value2", "value3");

        generator.generate(mockContext, mockSchema);

        verify(mockBuilder).appendSimpleQuoted("citrus:randomEnumValue('value1','value2','value3')");
    }

    @Test
    public void testGenerateWithEmptyEnum() {
        mockSchema.enum_ = emptyList();

        generator.generate(mockContext, mockSchema);

        verify(mockBuilder).appendSimpleQuoted("citrus:randomEnumValue()");
    }

    @Test
    public void testGenerateWithNullEnum() {
        mockSchema.enum_ = null;

        generator.generate(mockContext, mockSchema);

        verify(mockBuilder, never()).appendSimpleQuoted(anyString());
    }
}
