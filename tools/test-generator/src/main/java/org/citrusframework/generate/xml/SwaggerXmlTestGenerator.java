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

package org.citrusframework.generate.xml;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import io.swagger.models.ArrayModel;
import io.swagger.models.HttpMethod;
import io.swagger.models.Model;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.RefModel;
import io.swagger.models.Response;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.AbstractSerializableParameter;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.HeaderParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.PathParameter;
import io.swagger.models.parameters.QueryParameter;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.BooleanProperty;
import io.swagger.models.properties.DateProperty;
import io.swagger.models.properties.DateTimeProperty;
import io.swagger.models.properties.DoubleProperty;
import io.swagger.models.properties.FloatProperty;
import io.swagger.models.properties.IntegerProperty;
import io.swagger.models.properties.LongProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.properties.StringProperty;
import io.swagger.parser.SwaggerParser;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.generate.SwaggerTestGenerator;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.model.testcase.http.ObjectFactory;
import org.citrusframework.spi.Resource;
import org.citrusframework.spi.Resources;
import org.citrusframework.util.FileUtils;
import org.citrusframework.util.StringUtils;
import org.citrusframework.variable.dictionary.json.JsonPathMappingDataDictionary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

/**
 * Test generator creates one to many test cases based on operations defined in a XML schema XSD.
 * @author Christoph Deppisch
 * @since 2.7.4
 */
public class SwaggerXmlTestGenerator extends MessagingXmlTestGenerator<SwaggerXmlTestGenerator> implements SwaggerTestGenerator<SwaggerXmlTestGenerator> {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(SwaggerXmlTestGenerator.class);

    private String swaggerResource;

    private String contextPath;
    private String operation;

    private String namePrefix;
    private String nameSuffix = "_IT";

    private JsonPathMappingDataDictionary inboundDataDictionary = new JsonPathMappingDataDictionary();
    private JsonPathMappingDataDictionary outboundDataDictionary = new JsonPathMappingDataDictionary();

    @Override
    public void create() {
        Swagger swagger;
        try {
            swagger = new SwaggerParser().parse(FileUtils.readToString(Resources.create(swaggerResource)));
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to parse Swagger Open API specification: " + swaggerResource, e);
        }

        if (!StringUtils.hasText(namePrefix)) {
            withNamePrefix(Optional.ofNullable(swagger.getInfo().getTitle()).orElse("Swagger").replaceAll("\\s", "") + "_");
        }

        for (Map.Entry<String, Path> path : swagger.getPaths().entrySet()) {
            for (Map.Entry<HttpMethod, Operation> operation : path.getValue().getOperationMap().entrySet()) {

                // Now generate it
                withName(namePrefix + operation.getValue().getOperationId() + nameSuffix);

                HttpMessage requestMessage = new HttpMessage();

                if (getMode().equals(GeneratorMode.CLIENT)) {
                    String randomizedPath = path.getKey();
                    if (operation.getValue().getParameters() != null) {
                        List<PathParameter> pathParams = operation.getValue().getParameters().stream()
                                .filter(p -> p instanceof PathParameter)
                                .map(PathParameter.class::cast)
                                .collect(Collectors.toList());

                        for (PathParameter parameter : pathParams) {
                            randomizedPath = randomizedPath.replaceAll("\\{" + parameter.getName() + "\\}", createRandomValueExpression(parameter));
                        }
                    }

                    requestMessage.path(Optional.ofNullable(contextPath).orElse("") + Optional.ofNullable(swagger.getBasePath()).filter(basePath -> !basePath.equals("/")).orElse("") + randomizedPath);
                } else {
                    requestMessage.path("@assertThat(matchesPath(" + path.getKey() + "))@");
                }
                requestMessage.method(org.springframework.http.HttpMethod.valueOf(operation.getKey().name()));

                if (operation.getValue().getParameters() != null) {
                    operation.getValue().getParameters().stream()
                            .filter(p -> p instanceof HeaderParameter)
                            .filter(Parameter::getRequired)
                            .forEach(p -> requestMessage.setHeader(p.getName(), getMode().equals(GeneratorMode.CLIENT) ? createRandomValueExpression(((HeaderParameter) p).getItems(), swagger.getDefinitions(), false) : createValidationExpression(((HeaderParameter) p).getItems(), swagger.getDefinitions(), false)));

                    operation.getValue().getParameters().stream()
                            .filter(param -> param instanceof QueryParameter)
                            .filter(Parameter::getRequired)
                            .forEach(param -> requestMessage.queryParam(param.getName(), getMode().equals(GeneratorMode.CLIENT) ? createRandomValueExpression((QueryParameter) param) : createValidationExpression((QueryParameter) param)));

                    operation.getValue().getParameters().stream()
                            .filter(p -> p instanceof BodyParameter)
                            .filter(Parameter::getRequired)
                            .findFirst()
                            .ifPresent(p -> requestMessage.setPayload(getMode().equals(GeneratorMode.CLIENT) ? createOutboundPayload(((BodyParameter) p).getSchema(), swagger.getDefinitions()) : createInboundPayload(((BodyParameter) p).getSchema(), swagger.getDefinitions())));
                }
                withRequest(requestMessage);

                HttpMessage responseMessage = new HttpMessage();
                if (operation.getValue().getResponses() != null) {
                    Response response = operation.getValue().getResponses().get("200");
                    if (response == null) {
                        response = operation.getValue().getResponses().get("default");
                    }

                    if (response != null) {
                        responseMessage.status(HttpStatus.OK);

                        if (response.getHeaders() != null) {
                            for (Map.Entry<String, Property> header : response.getHeaders().entrySet()) {
                                responseMessage.setHeader(header.getKey(), getMode().equals(GeneratorMode.CLIENT) ? createValidationExpression(header.getValue(), swagger.getDefinitions(), false) : createRandomValueExpression(header.getValue(), swagger.getDefinitions(), false));
                            }
                        }

                        if (response.getSchema() != null) {
                            responseMessage.setPayload(getMode().equals(GeneratorMode.CLIENT) ? createInboundPayload(response.getSchema(), swagger.getDefinitions()): createOutboundPayload(response.getSchema(), swagger.getDefinitions()));
                        }
                    }
                }
                withResponse(responseMessage);

                super.create();

                logger.info("Successfully created new test case " + getTargetPackage() + "." + getName());
            }
        }
    }

