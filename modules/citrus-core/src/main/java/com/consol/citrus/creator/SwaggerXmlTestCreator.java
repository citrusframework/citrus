/*
 * Copyright 2006-2018 the original author or authors.
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

package com.consol.citrus.creator;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import io.swagger.models.*;
import io.swagger.models.parameters.*;
import io.swagger.models.properties.*;
import io.swagger.parser.SwaggerParser;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Test creator creates one to many test cases based on operations defined in a XML schema XSD.
 * @author Christoph Deppisch
 * @since 2.7.4
 */
public class SwaggerXmlTestCreator extends RequestResponseXmlTestCreator {

    private String swaggerResource;

    private String contextPath;
    private String operation;

    private String namePrefix;
    private String nameSuffix = "_IT";

    @Override
    public void create() {
        Swagger swagger;
        try {
            swagger = new SwaggerParser().read(new PathMatchingResourcePatternResolver().getResource(swaggerResource).getURI().toURL().toString());
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to parse Swagger Open API specification: " + swaggerResource, e);
        }

        if (!StringUtils.hasText(namePrefix)) {
            withNamePrefix(StringUtils.trimAllWhitespace(Optional.ofNullable(swagger.getInfo().getTitle()).orElse("Swagger")) + "_");
        }

        for (Map.Entry<String, Path> path : swagger.getPaths().entrySet()) {
            for (Map.Entry<io.swagger.models.HttpMethod, Operation> operation : path.getValue().getOperationMap().entrySet()) {

                // Now generate it
                withName(namePrefix + operation.getValue().getOperationId() + nameSuffix);

                Map<String, Object> requestHeaders = new HashMap<>();
                requestHeaders.put("citrus_http_request_uri", Optional.ofNullable(contextPath).orElse("") + (swagger.getBasePath() != null ? swagger.getBasePath() : "") + path.getKey());
                requestHeaders.put("citrus_http_method", HttpMethod.valueOf(operation.getKey().name()).name());

                withRequest("");
                if (operation.getValue().getParameters() != null) {
                    operation.getValue().getParameters().stream()
                            .filter(p -> p instanceof HeaderParameter)
                            .filter(Parameter::getRequired)
                            .forEach(p -> requestHeaders.put(p.getName(), createRandomValueExpression(((HeaderParameter) p).getItems(), swagger.getDefinitions(), false)));

                    String queryParams = operation.getValue().getParameters().stream()
                            .filter(param -> param instanceof QueryParameter)
                            .filter(Parameter::getRequired)
                            .map(param -> param.getName() + "=" + createRandomValueExpression((QueryParameter) param))
                            .collect(Collectors.joining("&"));

                    if (StringUtils.hasText(queryParams)) {
                        requestHeaders.put("citrus_http_query_params", queryParams);
                    }

                    operation.getValue().getParameters().stream()
                            .filter(p -> p instanceof BodyParameter)
                            .filter(Parameter::getRequired)
                            .findFirst()
                            .ifPresent(p -> withRequest(createOutboundPayload(((BodyParameter) p).getSchema(), swagger.getDefinitions())));
                }

                withRequestHeaders(requestHeaders);

                withResponse("");
                withResponseHeaders(Collections.emptyMap());
                if (operation.getValue().getResponses() != null) {
                    Response response = operation.getValue().getResponses().get("200");
                    if (response == null) {
                        response = operation.getValue().getResponses().get("default");
                    }

                    if (response != null) {
                        Map<String, Object> responseHeaders = new HashMap<>();
                        responseHeaders.put("citrus_http_status_code", "200");

                        if (response.getHeaders() != null) {
                            for (Map.Entry<String, Property> header : response.getHeaders().entrySet()) {
                                responseHeaders.put(header.getKey(), createValidationExpression(header.getValue(), swagger.getDefinitions()));
                            }
                        }

                        withResponseHeaders(responseHeaders);

                        if (response.getSchema() != null) {
                            withResponse(createInboundPayload(response.getSchema(), swagger.getDefinitions()));
                        }
                    }
                }

                super.create();

                log.info("Successfully created new test case " + getTargetPackage() + "." + getName());
            }
        }
    }

