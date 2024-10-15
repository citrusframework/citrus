package org.citrusframework.openapi.random;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertSame;

import io.apicurio.datamodels.openapi.models.OasSchema;
import io.apicurio.datamodels.openapi.v3.models.Oas30Schema;
import java.util.HashMap;
import java.util.Map;
import org.citrusframework.openapi.OpenApiSpecification;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class RandomContextTest {

    private OpenApiSpecification specificationMock;

    private RandomContext randomContext;

    private Map<String, OasSchema> schemaDefinitions;

    @BeforeMethod
    public void setUp() {
        RandomModelBuilder randomModelBuilderMock = mock();
        specificationMock = mock();

        schemaDefinitions =new HashMap<>();

        randomContext = spy(new RandomContext(specificationMock, true));
        ReflectionTestUtils.setField(randomContext, "randomModelBuilder", randomModelBuilderMock);

        doReturn(schemaDefinitions).when(randomContext).getSchemaDefinitions();
    }

    @Test
    public void testGenerateWithResolvedSchema() {
        OasSchema oasSchema = new Oas30Schema();
        randomContext.generate(oasSchema);
        verify(randomContext).doGenerate(oasSchema);
    }

    @Test
    public void testGenerateWithReferencedSchema() {
        OasSchema referencedSchema = new Oas30Schema();
        schemaDefinitions.put("reference", referencedSchema);
        OasSchema oasSchema = new Oas30Schema();
        oasSchema.$ref = "reference";

        randomContext.generate(oasSchema);
        verify(randomContext).doGenerate(referencedSchema);
    }

    @Test
    public void testGetRandomModelBuilder() {
        assertNotNull(randomContext.getRandomModelBuilder());
    }

    @Test
    public void testGetSpecification() {
        assertEquals(randomContext.getSpecification(), specificationMock);
    }

    @Test
    public void testCacheVariable() {
        HashMap<String, String> cachedValue1 = randomContext.get("testKey", k ->  new HashMap<>());
        HashMap<String, String> cachedValue2 = randomContext.get("testKey", k ->  new HashMap<>());

        assertSame(cachedValue1, cachedValue2);
    }
}
