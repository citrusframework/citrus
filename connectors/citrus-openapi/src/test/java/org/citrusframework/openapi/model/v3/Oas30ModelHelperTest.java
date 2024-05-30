package org.citrusframework.openapi.model.v3;

import io.apicurio.datamodels.openapi.models.OasSchema;
import io.apicurio.datamodels.openapi.v3.models.Oas30Header;
import io.apicurio.datamodels.openapi.v3.models.Oas30Response;
import io.apicurio.datamodels.openapi.v3.models.Oas30Schema;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

public class Oas30ModelHelperTest {

    @Test
    public void shouldNotFindRequiredHeadersWithoutRequiredAttribute() {
        var header = new Oas30Header("X-TEST");
        header.schema = new Oas30Schema();
        header.required = null; // explicitely assigned because this is test case
        var response = new Oas30Response("200");
        response.headers.put(header.getName(), header);

        Map<String, OasSchema> result = Oas30ModelHelper.getRequiredHeaders(response);

        Assert.assertEquals(result.size(), 0);
    }

    @Test
    public void shouldFindRequiredHeaders() {
        var header = new Oas30Header("X-TEST");
        header.schema = new Oas30Schema();
        header.required = Boolean.TRUE; // explicitely assigned because this is test case
        var response = new Oas30Response("200");
        response.headers.put(header.getName(), header);

        Map<String, OasSchema> result = Oas30ModelHelper.getRequiredHeaders(response);

        Assert.assertEquals(result.size(), 1);
        Assert.assertSame(result.get(header.getName()), header.schema);
    }

    @Test
    public void shouldNotFindOptionalHeaders() {
        var header = new Oas30Header("X-TEST");
        header.schema = new Oas30Schema();
        header.required = Boolean.FALSE; // explicitely assigned because this is test case
        var response = new Oas30Response("200");
        response.headers.put(header.getName(), header);

        Map<String, OasSchema> result = Oas30ModelHelper.getRequiredHeaders(response);

        Assert.assertEquals(result.size(), 0);
    }

}