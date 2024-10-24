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

package org.citrusframework.openapi.generator;

import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.servers.Server;
import lombok.Getter;
import lombok.Setter;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.openapi.testapi.RestApiReceiveMessageActionBuilder;
import org.citrusframework.openapi.testapi.RestApiSendMessageActionBuilder;
import org.citrusframework.openapi.testapi.SoapApiReceiveMessageActionBuilder;
import org.citrusframework.openapi.testapi.SoapApiSendMessageActionBuilder;
import org.openapitools.codegen.CodegenOperation;
import org.openapitools.codegen.CodegenParameter;
import org.openapitools.codegen.CodegenSecurity;
import org.openapitools.codegen.CodegenType;
import org.openapitools.codegen.SupportingFile;
import org.openapitools.codegen.languages.AbstractJavaCodegen;
import org.openapitools.codegen.model.ModelMap;
import org.openapitools.codegen.model.OperationMap;
import org.openapitools.codegen.model.OperationsMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import static java.lang.Boolean.TRUE;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toMap;
import static org.citrusframework.util.ReflectionHelper.copyFields;
import static org.citrusframework.util.StringUtils.appendSegmentToUrlPath;
import static org.citrusframework.util.StringUtils.convertFirstChartToUpperCase;
import static org.openapitools.codegen.CliOption.newString;
import static org.openapitools.codegen.utils.CamelizeOption.LOWERCASE_FIRST_LETTER;
import static org.openapitools.codegen.utils.StringUtils.camelize;

@Getter
@Setter
public class CitrusJavaCodegen extends AbstractJavaCodegen {

    public static final String CODEGEN_NAME = "java-citrus";
    public static final String API_TYPE_REST = "REST";
    public static final String API_TYPE_SOAP = "SOAP";
    public static final String API_ENDPOINT = "apiEndpoint";
    public static final String API_TYPE = "apiType";
    public static final String GENERATED_SCHEMA_FOLDER = "generatedSchemaFolder";
    public static final String PREFIX = "prefix";
    public static final String RESOURCE_FOLDER = "resourceFolder";
    public static final String SOURCE_FOLDER = "sourceFolder";
    public static final String TARGET_XMLNS_NAMESPACE = "targetXmlnsNamespace";
    public static final String REQUEST_BUILDER_CLASS = "requestBuilderClass";
    public static final String RESPONSE_BUILDER_CLASS = "responseBuilderClass";
    public static final String REQUEST_BUILDER_CLASS_NAME = "requestBuilderClassName";
    public static final String RESPONSE_BUILDER_CLASS_NAME = "responseBuilderClassName";
    private static final Logger logger = LoggerFactory.getLogger(CitrusJavaCodegen.class);
    protected String apiPrefix = "Api";

    protected String httpClient = API_ENDPOINT;

    protected String resourceFolder =
            "src" + File.separator + "main" + File.separator + "resources";
    protected String generatedSchemaFolder = "schema" + File.separator + "xsd";
    protected String targetXmlnsNamespace;

    protected String apiVersion = "1.0.0";
    private String invokerFolder;
    private String springFolder;
    private String schemaFolder;

    public CitrusJavaCodegen() {
        super();

        templateDir = CODEGEN_NAME;

        configureAdditionalProperties();
        configureReservedWords();
        configureCliOptions();
        configureTypeMappings();
    }

    private static void postProcessSecurityParameters(
            CustomCodegenOperation customCodegenOperation) {
        customCodegenOperation.hasApiKeyAuth = customCodegenOperation.authMethods.stream()
                .anyMatch(codegenSecurity -> codegenSecurity.isApiKey);

        customCodegenOperation.authWithParameters = customCodegenOperation.hasApiKeyAuth;
        for (CodegenSecurity codegenSecurity : customCodegenOperation.authMethods) {
            if (TRUE.equals(codegenSecurity.isBasicBasic)) {
                customCodegenOperation.optionalAndAuthParameterNames.add(
                        "basicAuthUsername");
                customCodegenOperation.optionalAndAuthParameterNames.add(
                        "basicAuthPassword");
                customCodegenOperation.authWithParameters = true;
            } else if (TRUE.equals(codegenSecurity.isApiKey)) {
                customCodegenOperation.optionalAndAuthParameterNames.add(
                        camelize(codegenSecurity.keyParamName, LOWERCASE_FIRST_LETTER));
                customCodegenOperation.authWithParameters = true;
            } else if (TRUE.equals(codegenSecurity.isBasicBearer)) {
                customCodegenOperation.optionalAndAuthParameterNames.add(
                        "basicAuthBearer");
                customCodegenOperation.authWithParameters = true;
            }
        }
    }

