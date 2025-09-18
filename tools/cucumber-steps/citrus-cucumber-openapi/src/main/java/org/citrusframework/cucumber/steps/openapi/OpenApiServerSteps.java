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

package org.citrusframework.cucumber.steps.openapi;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import io.apicurio.datamodels.openapi.models.OasOperation;
import io.apicurio.datamodels.openapi.models.OasParameter;
import io.apicurio.datamodels.openapi.models.OasPathItem;
import io.apicurio.datamodels.openapi.models.OasResponse;
import io.apicurio.datamodels.openapi.models.OasSchema;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.citrusframework.Citrus;
import org.citrusframework.CitrusSettings;
import org.citrusframework.TestCaseRunner;
import org.citrusframework.annotations.CitrusAnnotations;
import org.citrusframework.annotations.CitrusFramework;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.context.TestContext;
import org.citrusframework.cucumber.steps.http.HttpServerSteps;
import org.citrusframework.cucumber.steps.openapi.model.OasModelHelper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

public class OpenApiServerSteps {

    @CitrusResource
    private TestCaseRunner runner;

    @CitrusResource
    private TestContext context;

    @CitrusFramework
    private Citrus citrus;

    private HttpServerSteps httpServerSteps;

    private OasOperation operation;

    @Before
    public void before(Scenario scenario) {
        httpServerSteps = new HttpServerSteps();
        CitrusAnnotations.injectAll(httpServerSteps, citrus, context);
        CitrusAnnotations.injectTestRunner(httpServerSteps, runner);
        httpServerSteps.before(scenario);

        httpServerSteps.configureTimeout(OpenApiSettings.getTimeout());
        httpServerSteps.setServerPort(Integer.parseInt(context.replaceDynamicContentInString(OpenApiSettings.getServicePort())));
        httpServerSteps.setServer(OpenApiSettings.getServiceName());

        operation = null;
    }

    @Given("^OpenAPI server timeout is (\\d+)(?: ms| milliseconds)$")
    public void configureTimeout(long timeout) {
        httpServerSteps.configureTimeout(timeout);
    }

    @Given("^OpenAPI service \"([^\"\\s]+)\"$")
    public void setServiceName(String name) {
        httpServerSteps.setServer(name);
    }

    @Given("^OpenAPI service port ([^\\s]+)$")
    public void setServicePort(String port) {
        httpServerSteps.setServerPort(Integer.parseInt(context.replaceDynamicContentInString(port)));
    }

    @Given("^create OpenAPI service$")
    public void createService() {
        httpServerSteps.startServer();
    }

    @When("^(?:receive|expect|verify) operation: (.+)$")
    public void receiveOperation(String operationId) {
        for (OasPathItem path : OasModelHelper.getPathItems(OpenApiSteps.openApiDoc.paths)) {
            Optional<Map.Entry<String, OasOperation>> operationEntry = OasModelHelper.getOperationMap(path).entrySet().stream()
                    .filter(op -> operationId.equals(op.getValue().operationId))
                    .findFirst();

            if (operationEntry.isPresent()) {
                operation = operationEntry.get().getValue();
                receiveRequest(path.getPath(), operationEntry.get().getKey(), operationEntry.get().getValue());
                break;
            }
        }
    }

    @Then("^send operation result: (\\d+)(?: [^\\s]+)?$")
    public void sendResponseByStatus(int response) {
        sendResponse(operation, String.valueOf(response));
    }

    @And("^send operation response: (.+)$")
    public void sendResponseByName(String response) {
        sendResponse(operation, response);
    }

