package org.citrusframework.openapi.random;

import io.apicurio.datamodels.openapi.models.OasSchema;
import io.apicurio.datamodels.openapi.v3.models.Oas30Schema;
import org.citrusframework.openapi.OpenApiConstants;
import org.citrusframework.openapi.OpenApiSpecification;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class RandomObjectGeneratorTest {

    private RandomObjectGenerator generator;
    private RandomContext contextMock;
    private RandomModelBuilder randomModelBuilderSpy;
    private OpenApiSpecification specificationMock;

    @BeforeMethod
    public void setUp() {
        generator = new RandomObjectGenerator();
        contextMock = mock();
        specificationMock = mock();

        randomModelBuilderSpy = spy(new RandomModelBuilder(true));
        when(contextMock.getRandomModelBuilder()).thenReturn(randomModelBuilderSpy);
        when(contextMock.getSpecification()).thenReturn(specificationMock);
        when(contextMock.get(eq("OBJECT_STACK"), any())).thenReturn(new ArrayDeque<>());
    }

    @Test
    public void testHandlesObjectType() {
        OasSchema schema = new Oas30Schema();
        schema.type = OpenApiConstants.TYPE_OBJECT;

        assertTrue(generator.handles(schema));
    }

    @Test
    public void testDoesNotHandleNonObjectType() {
        OasSchema schema = new Oas30Schema();
        schema.type = OpenApiConstants.TYPE_STRING;

        assertFalse(generator.handles(schema));
    }

    @Test
    public void testGenerateObjectWithoutProperties() {
        OasSchema schema = new Oas30Schema();
        schema.type = OpenApiConstants.TYPE_OBJECT;

        generator.generate(contextMock, schema);

        verify(randomModelBuilderSpy).object(any());
    }

    @Test
    public void testGenerateObjectWithProperties() {
        OasSchema schema = new Oas30Schema();
        schema.type = OpenApiConstants.TYPE_OBJECT;
        schema.properties = new HashMap<>();
        OasSchema propertySchema = new Oas30Schema();
        schema.properties.put("property1", propertySchema);

        when(specificationMock.isGenerateOptionalFields()).thenReturn(true);

        generator.generate(contextMock, schema);

        verify(randomModelBuilderSpy).object(any());
        verify(randomModelBuilderSpy).property(eq("property1"), any());
        verify(contextMock).generate(propertySchema);
    }

    @Test
    public void testGenerateObjectWithRequiredProperties() {
        OasSchema schema = new Oas30Schema();
        schema.type = OpenApiConstants.TYPE_OBJECT;
        schema.properties = new HashMap<>();
        OasSchema propertySchema = new Oas30Schema();
        schema.properties.put("property1", propertySchema);
        schema.required = singletonList("property1");

        when(specificationMock.isGenerateOptionalFields()).thenReturn(false);

        generator.generate(contextMock, schema);

        verify(randomModelBuilderSpy).object(any());
        verify(randomModelBuilderSpy).property(eq("property1"), any());
        verify(contextMock).generate(propertySchema);
    }

    @Test
    public void testGenerateObjectWithOptionalProperties() {
        OasSchema schema = new Oas30Schema();
        schema.type = OpenApiConstants.TYPE_OBJECT;
        schema.properties = new HashMap<>();
        OasSchema propertySchema = new Oas30Schema();
        schema.properties.put("property1", propertySchema);
        schema.required = emptyList();
        when(specificationMock.isGenerateOptionalFields()).thenReturn(false);
        generator.generate(contextMock, schema);

        verify(randomModelBuilderSpy).object(any());
        verify(randomModelBuilderSpy, never()).property(eq("property1"), any());
        verify(contextMock, never()).generate(propertySchema);
    }

    @Test
    public void testGenerateObjectWithRecursion() {
        OasSchema schema = new Oas30Schema();
        schema.type = OpenApiConstants.TYPE_OBJECT;
        Deque<OasSchema> objectStack = new ArrayDeque<>();
        objectStack.push(schema);

        when(contextMock.get(eq("OBJECT_STACK"), any())).thenReturn(objectStack);

        generator.generate(contextMock, schema);

        verify(randomModelBuilderSpy, never()).object(any());
    }
}
