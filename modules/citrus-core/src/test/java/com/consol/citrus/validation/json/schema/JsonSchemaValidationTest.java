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

package com.consol.citrus.validation.json.schema;

import com.consol.citrus.json.JsonSchemaRepository;
import com.consol.citrus.json.schema.SimpleJsonSchema;
import com.consol.citrus.message.DefaultMessage;
import com.consol.citrus.message.Message;
import com.consol.citrus.validation.json.JsonMessageValidationContext;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class JsonSchemaValidationTest {

    private ApplicationContext applicationContextMock = mock(ApplicationContext.class);
    private JsonMessageValidationContext validationContextMock = mock(JsonMessageValidationContext.class);
    private JsonSchemaFilter jsonSchemaFilterMock = mock(JsonSchemaFilter.class);
    private JsonSchemaValidation validator = new JsonSchemaValidation(jsonSchemaFilterMock);

    @Test
    public void testValidJsonMessageSuccessfullyValidated() throws Exception {

        //GIVEN
        //Setup json schema repositories
        JsonSchemaRepository jsonSchemaRepository = new JsonSchemaRepository();
        jsonSchemaRepository.setBeanName("schemaRepository1");
        Resource schemaResource = new ClassPathResource("com/consol/citrus/validation/ProductsSchema.json");
        SimpleJsonSchema schema = new SimpleJsonSchema(schemaResource);
        schema.afterPropertiesSet();
        jsonSchemaRepository.getSchemas().add(schema);

        //Add json schema repositories to a list
        List<JsonSchemaRepository> schemaRepositories = Collections.singletonList(jsonSchemaRepository);

        //Mock the filter behavior
        when(jsonSchemaFilterMock.filter(schemaRepositories,  validationContextMock, applicationContextMock))
                .thenReturn(Collections.singletonList(schema));

        //Create the received message
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


        //WHEN
        ProcessingReport report = validator.validate(
                receivedMessage,
                schemaRepositories,
                validationContextMock,
                applicationContextMock);


        //THEN
        Assert.assertTrue(report.isSuccess());
    }

    @Test
    public void testInvalidJsonMessageValidationIsNotSuccessful() throws Exception {

        //GIVEN
        //Setup json schema repositories
        JsonSchemaRepository jsonSchemaRepository = new JsonSchemaRepository();
        jsonSchemaRepository.setBeanName("schemaRepository1");
        Resource schemaResource = new ClassPathResource("com/consol/citrus/validation/ProductsSchema.json");
        SimpleJsonSchema schema = new SimpleJsonSchema(schemaResource);
        schema.afterPropertiesSet();
        jsonSchemaRepository.getSchemas().add(schema);

        //Add json schema repositories to a list
        List<JsonSchemaRepository> schemaRepositories = Collections.singletonList(jsonSchemaRepository);

        //Mock the filter behavior
        when(jsonSchemaFilterMock.filter(schemaRepositories,  validationContextMock, applicationContextMock))
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


        //WHEN
        ProcessingReport report = validator.validate(
                receivedMessage,
                schemaRepositories,
                validationContextMock,
                applicationContextMock);


        //THEN
        Assert.assertFalse(report.isSuccess());
    }

    @Test
    public void testValidationIsSuccessfulIfOneSchemaMatches() throws Exception {

        //GIVEN
        JsonSchemaRepository jsonSchemaRepository = new JsonSchemaRepository();
        jsonSchemaRepository.setBeanName("schemaRepository1");

        Resource schemaResource = new ClassPathResource("com/consol/citrus/validation/BookSchema.json");
        SimpleJsonSchema schema = new SimpleJsonSchema(schemaResource);
        schema.afterPropertiesSet();
        jsonSchemaRepository.getSchemas().add(schema);

        schemaResource = new ClassPathResource("com/consol/citrus/validation/ProductsSchema.json");
        schema = new SimpleJsonSchema(schemaResource);
        schema.afterPropertiesSet();
        jsonSchemaRepository.getSchemas().add(schema);

        //Add json schema repositories to a list
        List<JsonSchemaRepository> schemaRepositories = Collections.singletonList(jsonSchemaRepository);

        //Mock the filter behavior
        when(jsonSchemaFilterMock.filter(schemaRepositories,  validationContextMock, applicationContextMock))
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


        //WHEN
        ProcessingReport report = validator.validate(
                receivedMessage,
                schemaRepositories,
                validationContextMock,
                applicationContextMock);


        //THEN
        Assert.assertTrue(report.isSuccess());
    }

    @Test
    public void testValidationOfJsonSchemaRepositoryList() throws Exception {

        //GIVEN
        List<JsonSchemaRepository> repositoryList = new LinkedList<>();

        //Setup Repository 1 - does not contain the valid schema
        JsonSchemaRepository jsonSchemaRepository = new JsonSchemaRepository();
        jsonSchemaRepository.setBeanName("schemaRepository1");

        Resource schemaResource = new ClassPathResource("com/consol/citrus/validation/BookSchema.json");
        SimpleJsonSchema schema = new SimpleJsonSchema(schemaResource);
        schema.afterPropertiesSet();
        jsonSchemaRepository.getSchemas().add(schema);
        repositoryList.add(jsonSchemaRepository);

        //Setup Repository 2 - contains the valid schema
        jsonSchemaRepository = new JsonSchemaRepository();
        jsonSchemaRepository.setBeanName("schemaRepository2");

        schemaResource = new ClassPathResource("com/consol/citrus/validation/ProductsSchema.json");
        schema = new SimpleJsonSchema(schemaResource);
        schema.afterPropertiesSet();
        jsonSchemaRepository.getSchemas().add(schema);
        repositoryList.add(jsonSchemaRepository);

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


        //WHEN
        ProcessingReport report = validator.validate(
                receivedMessage,
                repositoryList,
                validationContextMock,
                applicationContextMock);


        //THEN
        Assert.assertTrue(report.isSuccess());
    }

    @Test
    public void testJsonSchemaFilterIsCalled() {

        //GIVEN
        List<JsonSchemaRepository> repositoryList = Collections.singletonList(mock(JsonSchemaRepository.class));
        Message message = mock(Message.class);
        JsonMessageValidationContext jsonMessageValidationContext = mock(JsonMessageValidationContext.class);
        ApplicationContext applicationContext = mock(ApplicationContext.class);

        //WHEN
        validator.validate(message, repositoryList, jsonMessageValidationContext, applicationContext);

        //THEN
        verify(jsonSchemaFilterMock).filter(repositoryList, jsonMessageValidationContext, applicationContext);
    }

}