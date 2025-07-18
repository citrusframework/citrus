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

package org.citrusframework.maven.plugin;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.annotations.VisibleForTesting;
import jakarta.annotation.Nonnull;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.citrusframework.openapi.generator.WsdlToOpenApiTransformer;
import org.citrusframework.openapi.generator.exception.WsdlToOpenApiTransformationException;
import org.citrusframework.util.StringUtils;
import org.openapitools.codegen.plugin.CodeGenMojo;
import org.sonatype.plexus.build.incremental.BuildContext;
import org.sonatype.plexus.build.incremental.DefaultBuildContext;

import static java.lang.Boolean.TRUE;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.citrusframework.openapi.generator.CitrusJavaCodegen.API_ENDPOINT;
import static org.citrusframework.openapi.generator.CitrusJavaCodegen.API_TYPE;
import static org.citrusframework.openapi.generator.CitrusJavaCodegen.PREFIX;
import static org.citrusframework.openapi.generator.CitrusJavaCodegen.ROOT_CONTEXT_PATH;
import static org.citrusframework.openapi.generator.CitrusJavaCodegen.TARGET_XMLNS_NAMESPACE;
import static org.springframework.util.ReflectionUtils.findField;
import static org.springframework.util.ReflectionUtils.makeAccessible;
import static org.springframework.util.ReflectionUtils.setField;

/**
 * The Citrus OpenAPI Generator Maven Plugin is designed to facilitate the integration of multiple
 * OpenAPI specifications into the Citrus testing framework by automatically generating necessary
 * test classes and XSDs. This plugin wraps the {@code CodeGenMojo} and extends its functionality to
 * support multiple API configurations.
 * <p>
 * Features: - Multiple API Configurations: Easily configure multiple OpenAPI specifications to
 * generate test APIs with specific prefixes. - Citrus Integration: Generates classes and XSDs
 * tailored for use within the Citrus framework, streamlining the process of creating robust
 * integration tests.
 * </p>
 */
@Mojo(
    name = "create-test-api",
    defaultPhase = LifecyclePhase.GENERATE_TEST_SOURCES,
    requiresDependencyCollection = ResolutionScope.TEST,
    requiresDependencyResolution = ResolutionScope.TEST,
    threadSafe = true
)
public class TestApiGeneratorMojo extends AbstractMojo {

    public static final String RESOURCE_FOLDER = "resourceFolder";

    public static final String DEFAULT_BASE_PACKAGE = "org.citrusframework.automation.%PREFIX%.%VERSION%";
    public static final String DEFAULT_INVOKER_PACKAGE = DEFAULT_BASE_PACKAGE;
    public static final String DEFAULT_API_PACKAGE = DEFAULT_BASE_PACKAGE + ".api";
    public static final String DEFAULT_MODEL_PACKAGE = DEFAULT_BASE_PACKAGE + ".model";
    public static final String DEFAULT_SCHEMA_FOLDER_TEMPLATE = "schema/xsd/%VERSION%";
    public static final ApiType DEFAULT_API_TYPE = ApiType.REST;

    /**
     * Marker fragment in the schema name of a generated schema. Used to distinguish generated from
     * non generated values, when manipulating spring meta-data files.
     */
    public static final String CITRUS_TEST_SCHEMA = "citrus-test-schema";

    /**
     * Marker text to mark the next line of a spring.handlers or spring.schemas file not to be
     * removed by the generator.
     */
    public static final String CITRUS_TEST_SCHEMA_KEEP_HINT = "citrus-test-schema-keep";

    /**
     * Specifies the default target namespace template. When changing the default value, it's
     * important to maintain the 'citrus-test-schema' part, as this name serves to differentiate
     * between generated and non-generated schemas. This differentiation aids in the creation of
     * supporting Spring files such as 'spring.handlers' and 'spring.schemas'.
     */
    public static final String DEFAULT_TARGET_NAMESPACE_TEMPLATE =
        "http://www.citrusframework.org/" + CITRUS_TEST_SCHEMA + "/%VERSION%/%PREFIX%-api";

    /**
     * resourceFolder: specifies the location to which the resources are generated. Defaults to
     * 'generated-test-resources'.
     */
    public static final String RESOURCE_FOLDER_PROPERTY = "citrus.test.api.generator.resource.folder";

    /**
     * schemaFolder: specifies the location for the generated xsd schemas. Defaults to
     * 'schema/xsd/%VERSION%'
     */
    public static final String API_SCHEMA_FOLDER = "citrus.test.api.generator.schema.folder";

