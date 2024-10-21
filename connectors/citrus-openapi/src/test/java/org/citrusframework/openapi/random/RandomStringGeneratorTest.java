package org.citrusframework.openapi.random;

import io.apicurio.datamodels.openapi.models.OasSchema;
import io.apicurio.datamodels.openapi.v3.models.Oas30Schema;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RandomStringGeneratorTest {

    private RandomStringGenerator generator;
    private RandomContext mockContext;
    private RandomModelBuilder mockBuilder;
    private OasSchema schema;

    @BeforeMethod
    public void setUp() {
        generator = new RandomStringGenerator();
        mockContext = mock(RandomContext.class);
        mockBuilder = mock(RandomModelBuilder.class);
        schema = new Oas30Schema();

        when(mockContext.getRandomModelBuilder()).thenReturn(mockBuilder);
    }

    @Test
    public void testGenerateDefaultLength() {
        generator.generate(mockContext, schema);
        verify(mockBuilder).appendSimpleQuoted("citrus:randomString(10,MIXED,true,1)");
    }

    @Test
    public void testGenerateWithMinLength() {
        schema.minLength = 5;
        generator.generate(mockContext, schema);
        verify(mockBuilder).appendSimpleQuoted("citrus:randomString(10,MIXED,true,5)");
    }

    @Test
    public void testGenerateWithMaxLength() {
        schema.maxLength = 15;
        generator.generate(mockContext, schema);
        verify(mockBuilder).appendSimpleQuoted("citrus:randomString(15,MIXED,true,1)");
    }

    @Test
    public void testGenerateWithMinAndMaxLength() {
        schema.minLength = 3;
        schema.maxLength = 8;
        generator.generate(mockContext, schema);
        verify(mockBuilder).appendSimpleQuoted("citrus:randomString(8,MIXED,true,3)");
    }

    @Test
    public void testGenerateWithZeroMinLength() {
        schema.minLength = 0;
        generator.generate(mockContext, schema);
        verify(mockBuilder).appendSimpleQuoted("citrus:randomString(10,MIXED,true,1)");
    }

    @Test
    public void testGenerateWithZeroMaxLength() {
        schema.maxLength = 0;
        generator.generate(mockContext, schema);
        verify(mockBuilder).appendSimpleQuoted("citrus:randomString(10,MIXED,true,1)");
    }
}
