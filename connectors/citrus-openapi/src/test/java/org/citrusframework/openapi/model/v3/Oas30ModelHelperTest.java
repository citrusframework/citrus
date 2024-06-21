package org.citrusframework.openapi.model.v3;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import io.apicurio.datamodels.openapi.models.OasResponse;
import io.apicurio.datamodels.openapi.models.OasSchema;
import io.apicurio.datamodels.openapi.v3.models.Oas30Document;
import io.apicurio.datamodels.openapi.v3.models.Oas30Header;
import io.apicurio.datamodels.openapi.v3.models.Oas30MediaType;
import io.apicurio.datamodels.openapi.v3.models.Oas30Operation;
import io.apicurio.datamodels.openapi.v3.models.Oas30Parameter;
import io.apicurio.datamodels.openapi.v3.models.Oas30Response;
import io.apicurio.datamodels.openapi.v3.models.Oas30Responses;
import io.apicurio.datamodels.openapi.v3.models.Oas30Schema;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import org.citrusframework.openapi.model.OasModelHelper;
import org.springframework.http.MediaType;
import org.testng.annotations.Test;

public class Oas30ModelHelperTest {

    @Test
    public void shouldNotFindRequiredHeadersWithoutRequiredAttribute() {
        var header = new Oas30Header("X-TEST");
        header.schema = new Oas30Schema();
        header.required = null;
        var response = new Oas30Response("200");
        response.headers.put(header.getName(), header);

        Map<String, OasSchema> result = Oas30ModelHelper.getRequiredHeaders(response);

        assertEquals(result.size(), 0);
    }

    @Test
    public void shouldFindRequiredHeaders() {
        var header = new Oas30Header("X-TEST");
        header.schema = new Oas30Schema();
        header.required = Boolean.TRUE;
        var response = new Oas30Response("200");
        response.headers.put(header.getName(), header);

        Map<String, OasSchema> result = Oas30ModelHelper.getRequiredHeaders(response);

        assertEquals(result.size(), 1);
        assertSame(result.get(header.getName()), header.schema);
    }

    @Test
    public void shouldNotFindOptionalHeaders() {
        var header = new Oas30Header("X-TEST");
        header.schema = new Oas30Schema();
        header.required = Boolean.FALSE;
        var response = new Oas30Response("200");
        response.headers.put(header.getName(), header);

        Map<String, OasSchema> result = Oas30ModelHelper.getRequiredHeaders(response);

        assertEquals(result.size(), 0);
    }

    @Test
    public void shouldFindAllRequestTypesForOperation() {
        Oas30Operation operation = new Oas30Operation("GET");
        operation.responses = new Oas30Responses();

        Oas30Response response = new Oas30Response("200");
        response.content = Map.of(MediaType.APPLICATION_JSON_VALUE,
            new Oas30MediaType(MediaType.APPLICATION_JSON_VALUE),
            MediaType.APPLICATION_XML_VALUE, new Oas30MediaType(MediaType.APPLICATION_XML_VALUE));

        operation.responses = new Oas30Responses();
        operation.responses.addResponse("200", response);

        Collection<String> responseTypes = Oas30ModelHelper.getResponseTypes(operation, response);

        assertTrue(responseTypes.contains(MediaType.APPLICATION_JSON_VALUE));
        assertTrue(responseTypes.contains(MediaType.APPLICATION_XML_VALUE));

    }