    /**
     * metaInfFolder: specifies the location to which the resources are generated. Defaults to
     * 'generated-resources'.
     */
    public static final String META_INF_FOLDER = "citrus.test.api.generator.meta.inf.folder";

    /**
     * resourceFolder: specifies the location to which the resources are generated. Defaults to
     * 'generated-resources'.
     */
    public static final String GENERATE_SPRING_INTEGRATION_FILES = "citrus.test.api.generator.generate.spring.integration.files";

    @Component
    private final BuildContext buildContext = new DefaultBuildContext();

    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject mavenProject;

    @Parameter(defaultValue = "${mojoExecution}", readonly = true)
    private MojoExecution mojoExecution;

    @Parameter(name = "output", property = "openapi.generator.maven.plugin.output")
    private File output;

    @Parameter(property = API_SCHEMA_FOLDER, defaultValue = DEFAULT_SCHEMA_FOLDER_TEMPLATE)
    @SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal"})  // Maven injected
    private String schemaFolder = DEFAULT_SCHEMA_FOLDER_TEMPLATE;

    @Parameter(property = META_INF_FOLDER)
    @SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal"})  // Maven injected
    private String metaInfFolder;

    @Parameter(property = GENERATE_SPRING_INTEGRATION_FILES, defaultValue = "true")
    @SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal"})  // Maven injected
    private boolean generateSpringIntegrationFiles = true;

    @Parameter(name = "skip", property = "codegen.skip", defaultValue = "false")
    @SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal"})  // Maven injected
    private Boolean skip;

    @Parameter
    private List<ApiConfig> apis;

    /**
     * Config options used for all APIS may be overwritten by api configOptions
     */
    @Parameter
    protected Map<String, String> globalConfigOptions = new HashMap<>();

    @Parameter
    protected Map<String, String> globalProperties = new HashMap<>();

    /**
     * Replace the placeholders '%PREFIX%' and '%VERSION%' in the given text.
     */
    static String replaceDynamicVars(String text, String prefix, String version) {
        if (text == null) {
            return null;
        }

        return text.replace("%PREFIX%", prefix)
            .replace(".%VERSION%", version != null ? "." + version : "")
            .replace("/%VERSION%", version != null ? "/" + version : "")
            .replace("-%VERSION%", version != null ? "-" + version : "")
            .replace("%VERSION%", version != null ? version : "");
    }

    /**
     * Replace the placeholders '%PREFIX%' and '%VERSION%' in the given text, performing a
     * toLowerCase on the prefix.
     */
    static String replaceDynamicVarsToLowerCase(String text, String prefix, String version) {
        return replaceDynamicVars(text, prefix.toLowerCase(), version);
    }

    protected MavenProject getMavenProject() {
        return mavenProject;
    }

    protected void setMavenProject(MavenProject mavenProject) {
        this.mavenProject = mavenProject;
    }

    public List<ApiConfig> getApiConfigs() {
        return apis;
    }

    @VisibleForTesting
    void setMojoExecution(MojoExecution mojoExecution) {
        this.mojoExecution = mojoExecution;
    }

    /**
     * Returns the fully qualified schema folder
     */
    public String schemaFolder(ApiConfig apiConfig) {
        return replaceDynamicVars(schemaFolder, apiConfig.getPrefix(), apiConfig.getVersion());
    }

    @Override
    public void execute() throws MojoExecutionException {

        for (int index = 0; index < apis.size(); index++) {
            ApiConfig apiConfig = apis.get(index);
            validateApiConfig(index, apiConfig);
            delegateExecution(configureCodeGenMojo(apiConfig));
        }

        if (!TRUE.equals(skip) && generateSpringIntegrationFiles) {
            new SpringMetaFileGenerator(this).generateSpringIntegrationMetaFiles();
        }
    }

    @Nonnull
    public String getMetaInfFolder() {
        if (metaInfFolder == null) {
            Path basePath = Paths.get(mavenProject.getBasedir().toURI());
            Path metaInfResourceFolderPath = Paths.get(
                new File(mavenProject.getBuild().getDirectory(),
                    LifecyclePhase.GENERATE_TEST_SOURCES.id()
                        .equals(mojoExecution.getLifecyclePhase()) ?
                        "generated-test-sources/openapi/src/main/resources"
                        : "generated-sources/openapi/src/main/resources").toURI());
            Resource metaInfResource = new Resource();
            metaInfResource.setDirectory(metaInfResourceFolderPath.toString());
            mavenProject.addResource(metaInfResource);
            metaInfFolder =
                basePath.relativize(metaInfResourceFolderPath) + File.separator
                    + "META-INF";
        }
        return metaInfFolder;
    }