    /**
     * Invoke request for given API operation. The request parameters, headers and payload are generated via specification
     * details in that operation.
     * @param path
     * @param method
     * @param operation
     */
    private void receiveRequest(String path, String method, OasOperation operation) {
        if (operation.parameters != null) {
            operation.parameters.stream()
                    .filter(param -> "header".equals(param.in))
                    .filter(param -> (param.required != null && param.required) || context.getVariables().containsKey(param.getName()))
                    .forEach(param -> httpServerSteps.addRequestHeader(param.getName(),
                            OpenApiTestDataGenerator.createValidationExpression(param.getName(), (OasSchema) param.schema,
                                    OasModelHelper.getSchemaDefinitions(OpenApiSteps.openApiDoc), false, context)));

            operation.parameters.stream()
                    .filter(param -> "query".equals(param.in))
                    .filter(param -> (param.required != null && param.required) || context.getVariables().containsKey(param.getName()))
                    .forEach(param -> httpServerSteps.addRequestQueryParam(param.getName(),
                            OpenApiTestDataGenerator.createValidationExpression(param.getName(), (OasSchema) param.schema,
                                    OasModelHelper.getSchemaDefinitions(OpenApiSteps.openApiDoc), false, context)));
        }

        Optional<OasSchema> body = OasModelHelper.getRequestBodySchema(OpenApiSteps.openApiDoc, operation);
        if (body.isPresent()) {
            httpServerSteps.setRequestBody(OpenApiTestDataGenerator.createInboundPayload(body.get(), OasModelHelper.getSchemaDefinitions(OpenApiSteps.openApiDoc)));

            if (OasModelHelper.isReferenceType(body.get())
                    || OasModelHelper.isObjectType(body.get())
                    || OasModelHelper.isArrayType(body.get())) {
                httpServerSteps.setInboundDictionary(OpenApiSteps.inboundDictionary);
            }
        }

        String randomizedPath = OasModelHelper.getBasePath(OpenApiSteps.openApiDoc) + path;
        randomizedPath = randomizedPath.replaceAll("//", "/");

        if (operation.parameters != null) {
            List<OasParameter> pathParams = operation.parameters.stream()
                    .filter(p -> "path".equals(p.in))
                    .collect(Collectors.toList());

            for (OasParameter parameter : pathParams) {
                String parameterValue;
                if (context.getVariables().containsKey(parameter.getName())) {
                    parameterValue = "\\" + CitrusSettings.VARIABLE_PREFIX + parameter.getName() + CitrusSettings.VARIABLE_SUFFIX;
                } else {
                    parameterValue = OpenApiTestDataGenerator.createValidationExpression((OasSchema) parameter.schema, OasModelHelper.getSchemaDefinitions(OpenApiSteps.openApiDoc), false);
                }
                randomizedPath = Pattern.compile("\\{" + parameter.getName() + "}")
                                        .matcher(randomizedPath)
                                        .replaceAll(parameterValue);
            }
        }

        Optional<String> contentType = OasModelHelper.getRequestContentType(operation);
        contentType.ifPresent(s -> httpServerSteps.addRequestHeader(HttpHeaders.CONTENT_TYPE, String.format("@startsWith(%s)@", s)));

        httpServerSteps.receiveServerRequest(method.toUpperCase(), randomizedPath);
    }

    /**
     * Verify operation response where expected parameters, headers and payload are generated using the operation specification details.
     * @param operation
     * @param status
     */
    private void sendResponse(OasOperation operation, String status) {
        if (operation.responses != null) {
            OasResponse response = Optional.ofNullable(operation.responses.getItem(status))
                                        .orElse(operation.responses.default_);

            if (response != null) {
                Map<String, OasSchema> requiredHeaders = OasModelHelper.getRequiredHeaders(response);
                for (Map.Entry<String, OasSchema> header : requiredHeaders.entrySet()) {
                    httpServerSteps.addResponseHeader(header.getKey(),
                            OpenApiTestDataGenerator.createRandomValueExpression(header.getKey(), header.getValue(),
                                    OasModelHelper.getSchemaDefinitions(OpenApiSteps.openApiDoc), false, context));
                }

                Map<String, OasSchema> headers = OasModelHelper.getHeaders(response);
                for (Map.Entry<String, OasSchema> header : headers.entrySet()) {
                    if (!requiredHeaders.containsKey(header.getKey()) && context.getVariables().containsKey(header.getKey())) {
                        httpServerSteps.addResponseHeader(header.getKey(),CitrusSettings.VARIABLE_PREFIX + header.getKey() + CitrusSettings.VARIABLE_SUFFIX);
                    }
                }

                Optional<OasSchema> responseSchema = OasModelHelper.getSchema(response);
                if (responseSchema.isPresent()) {
                    httpServerSteps.setResponseBody(OpenApiTestDataGenerator.createOutboundPayload(responseSchema.get(), OasModelHelper.getSchemaDefinitions(OpenApiSteps.openApiDoc)));

                    if (OasModelHelper.isReferenceType(responseSchema.get())
                            || OasModelHelper.isObjectType(responseSchema.get())
                            || OasModelHelper.isArrayType(responseSchema.get())) {
                        httpServerSteps.setOutboundDictionary(OpenApiSteps.outboundDictionary);
                    }
                }
            }
        }

        Optional<String> contentType = OasModelHelper.getResponseContentType(OpenApiSteps.openApiDoc, operation);
        contentType.ifPresent(s -> httpServerSteps.addResponseHeader(HttpHeaders.CONTENT_TYPE, s));

        if (Pattern.compile("[0-9]+").matcher(status).matches()) {
            httpServerSteps.sendServerResponse(Integer.parseInt(status));
        } else {
            httpServerSteps.sendServerResponse(HttpStatus.OK.value());
        }
    }
}
