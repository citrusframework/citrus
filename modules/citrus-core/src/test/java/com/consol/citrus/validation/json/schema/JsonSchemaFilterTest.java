package com.consol.citrus.validation.json.schema;

import com.consol.citrus.json.JsonSchemaRepository;
import com.consol.citrus.json.schema.SimpleJsonSchema;
import com.consol.citrus.validation.json.JsonMessageValidationContext;
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
    public void testFilterOnSchemaRepositoryName(){

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
        Assert.assertEquals(2, simpleJsonSchemas.size());
        Assert.assertTrue(simpleJsonSchemas.contains(secondSimpleJsonSchema));
        Assert.assertTrue(simpleJsonSchemas.contains(thirdSimpleJsonSchema));
    }

    @Test
    public void testFilterOnSchemaNameUsesApplicationContext(){

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


        //WHEN
        jsonSchemaFilter.filter(schemaRepositories, validationContext, applicationContext);


        //THEN
        verify(applicationContext).getBean(validationContext.getSchema(), SimpleJsonSchema.class);
    }

    @Test
    public void testFilterOnSchemaNameReturnsCorrectSchema(){

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
        Assert.assertEquals(1, simpleJsonSchemas.size());
        Assert.assertEquals(expectedSimpleJsonSchema, simpleJsonSchemas.get(0));
    }

    @Test
    public void testNoSchemaRepositoryFoundReturnsEmptyList(){

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
        List<SimpleJsonSchema> simpleJsonSchemas =
                jsonSchemaFilter.filter(schemaRepositories, validationContext, mock(ApplicationContext.class));


        //THEN
        Assert.assertTrue(simpleJsonSchemas.isEmpty());
    }

    @Test
    public void testNoSchemaFoundReturnsEmptyList(){

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


        //WHEN
        List<SimpleJsonSchema> simpleJsonSchemas =
                jsonSchemaFilter.filter(schemaRepositories, validationContext, mock(ApplicationContext.class));


        //THEN
        Assert.assertTrue(simpleJsonSchemas.isEmpty());
    }
}