    @VisibleForTesting
    void delegateExecution(CodeGenMojo codeGenMojo) throws MojoExecutionException {
        codeGenMojo.execute();
    }

    CodeGenMojo configureCodeGenMojo(ApiConfig apiConfig) throws MojoExecutionException {

        if (apiConfig.getSource().toUpperCase().trim().endsWith(".WSDL")) {
            apiConfig.source = convertToOpenApi(apiConfig.getSource());
        }

        Map<String, String> apiConfigOptions = createApiConfigOptions(apiConfig);

        CodeGenMojo codeGenMojo = new CodeGenMojoWrapper()
            .schemaFolder(schemaFolder(apiConfig))
            .skip(skip)
            .output(output)
            .mojoExecution(mojoExecution)
            .project(mavenProject)
            .inputSpec(apiConfig.getSource())
            .globalProperties(globalProperties)
            .configOptions(apiConfigOptions);


        codeGenMojo.setPluginContext(getPluginContext());

        if (StringUtils.hasText(apiConfig.rootContextPath)) {
            List<String> properties = new ArrayList<>();
            if (apiConfig.additionalProperties != null) {
                properties.addAll(apiConfig.additionalProperties);
            }
            apiConfig.additionalProperties = properties;
            apiConfig.additionalProperties.add(
                format("%s=%s", ROOT_CONTEXT_PATH, apiConfig.rootContextPath));
        }

        propagateBuildContext(codeGenMojo);
        propagateAdditionalProperties(codeGenMojo, apiConfig.additionalProperties);

        return codeGenMojo;
    }

    private Map<String, String> createApiConfigOptions(ApiConfig apiConfig) {
        Map<String, String> configOptions = new HashMap<>();

        if (LifecyclePhase.GENERATE_TEST_SOURCES.id().equals(mojoExecution.getLifecyclePhase())) {
            configOptions.put("addCompileSourceRoot", "false");
            configOptions.put("addTestCompileSourceRoot", "true");
        }

        //noinspection unchecked
        configOptions.putAll(apiConfig.toConfigOptionsProperties(globalConfigOptions));
        return configOptions;
    }

    private String convertToOpenApi(String source) throws MojoExecutionException {
        String apiFile = source;
        String path = source.replace("\\", "/");

        int lastSegmentIndex = path.lastIndexOf("/");
        if (lastSegmentIndex > 0) {
            apiFile = path.substring(lastSegmentIndex + 1);
        }

        File resourceFile;
        if (new File(source).isAbsolute()) {
            resourceFile = new File(source);
            if (!resourceFile.exists()) {
                throw new MojoExecutionException(
                    "File not found at the provided absolute path: " + source);
            }
        } else {
            URL resourceUrl = getClass().getClassLoader().getResource(source);
            if (resourceUrl == null) {
                throw new MojoExecutionException("Resource not found in classpath: " + source);
            }
            resourceFile = new File(resourceUrl.getFile());
        }

        File tmpDir = new File(mavenProject.getBuild().getDirectory() + "/wsdlToOpenApi");
        if (!tmpDir.exists() && !tmpDir.mkdirs()) {
            throw new MojoExecutionException(
                "Unable to create directory for temporary storage of converted WSDL: "
                    + tmpDir.getAbsolutePath());
        }

        try {
            WsdlToOpenApiTransformer transformer = new WsdlToOpenApiTransformer(
                resourceFile.toURI());

            String openApiContent = transformer.transformToOpenApi();

            Path openApiPath = Paths.get(tmpDir.getAbsolutePath(), apiFile);
            Files.writeString(openApiPath, openApiContent, StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);

            return openApiPath.toString();
        } catch (WsdlToOpenApiTransformationException | IOException e) {
            throw new MojoExecutionException(
                String.format("Unable to transform WSDL %s to OpenAPI", source), e);
        }
    }

    /**
     * Reflectively inject the buildContext. This is set by maven and needs to be propagated into
     * the plugin, that actually performs the work.
     */
    private void propagateBuildContext(CodeGenMojo codeGenMojo) {

        Field buildContextField = findField(CodeGenMojo.class, "buildContext");
        makeAccessible(requireNonNull(buildContextField,
            "Could not retrieve 'buildContext' field from CodeGenMojo delegate."));
        setField(buildContextField, codeGenMojo, buildContext);
    }