    private void configureAdditionalProperties() {
        additionalProperties.put("apiVersion", apiVersion);
        additionalProperties.put(API_TYPE, API_TYPE_REST);
        additionalProperties.put("useJakartaEe", true);
    }

    private void configureReservedWords() {
        Set<String> reservedWordsTemp = reservedWords();
        reservedWordsTemp.addAll(
                asList(
                        "name",
                        "description",
                        "httpClient",
                        "message",
                        "endpoint",
                        "validate",
                        "validator",
                        "validators",
                        "process",
                        "selector",
                        "transform",
                        "build",
                        "actor",
                        "process")
        );
        setReservedWordsLowerCase(new ArrayList<>(reservedWordsTemp));
    }

    private void configureCliOptions() {
        cliOptions.add(
                newString(API_ENDPOINT,
                        "Which http client should be used (default " + httpClient + ")."));
        cliOptions.add(
                newString(API_TYPE,
                        "Specifies the type of API to be generated specify SOAP to generate a SOAP API. By default a REST API will be generated"
                )
        );
        cliOptions.add(
                newString(GENERATED_SCHEMA_FOLDER,
                        "The schema output directory (default " + generatedSchemaFolder + ").")
        );
        cliOptions.add(
                newString(PREFIX,
                        "Add a prefix before the name of the files. First character should be upper case (default "
                                + apiPrefix + ")."
                )
        );
        cliOptions.add(newString(PREFIX, "The api prefix (default " + apiPrefix + ")."));
        cliOptions.add(
                newString(RESOURCE_FOLDER,
                        "Where the resource files are emitted (default " + resourceFolder + ")."));
        cliOptions.add(
                newString(TARGET_XMLNS_NAMESPACE,
                        "Xmlns namespace of the schema (default " + targetXmlnsNamespace + ").")
        );
    }

    private void configureTypeMappings() {
        this.typeMapping.put("binary", "Resource");
        this.typeMapping.put("file", "Resource");
    }

    /**
     * Returns human-friendly help for the generator. Provide the consumer with help tips,
     * parameters here
     *
     * @return A string value for the help message
     */
    @Override
    public String getHelp() {
        return "Generates citrus api requests.";
    }

    /**
     * Configures a friendly name for the generator. This will be used by the generator to select
     * the library with the -g flag.
     *
     * @return the friendly name for the generator
     */
    @Override
    public String getName() {
        return CODEGEN_NAME;
    }

    /**
     * Configures the type of generator.
     *
     * @return the CodegenType for this generator
     * @see org.openapitools.codegen.CodegenType
     */
    @Override
    public CodegenType getTag() {
        return CodegenType.CLIENT;
    }

    @Override
    public void processOpts() {
        super.processOpts();
        setupEndpoint();
        setupApiPrefix();
        setupNamespace();
        setupFolders();
        setupApiType();
        addDefaultSupportingFiles();
        writeApiToResourceFolder();
    }

    private void setupEndpoint() {
        if (additionalProperties.containsKey(API_ENDPOINT)) {
            this.setHttpClient(additionalProperties.get(API_ENDPOINT).toString());
        }
        additionalProperties.put(API_ENDPOINT, httpClient);
    }

