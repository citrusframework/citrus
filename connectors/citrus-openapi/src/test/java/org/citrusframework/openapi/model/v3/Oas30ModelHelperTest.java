/*
 * Copyright the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.openapi.model.v3;

import io.apicurio.datamodels.openapi.models.OasResponse;
import io.apicurio.datamodels.openapi.models.OasSchema;
import io.apicurio.datamodels.openapi.v3.models.Oas30Components;
import io.apicurio.datamodels.openapi.v3.models.Oas30Document;
import io.apicurio.datamodels.openapi.v3.models.Oas30Header;
import io.apicurio.datamodels.openapi.v3.models.Oas30MediaType;
import io.apicurio.datamodels.openapi.v3.models.Oas30Operation;
import io.apicurio.datamodels.openapi.v3.models.Oas30Parameter;
import io.apicurio.datamodels.openapi.v3.models.Oas30Response;
import io.apicurio.datamodels.openapi.v3.models.Oas30Responses;
import io.apicurio.datamodels.openapi.v3.models.Oas30Schema;
import io.apicurio.datamodels.openapi.v3.models.Oas30SchemaDefinition;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.openapi.model.OasModelHelper;
import org.springframework.http.MediaType;
import org.testng.annotations.Test;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class Oas30ModelHelperTest {

    @Test
    public void getRequiredHeaders_shouldNotFindRequiredHeadersWithoutRequiredAttribute() {
        var header = new Oas30Header("X-TEST");
        header.schema = new Oas30Schema();
        header.required = null;
        var response = new Oas30Response("200");
        response.headers.put(header.getName(), header);

        Map<String, OasSchema> result = Oas30ModelHelper.getRequiredHeaders(null, response);

        assertThat(result).isEmpty();
    }

    @Test
    public void getRequiredHeaders_shouldFindRequiredHeaders() {
        var header = new Oas30Header("X-TEST");
        header.schema = new Oas30Schema();
        header.required = Boolean.TRUE;
        var response = new Oas30Response("200");
        response.headers.put(header.getName(), header);

        Map<String, OasSchema> result = Oas30ModelHelper.getRequiredHeaders(null, response);

        assertThat(result)
                .containsExactly(Map.entry(header.getName(), header.schema));
    }

    @Test
    public void getRequiredHeaders_shouldNotFindOptionalHeaders() {
        var header = new Oas30Header("X-TEST");
        header.schema = new Oas30Schema();
        header.required = Boolean.FALSE;
        var response = new Oas30Response("200");
        response.headers.put(header.getName(), header);

        Map<String, OasSchema> result = Oas30ModelHelper.getRequiredHeaders(null, response);

        assertThat(result).isEmpty();
    }

    @Test
    public void getRequiredHeaders_shouldResolveReferencesOfRequiredHeaders() {
        var header = new Oas30Header("X-TEST");
        var reference = "reference";
        header.setReference(reference);
        header.required = Boolean.TRUE;
        var response = new Oas30Response("200");
        response.headers.put(header.getName(), header);

        var oasDocument = new Oas30Document();
        oasDocument.components = new Oas30Components();
        var referencedSchema = new Oas30SchemaDefinition(reference);
        oasDocument.components.schemas = Map.of(reference, referencedSchema);

        Map<String, OasSchema> result = Oas30ModelHelper.getRequiredHeaders(oasDocument, response);

        assertThat(result)
                .containsExactly(Map.entry(header.getName(), referencedSchema));
    }

    @Test
    public void getRequiredHeaders_shouldThrowException_whenHeaderDefinitionIsInvalid() {
        var header = new Oas30Header("X-TEST");
        header.required = Boolean.TRUE;
        var response = new Oas30Response("200");
        response.headers.put(header.getName(), header);

        var oasDocument = new Oas30Document();
        oasDocument.components = new Oas30Components();
        oasDocument.components.schemas = emptyMap();

        assertThatThrownBy(() -> Oas30ModelHelper.getHeaders(oasDocument, response))
                .isInstanceOf(CitrusRuntimeException.class)
                .hasMessage("Failed to resolve schema in OpenAPI specification, tried reference as well!");
    }

    @Test
    public void getHeaders_shouldFindRequiredHeadersWithoutRequiredAttribute() {
        var header = new Oas30Header("X-TEST");
        header.schema = new Oas30Schema();
        header.required = null;
        var response = new Oas30Response("200");
        response.headers.put(header.getName(), header);

        Map<String, OasSchema> result = Oas30ModelHelper.getHeaders(null, response);

        assertThat(result)
                .containsExactly(Map.entry(header.getName(), header.schema));
    }

    @Test
    public void getHeaders_shouldFindRequiredHeaders() {
        var header = new Oas30Header("X-TEST");
        header.schema = new Oas30Schema();
        header.required = Boolean.TRUE;
        var response = new Oas30Response("200");
        response.headers.put(header.getName(), header);

        Map<String, OasSchema> result = Oas30ModelHelper.getHeaders(null, response);

        assertThat(result)
                .containsExactly(Map.entry(header.getName(), header.schema));
    }

    @Test
    public void getHeaders_shouldFindOptionalHeaders() {
        var header = new Oas30Header("X-TEST");
        header.schema = new Oas30Schema();
        header.required = Boolean.FALSE;
        var response = new Oas30Response("200");
        response.headers.put(header.getName(), header);

        Map<String, OasSchema> result = Oas30ModelHelper.getHeaders(null, response);

        assertThat(result)
                .containsExactly(Map.entry(header.getName(), header.schema));
    }

    @Test
    public void getHeaders_shouldResolveReferencesOfHeaders() {
        var header = new Oas30Header("X-TEST");
        var reference = "reference";
        header.setReference(reference);
        var response = new Oas30Response("200");
        response.headers.put(header.getName(), header);

        var oasDocument = new Oas30Document();
        oasDocument.components = new Oas30Components();
        var referencedSchema = new Oas30SchemaDefinition(reference);
        oasDocument.components.schemas = Map.of(reference, referencedSchema);

        Map<String, OasSchema> result = Oas30ModelHelper.getHeaders(oasDocument, response);

        assertThat(result)
                .containsExactly(Map.entry(header.getName(), referencedSchema));
    }

    @Test
    public void getHeaders_shouldThrowException_whenHeaderDefinitionIsInvalid() {
        var header = new Oas30Header("X-TEST");
        var response = new Oas30Response("200");
        response.headers.put(header.getName(), header);

        var oasDocument = new Oas30Document();
        oasDocument.components = new Oas30Components();
        oasDocument.components.schemas = emptyMap();

        assertThatThrownBy(() -> Oas30ModelHelper.getHeaders(oasDocument, response))
                .isInstanceOf(CitrusRuntimeException.class)
                .hasMessage("Failed to resolve schema in OpenAPI specification, tried reference as well!");
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