    /**
     * Reflectively inject the additionProperties.
     */
    private void propagateAdditionalProperties(CodeGenMojo codeGenMojo,
        List<String> additionalProperties) {

        Field buildContextField = findField(CodeGenMojo.class, "additionalProperties");
        makeAccessible(requireNonNull(buildContextField,
            "Could not retrieve 'additionalProperties' field from CodeGenMojo delegate."));
        setField(buildContextField, codeGenMojo, additionalProperties);
    }

    private void validateApiConfig(int apiIndex, ApiConfig apiConfig)
        throws MojoExecutionException {
        requireNonBlankParameter("prefix", apiIndex, apiConfig.getPrefix());
        requireNonBlankParameter("source", apiIndex, apiConfig.getSource());
    }

    private void requireNonBlankParameter(String name, int index, String parameterValue)
        throws MojoExecutionException {
        if (isBlank(parameterValue)) {
            throw new MojoExecutionException(
                format("Required parameter '%s' not set for api at index '%d'!", name, index));
        }
    }

    public enum ApiType {
        REST, SOAP
    }

    /**
     * Note that the default values are not properly set by maven processor. Therefore, the default
     * values have been assigned additionally on field level.
     */
    public static class ApiConfig {

        public static final String DEFAULT_ENDPOINT = "PREFIX_ENDPOINT";

        /**
         * prefix: specifies the prefixed used for the test api. Typically, an acronym for the
         * application which is being tested.
         */
        public static final String API_PREFIX_PROPERTY = "citrus.test.api.generator.prefix";

        /**
         * rootContextPath: specifies the context path that will be prepended to all OpenAPI
         * operation paths.
         */
        public static final String ROOT_CONTEXT_PATH = "citrus.test.api.generator.root-context-path";

        /**
         * source: specifies the source of the test api.
         */
        public static final String API_SOURCE_PROPERTY = "citrus.test.api.generator.source";

        /**
         * version: specifies the version of the api. May be null.
         */
        public static final String API_VERSION_PROPERTY = "citrus.test.api.generator.version";

        /**
         * endpoint: specifies the endpoint of the test api. Defaults to 'prefixEndpoint'.
         */
        public static final String API_ENDPOINT_PROPERTY = "citrus.test.api.generator.endpoint";

        /**
         * type: specifies the type of the test api. Defaults to 'REST'
         */
        public static final String API_TYPE_PROPERTY = "citrus.test.api.generator.type";

        /**
         * useTags: specifies whether tags should be used by the generator. Defaults to 'true'. If
         * useTags is set to true, the generator will organize the generated code based on the tags
         * defined in your API specification.
         */
        public static final String API_USE_TAGS_PROPERTY = "citrus.test.api.generator.use.tags";

        /**
         * invokerPackage: specifies the package for the test api classes. Defaults to
         * 'org.citrusframework.automation.%PREFIX%.%VERSION%'.
         */
        public static final String API_INVOKER_PACKAGE_PROPERTY = "citrus.test.api.generator.invoker.package";

        /**
         * apiPackage: specifies the package for the test api classes. Defaults to
         * 'org.citrusframework.automation.%PREFIX%.%VERSION%.api'.
         */
        public static final String API_API_PACKAGE_PROPERTY = "citrus.test.api.generator.api.package";

        /**
         * modelPackage: specifies the package for the test api classes. Defaults to
         * 'org.citrusframework.automation.%PREFIX%.%VERSION%.model'.
         */
        public static final String API_MODEL_PACKAGE_PROPERTY = "citrus.test.api.generator.model.package";

        /**
         * targetXmlNamespace: specifies the xml namespace to be used by the api. Defaults to
         * 'http://www.citrusframework.org/schema/%VERSION%/%PREFIX%-api'
         */
        @SuppressWarnings("JavadocLinkAsPlainText")
        public static final String API_NAMESPACE_PROPERTY = "citrus.test.api.generator.namespace";

        @Parameter(required = true, property = API_PREFIX_PROPERTY)
        private String prefix;

        @Parameter(required = true, property = ROOT_CONTEXT_PATH)
        private String rootContextPath;

        @Parameter(required = true, property = API_SOURCE_PROPERTY)
        private String source;

        @Parameter(property = API_VERSION_PROPERTY)
        private String version;

        @Parameter(property = API_ENDPOINT_PROPERTY, defaultValue = DEFAULT_ENDPOINT)
        private String endpoint = DEFAULT_ENDPOINT;

