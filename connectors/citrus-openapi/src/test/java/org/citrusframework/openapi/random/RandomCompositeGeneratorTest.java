package org.citrusframework.openapi.random;

import io.apicurio.datamodels.openapi.models.OasSchema;
import io.apicurio.datamodels.openapi.v3.models.Oas30Schema;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.assertArg;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RandomCompositeGeneratorTest {

    private RandomCompositeGenerator generator;
    private RandomContext mockContext;
    private RandomModelBuilder builderSpy;

    @BeforeMethod
    public void setUp() {
        generator = new RandomCompositeGenerator();
        mockContext = mock(RandomContext.class);
        builderSpy = spy(new RandomModelBuilder(true));

        when(mockContext.getRandomModelBuilder()).thenReturn(builderSpy);
    }

    @Test
    public void testHandlesCompositeSchema() {
        Oas30Schema schema = new Oas30Schema();
        schema.allOf = Collections.singletonList(new Oas30Schema());

        assertThat(generator.handles(schema)).isTrue();
    }

    @Test
    public void testGenerateAllOf() {
        Oas30Schema schema = new Oas30Schema();
        schema.allOf = List.of(new Oas30Schema(), new Oas30Schema(), new Oas30Schema());

        generator.generate(mockContext, schema);

        verify(builderSpy).object(any());
        verify(mockContext).generate(schema.allOf.get(0));
        verify(mockContext).generate(schema.allOf.get(1));
        verify(mockContext).generate(schema.allOf.get(2));
    }

    @Test
    public void testGenerateAnyOf() {
        Oas30Schema schema = new Oas30Schema();
        schema.anyOf = List.of(new Oas30Schema(), new Oas30Schema(), new Oas30Schema());

        generator.generate(mockContext, schema);

        verify(builderSpy, atMost(2)).object(any());
        verify(mockContext, atLeast(1)).generate(assertArg(arg -> assertThat(schema.anyOf).contains(arg)));
        verify(mockContext, atMost(3)).generate(assertArg(arg -> assertThat(schema.anyOf).contains(arg)));
    }

    @Test
    public void testGenerateOneOf() {
        Oas30Schema schema = new Oas30Schema();
        schema.oneOf = List.of(new Oas30Schema(), new Oas30Schema(), new Oas30Schema());

        generator.generate(mockContext, schema);

        verify(builderSpy, atLeastOnce()).object(any());
        verify(mockContext).generate(any(OasSchema.class));
    }

    @Test
    public void testGenerateWithNoCompositeSchema() {
        Oas30Schema schema = new Oas30Schema();

        generator.generate(mockContext, schema);

        verify(builderSpy, never()).object(any());
        verify(mockContext, never()).generate(any(OasSchema.class));
    }
}
