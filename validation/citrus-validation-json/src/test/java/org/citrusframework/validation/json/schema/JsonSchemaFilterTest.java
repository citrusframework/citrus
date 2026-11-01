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

package org.citrusframework.validation.json.schema;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.json.JsonSchemaRepository;
import org.citrusframework.json.schema.SimpleJsonSchema;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.validation.context.json.JsonMessageValidationContext;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class JsonSchemaFilterTest {

    private JsonSchemaFilter jsonSchemaFilter = new JsonSchemaFilter();

    @Mock
    private ReferenceResolver referenceResolverMock;

    @Test
    public void testFilterOnSchemaRepositoryName() {

        //GIVEN
        //Setup Schema repositories
        JsonSchemaRepository firstJsonSchemaRepository = new JsonSchemaRepository();
        firstJsonSchemaRepository.setName("schemaRepository1");
        SimpleJsonSchema firstSimpleJsonSchema = mock(SimpleJsonSchema.class);
        firstJsonSchemaRepository.getSchemas().add(firstSimpleJsonSchema);

        JsonSchemaRepository secondJsonSchemaRepository = new JsonSchemaRepository();
        secondJsonSchemaRepository.setName("schemaRepository2");
        SimpleJsonSchema secondSimpleJsonSchema = mock(SimpleJsonSchema.class);
        secondJsonSchemaRepository.getSchemas().add(secondSimpleJsonSchema);
        SimpleJsonSchema thirdSimpleJsonSchema = mock(SimpleJsonSchema.class);
        secondJsonSchemaRepository.getSchemas().add(thirdSimpleJsonSchema);

        List<JsonSchemaRepository> schemaRepositories =
                Arrays.asList(firstJsonSchemaRepository, secondJsonSchemaRepository);

        //Setup validation validationContext
        JsonMessageValidationContext validationContext = new JsonMessageValidationContext.Builder()
                .schemaValidation(true)
                .schemaRepository("schemaRepository2")
                .build();

        //WHEN
        List<SimpleJsonSchema> simpleJsonSchemas =
                jsonSchemaFilter.filter(schemaRepositories, validationContext, referenceResolverMock);

        //THEN
        Assertions.assertEquals(2, simpleJsonSchemas.size());
        Assertions.assertTrue(simpleJsonSchemas.contains(secondSimpleJsonSchema));
        Assertions.assertTrue(simpleJsonSchemas.contains(thirdSimpleJsonSchema));
    }

    @Test
    public void testFilterOnSchemaNameUsesApplicationContext() {

        //GIVEN
        //Setup Schema repositories
        JsonSchemaRepository jsonSchemaRepository = new JsonSchemaRepository();
        jsonSchemaRepository.setName("schemaRepository");
        SimpleJsonSchema jsonSchema = mock(SimpleJsonSchema.class);
        jsonSchemaRepository.getSchemas().add(jsonSchema);

        List<JsonSchemaRepository> schemaRepositories = Collections.singletonList(jsonSchemaRepository);

        //Setup validation validationContext
        JsonMessageValidationContext validationContext = new JsonMessageValidationContext.Builder()
                .schemaValidation(true)
                .schema("mySchema")
                .build();

        //Setup application validationContext
        when(referenceResolverMock.resolve("mySchema", SimpleJsonSchema.class))
                .thenReturn(mock(SimpleJsonSchema.class));

        //WHEN
        jsonSchemaFilter.filter(schemaRepositories, validationContext, referenceResolverMock);

        //THEN
        verify(referenceResolverMock).resolve(validationContext.getSchema(), SimpleJsonSchema.class);
    }

    @Test
    public void testFilterOnSchemaNameReturnsCorrectSchema() {

        //GIVEN
        //Setup Schema repositories
        JsonSchemaRepository jsonSchemaRepository = new JsonSchemaRepository();
        jsonSchemaRepository.setName("schemaRepository");
        SimpleJsonSchema jsonSchema = mock(SimpleJsonSchema.class);
        jsonSchemaRepository.getSchemas().add(jsonSchema);

        List<JsonSchemaRepository> schemaRepositories = Collections.singletonList(jsonSchemaRepository);

        //Setup validation validationContext
        JsonMessageValidationContext validationContext = new JsonMessageValidationContext.Builder()
                .schemaValidation(true)
                .schema("mySchema")
                .build();

        //Setup expected SimpleJsonSchema
        SimpleJsonSchema expectedSimpleJsonSchema = mock(SimpleJsonSchema.class);

        //Setup application validationContext
        when(referenceResolverMock.resolve(validationContext.getSchema(), SimpleJsonSchema.class))
                .thenReturn(expectedSimpleJsonSchema);

        //WHEN
        List<SimpleJsonSchema> simpleJsonSchemas =
                jsonSchemaFilter.filter(schemaRepositories, validationContext, referenceResolverMock);

        //THEN
        Assertions.assertEquals(1,simpleJsonSchemas.size());
        Assertions.assertEquals(simpleJsonSchemas.get(0), expectedSimpleJsonSchema);
    }

    @Test
    public void testNoSchemaRepositoryFoundThrowsException() {

        //GIVEN
        //Setup Schema repositories
        JsonSchemaRepository firstJsonSchemaRepository = new JsonSchemaRepository();
        firstJsonSchemaRepository.setName("schemaRepository1");
        SimpleJsonSchema firstSimpleJsonSchema = mock(SimpleJsonSchema.class);
        firstJsonSchemaRepository.getSchemas().add(firstSimpleJsonSchema);

        List<JsonSchemaRepository> schemaRepositories = Collections.singletonList(firstJsonSchemaRepository);

        //Setup validation validationContext
        JsonMessageValidationContext validationContext = new JsonMessageValidationContext.Builder()
                .schemaValidation(true)
                .schemaRepository("schemaRepository2")
                .build();

        //WHEN
        Assertions.assertThrows(CitrusRuntimeException.class, () -> jsonSchemaFilter.filter(schemaRepositories, validationContext, referenceResolverMock));

        //THEN
        //Exception has been thrown
    }

    @Test
    public void testNoSchemaFoundThrowsException() {

        //GIVEN
        //Setup Schema repositories
        JsonSchemaRepository firstJsonSchemaRepository = new JsonSchemaRepository();
        firstJsonSchemaRepository.setName("schemaRepository1");
        SimpleJsonSchema firstSimpleJsonSchema = mock(SimpleJsonSchema.class);
        firstJsonSchemaRepository.getSchemas().add(firstSimpleJsonSchema);

        List<JsonSchemaRepository> schemaRepositories = Collections.singletonList(firstJsonSchemaRepository);

        //Setup validation validationContext
        JsonMessageValidationContext validationContext = new JsonMessageValidationContext.Builder()
                .schemaValidation(true)
                .schema("foo")
                .build();

        //Setup application validationContext
        when(referenceResolverMock.resolve(validationContext.getSchema(), SimpleJsonSchema.class))
                .thenThrow(CitrusRuntimeException.class);

        //WHEN
        Assertions.assertThrows(CitrusRuntimeException.class, () -> jsonSchemaFilter.filter(schemaRepositories, validationContext, referenceResolverMock));

        //THEN
        //Exception has been thrown
    }

    @Test
    public void testNoFilterReturnAllSchemas() {

        //GIVEN
        //Setup Schema repositories
        JsonSchemaRepository firstJsonSchemaRepository = new JsonSchemaRepository();
        firstJsonSchemaRepository.setName("schemaRepository1");
        SimpleJsonSchema firstSimpleJsonSchema = mock(SimpleJsonSchema.class);
        firstJsonSchemaRepository.getSchemas().add(firstSimpleJsonSchema);

        JsonSchemaRepository secondJsonSchemaRepository = new JsonSchemaRepository();
        secondJsonSchemaRepository.setName("schemaRepository2");
        SimpleJsonSchema secondSimpleJsonSchema = mock(SimpleJsonSchema.class);
        secondJsonSchemaRepository.getSchemas().add(secondSimpleJsonSchema);
        SimpleJsonSchema thirdSimpleJsonSchema = mock(SimpleJsonSchema.class);
        secondJsonSchemaRepository.getSchemas().add(thirdSimpleJsonSchema);

        List<JsonSchemaRepository> schemaRepositories =
                Arrays.asList(firstJsonSchemaRepository, secondJsonSchemaRepository);

        //Setup validation validationContext
        JsonMessageValidationContext validationContext = new JsonMessageValidationContext.Builder()
                .schemaValidation(true)
                .build();

        //WHEN
        List<SimpleJsonSchema> simpleJsonSchemas =
                jsonSchemaFilter.filter(schemaRepositories, validationContext, referenceResolverMock);

        //THEN
        Assertions.assertEquals(3, simpleJsonSchemas.size());
        Assertions.assertTrue(simpleJsonSchemas.contains(firstSimpleJsonSchema));
        Assertions.assertTrue(simpleJsonSchemas.contains(secondSimpleJsonSchema));
        Assertions.assertTrue(simpleJsonSchemas.contains(thirdSimpleJsonSchema));
    }
}
