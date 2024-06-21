package org.citrusframework.openapi.model;

import io.apicurio.datamodels.openapi.models.OasOperation;
import io.apicurio.datamodels.openapi.v3.models.Oas30Operation;
import org.citrusframework.openapi.OpenApiUtils;
import org.citrusframework.openapi.model.OperationPathAdapter;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static java.lang.String.format;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

public class OperationPathAdapterTest {


    @Test
    public void shouldReturnFormattedStringWhenToStringIsCalled() {
        // Given
        Oas30Operation oas30Operation = new Oas30Operation("get");
        oas30Operation.operationId = "operationId";

        OperationPathAdapter adapter = new OperationPathAdapter("/api/path", "/context/path", "/full/path", oas30Operation);

        // When
        String expectedString = format("%s (%s)", OpenApiUtils.getMethodPath("GET", "/api/path"), "operationId");

        // Then
        assertEquals(adapter.toString(), expectedString);
    }
}