    /**
     * Creates payload from schema for outbound message.
     * @param model
     * @param definitions
     * @return
     */
    private String createOutboundPayload(Model model, Map<String, Model> definitions) {
        StringBuilder payload = new StringBuilder();

        if (model instanceof RefModel) {
            model = definitions.get(((RefModel) model).getSimpleRef());
        }

        if (model instanceof ArrayModel) {
            payload.append(createOutboundPayload(((ArrayModel) model).getItems(), definitions));
        } else {
            payload.append("{");

            if (model.getProperties() != null) {
                for (Map.Entry<String, Property> entry : model.getProperties().entrySet()) {
                    payload.append("\"").append(entry.getKey()).append("\": ").append(createOutboundPayload(entry.getValue(), definitions)).append(",");
                }
            }

            if (payload.toString().endsWith(",")) {
                payload.replace(payload.length() - 1, payload.length(), "");
            }

            payload.append("}");
        }

        return payload.toString();
    }

    /**
     * Creates payload from property for outbound message.
     * @param property
     * @param definitions
     * @return
     */
    private String createOutboundPayload(Property property, Map<String, Model> definitions) {
        StringBuilder payload = new StringBuilder();

        if (property instanceof RefProperty) {
            Model model = definitions.get(((RefProperty) property).getSimpleRef());
            payload.append("{");

            if (model.getProperties() != null) {
                for (Map.Entry<String, Property> entry : model.getProperties().entrySet()) {
                    payload.append("\"").append(entry.getKey()).append("\": ").append(createRandomValueExpression(entry.getValue(), definitions, true)).append(",");
                }
            }

            if (payload.toString().endsWith(",")) {
                payload.replace(payload.length() - 1, payload.length(), "");
            }

            payload.append("}");
        } else if (property instanceof ArrayProperty) {
            payload.append("[");
            payload.append(createRandomValueExpression(((ArrayProperty) property).getItems(), definitions, true));
            payload.append("]");
        } else {
            payload.append(createRandomValueExpression(property, definitions, false));
        }

        return payload.toString();
    }

    /**
     * Create payload from schema with random values.
     * @param property
     * @param definitions
     * @param quotes
     * @return
     */
    private String createRandomValueExpression(Property property, Map<String, Model> definitions, boolean quotes) {
        StringBuilder payload = new StringBuilder();

        if (property instanceof RefProperty) {
            payload.append(createOutboundPayload(property, definitions));
        } else if (property instanceof ArrayProperty) {
            payload.append(createOutboundPayload(property, definitions));
        } else if (property instanceof StringProperty || property instanceof DateProperty || property instanceof DateTimeProperty) {
            if (quotes) {
                payload.append("\"");
            }

            if (property instanceof DateProperty) {
                payload.append("citrus:currentDate()");
            } else if (property instanceof DateTimeProperty) {
                payload.append("citrus:currentDate('yyyy-MM-dd'T'hh:mm:ss')");
            } else if (!CollectionUtils.isEmpty(((StringProperty) property).getEnum())) {
                payload.append("citrus:randomEnumValue(").append(((StringProperty) property).getEnum().stream().map(value -> "'" + value + "'").collect(Collectors.joining(","))).append(")");
            } else {
                payload.append("citrus:randomString(").append(((StringProperty) property).getMaxLength() != null && ((StringProperty) property).getMaxLength() > 0 ? ((StringProperty) property).getMaxLength() : (((StringProperty) property).getMinLength() != null && ((StringProperty) property).getMinLength() > 0 ? ((StringProperty) property).getMinLength() : 10)).append(")");
            }

            if (quotes) {
                payload.append("\"");
            }
        } else if (property instanceof IntegerProperty || property instanceof LongProperty) {
            payload.append("citrus:randomNumber(10)");
        } else if (property instanceof FloatProperty || property instanceof DoubleProperty) {
            payload.append("citrus:randomNumber(10)");
        } else if (property instanceof BooleanProperty) {
            payload.append("citrus:randomEnumValue('true', 'false')");
        } else {
            if (quotes) {
                payload.append("\"\"");
            } else {
                payload.append("");
            }
        }

        return payload.toString();
    }