    @Override
    protected List<String> getMarshallerContextPaths() {
        List<String> contextPaths = super.getMarshallerContextPaths();
        contextPaths.add(ObjectFactory.class.getPackage().getName());
        return contextPaths;
    }

    @Override
    protected List<Resource> getMarshallerSchemas() {
        List<Resource> schemas = super.getMarshallerSchemas();
        schemas.add(Resources.fromClasspath("org/citrusframework/schema/citrus-http-testcase.xsd"));
        return schemas;
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
            payload.append(createRandomValueExpression(property, definitions, true));
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
            } else if (((StringProperty)((StringProperty) property).getEnum()) != null && !((StringProperty) property).getEnum().isEmpty()) {
                payload.append("citrus:randomEnumValue(").append(((StringProperty) property).getEnum().stream().map(value -> "'" + value + "'").collect(Collectors.joining(","))).append(")");
            } else if (Optional.ofNullable(property.getFormat()).orElse("").equalsIgnoreCase("uuid")) {
                payload.append("citrus:randomUUID()");
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
                    payload.append("\"").append(entry.getKey()).append("\": ").append(createValidationExpression(entry.getValue(), definitions, true)).append(",");
                }
            }

            if (payload.toString().endsWith(",")) {
                payload.replace(payload.length() - 1, payload.length(), "");
            }

            payload.append("}");
        } else if (property instanceof ArrayProperty) {
            payload.append("[");
            payload.append(createValidationExpression(((ArrayProperty) property).getItems(), definitions, true));
            payload.append("]");
        } else {
            payload.append(createValidationExpression(property, definitions, false));
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
            payload.append(createValidationExpression(((ArrayModel) model).getItems(), definitions, true));
            payload.append("]");
        } else {
            payload.append("{");

            if (model.getProperties() != null) {
                for (Map.Entry<String, Property> entry : model.getProperties().entrySet()) {
                    payload.append("\"").append(entry.getKey()).append("\": ").append(createValidationExpression(entry.getValue(), definitions, true)).append(",");
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
     * @param quotes
     * @return
     */
    private String createValidationExpression(Property property, Map<String, Model> definitions, boolean quotes) {
        StringBuilder payload = new StringBuilder();
        if (property instanceof RefProperty) {
            Model model = definitions.get(((RefProperty) property).getSimpleRef());
            payload.append("{");

            if (model.getProperties() != null) {
                for (Map.Entry<String, Property> entry : model.getProperties().entrySet()) {
                    payload.append("\"").append(entry.getKey()).append("\": ").append(createValidationExpression(entry.getValue(), definitions, quotes)).append(",");
                }
            }

            if (payload.toString().endsWith(",")) {
                payload.replace(payload.length() - 1, payload.length(), "");
            }

            payload.append("}");
        } else if (property instanceof ArrayProperty) {
            if (quotes) {
                payload.append("\"");
            }

            payload.append("@ignore@");

            if (quotes) {
                payload.append("\"");
            }
        } else if (property instanceof StringProperty) {
            if (quotes) {
                payload.append("\"");
            }

            if (StringUtils.hasText(((StringProperty) property).getPattern())) {
                payload.append("@matches(").append(((StringProperty) property).getPattern()).append(")@");
            } else if (((StringProperty) property).getEnum() != null && !((StringProperty) property).getEnum().isEmpty()) {
                payload.append("@matches(").append(((StringProperty) property).getEnum().stream().collect(Collectors.joining("|"))).append(")@");
            } else {
                payload.append("@notEmpty()@");
            }

            if (quotes) {
                payload.append("\"");
            }
        } else if (property instanceof DateProperty) {
            if (quotes) {
                payload.append("\"");
            }

            payload.append("@matchesDatePattern('yyyy-MM-dd')@");

            if (quotes) {
                payload.append("\"");
            }
        } else if (property instanceof DateTimeProperty) {
            if (quotes) {
                payload.append("\"");
            }

            payload.append("@matchesDatePattern('yyyy-MM-dd'T'hh:mm:ss')@");

            if (quotes) {
                payload.append("\"");
            }
        } else if (property instanceof IntegerProperty || property instanceof LongProperty) {
            if (quotes) {
                payload.append("\"");
            }

            payload.append("@isNumber()@");

            if (quotes) {
                payload.append("\"");
            }
        } else if (property instanceof FloatProperty || property instanceof DoubleProperty) {
            if (quotes) {
                payload.append("\"");
            }

            payload.append("@isNumber()@");

            if (quotes) {
                payload.append("\"");
            }
        } else if (property instanceof BooleanProperty) {
            if (quotes) {
                payload.append("\"");
            }

            payload.append("@matches(true|false)@");

            if (quotes) {
                payload.append("\"");
            }
        } else {
            if (quotes) {
                payload.append("\"");
            }

            payload.append("@ignore@");

            if (quotes) {
                payload.append("\"");
            }
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
                } else if (parameter.getEnum() != null && !parameter.getEnum().isEmpty()) {
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
                } else if (parameter.getEnum() != null && !parameter.getEnum().isEmpty()) {
                    return "\"citrus:randomEnumValue(" + (parameter.getEnum().stream().collect(Collectors.joining(","))) + ")\"";
                } else if (Optional.ofNullable(parameter.getFormat()).orElse("").equalsIgnoreCase("uuid")){
                    return "citrus:randomUUID()";
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
    public SwaggerXmlTestGenerator withSpec(String swaggerResource) {
        this.swaggerResource = swaggerResource;
        return this;
    }

    /**
     * Set the server context path to use.
     * @param contextPath
     * @return
     */
    public SwaggerXmlTestGenerator withContextPath(String contextPath) {
        this.nameSuffix = contextPath;
        return this;
    }

    /**
     * Set the test name prefix to use.
     * @param prefix
     * @return
     */
    public SwaggerXmlTestGenerator withNamePrefix(String prefix) {
        this.namePrefix = prefix;
        return this;
    }

    /**
     * Set the test name suffix to use.
     * @param suffix
     * @return
     */
    public SwaggerXmlTestGenerator withNameSuffix(String suffix) {
        this.nameSuffix = suffix;
        return this;
    }

    /**
     * Set the swagger operation to use.
     * @param operation
     * @return
     */
    public SwaggerXmlTestGenerator withOperation(String operation) {
        this.operation = operation;
        return this;
    }

    /**
     * Add inbound JsonPath expression mappings to manipulate inbound message content.
     * @param mappings
     * @return
     */
    public SwaggerXmlTestGenerator withInboundMappings(Map<String, String> mappings) {
        this.inboundDataDictionary.getMappings().putAll(mappings);
        return this;
    }

    /**
     * Add outbound JsonPath expression mappings to manipulate outbound message content.
     * @param mappings
     * @return
     */
    public SwaggerXmlTestGenerator withOutboundMappings(Map<String, String> mappings) {
        this.outboundDataDictionary.getMappings().putAll(mappings);
        return this;
    }

    /**
     * Add inbound JsonPath expression mappings file to manipulate inbound message content.
     * @param mappingFile
     * @return
     */
    public SwaggerXmlTestGenerator withInboundMappingFile(String mappingFile) {
        this.inboundDataDictionary.setMappingFile(Resources.create(mappingFile));
        this.inboundDataDictionary.initialize();
        return this;
    }

    /**
     * Add outbound JsonPath expression mappings file to manipulate outbound message content.
     * @param mappingFile
     * @return
     */
    public SwaggerXmlTestGenerator withOutboundMappingFile(String mappingFile) {
        this.outboundDataDictionary.setMappingFile(Resources.create(mappingFile));
        this.outboundDataDictionary.initialize();
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