    private void setupApiPrefix() {
        if (additionalProperties.containsKey(PREFIX)) {
            this.setApiPrefix(additionalProperties.get(PREFIX).toString());
            additionalProperties.put(PREFIX, apiPrefix);
            additionalProperties.put(PREFIX + "LowerCase", apiPrefix.toLowerCase());
        } else {
            logger.warn(
                    "Using empty prefix for code generation. A prefix can be configured using \"{}\" property.",
                    PREFIX);
            apiPrefix = "";
        }
    }

    private void setupNamespace() {
        if (additionalProperties.containsKey(TARGET_XMLNS_NAMESPACE)) {
            this.setTargetXmlnsNamespace(
                    additionalProperties.get(TARGET_XMLNS_NAMESPACE).toString());
        } else {
            this.targetXmlnsNamespace = format(
                    "http://www.citrusframework.org/citrus-test-schema/%s-api",
                    apiPrefix.toLowerCase());
        }
        additionalProperties.put(TARGET_XMLNS_NAMESPACE, targetXmlnsNamespace);
    }

    private void setupFolders() {
        if (additionalProperties.containsKey(GENERATED_SCHEMA_FOLDER)) {
            this.setGeneratedSchemaFolder(
                    additionalProperties.get(GENERATED_SCHEMA_FOLDER).toString());
        }
        additionalProperties.put(GENERATED_SCHEMA_FOLDER, generatedSchemaFolder);

        if (additionalProperties.containsKey(RESOURCE_FOLDER)) {
            this.setResourceFolder(additionalProperties.get(RESOURCE_FOLDER).toString());
        }
        additionalProperties.put(RESOURCE_FOLDER, resourceFolder);

        invokerFolder = (sourceFolder + File.separator + invokerPackage).replace(".",
                File.separator);
        springFolder = invokerFolder + File.separator + "spring";
        schemaFolder = resourceFolder + File.separator + generatedSchemaFolder;
    }

    private void setupApiType() {
        Object apiType = additionalProperties.get(API_TYPE);
        if (API_TYPE_REST.equals(apiType)) {
            setupRestApiType(springFolder, schemaFolder);
        } else if (API_TYPE_SOAP.equals(apiType)) {
            setupSoapApiType(springFolder, schemaFolder);
        } else {
            throw new IllegalArgumentException(format("Unknown API_TYPE: '%s'", apiType));
        }
    }

    private void setupSoapApiType(String springFolder, String schemaFolder) {
        additionalProperties.put(REQUEST_BUILDER_CLASS,
                SoapApiSendMessageActionBuilder.class.getName());
        additionalProperties.put(REQUEST_BUILDER_CLASS_NAME,
                SoapApiSendMessageActionBuilder.class.getSimpleName());
        additionalProperties.put(RESPONSE_BUILDER_CLASS,
                SoapApiReceiveMessageActionBuilder.class.getName());
        additionalProperties.put(RESPONSE_BUILDER_CLASS_NAME,
                SoapApiReceiveMessageActionBuilder.class.getSimpleName());
        additionalProperties.put("isRest", false);
        additionalProperties.put("isSoap", true);
        addSoapSupportingFiles(springFolder, schemaFolder);
    }

    private void setupRestApiType(String springFolder, String schemaFolder) {
        additionalProperties.put(REQUEST_BUILDER_CLASS,
                RestApiSendMessageActionBuilder.class.getName());
        additionalProperties.put(REQUEST_BUILDER_CLASS_NAME,
                RestApiSendMessageActionBuilder.class.getSimpleName());
        additionalProperties.put(RESPONSE_BUILDER_CLASS,
                RestApiReceiveMessageActionBuilder.class.getName());
        additionalProperties.put(RESPONSE_BUILDER_CLASS_NAME,
                RestApiReceiveMessageActionBuilder.class.getSimpleName());
        additionalProperties.put("isRest", true);
        additionalProperties.put("isSoap", false);

        addRestSupportingFiles(springFolder, schemaFolder);
    }

