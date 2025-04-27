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

import java.util.function.BiConsumer;

import io.apicurio.datamodels.openapi.models.OasSchema;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class RandomGeneratorBuilderTest {

    private BiConsumer<RandomContext, OasSchema> consumerMock;
    private RandomContext contextMock;
    private OasSchema schemaMock;

    @BeforeMethod
    public void setUp() {
        consumerMock = mock();
        contextMock = mock();
        schemaMock = mock();
    }

    @Test
    public void testRandomGeneratorBuilderWithTypeAndFormat() {
        String type = "type1";
        String format = "format1";

        RandomGenerator generator = RandomGeneratorBuilder.randomGeneratorBuilder(type, format)
                .build(consumerMock);
        OasSchema schema = (OasSchema) ReflectionTestUtils.getField(generator, "schema");
        assertNotNull(schema);
        assertEquals(schema.type, type);
        assertEquals(schema.format, format);
    }

    @Test
    public void testRandomGeneratorBuilderWithType() {
        String type = "type1";

        RandomGenerator generator = RandomGeneratorBuilder.randomGeneratorBuilder()
                .withType(type)
                .build(consumerMock);
        OasSchema schema = (OasSchema) ReflectionTestUtils.getField(generator, "schema");
        assertNotNull(schema);
        assertEquals(schema.type, type);
    }

    @Test
    public void testRandomGeneratorBuilderWithFormat() {
        String format = "format1";

        RandomGenerator generator = RandomGeneratorBuilder.randomGeneratorBuilder()
                .withFormat(format)
                .build(consumerMock);
        OasSchema schema = (OasSchema) ReflectionTestUtils.getField(generator, "schema");
        assertNotNull(schema);
        assertEquals(schema.format, format);
    }

    @Test
    public void testRandomGeneratorBuilderWithPattern() {
        String pattern = "pattern1";

        RandomGenerator generator = RandomGeneratorBuilder.randomGeneratorBuilder()
                .withPattern(pattern)
                .build(consumerMock);
        OasSchema schema = (OasSchema) ReflectionTestUtils.getField(generator, "schema");
        assertNotNull(schema);
        assertEquals(schema.pattern, pattern);
    }

    @Test
    public void testRandomGeneratorBuilderWithEnum() {
        RandomGenerator generator = RandomGeneratorBuilder.randomGeneratorBuilder()
                .withEnum()
                .build(consumerMock);
        OasSchema schema = (OasSchema) ReflectionTestUtils.getField(generator, "schema");
        assertNotNull(schema);
        assertNotNull(schema.enum_);
        assertTrue(schema.enum_.isEmpty());
    }

    @Test
    public void testBuildGenerator() {
        RandomGenerator generator = RandomGeneratorBuilder.randomGeneratorBuilder()
                .build(consumerMock);

        generator.generateIntoContext(contextMock, schemaMock);

        verify(consumerMock).accept(contextMock, schemaMock);
    }
}
