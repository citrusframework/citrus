package org.citrusframework.openapi;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class OpenApiSpecificationAdapterTest {

    @Mock
    private OpenApiSpecification openApiSpecificationMock;

    @Mock
    private Object entityMock;

    private OpenApiSpecificationAdapter<Object> openApiSpecificationAdapter;

    private AutoCloseable mockCloseable;

    @BeforeMethod
    public void setUp() {
        mockCloseable = MockitoAnnotations.openMocks(this);
        openApiSpecificationAdapter = new OpenApiSpecificationAdapter<>(openApiSpecificationMock, entityMock);
    }

    @AfterMethod
    public void tearDown() throws Exception {
        mockCloseable.close();
    }

    @Test
    public void shouldProvideOpenApiSpecification() {
        OpenApiSpecification specification = openApiSpecificationAdapter.openApiSpecification();
        assertNotNull(specification, "OpenApiSpecification should not be null");
        assertEquals(specification, openApiSpecificationMock, "OpenApiSpecification should match the mock");
    }

    @Test
    public void shouldProvideEntity() {
        Object entity = openApiSpecificationAdapter.entity();
        assertNotNull(entity, "Entity should not be null");
        assertEquals(entity, entityMock, "Entity should match the mock");
    }

}