    /**
     * Creates control payload from property for validation.
     * @param property
     * @param definitions
     * @return
     */
    private String createInboundPayload(Property property, Map<String, Model> definitions) {
        StringBuilder payload = new StringBuilder();

        if (property instanceof RefProperty) {
            Model model = definitions.get(((RefProperty) property).getSimpleRef());
            payload.append("{");

            if (model.getProperties() != null) {
                for (Map.Entry<String, Property> entry : model.getProperties().entrySet()) {
                    payload.append("\"").append(entry.getKey()).append("\": ").append(createValidationExpression(entry.getValue(), definitions)).append(",");
                }
            }

            if (payload.toString().endsWith(",")) {
                payload.replace(payload.length() - 1, payload.length(), "");
            }

            payload.append("}");
        } else if (property instanceof ArrayProperty) {
            payload.append("[");
            payload.append(createValidationExpression(((ArrayProperty) property).getItems(), definitions));
            payload.append("]");
        } else {
            payload.append(createValidationExpression(property, definitions));
        }

        return payload.toString();
    }

    /**
     * Creates control payload from schema for validation.
     * @param model
     * @param definitions
     * @return
     */
    private String createInboundPayload(Model model, Map<String, Model> definitions) {
        StringBuilder payload = new StringBuilder();

        if (model instanceof RefModel) {
            model = definitions.get(((RefModel) model).getSimpleRef());
        }

        if (model instanceof ArrayModel) {
            payload.append("[");
            payload.append(createValidationExpression(((ArrayModel) model).getItems(), definitions));
            payload.append("]");
        } else {
            payload.append("{");

            if (model.getProperties() != null) {
                for (Map.Entry<String, Property> entry : model.getProperties().entrySet()) {
                    payload.append("\"").append(entry.getKey()).append("\": ").append(createValidationExpression(entry.getValue(), definitions)).append(",");
                }
            }

            if (payload.toString().endsWith(",")) {
                payload.replace(payload.length() - 1, payload.length(), "");
            }

            payload.append("}");
        }

        return payload.toString();
    }

    /**
     * Create validation expression using functions according to parameter type and format.
     * @param property
     * @param definitions
     * @return
     */
    private String createValidationExpression(Property property, Map<String, Model> definitions) {
        StringBuilder payload = new StringBuilder();
        if (property instanceof RefProperty) {
            Model model = definitions.get(((RefProperty) property).getSimpleRef());
            payload.append("{");

            if (model.getProperties() != null) {
                for (Map.Entry<String, Property> entry : model.getProperties().entrySet()) {
                    payload.append("\"").append(entry.getKey()).append("\": ").append(createValidationExpression(entry.getValue(), definitions)).append(",");
                }
            }

            if (payload.toString().endsWith(",")) {
                payload.replace(payload.length() - 1, payload.length(), "");
            }

            payload.append("}");
        } else if (property instanceof ArrayProperty) {
            payload.append("\"@ignore@\"");
        } else if (property instanceof StringProperty) {
            if (StringUtils.hasText(((StringProperty) property).getPattern())) {
                payload.append("\"@matches(").append(((StringProperty) property).getPattern()).append(")@\"");
            } else if (!CollectionUtils.isEmpty(((StringProperty) property).getEnum())) {
                payload.append("\"@matches(").append(((StringProperty) property).getEnum().stream().collect(Collectors.joining("|"))).append(")@\"");
            } else {
                payload.append("\"@notEmpty()@\"");
            }
        } else if (property instanceof DateProperty) {
            payload.append("\"@matchesDatePattern('yyyy-MM-dd')@\"");
        } else if (property instanceof DateTimeProperty) {
            payload.append("\"@matchesDatePattern('yyyy-MM-dd'T'hh:mm:ss')@\"");
        } else if (property instanceof IntegerProperty || property instanceof LongProperty) {
            payload.append("\"@isNumber()@\"");
        } else if (property instanceof FloatProperty || property instanceof DoubleProperty) {
            payload.append("\"@isNumber()@\"");
        } else if (property instanceof BooleanProperty) {
            payload.append("\"@matches(true|false)@\"");
        } else {
            payload.append("\"@ignore@\"");
        }

        return payload.toString();
    }