        @Parameter(property = API_TYPE_PROPERTY, defaultValue = "REST")
        private ApiType type = DEFAULT_API_TYPE;

        @Parameter(property = API_USE_TAGS_PROPERTY, defaultValue = "true")
        private boolean useTags = true;

        @Parameter(property = API_INVOKER_PACKAGE_PROPERTY, defaultValue = DEFAULT_INVOKER_PACKAGE)
        private String invokerPackage = DEFAULT_INVOKER_PACKAGE;

        @Parameter(property = API_API_PACKAGE_PROPERTY, defaultValue = DEFAULT_API_PACKAGE)
        private String apiPackage = DEFAULT_API_PACKAGE;

        @Parameter(property = API_MODEL_PACKAGE_PROPERTY, defaultValue = DEFAULT_MODEL_PACKAGE)
        private String modelPackage = DEFAULT_MODEL_PACKAGE;

        @Parameter(property = API_NAMESPACE_PROPERTY, defaultValue = DEFAULT_TARGET_NAMESPACE_TEMPLATE)
        private String targetXmlnsNamespace = DEFAULT_TARGET_NAMESPACE_TEMPLATE;

        @SuppressWarnings("rawtypes")
        @Parameter(name = "apiConfigOptions")
        private Map<String, String> apiConfigOptions;

        @SuppressWarnings("rawtypes")
        @Parameter(name = "additionalProperties")
        private List<String> additionalProperties;

        public String getPrefix() {
            return prefix;
        }

        public void setPrefix(String prefix) {
            this.prefix = prefix;
        }

        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getEndpoint() {
            return endpoint;
        }

        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }

        public ApiType getType() {
            return type;
        }

        public void setType(ApiType type) {
            this.type = type;
        }

        public boolean isUseTags() {
            return useTags;
        }

        public void setUseTags(boolean useTags) {
            this.useTags = useTags;
        }

        public String getInvokerPackage() {
            return invokerPackage;
        }

        public void setInvokerPackage(String invokerPackage) {
            this.invokerPackage = invokerPackage;
        }

        public String getApiPackage() {
            return apiPackage;
        }

        public void setApiPackage(String apiPackage) {
            this.apiPackage = apiPackage;
        }

        public String getModelPackage() {
            return modelPackage;
        }

        public void setModelPackage(String modelPackage) {
            this.modelPackage = modelPackage;
        }

        public String getTargetXmlnsNamespace() {
            return targetXmlnsNamespace;
        }

        public void setTargetXmlnsNamespace(String targetXmlnsNamespace) {
            this.targetXmlnsNamespace = targetXmlnsNamespace;
        }

        public String qualifiedEndpoint() {
            return DEFAULT_ENDPOINT.equals(endpoint) ? getPrefix().toLowerCase() + "Endpoint"
                : endpoint;
        }

        public void setRootContextPath(String rootContextPath) {
            this.rootContextPath = rootContextPath;
        }

        public void setApiConfigOptions(Map<String, String> apiConfigOptions) {
            this.apiConfigOptions = apiConfigOptions;
        }

        public Map<String, String> getApiConfigOptions() {
            return apiConfigOptions;
        }

        public void setAdditionalProperties(List<String> additionalProperties) {
            this.additionalProperties = additionalProperties;
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        Map toConfigOptionsProperties(Map globalConfigOptions) {
            Map configOptionsProperties = new HashMap<>();
            configOptionsProperties.put(PREFIX, prefix);
            configOptionsProperties.put(API_ENDPOINT, qualifiedEndpoint());
            configOptionsProperties.put(API_TYPE, type.toString());
            configOptionsProperties.put(TARGET_XMLNS_NAMESPACE,
                replaceDynamicVarsToLowerCase(targetXmlnsNamespace, prefix, version));
            configOptionsProperties.put("invokerPackage",
                replaceDynamicVarsToLowerCase(invokerPackage, prefix, version));
            configOptionsProperties.put("apiPackage",
                replaceDynamicVarsToLowerCase(apiPackage, prefix, version));
            configOptionsProperties.put("modelPackage",
                replaceDynamicVarsToLowerCase(modelPackage, prefix, version));
            configOptionsProperties.put("useTags", useTags);

            if (globalConfigOptions != null) {
                configOptionsProperties.putAll(globalConfigOptions);
            }

            if (apiConfigOptions != null) {
                configOptionsProperties.putAll(apiConfigOptions);
            }

            return configOptionsProperties;
        }

    }
}
