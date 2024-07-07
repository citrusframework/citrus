package org.citrusframework.openapi.random;

import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import io.apicurio.datamodels.openapi.models.OasSchema;
import java.util.List;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

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
        mockSchema.enum_ = List.of();

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