    /**
     * Create validation expression using functions according to parameter type and format.
     * @param parameter
     * @return
     */
    private String createValidationExpression(AbstractSerializableParameter parameter) {
        switch (parameter.getType()) {
            case "integer":
                return "@isNumber()@";
            case "string":
                if (parameter.getFormat() != null && parameter.getFormat().equals("date")) {
                    return "\"@matchesDatePattern('yyyy-MM-dd')@\"";
                } else if (parameter.getFormat() != null && parameter.getFormat().equals("date-time")) {
                    return "\"@matchesDatePattern('yyyy-MM-dd'T'hh:mm:ss')@\"";
                } else if (StringUtils.hasText(parameter.getPattern())) {
                    return "\"@matches(" + parameter.getPattern() + ")@\"";
                } else if (!CollectionUtils.isEmpty(parameter.getEnum())) {
                    return "\"@matches(" + (parameter.getEnum().stream().collect(Collectors.joining("|"))) + ")@\"";
                } else {
                    return "@notEmpty()@";
                }
            case "boolean":
                return "@matches(true|false)@";
            default:
                return "@ignore@";
        }
    }

    /**
     * Create random value expression using functions according to parameter type and format.
     * @param parameter
     * @return
     */
    private String createRandomValueExpression(AbstractSerializableParameter parameter) {
        switch (parameter.getType()) {
            case "integer":
                return "citrus:randomNumber(10)";
            case "string":
                if (parameter.getFormat() != null && parameter.getFormat().equals("date")) {
                    return "\"citrus:currentDate('yyyy-MM-dd')\"";
                } else if (parameter.getFormat() != null && parameter.getFormat().equals("date-time")) {
                    return "\"citrus:currentDate('yyyy-MM-dd'T'hh:mm:ss')\"";
                } else if (StringUtils.hasText(parameter.getPattern())) {
                    return "\"citrus:randomValue(" + parameter.getPattern() + ")\"";
                } else if (!CollectionUtils.isEmpty(parameter.getEnum())) {
                    return "\"citrus:randomEnumValue(" + (parameter.getEnum().stream().collect(Collectors.joining("|"))) + ")\"";
                } else {
                    return "citrus:randomString(10)";
                }
            case "boolean":
                return "true";
            default:
                return "";
        }
    }

    /**
     * Set the swagger Open API resource to use.
     * @param swaggerResource
     * @return
     */
    public SwaggerXmlTestCreator withSpec(String swaggerResource) {
        this.swaggerResource = swaggerResource;
        return this;
    }

    /**
     * Set the server context path to use.
     * @param contextPath
     * @return
     */
    public SwaggerXmlTestCreator withContextPath(String contextPath) {
        this.nameSuffix = contextPath;
        return this;
    }

    /**
     * Set the test name prefix to use.
     * @param prefix
     * @return
     */
    public SwaggerXmlTestCreator withNamePrefix(String prefix) {
        this.namePrefix = prefix;
        return this;
    }

    /**
     * Set the test name suffix to use.
     * @param suffix
     * @return
     */
    public SwaggerXmlTestCreator withNameSuffix(String suffix) {
        this.nameSuffix = suffix;
        return this;
    }

    /**
     * Set the swagger operation to use.
     * @param operation
     * @return
     */
    public SwaggerXmlTestCreator withOperation(String operation) {
        this.operation = operation;
        return this;
    }

    /**
     * Gets the swaggerResource.
     *
     * @return
     */
    public String getSwaggerResource() {
        return swaggerResource;
    }

    /**
     * Sets the swaggerResource.
     *
     * @param swaggerResource
     */
    public void setSwaggerResource(String swaggerResource) {
        this.swaggerResource = swaggerResource;
    }

    /**
     * Gets the contextPath.
     *
     * @return
     */
    public String getContextPath() {
        return contextPath;
    }

    /**
     * Sets the contextPath.
     *
     * @param contextPath
     */
    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    /**
     * Sets the nameSuffix.
     *
     * @param nameSuffix
     */
    public void setNameSuffix(String nameSuffix) {
        this.nameSuffix = nameSuffix;
    }

    /**
     * Gets the nameSuffix.
     *
     * @return
     */
    public String getNameSuffix() {
        return nameSuffix;
    }

    /**
     * Sets the namePrefix.
     *
     * @param namePrefix
     */
    public void setNamePrefix(String namePrefix) {
        this.namePrefix = namePrefix;
    }

    /**
     * Gets the namePrefix.
     *
     * @return
     */
    public String getNamePrefix() {
        return namePrefix;
    }

    /**
     * Sets the operation.
     *
     * @param operation
     */
    public void setOperation(String operation) {
        this.operation = operation;
    }

    /**
     * Gets the operation.
     *
     * @return
     */
    public String getOperation() {
        return operation;
    }
}