    /**
     * Store a copy of the source open api as resource.
     */
    private void writeApiToResourceFolder() {

        String directoryPath = appendSegmentToUrlPath(getOutputDir(), getResourceFolder());
        directoryPath = appendSegmentToUrlPath(directoryPath,
                invokerPackage.replace('.', File.separatorChar));

        String filename = getApiPrefix() + "_openApi.yaml";

        File directory = new File(directoryPath);
        if (!directory.exists() && !directory.mkdirs()) {
            throw new CitrusRuntimeException("Unable to create directory for api resource!");
        }

        File file = new File(directory, filename);

        try (FileWriter writer = new FileWriter(file)) {
            String yamlContent = Yaml.pretty(openAPI);
            writer.write(yamlContent);
        } catch (IOException e) {
            throw new CitrusRuntimeException(
                    "Unable to write OpenAPI to resource folder: " + file.getAbsolutePath());
        }
    }

    @Override
    public void preprocessOpenAPI(OpenAPI openAPI) {
        super.preprocessOpenAPI(openAPI);

        Info info = openAPI.getInfo();
        Map<String, Object> extensions = info.getExtensions();
        if (extensions != null) {
            additionalProperties.putAll(extensions);

            Map<String, Object> infoExtensions = extensions.entrySet().stream()
                    .filter(entry -> entry.getKey().toUpperCase().startsWith("X-"))
                    .collect(toMap(Entry::getKey, Entry::getValue));
            additionalProperties.put("infoExtensions", infoExtensions);
        }
    }

    private void addRestSupportingFiles(String springFolder,
                                        String schemaFolder) {

        supportingFiles.add(new SupportingFile("namespace_handler.mustache", springFolder,
                convertFirstChartToUpperCase(apiPrefix) + "NamespaceHandler.java"));
        supportingFiles.add(new SupportingFile("schema.mustache", schemaFolder,
                apiPrefix.toLowerCase() + "-api.xsd"));
    }

    private void addSoapSupportingFiles(String springFolder, String schemaFolder) {
        // Remove the default api template file
        apiTemplateFiles().remove("api.mustache");
        apiTemplateFiles().put("api_soap.mustache", ".java");

        supportingFiles.add(new SupportingFile("namespace_handler_soap.mustache", springFolder,
                convertFirstChartToUpperCase(apiPrefix) + "NamespaceHandler.java"));
        supportingFiles.add(new SupportingFile("schema_soap.mustache", schemaFolder,
                apiPrefix.toLowerCase() + "-api.xsd"));
    }

    private void addDefaultSupportingFiles() {
        supportingFiles.add(new SupportingFile("api_locator.mustache", invokerFolder,
                convertFirstChartToUpperCase(apiPrefix) + ".java"));
        supportingFiles.add(new SupportingFile("bean_configuration.mustache", springFolder,
                convertFirstChartToUpperCase(apiPrefix) + "BeanConfiguration.java"));
    }

    @Override
    public CodegenParameter fromRequestBody(RequestBody body, Set<String> imports,
                                            String bodyParameterName) {
        CodegenParameter codegenParameter = super.fromRequestBody(body, imports, bodyParameterName);
        return convertToCustomCodegenParameter(codegenParameter);
    }

    @Override
    public CodegenParameter fromFormProperty(String name, Schema propertySchema,
                                             Set<String> imports) {
        CodegenParameter codegenParameter = super.fromFormProperty(name, propertySchema, imports);
        return convertToCustomCodegenParameter(codegenParameter);
    }

    @Override
    public CodegenParameter fromParameter(Parameter parameter, Set<String> imports) {
        CodegenParameter codegenParameter = super.fromParameter(parameter, imports);

        if ("File".equals(codegenParameter.dataType)) {
            codegenParameter.dataType = "Resource";
        }

        return convertToCustomCodegenParameter(codegenParameter);
    }

    /**
     * Converts given codegenParameter to a custom {@link CustomCodegenParameter} to provide
     * additional derived properties.
     */
    private CustomCodegenParameter convertToCustomCodegenParameter(
            CodegenParameter codegenParameter) {
        CustomCodegenParameter customCodegenParameter = new CustomCodegenParameter();
        copyFields(CodegenParameter.class, codegenParameter, customCodegenParameter);

        customCodegenParameter.isBaseTypeString = codegenParameter.isString || "String".equals(
                codegenParameter.baseType);

        return customCodegenParameter;
    }

