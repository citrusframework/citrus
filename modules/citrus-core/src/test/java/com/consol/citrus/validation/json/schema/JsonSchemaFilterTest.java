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

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.json.JsonSchemaRepository;
import com.consol.citrus.json.schema.SimpleJsonSchema;
import com.consol.citrus.validation.json.JsonMessageValidationContext;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class JsonSchemaFilterTest {

    private JsonSchemaFilter jsonSchemaFilter = new JsonSchemaFilter();

    @Test
    public void testFilterOnSchemaRepositoryName() {

        //GIVEN
        //Setup Schema repositories
        JsonSchemaRepository firstJsonSchemaRepository = new JsonSchemaRepository();
        firstJsonSchemaRepository.setBeanName("schemaRepository1");
        SimpleJsonSchema firstSimpleJsonSchema = mock(SimpleJsonSchema.class);
        firstJsonSchemaRepository.getSchemas().add(firstSimpleJsonSchema);

        JsonSchemaRepository secondJsonSchemaRepository = new JsonSchemaRepository();
        secondJsonSchemaRepository.setBeanName("schemaRepository2");
        SimpleJsonSchema secondSimpleJsonSchema = mock(SimpleJsonSchema.class);
        secondJsonSchemaRepository.getSchemas().add(secondSimpleJsonSchema);
        SimpleJsonSchema thirdSimpleJsonSchema = mock(SimpleJsonSchema.class);
        secondJsonSchemaRepository.getSchemas().add(thirdSimpleJsonSchema);

        List<JsonSchemaRepository> schemaRepositories =
                Arrays.asList(firstJsonSchemaRepository, secondJsonSchemaRepository);

        //Setup validation validationContext
        JsonMessageValidationContext validationContext = new JsonMessageValidationContext();
        validationContext.setSchemaValidation(true);
        validationContext.setSchemaRepository("schemaRepository2");

        //WHEN
        List<SimpleJsonSchema> simpleJsonSchemas =
                jsonSchemaFilter.filter(schemaRepositories, validationContext, mock(ApplicationContext.class));

        //THEN
        Assert.assertEquals(simpleJsonSchemas.size(), 2);
        Assert.assertTrue(simpleJsonSchemas.contains(secondSimpleJsonSchema));
        Assert.assertTrue(simpleJsonSchemas.contains(thirdSimpleJsonSchema));
    }

    @Test
    public void testFilterOnSchemaNameUsesApplicationContext() {

        //GIVEN
        //Setup Schema repositories
        JsonSchemaRepository jsonSchemaRepository = new JsonSchemaRepository();
        jsonSchemaRepository.setBeanName("schemaRepository");
        SimpleJsonSchema jsonSchema = mock(SimpleJsonSchema.class);
        jsonSchemaRepository.getSchemas().add(jsonSchema);

        List<JsonSchemaRepository> schemaRepositories = Collections.singletonList(jsonSchemaRepository);

        //Setup validation validationContext
        JsonMessageValidationContext validationContext = new JsonMessageValidationContext();
        validationContext.setSchemaValidation(true);
        validationContext.setSchema("mySchema");

        //Setup application validationContext
        ApplicationContext applicationContext = mock(ApplicationContext.class);
        when(applicationContext.getBean("mySchema", SimpleJsonSchema.class))
                .thenReturn(mock(SimpleJsonSchema.class));

        //WHEN
        jsonSchemaFilter.filter(schemaRepositories, validationContext, applicationContext);

        //THEN
        verify(applicationContext).getBean(validationContext.getSchema(), SimpleJsonSchema.class);
    }

    @Test
    public void testFilterOnSchemaNameReturnsCorrectSchema() {

        //GIVEN
        //Setup Schema repositories
        JsonSchemaRepository jsonSchemaRepository = new JsonSchemaRepository();
        jsonSchemaRepository.setBeanName("schemaRepository");
        SimpleJsonSchema jsonSchema = mock(SimpleJsonSchema.class);
        jsonSchemaRepository.getSchemas().add(jsonSchema);

        List<JsonSchemaRepository> schemaRepositories = Collections.singletonList(jsonSchemaRepository);

        //Setup validation validationContext
        JsonMessageValidationContext validationContext = new JsonMessageValidationContext();
        validationContext.setSchemaValidation(true);
        validationContext.setSchema("mySchema");

        //Setup expected SimpleJsonSchema
        SimpleJsonSchema expectedSimpleJsonSchema = mock(SimpleJsonSchema.class);

        //Setup application validationContext
        ApplicationContext applicationContext = mock(ApplicationContext.class);
        when(applicationContext.getBean(validationContext.getSchema(), SimpleJsonSchema.class))
                .thenReturn(expectedSimpleJsonSchema);

        //WHEN
        List<SimpleJsonSchema> simpleJsonSchemas =
                jsonSchemaFilter.filter(schemaRepositories, validationContext, applicationContext);

        //THEN
        Assert.assertEquals(simpleJsonSchemas.size(),1);
        Assert.assertEquals(expectedSimpleJsonSchema, simpleJsonSchemas.get(0));
    }

    @Test(expectedExceptions = CitrusRuntimeException.class)
    public void testNoSchemaRepositoryFoundThrowsException() {

        //GIVEN
        //Setup Schema repositories
        JsonSchemaRepository firstJsonSchemaRepository = new JsonSchemaRepository();
        firstJsonSchemaRepository.setBeanName("schemaRepository1");
        SimpleJsonSchema firstSimpleJsonSchema = mock(SimpleJsonSchema.class);
        firstJsonSchemaRepository.getSchemas().add(firstSimpleJsonSchema);

        List<JsonSchemaRepository> schemaRepositories = Collections.singletonList(firstJsonSchemaRepository);

        //Setup validation validationContext
        JsonMessageValidationContext validationContext = new JsonMessageValidationContext();
        validationContext.setSchemaValidation(true);
        validationContext.setSchemaRepository("schemaRepository2");

        //WHEN
        jsonSchemaFilter.filter(schemaRepositories, validationContext, mock(ApplicationContext.class));

        //THEN
        //Exception has been thrown
    }

    @Test(expectedExceptions = CitrusRuntimeException.class)
    public void testNoSchemaFoundThrowsException() {

        //GIVEN
        //Setup Schema repositories
        JsonSchemaRepository firstJsonSchemaRepository = new JsonSchemaRepository();
        firstJsonSchemaRepository.setBeanName("schemaRepository1");
        SimpleJsonSchema firstSimpleJsonSchema = mock(SimpleJsonSchema.class);
        firstJsonSchemaRepository.getSchemas().add(firstSimpleJsonSchema);

        List<JsonSchemaRepository> schemaRepositories = Collections.singletonList(firstJsonSchemaRepository);

        //Setup validation validationContext
        JsonMessageValidationContext validationContext = new JsonMessageValidationContext();
        validationContext.setSchemaValidation(true);
        validationContext.setSchema("foo");


        //Setup application validationContext
        ApplicationContext applicationContext = mock(ApplicationContext.class);
        when(applicationContext.getBean(validationContext.getSchema(), SimpleJsonSchema.class))
                .thenThrow(NoSuchBeanDefinitionException.class);

        //WHEN
        jsonSchemaFilter.filter(schemaRepositories, validationContext, applicationContext);

        //THEN
        //Exception has been thrown
    }

    @Test
    public void testNoFilterReturnAllSchemas() {

        //GIVEN
        //Setup Schema repositories
        JsonSchemaRepository firstJsonSchemaRepository = new JsonSchemaRepository();
        firstJsonSchemaRepository.setBeanName("schemaRepository1");
        SimpleJsonSchema firstSimpleJsonSchema = mock(SimpleJsonSchema.class);
        firstJsonSchemaRepository.getSchemas().add(firstSimpleJsonSchema);

        JsonSchemaRepository secondJsonSchemaRepository = new JsonSchemaRepository();
        secondJsonSchemaRepository.setBeanName("schemaRepository2");
        SimpleJsonSchema secondSimpleJsonSchema = mock(SimpleJsonSchema.class);
        secondJsonSchemaRepository.getSchemas().add(secondSimpleJsonSchema);
        SimpleJsonSchema thirdSimpleJsonSchema = mock(SimpleJsonSchema.class);
        secondJsonSchemaRepository.getSchemas().add(thirdSimpleJsonSchema);

        List<JsonSchemaRepository> schemaRepositories =
                Arrays.asList(firstJsonSchemaRepository, secondJsonSchemaRepository);

        //Setup validation validationContext
        JsonMessageValidationContext validationContext = new JsonMessageValidationContext();
        validationContext.setSchemaValidation(true);

        //WHEN
        List<SimpleJsonSchema> simpleJsonSchemas =
                jsonSchemaFilter.filter(schemaRepositories, validationContext, mock(ApplicationContext.class));

        //THEN
        Assert.assertEquals(simpleJsonSchemas.size(), 3);
        Assert.assertTrue(simpleJsonSchemas.contains(firstSimpleJsonSchema));
        Assert.assertTrue(simpleJsonSchemas.contains(secondSimpleJsonSchema));
        Assert.assertTrue(simpleJsonSchemas.contains(thirdSimpleJsonSchema));
    }
}