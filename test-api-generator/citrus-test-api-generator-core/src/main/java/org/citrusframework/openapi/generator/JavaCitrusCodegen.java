package org.citrusframework.openapi.generator;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toMap;
import static org.openapitools.codegen.CliOption.newString;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.openapitools.codegen.CodegenType;
import org.openapitools.codegen.SupportingFile;
import org.openapitools.codegen.languages.AbstractJavaCodegen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Thorsten Schlathoelter
 */
public class JavaCitrusCodegen extends AbstractJavaCodegen {

    private static final Logger logger = LoggerFactory.getLogger(JavaCitrusCodegen.class);

    private static final String ABSTRACT_TEST_REQUEST_JAVA = "AbstractTestRequest.java";
    private static final String API_TYPE_REST = "REST";
    private static final String API_TYPE_SOAP = "SOAP";
    public static final String CODEGEN_NAME = "java-citrus";

    // possible optional parameters
    public static final String API_ENDPOINT = "apiEndpoint";
    public static final String API_TYPE = "apiType";
    public static final String GENERATED_SCHEMA_FOLDER = "generatedSchemaFolder";
    public static final String HTTP_PATH_PREFIX = "httpPathPrefix";
    public static final String OPENAPI_SCHEMA = "openapiSchema";
    public static final String PREFIX = "prefix";
    public static final String RESOURCE_FOLDER = "resourceFolder";
    public static final String SOURCE_FOLDER = "sourceFolder";
    public static final String TARGET_XMLNS_NAMESPACE = "targetXmlnsNamespace";

    // default values for optional parameters
    protected String apiPrefix = "Api";

    protected String httpClient = API_ENDPOINT;
    protected String httpPathPrefix = "api";
    protected String openapiSchema = "oas3";
    protected String resourceFolder =
        "src" + File.separator + "main" + File.separator + "resources";
    protected String generatedSchemaFolder = "schema" + File.separator + "xsd";
    protected String targetXmlnsNamespace;

    protected String apiVersion = "1.0.0";

    public JavaCitrusCodegen() {
        super();
        // the root folder where all files are emitted
        outputFolder = "generated-code" + File.separator + "java";

        // this is the location which templates will be read from in the - resources - directory
        templateDir = CODEGEN_NAME;

        // register additional properties which will be available in the templates
        additionalProperties.put("apiVersion", apiVersion);

        // set default
        additionalProperties.put(API_TYPE, API_TYPE_REST);

        // add additional reserved words used in CitrusAbstractTestRequest and its base class to prevent name collisions
        Set<String> reservedWordsTemp = reservedWords();
        reservedWordsTemp.addAll(
            asList(
                "name",
                "description",
                "actor",
                "httpClient",
                "dataSource",
                "schemaValidation",
                "schema",
                "headerContentType",
                "headerAccept",
                "bodyFile",
                "responseType",
                "responseStatus",
                "responseReasonPhrase",
                "responseVersion",
                "resource",
                "responseVariable",
                "responseValue",
                "cookies",
                "script",
                "type"
            )
        );
        setReservedWordsLowerCase(new ArrayList<>(reservedWordsTemp));

        // add posibility to set a new value for the properties
        cliOptions.add(newString(API_ENDPOINT,
            "Which http client should be used (default " + httpClient + ")."));
        cliOptions.add(
            newString(
                API_TYPE,
                "Specifies the type of API to be generated specify SOAP to generate a SOAP API. By default a REST API will be generated"
            )
        );
        cliOptions.add(
            newString(GENERATED_SCHEMA_FOLDER,
                "The schema output directory (default " + generatedSchemaFolder + ").")
        );
        cliOptions.add(newString(HTTP_PATH_PREFIX,
            "Add a prefix to http path for all APIs (default " + httpPathPrefix + ")."));
        cliOptions.add(newString(OPENAPI_SCHEMA,
            "Which OpenAPI schema should be used (default " + openapiSchema + ")."));
        cliOptions.add(
            newString(
                PREFIX,
                "Add a prefix before the name of the files. First character should be upper case (default "
                    + apiPrefix + ")."
            )
        );
        cliOptions.add(newString(PREFIX, "The api prefix (default " + apiPrefix + ")."));
        cliOptions.add(newString(RESOURCE_FOLDER,
            "Where the resource files are emitted (default " + resourceFolder + ")."));
        cliOptions.add(
            newString(TARGET_XMLNS_NAMESPACE,
                "Xmlns namespace of the schema (default " + targetXmlnsNamespace + ").")
        );
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

        if (additionalProperties.containsKey(API_ENDPOINT)) {
            this.setHttpClient(additionalProperties.get(API_ENDPOINT).toString());
        }
        additionalProperties.put(API_ENDPOINT, httpClient);

        if (additionalProperties.containsKey(GENERATED_SCHEMA_FOLDER)) {
            this.setGeneratedSchemaFolder(
                additionalProperties.get(GENERATED_SCHEMA_FOLDER).toString());
        }
        additionalProperties.put(GENERATED_SCHEMA_FOLDER, generatedSchemaFolder);

        if (additionalProperties.containsKey(HTTP_PATH_PREFIX)) {
            this.setHttpPathPrefix(additionalProperties.get(HTTP_PATH_PREFIX).toString());
            additionalProperties.put(HTTP_PATH_PREFIX, httpPathPrefix);
        } else {
            logger.warn(
                "Using empty http-path-prefix for code generation. A http-path-prefix can be configured using \"{}\" property.",
                HTTP_PATH_PREFIX
            );
            httpPathPrefix = "";
        }

        if (additionalProperties.containsKey(OPENAPI_SCHEMA)) {
            this.setOpenapiSchema(additionalProperties.get(OPENAPI_SCHEMA).toString());
        }
        additionalProperties.put(OPENAPI_SCHEMA, openapiSchema);

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

        if (additionalProperties.containsKey(RESOURCE_FOLDER)) {
            this.setResourceFolder(additionalProperties.get(RESOURCE_FOLDER).toString());
        }
        additionalProperties.put(RESOURCE_FOLDER, resourceFolder);

        if (additionalProperties.containsKey(TARGET_XMLNS_NAMESPACE)) {
            this.setTargetXmlnsNamespace(
                additionalProperties.get(TARGET_XMLNS_NAMESPACE).toString());
        } else {
            this.targetXmlnsNamespace = String.format(
                "http://www.citrusframework.org/citrus-test-schema/%s-api", apiPrefix.toLowerCase());
        }
        additionalProperties.put(TARGET_XMLNS_NAMESPACE, targetXmlnsNamespace);

        // define different folders where the files will be emitted
        final String invokerFolder = (sourceFolder + File.separator + invokerPackage).replace(".",
            File.separator);
        final String citrusFolder = invokerFolder + File.separator + "citrus";
        final String extensionFolder = citrusFolder + File.separator + "extension";
        final String springFolder = invokerFolder + File.separator + "spring";
        final String schemaFolder = resourceFolder + File.separator + generatedSchemaFolder;

        Object apiType = additionalProperties.get(API_TYPE);
        if (API_TYPE_REST.equals(apiType)) {
            addRestSupportingFiles(citrusFolder, schemaFolder);
        } else if (API_TYPE_SOAP.equals(apiType)) {
            addSoapSupportingFiles(citrusFolder, schemaFolder);
        } else {
            throw new IllegalArgumentException(String.format("Unknown API_TYPE: '%s'", apiType));
        }

        addDefaultSupportingFiles(citrusFolder, extensionFolder, springFolder);
    }

