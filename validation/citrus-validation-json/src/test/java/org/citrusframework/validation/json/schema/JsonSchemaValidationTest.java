/*
 * Copyright 2006-2018 the original author or authors.
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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.citrusframework.json.JsonSchemaRepository;
import org.citrusframework.json.schema.SimpleJsonSchema;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.Resource;
import org.citrusframework.spi.Resources;
import org.citrusframework.validation.SchemaValidator;
import org.citrusframework.validation.context.SchemaValidationContext;
import org.citrusframework.validation.json.JsonMessageValidationContext;
import org.citrusframework.validation.json.report.GraciousProcessingReport;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class JsonSchemaValidationTest {

    @Mock
    private ReferenceResolver referenceResolverMock;

    @Mock
    private JsonMessageValidationContext validationContextMock;

    @Mock
    private JsonSchemaFilter jsonSchemaFilterMock;

    private JsonSchemaValidation fixture;

    @BeforeMethod
    void beforeMethodSetup() {
        MockitoAnnotations.openMocks(this);
        fixture = new JsonSchemaValidation(jsonSchemaFilterMock);
    }

    @Test
    public void testValidJsonMessageSuccessfullyValidated() {
        // Setup json schema repositories
        JsonSchemaRepository jsonSchemaRepository = new JsonSchemaRepository();
        jsonSchemaRepository.setName("schemaRepository1");
        Resource schemaResource = Resources.fromClasspath("org/citrusframework/validation/ProductsSchema.json");
        SimpleJsonSchema schema = new SimpleJsonSchema(schemaResource);
        schema.initialize();
        jsonSchemaRepository.getSchemas().add(schema);

        // Add json schema repositories to a list
        List<JsonSchemaRepository> schemaRepositories = Collections.singletonList(jsonSchemaRepository);

        // Mock the filter behavior
        when(jsonSchemaFilterMock.filter(schemaRepositories,  validationContextMock, referenceResolverMock))
                .thenReturn(Collections.singletonList(schema));

        // Create the received message
        Message receivedMessage = new DefaultMessage("[\n" +
                "              {\n" +
                "                \"id\": 2,\n" +
                "                \"name\": \"An ice sculpture\",\n" +
                "                \"price\": 12.50,\n" +
                "                \"tags\": [\"cold\", \"ice\"],\n" +
                "                \"dimensions\": {\n" +
                    "                \"length\": 7.0,\n" +
                    "                \"width\": 12.0,\n" +
                    "                \"height\": 9.5\n" +
                "                 }\n" +
                "              }\n" +
                "            ]");


        GraciousProcessingReport report = fixture.validate(
                receivedMessage,
                schemaRepositories,
                validationContextMock,
                referenceResolverMock);

        assertTrue(report.isSuccess());
        assertEquals(0, report.getValidationMessages().size());
    }

    @Test
    public void testInvalidJsonMessageValidationIsNotSuccessful() {
        // Setup json schema repositories
        JsonSchemaRepository jsonSchemaRepository = new JsonSchemaRepository();
        jsonSchemaRepository.setName("schemaRepository1");
        Resource schemaResource = Resources.fromClasspath("org/citrusframework/validation/ProductsSchema.json");
        SimpleJsonSchema schema = new SimpleJsonSchema(schemaResource);
        schema.initialize();
        jsonSchemaRepository.getSchemas().add(schema);

        // Add json schema repositories to a list
        List<JsonSchemaRepository> schemaRepositories = Collections.singletonList(jsonSchemaRepository);

        // Mock the filter behavior
        when(jsonSchemaFilterMock.filter(schemaRepositories,  validationContextMock, referenceResolverMock))
                .thenReturn(Collections.singletonList(schema));

        Message receivedMessage = new DefaultMessage("[\n" +
                "              {\n" +
                "                \"name\": \"An ice sculpture\",\n" +
                "                \"price\": 12.50,\n" +
                "                \"tags\": [\"cold\", \"ice\"],\n" +
                "                \"dimensions\": {\n" +
                    "                \"length\": 7.0,\n" +
                    "                \"width\": 12.0,\n" +
                    "                \"height\": 9.5\n" +
                "                 }\n" +
                "              }\n" +
                "            ]");

        GraciousProcessingReport report = fixture.validate(
                receivedMessage,
                schemaRepositories,
                validationContextMock,
                referenceResolverMock);

        assertFalse(report.isSuccess());
        assertEquals(1, report.getValidationMessages().size());
    }

    @Test
    public void testValidationIsSuccessfulIfOneSchemaMatches() {
        // Setup json schema repositories
        JsonSchemaRepository jsonSchemaRepository = new JsonSchemaRepository();
        jsonSchemaRepository.setName("schemaRepository1");

        Resource schemaResource = Resources.fromClasspath("org/citrusframework/validation/BookSchema.json");
        SimpleJsonSchema schema = new SimpleJsonSchema(schemaResource);
        schema.initialize();
        jsonSchemaRepository.getSchemas().add(schema);

        schemaResource = Resources.fromClasspath("org/citrusframework/validation/ProductsSchema.json");
        schema = new SimpleJsonSchema(schemaResource);
        schema.initialize();
        jsonSchemaRepository.getSchemas().add(schema);

        // Add json schema repositories to a list
        List<JsonSchemaRepository> schemaRepositories = Collections.singletonList(jsonSchemaRepository);

        // Mock the filter behavior
        when(jsonSchemaFilterMock.filter(schemaRepositories,  validationContextMock, referenceResolverMock))
                .thenReturn(Collections.singletonList(schema));

        Message receivedMessage = new DefaultMessage("[\n" +
                "              {\n" +
                "                \"id\": 2,\n" +
                "                \"name\": \"An ice sculpture\",\n" +
                "                \"price\": 12.50,\n" +
                "                \"tags\": [\"cold\", \"ice\"],\n" +
                "                \"dimensions\": {\n" +
                    "                \"length\": 7.0,\n" +
                    "                \"width\": 12.0,\n" +
                    "                \"height\": 9.5\n" +
                "                 }\n" +
                "              }\n" +
                "            ]");

        GraciousProcessingReport report = fixture.validate(
                receivedMessage,
                schemaRepositories,
                validationContextMock,
                referenceResolverMock);

        assertTrue(report.isSuccess());
        assertEquals(0, report.getValidationMessages().size());
    }

    @Test
    public void testValidationIsSuccessfulIfOneSchemaMatchesWithRepositoryMerge() {
        List<JsonSchemaRepository> repositoryList = new LinkedList<>();

        // Setup Repository 1 - does not contain the valid schema
        JsonSchemaRepository jsonSchemaRepository = new JsonSchemaRepository();
        jsonSchemaRepository.setName("schemaRepository1");

        Resource schemaResource = Resources.fromClasspath("org/citrusframework/validation/BookSchema.json");
        SimpleJsonSchema invalidSchema = new SimpleJsonSchema(schemaResource);
        invalidSchema.initialize();
        jsonSchemaRepository.getSchemas().add(invalidSchema);
        repositoryList.add(jsonSchemaRepository);

        // Setup Repository 2 - contains the valid schema
        jsonSchemaRepository = new JsonSchemaRepository();
        jsonSchemaRepository.setName("schemaRepository2");

        schemaResource = Resources.fromClasspath("org/citrusframework/validation/ProductsSchema.json");
        SimpleJsonSchema validSchema = new SimpleJsonSchema(schemaResource);
        validSchema.initialize();
        jsonSchemaRepository.getSchemas().add(validSchema);
        repositoryList.add(jsonSchemaRepository);

        // Mock the filter behavior
        when(jsonSchemaFilterMock.filter(repositoryList,  validationContextMock, referenceResolverMock))
                .thenReturn(List.of(invalidSchema, validSchema));

        Message receivedMessage = new DefaultMessage("[\n" +
                "              {\n" +
                "                \"id\": 2,\n" +
                "                \"name\": \"An ice sculpture\",\n" +
                "                \"price\": 12.50,\n" +
                "                \"tags\": [\"cold\", \"ice\"],\n" +
                "                \"dimensions\": {\n" +
                    "                \"length\": 7.0,\n" +
                    "                \"width\": 12.0,\n" +
                    "                \"height\": 9.5\n" +
                "                 }\n" +
                "              }\n" +
                "            ]");

        GraciousProcessingReport report = fixture.validate(
                receivedMessage,
                repositoryList,
                validationContextMock,
                referenceResolverMock);

        assertTrue(report.isSuccess());
        assertEquals(1, report.getValidationMessages().size());
    }

    @Test
    public void testJsonSchemaFilterIsCalled() {
        List<JsonSchemaRepository> repositoryList = Collections.singletonList(mock(JsonSchemaRepository.class));
        Message message = mock(Message.class);
        JsonMessageValidationContext jsonMessageValidationContext = mock(JsonMessageValidationContext.class);

        fixture.validate(message, repositoryList, jsonMessageValidationContext, referenceResolverMock);

        verify(jsonSchemaFilterMock).filter(repositoryList, jsonMessageValidationContext, referenceResolverMock);
    }

    @Test
    public void testLookup() {
        Map<String, SchemaValidator<? extends SchemaValidationContext>> validators = SchemaValidator.lookup();
        assertEquals(validators.size(), 1L);
        assertNotNull(validators.get("defaultJsonSchemaValidator"));
        assertEquals(validators.get("defaultJsonSchemaValidator").getClass(), JsonSchemaValidation.class);
    }

    @Test
    public void testTestLookup() {
        assertTrue(SchemaValidator.lookup("json").isPresent());
    }
}
