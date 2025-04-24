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
import io.apicurio.datamodels.openapi.v3.models.Oas30Schema;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class RandomGeneratorTest {

    private RandomGenerator generator;
    private OasSchema mockSchema;

    @BeforeMethod
    public void setUp() {
        mockSchema = mock(OasSchema.class);
        generator = new RandomGenerator(mockSchema) {
            @Override
            void generateIntoContext(RandomContext randomContext, OasSchema schema) {
                // Implementation not needed for this test
            }
        };
    }

    @Test
    public void testHandlesWithMatchingTypeAndFormat() {
        OasSchema otherSchema = new Oas30Schema();
        otherSchema.type = "type1";
        otherSchema.format = "format1";

        mockSchema.type = "type1";
        mockSchema.format = "format1";

        assertTrue(generator.handles(otherSchema));
    }

    @Test
    public void testHandlesWithTypeAny() {
        OasSchema otherSchema = new Oas30Schema();
        otherSchema.type = "type1";
        otherSchema.format = "format1";

        mockSchema.type = RandomGenerator.ANY;
        mockSchema.format = "format1";

        assertTrue(generator.handles(otherSchema));
    }

    @Test
    public void testHandlesWithFormatAny() {
        OasSchema otherSchema = new Oas30Schema();
        otherSchema.type = "type1";
        otherSchema.format = "format1";

        mockSchema.type = "type1";
        mockSchema.format = RandomGenerator.ANY;

        assertTrue(generator.handles(otherSchema));
    }

    @Test
    public void testHandlesWithPatternAny() {
        OasSchema otherSchema = new Oas30Schema();
        otherSchema.type = "type1";
        otherSchema.pattern = "pattern1";

        mockSchema.type = "type1";
        mockSchema.pattern = RandomGenerator.ANY;

        assertTrue(generator.handles(otherSchema));
    }

    @Test
    public void testHandlesWithMatchingPattern() {
        OasSchema otherSchema = new Oas30Schema();
        otherSchema.type = "type1";
        otherSchema.pattern = "pattern1";

        mockSchema.type = "type1";
        mockSchema.pattern = "pattern1";

        assertTrue(generator.handles(otherSchema));
    }

    @Test
    public void testHandlesWithMatchingEnum() {
        OasSchema otherSchema = new Oas30Schema();
        otherSchema.type = "type1";
        otherSchema.enum_ = List.of("value1", "value2");

        mockSchema.type = "type1";
        mockSchema.enum_ = List.of("value1", "value2");

        assertTrue(generator.handles(otherSchema));
    }

    @Test
    public void testHandlesWithNonMatchingType() {
        OasSchema otherSchema = new Oas30Schema();
        otherSchema.type = "type2";
        otherSchema.format = "format1";

        mockSchema.type = "type1";
        mockSchema.format = "format1";

        assertFalse(generator.handles(otherSchema));
    }

    @Test
    public void testHandlesWithNonMatchingFormat() {
        OasSchema otherSchema = new Oas30Schema();
        otherSchema.type = "type1";
        otherSchema.format = "format2";

        mockSchema.type = "type1";
        mockSchema.format = "format1";

        assertFalse(generator.handles(otherSchema));
    }

    @Test
    public void testHandlesWithNullSchema() {
        assertFalse(generator.handles(null));
    }

    @Test
    public void testHandlesWithNullGeneratorSchema() {
        RandomGenerator generatorWithNullSchema = new RandomGenerator() {
            @Override
            void generateIntoContext(RandomContext randomContext, OasSchema schema) {
                // Do nothing
            }
        };

        assertFalse(generatorWithNullSchema.handles(mockSchema));
    }

    @Test
    public void testNullGenerator() {
        RandomContext mockContext = mock(RandomContext.class);

        RandomGenerator.NOOP_RANDOM_GENERATOR.generateIntoContext(mockContext, mockSchema);

        verify(mockContext, never()).getRandomModelBuilder();
    }
}