    @Override
    public CodegenOperation fromOperation(String path,
                                          String httpMethod,
                                          Operation operation,
                                          List<Server> servers) {
        CodegenOperation op = super.fromOperation(path, httpMethod, operation, servers);
        return convertToCustomCodegenOperation(op);

    }

    /**
     * Converts given codegenOperation to a custom {@link CustomCodegenOperation} to provide
     * additional derived properties.
     */
    private CustomCodegenOperation convertToCustomCodegenOperation(
            CodegenOperation codegenOperation) {

        CustomCodegenOperation customOperation = new CustomCodegenOperation();

        copyFields(CodegenOperation.class, codegenOperation, customOperation);

        customOperation.requiredNonBodyParams.addAll(customOperation.requiredParams
                .stream()
                .filter(param -> !param.isBodyParam).toList());

        customOperation.needsConstructorWithAllStringParameter =
                !customOperation.requiredParams.isEmpty() &&
                        customOperation.requiredParams
                                .stream()
                                .anyMatch(
                                        param -> !param.isBodyParam && !"String".equals(param.dataType));

        if (customOperation.optionalParams != null) {
            customOperation.optionalAndAuthParameterNames.addAll(
                    customOperation.optionalParams.stream()
                            .map(codegenParameter ->
                                    toVarName(codegenParameter.nameInCamelCase))
                            .toList());
        }

        return customOperation;
    }

    @Override
    public OperationsMap postProcessOperationsWithModels(OperationsMap objs,
                                                         List<ModelMap> allModels) {
        OperationsMap operationsMap = super.postProcessOperationsWithModels(objs, allModels);

        OperationMap operations = objs.getOperations();
        List<CodegenOperation> operationList = operations.getOperation();
        if (operationList != null) {
            operationList.forEach(codegenOperation -> {
                if (codegenOperation instanceof CustomCodegenOperation customCodegenOperation
                        && customCodegenOperation.authMethods != null) {
                    postProcessSecurityParameters(customCodegenOperation);
                }
            });
        }

        return operationsMap;
    }

    static class CustomCodegenOperation extends CodegenOperation {

        private final List<CodegenParameter> requiredNonBodyParams;

        /**
         * List of all optional parameters plus all authentication specific parameter names.
         */
        private final List<String> optionalAndAuthParameterNames;

        private boolean needsConstructorWithAllStringParameter;

        private boolean authWithParameters;

        private boolean hasApiKeyAuth;

        public CustomCodegenOperation() {
            super();
            requiredNonBodyParams = new ArrayList<>();
            optionalAndAuthParameterNames = new ArrayList<>();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            if (!super.equals(o)) {
                return false;
            }
            CustomCodegenOperation that = (CustomCodegenOperation) o;
            return needsConstructorWithAllStringParameter
                    == that.needsConstructorWithAllStringParameter
                    && hasApiKeyAuth == that.hasApiKeyAuth && Objects.equals(requiredNonBodyParams,
                    that.requiredNonBodyParams) && Objects.equals(optionalAndAuthParameterNames,
                    that.optionalAndAuthParameterNames);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), requiredNonBodyParams,
                    optionalAndAuthParameterNames,
                    needsConstructorWithAllStringParameter, hasApiKeyAuth);
        }
    }

    static class CustomCodegenParameter extends CodegenParameter {

        boolean isBaseTypeString;

        public CustomCodegenParameter() {
            super();
        }

        @Override
        public CustomCodegenParameter copy() {
            CodegenParameter copy = super.copy();
            CustomCodegenParameter customCopy = new CustomCodegenParameter();

            copyFields(CodegenParameter.class, copy, customCopy);
            customCopy.isBaseTypeString = isBaseTypeString;

            return customCopy;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            if (!super.equals(o)) {
                return false;
            }
            CustomCodegenParameter that = (CustomCodegenParameter) o;
            return isBaseTypeString == that.isBaseTypeString;
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), isBaseTypeString);
        }
    }
}
