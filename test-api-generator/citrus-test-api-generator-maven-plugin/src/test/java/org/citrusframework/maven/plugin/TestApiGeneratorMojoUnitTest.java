package org.citrusframework.maven.plugin;

import static org.citrusframework.maven.plugin.TestApiGeneratorMojo.DEFAULT_API_PACKAGE;
import static org.citrusframework.maven.plugin.TestApiGeneratorMojo.DEFAULT_API_TYPE;
import static org.citrusframework.maven.plugin.TestApiGeneratorMojo.DEFAULT_INVOKER_PACKAGE;
import static org.citrusframework.maven.plugin.TestApiGeneratorMojo.DEFAULT_META_INF_FOLDER;
import static org.citrusframework.maven.plugin.TestApiGeneratorMojo.DEFAULT_MODEL_PACKAGE;
import static org.citrusframework.maven.plugin.TestApiGeneratorMojo.DEFAULT_RESOURCE_FOLDER;
import static org.citrusframework.maven.plugin.TestApiGeneratorMojo.DEFAULT_SCHEMA_FOLDER_TEMPLATE;
import static org.citrusframework.maven.plugin.TestApiGeneratorMojo.DEFAULT_SOURCE_FOLDER;
import static org.citrusframework.maven.plugin.TestApiGeneratorMojo.DEFAULT_TARGET_NAMESPACE_TEMPLATE;
import static org.citrusframework.maven.plugin.TestApiGeneratorMojo.replaceDynamicVars;
import static org.citrusframework.maven.plugin.TestApiGeneratorMojo.replaceDynamicVarsToLowerCase;
import static java.lang.Boolean.TRUE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.util.ReflectionTestUtils.getField;

import jakarta.validation.constraints.NotNull;
import org.citrusframework.maven.plugin.TestApiGeneratorMojo;
import org.citrusframework.maven.plugin.TestApiGeneratorMojo.ApiConfig;
import org.citrusframework.maven.plugin.TestApiGeneratorMojo.ApiType;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import org.apache.maven.model.Build;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openapitools.codegen.plugin.CodeGenMojo;


/**
 * @author Thorsten Schlathoelter
 */
@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"JUnitMalformedDeclaration", "JUnitMixedFramework"})
class TestApiGeneratorMojoUnitTest extends AbstractMojoTestCase {

    private TestApiGeneratorMojo fixture;

    @Mock
    private Build buildMock;

    @Mock
    private MavenProject mavenProjectMock;

    @Mock
    private MojoExecution mojoExecutionMock;

    @BeforeEach
    void beforeEach() {
        fixture = new TestApiGeneratorMojo();
    }

    static Stream<Arguments> replaceDynamicVarsInPattern() {
        return Stream.of(
            arguments("%PREFIX%-aa-%VERSION%", "MyPrefix", "1", false, "MyPrefix-aa-1"),
            arguments("%PREFIX%-aa-%VERSION%", "MyPrefix", null, false, "MyPrefix-aa"),
            arguments("%PREFIX%/aa/%VERSION%", "MyPrefix", "1", false, "MyPrefix/aa/1"),
            arguments("%PREFIX%/aa/%VERSION%", "MyPrefix", null, false, "MyPrefix/aa"),
            arguments("%PREFIX%.aa.%VERSION%", "MyPrefix", "1", true, "myprefix.aa.1"),
            arguments("%PREFIX%.aa.%VERSION%", "MyPrefix", null, true, "myprefix.aa")
        );
    }

    @ParameterizedTest
    @MethodSource
    void replaceDynamicVarsInPattern(String pattern, String prefix, String version, boolean toLowerCasePrefix, String expectedResult) {

        if (toLowerCasePrefix) {
            assertThat(
                replaceDynamicVarsToLowerCase(pattern, prefix, version)).isEqualTo(expectedResult);
        } else {
            assertThat(
                replaceDynamicVars(pattern, prefix, version)).isEqualTo(expectedResult);
        }
    }