    @Override
    public void preprocessOpenAPI(OpenAPI openAPI) {
        super.preprocessOpenAPI(openAPI);

        Info info = openAPI.getInfo();
        Map<String, Object> extensions = info.getExtensions();
        if (extensions != null) {
            additionalProperties.putAll(extensions);

            Map<String, Object> infoExtensions = extensions.entrySet().stream()
                .filter(entry -> entry.getKey().toUpperCase(
                ).startsWith("X-"))
                .collect(toMap(Entry::getKey, Entry::getValue));
            additionalProperties.put("infoExtensions", infoExtensions);
        }
    }

    public void setApiPrefix(String apiPrefix) {
        this.apiPrefix = apiPrefix;
    }

    public String getHttpClient() {
        return httpClient;
    }

    public void setHttpClient(String httpClient) {
        this.httpClient = httpClient;
    }

    public String getHttpPathPrefix() {
        return httpPathPrefix;
    }

    public void setHttpPathPrefix(String httpPathPrefix) {
        this.httpPathPrefix = httpPathPrefix;
    }

    public String getOpenapiSchema() {
        return openapiSchema;
    }

    public void setOpenapiSchema(String openapiSchema) {
        this.openapiSchema = openapiSchema;
    }

    public String getResourceFolder() {
        return resourceFolder;
    }

    public void setResourceFolder(String resourceFolder) {
        this.resourceFolder = resourceFolder;
    }

    public String getGeneratedSchemaFolder() {
        return generatedSchemaFolder;
    }

    public void setGeneratedSchemaFolder(String generatedSchemaFolder) {
        this.generatedSchemaFolder = generatedSchemaFolder;
    }

    public String getTargetXmlnsNamespace() {
        return targetXmlnsNamespace;
    }

    public void setTargetXmlnsNamespace(String targetXmlnsNamespace) {
        this.targetXmlnsNamespace = targetXmlnsNamespace;
    }

    public String getApiPrefix() {
        return apiPrefix;
    }

    private void addRestSupportingFiles(final String citrusFolder, String schemaFolder) {
        supportingFiles.add(new SupportingFile("schema.mustache", schemaFolder,
            apiPrefix.toLowerCase() + "-api.xsd"));
        supportingFiles.add(new SupportingFile("test_base.mustache", citrusFolder,
            apiPrefix + ABSTRACT_TEST_REQUEST_JAVA));
    }

    private void addSoapSupportingFiles(final String citrusFolder, String schemaFolder) {
        // Remove the default api template file
        apiTemplateFiles().remove("api.mustache");
        apiTemplateFiles().put("api_soap.mustache", ".java");

        supportingFiles.add(new SupportingFile("schema_soap.mustache", schemaFolder,
            apiPrefix.toLowerCase() + "-api.xsd"));
        supportingFiles.add(new SupportingFile("api_soap.mustache", citrusFolder,
            apiPrefix + ABSTRACT_TEST_REQUEST_JAVA));
        supportingFiles.add(new SupportingFile("test_base_soap.mustache", citrusFolder,
            apiPrefix + ABSTRACT_TEST_REQUEST_JAVA));
    }

    private void addDefaultSupportingFiles(final String citrusFolder, final String extensionFolder,
        final String springFolder) {
        supportingFiles.add(new SupportingFile("bean_configuration.mustache", springFolder,
            apiPrefix + "BeanConfiguration.java"));
        supportingFiles.add(new SupportingFile("bean_definition_parser.mustache", citrusFolder,
            apiPrefix + "BeanDefinitionParser.java"));
        supportingFiles.add(new SupportingFile("namespace_handler.mustache", extensionFolder,
            apiPrefix + "NamespaceHandler.java"));
        supportingFiles.add(new SupportingFile("api-model.mustache", resourceFolder,
            apiPrefix.toLowerCase() + "-api-model.csv"));
    }

}