    @Test
    public void shouldFindRandomResponseWithGoodStatusCode() {
        Oas30Document document = new Oas30Document();
        Oas30Operation operation = new Oas30Operation("GET");

        operation.responses = new Oas30Responses();

        Oas30Response nokResponse = new Oas30Response("403");
        Oas30MediaType plainTextMediaType = new Oas30MediaType(MediaType.TEXT_PLAIN_VALUE);
        plainTextMediaType.schema = new Oas30Schema();
        nokResponse.content = Map.of(MediaType.TEXT_PLAIN_VALUE, plainTextMediaType);

        Oas30Response okResponse = new Oas30Response("200");
        Oas30MediaType jsonMediaType = new Oas30MediaType(MediaType.APPLICATION_JSON_VALUE);
        jsonMediaType.schema = new Oas30Schema();

        Oas30MediaType xmlMediaType = new Oas30MediaType(MediaType.APPLICATION_XML_VALUE);
        xmlMediaType.schema = new Oas30Schema();

        okResponse.content = Map.of(MediaType.APPLICATION_JSON_VALUE, jsonMediaType,
            MediaType.APPLICATION_XML_VALUE, xmlMediaType);

        operation.responses = new Oas30Responses();
        operation.responses.addResponse("403", nokResponse);
        operation.responses.addResponse("200", okResponse);

        Optional<OasResponse> responseForRandomGeneration = OasModelHelper.getResponseForRandomGeneration(
            document, operation, null, null);
        assertTrue(responseForRandomGeneration.isPresent());
        assertEquals(okResponse, responseForRandomGeneration.get());
    }

    @Test
    public void shouldFindFirstResponseInAbsenceOfAGoodOne() {
        Oas30Document document = new Oas30Document();
        Oas30Operation operation = new Oas30Operation("GET");

        operation.responses = new Oas30Responses();

        Oas30Response nokResponse403 = new Oas30Response("403");
        Oas30MediaType plainTextMediaType = new Oas30MediaType(MediaType.TEXT_PLAIN_VALUE);
        plainTextMediaType.schema = new Oas30Schema();
        nokResponse403.content = Map.of(MediaType.TEXT_PLAIN_VALUE, plainTextMediaType);

        Oas30Response nokResponse407 = new Oas30Response("407");
        nokResponse407.content = Map.of(MediaType.TEXT_PLAIN_VALUE, plainTextMediaType);

        operation.responses = new Oas30Responses();
        operation.responses.addResponse("403", nokResponse403);
        operation.responses.addResponse("407", nokResponse407);

        Optional<OasResponse> responseForRandomGeneration = OasModelHelper.getResponseForRandomGeneration(
            document, operation, null, null);
        assertTrue(responseForRandomGeneration.isPresent());
        assertEquals(responseForRandomGeneration.get().getStatusCode(), "403");

    }

    @Test
    public void shouldFindDefaultResponseInAbsenceOfAGoodOne() {
        Oas30Document document = new Oas30Document();
        Oas30Operation operation = new Oas30Operation("GET");

        operation.responses = new Oas30Responses();

        Oas30Response nokResponse403 = new Oas30Response("403");
        Oas30MediaType plainTextMediaType = new Oas30MediaType(MediaType.TEXT_PLAIN_VALUE);
        plainTextMediaType.schema = new Oas30Schema();
        nokResponse403.content = Map.of(MediaType.TEXT_PLAIN_VALUE, plainTextMediaType);

        Oas30Response nokResponse407 = new Oas30Response("407");
        nokResponse407.content = Map.of(MediaType.TEXT_PLAIN_VALUE, plainTextMediaType);

        operation.responses = new Oas30Responses();
        operation.responses.default_ = nokResponse407;
        operation.responses.addResponse("403", nokResponse403);
        operation.responses.addResponse("407", nokResponse407);

        Optional<OasResponse> responseForRandomGeneration = OasModelHelper.getResponseForRandomGeneration(
            document, operation, null, null);
        assertTrue(responseForRandomGeneration.isPresent());
        assertEquals(responseForRandomGeneration.get().getStatusCode(), "407");
    }

    @Test
    public void shouldFindParameterSchema() {
        Oas30Parameter parameter = new Oas30Parameter();
        parameter.schema = new Oas30Schema();

        Optional<OasSchema> parameterSchema = Oas30ModelHelper.getParameterSchema(parameter);
        assertTrue(parameterSchema.isPresent());
        assertEquals(parameter.schema, parameterSchema.get());
    }

    @Test
    public void shouldNotFindParameterSchema() {
        Oas30Parameter parameter = new Oas30Parameter();

        Optional<OasSchema> parameterSchema = Oas30ModelHelper.getParameterSchema(parameter);
        assertTrue(parameterSchema.isEmpty());
    }

}