    static Stream<Arguments> configureMojo() {
        return Stream.of(
            arguments("DefaultConfigWithoutVersion", createMinimalApiConfig(null),
                createMinimalCodeGenMojoParams(
                    "schema/xsd",
                    "org.citrusframework.automation.mydefaultprefix",
                    "org.citrusframework.automation.mydefaultprefix.model",
                    "org.citrusframework.automation.mydefaultprefix.api",
                    "http://www.citrusframework.org/citrus-test-schema/mydefaultprefix-api"
                )),
            arguments("DefaultConfigWithVersion", createMinimalApiConfig("v1"),
                createMinimalCodeGenMojoParams(
                    "schema/xsd/v1",
                    "org.citrusframework.automation.mydefaultprefix.v1",
                    "org.citrusframework.automation.mydefaultprefix.v1.model",
                    "org.citrusframework.automation.mydefaultprefix.v1.api",
                    "http://www.citrusframework.org/citrus-test-schema/v1/mydefaultprefix-api"
                )),
            arguments("CustomConfigWithoutVersion", createFullApiConfig(null),
                createCustomCodeGenMojoParams(
                    "schema/xsd",
                    "my.mycustomprefix.invoker.package",
                    "my.mycustomprefix.model.package",
                    "my.mycustomprefix.api.package",
                    "myNamespace/citrus-test-schema/mycustomprefix"
                )),
            arguments("CustomConfigWithVersion", createFullApiConfig("v1"),
                createCustomCodeGenMojoParams(
                    "schema/xsd/v1",
                    "my.mycustomprefix.v1.invoker.package",
                    "my.mycustomprefix.v1.model.package",
                    "my.mycustomprefix.v1.api.package",
                    "myNamespace/citrus-test-schema/mycustomprefix/v1"
                    ))
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource
    void configureMojo(String name, ApiConfig apiConfig, CodeGenMojoParams controlParams) throws MojoExecutionException {
        doReturn("target").when(buildMock).getDirectory();
        doReturn(buildMock).when(mavenProjectMock).getBuild();
        fixture.setMavenProject(mavenProjectMock);
        fixture.setMojoExecution(mojoExecutionMock);

        CodeGenMojo codeGenMojo = fixture.configureCodeGenMojo(apiConfig);
        assertThat(getField(codeGenMojo, "project")).isEqualTo(mavenProjectMock);
        assertThat(getField(codeGenMojo, "mojo")).isEqualTo(mojoExecutionMock);
        assertThat(((File) getField(codeGenMojo, "output"))).hasName(controlParams.output);
        assertThat(getField(codeGenMojo, "inputSpec")).isEqualTo(controlParams.source);
        assertThat(getField(codeGenMojo, "generateSupportingFiles")).isEqualTo(TRUE);
        assertThat(getField(codeGenMojo, "generatorName")).isEqualTo("java-citrus");

        //noinspection unchecked
        assertThat((Map<Object, Object>)getField(codeGenMojo, "configOptions"))
            .containsExactlyInAnyOrderEntriesOf(controlParams.configOptions);
    }

    /**
     * Create an {@link ApiConfig} with the minimal configuration, that is required. All other values will be chosen as defaults.
     */
    @NotNull
    private static ApiConfig createMinimalApiConfig(String version) {
        ApiConfig apiConfig = new ApiConfig();
        apiConfig.setPrefix("MyDefaultPrefix");
        apiConfig.setSource("myDefaultSource");
        apiConfig.setVersion(version);

        return apiConfig;
    }

    /**
     * Create an {@link ApiConfig} with all possible configurations, no defaults.
     */
    @NotNull
    private static ApiConfig createFullApiConfig(String version) {
        ApiConfig apiConfig = new ApiConfig();
        apiConfig.setPrefix("MyCustomPrefix");
        apiConfig.setSource("myCustomSource");
        apiConfig.setApiPackage("my.%PREFIX%.%VERSION%.api.package");
        apiConfig.setInvokerPackage("my.%PREFIX%.%VERSION%.invoker.package");
        apiConfig.setModelPackage("my.%PREFIX%.%VERSION%.model.package");
        apiConfig.setEndpoint("myEndpoint");
        apiConfig.setTargetXmlnsNamespace("myNamespace/citrus-test-schema/%PREFIX%/%VERSION%");
        apiConfig.setUseTags(false);
        apiConfig.setType(ApiType.SOAP);
        apiConfig.setVersion(version);

        return apiConfig;
    }

    @NotNull
    private static CodeGenMojoParams createMinimalCodeGenMojoParams(String schemaFolder, String invokerPackage, String modelPackage, String apiPackage, String targetXmlnsNamespace) {

        Map<String, Object> configOptionsControlMap = new HashMap<>();
        configOptionsControlMap.put("prefix", "MyDefaultPrefix");
        configOptionsControlMap.put("generatedSchemaFolder", schemaFolder);
        configOptionsControlMap.put("invokerPackage", invokerPackage);
        configOptionsControlMap.put("apiPackage", apiPackage);
        configOptionsControlMap.put("modelPackage", modelPackage);
        configOptionsControlMap.put("resourceFolder", "generated-test-resources");
        configOptionsControlMap.put("sourceFolder", "generated-test-sources");
        configOptionsControlMap.put("apiEndpoint", "mydefaultprefixEndpoint");
        configOptionsControlMap.put("targetXmlnsNamespace", targetXmlnsNamespace);
        configOptionsControlMap.put("apiType", "REST");
        configOptionsControlMap.put("useTags", true);

        return  new CodeGenMojoParams("target", "myDefaultSource", configOptionsControlMap);
    }

    @NotNull
    private static CodeGenMojoParams createCustomCodeGenMojoParams(String schemaFolder, String invokerPackage, String modelPackage, String apiPackage, String targetXmlnsNamespace) {

        Map<String, Object> configOptionsControlMap = new HashMap<>();
        configOptionsControlMap.put("prefix", "MyCustomPrefix");
        configOptionsControlMap.put("generatedSchemaFolder", schemaFolder);
        configOptionsControlMap.put("invokerPackage", invokerPackage);
        configOptionsControlMap.put("modelPackage", modelPackage);
        configOptionsControlMap.put("apiPackage", apiPackage);
        configOptionsControlMap.put("resourceFolder", "generated-test-resources");
        configOptionsControlMap.put("sourceFolder", "generated-test-sources");
        configOptionsControlMap.put("apiEndpoint", "myEndpoint");
        configOptionsControlMap.put("targetXmlnsNamespace", targetXmlnsNamespace);
        configOptionsControlMap.put("apiType", "SOAP");
        configOptionsControlMap.put("useTags", false);

        return  new CodeGenMojoParams("target", "myCustomSource", configOptionsControlMap);
    }

    @Nested
    class ApiConfigTest {

        private ApiConfig configFixture;

        @BeforeEach
        void beforeEach() {
            configFixture = new ApiConfig();
        }

        @Test
        void apiPackagePathDefault() {
            assertThat(configFixture.getApiPackage()).isEqualTo(DEFAULT_API_PACKAGE);
        }

        @Test
        void invokerPackagePathDefault() {
            assertThat(configFixture.getInvokerPackage()).isEqualTo(DEFAULT_INVOKER_PACKAGE);
        }

        @Test
        void modelPackagePathDefault() {
            assertThat(configFixture.getModelPackage()).isEqualTo(DEFAULT_MODEL_PACKAGE);
        }

        @Test
        void targetXmlnsNamespaceDefault() {
            assertThat(configFixture.getTargetXmlnsNamespace()).isEqualTo(DEFAULT_TARGET_NAMESPACE_TEMPLATE);
        }

        @Test
        void endpointDefault() {
            configFixture.setPrefix("MyPrefix");
            assertThat(configFixture.qualifiedEndpoint()).isEqualTo("myprefixEndpoint");
        }

        @Test
        void apiTypeDefault() {
            assertThat(configFixture.getType()).isEqualTo(DEFAULT_API_TYPE);
        }

        @Test
        void schemaFolderDefault() {
            assertThat((String)getField(fixture, "schemaFolder")).isEqualTo(DEFAULT_SCHEMA_FOLDER_TEMPLATE);
        }

        @Test
        void sourceFolderDefault() {
            assertThat((String) getField(fixture, "sourceFolder")).isEqualTo(DEFAULT_SOURCE_FOLDER);
        }

        @Test
        void resourceFolderDefault() {
            assertThat((String) getField(fixture, "resourceFolder")).isEqualTo(DEFAULT_RESOURCE_FOLDER);
        }

        @Test
        void metaInfFolderDefault() {
            assertThat((String) getField(fixture, "metaInfFolder")).isEqualTo(DEFAULT_META_INF_FOLDER);
        }
    }

    private record CodeGenMojoParams(String output, String source, Map<String, Object> configOptions) {

    }
}
