package org.citrusframework.maven.plugin;

import static com.google.common.collect.Streams.concat;
import static java.lang.Boolean.TRUE;
import static java.util.Arrays.stream;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.citrusframework.maven.plugin.TestApiGeneratorMojo.CITRUS_TEST_SCHEMA;
import static org.citrusframework.maven.plugin.TestApiGeneratorMojo.replaceDynamicVars;
import static org.citrusframework.maven.plugin.TestApiGeneratorMojo.replaceDynamicVarsToLowerCase;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.springframework.test.util.ReflectionTestUtils.getField;

import jakarta.validation.constraints.NotNull;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.project.MavenProject;
import org.assertj.core.api.Assertions;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.TestCaseFailedException;
import org.citrusframework.maven.plugin.TestApiGeneratorMojo.ApiConfig;
import org.citrusframework.maven.plugin.TestApiGeneratorMojo.ApiType;
import org.citrusframework.maven.plugin.stubs.CitrusOpenApiGeneratorMavenProjectStub;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class TestApiGeneratorMojoIntegrationTest extends AbstractMojoTestCase {

    public static final String OTHER_META_FILE_CONTENT = "somenamespace=somevalue";

    public static final String OTHER_CITRUS_META_FILE_CONTENT = String.format("somenamespace/%s/aa=somevalue", CITRUS_TEST_SCHEMA);

    /**
     * Array containing path templates for each generated file, specified with tokens. Tokens can be replaced with values of the respective
     * testing scenario.
     */
     private static final String[] STANDARD_FILE_PATH_TEMPLATES = new String[]{
        "%TARGET_FOLDER%/%GENERATED_SOURCES_FOLDER%/%INVOKER_FOLDER%/citrus/extension/%CAMEL_PREFIX%NamespaceHandler.java",
        "%TARGET_FOLDER%/%GENERATED_SOURCES_FOLDER%/%INVOKER_FOLDER%/citrus/%CAMEL_PREFIX%AbstractTestRequest.java",
        "%TARGET_FOLDER%/%GENERATED_SOURCES_FOLDER%/%INVOKER_FOLDER%/citrus/%CAMEL_PREFIX%BeanDefinitionParser.java",
        "%TARGET_FOLDER%/%GENERATED_SOURCES_FOLDER%/%INVOKER_FOLDER%/spring/%CAMEL_PREFIX%BeanConfiguration.java",
        "%TARGET_FOLDER%/%GENERATED_SOURCES_FOLDER%/%MODEL_FOLDER%/PingReqType.java",
        "%TARGET_FOLDER%/%GENERATED_SOURCES_FOLDER%/%MODEL_FOLDER%/PingRespType.java",
        "%TARGET_FOLDER%/%GENERATED_SOURCES_FOLDER%/%REQUEST_FOLDER%/PingApi.java",
        "%TARGET_FOLDER%/%GENERATED_SOURCES_FOLDER%/%REQUEST_FOLDER%/PungApi.java",
        "%TARGET_FOLDER%/%GENERATED_RESOURCES_FOLDER%/%SCHEMA_FOLDER%/%LOWER_PREFIX%-api.xsd",
        "%TARGET_FOLDER%/%GENERATED_RESOURCES_FOLDER%/%LOWER_PREFIX%-api-model.csv"
    };

    /**
     * Array containing path templates for each generated spring meta file, specified with tokens. Tokens can be replaced with values of the respective
     * testing scenario.
     */
    private static final String[] SPRING_META_FILE_TEMPLATES = new String[]{
        "%BASE_FOLDER%/%META_INF_FOLDER%/spring.handlers",
        "%BASE_FOLDER%/%META_INF_FOLDER%/spring.schemas"
    };

    private TestApiGeneratorMojo fixture;

    @BeforeEach
    @SuppressWarnings("JUnitMixedFramework")
    void beforeEachSetup() throws Exception {
        setUp();
    }

    static Stream<Arguments> executeMojoWithConfigurations() {
        return Stream.of(
            arguments("pom-missing-prefix",
                new MojoExecutionException("Required parameter 'prefix' not set for api at index '0'!")),
            arguments("pom-missing-source",
                new MojoExecutionException("Required parameter 'source' not set for api at index '0'!")),
            arguments("pom-minimal-config", null),
            arguments("pom-minimal-with-version-config", null),
            arguments("pom-multi-config", null),
            arguments("pom-full-config", null),
            arguments("pom-full-with-version-config", null),
            arguments("pom-soap-config", null)
        );
    }

    @ParameterizedTest
    @MethodSource
    void executeMojoWithConfigurations(String configName, Exception expectedException)
        throws Exception {

        try {
            fixture = fixtureFromPom(configName);
        } catch (MojoExecutionException | MojoFailureException e) {
            Assertions.fail("Test setup failed!", e);
        }

        @SuppressWarnings("unchecked")
        List<ApiConfig> apiConfigs = (List<ApiConfig>) getField(fixture, "apis");

        assertThat(apiConfigs).isNotNull();

        if (expectedException == null) {
            // Given
            writeSomeValuesToSpringMetaFiles(apiConfigs);

            // When
            assertThatCode(() -> fixture.execute()).doesNotThrowAnyException();

            // Then
            for (ApiConfig apiConfig : apiConfigs) {
                assertFilesGenerated(apiConfig);
                assertSpecificFileContent(apiConfig);
            }
        } else {
            // When/Then
            assertThatThrownBy(() -> fixture.execute()).isInstanceOf(expectedException.getClass())
                .hasMessage(expectedException.getMessage());
        }
    }

    /**
     * Writes values to spring meta files, to make sure existing non generated and existing generated values are treated properly.
     */
    private void writeSomeValuesToSpringMetaFiles(List<ApiConfig> apiConfigs) {
        for (ApiConfig apiConfig : apiConfigs) {
            for (String filePathTemplate : SPRING_META_FILE_TEMPLATES) {

                String filePath = resolveFilePath(apiConfig, filePathTemplate);
                File file = new File(filePath);
                if (!file.getParentFile().exists() && !new File(filePath).getParentFile().mkdirs()) {
                    Assertions.fail("Unable to prepare test data.");
                }

                try (FileWriter fileWriter = new FileWriter(filePath)) {
                    fileWriter.append(String.format("%s%n", OTHER_META_FILE_CONTENT));
                    fileWriter.append(String.format("%s%n", OTHER_CITRUS_META_FILE_CONTENT));
                } catch (IOException e) {
                    throw new CitrusRuntimeException("Unable to write spring meta files", e);
                }
            }
        }
    }

    private void assertFilesGenerated(ApiConfig apiConfig) {

        for (String filePathTemplate : STANDARD_FILE_PATH_TEMPLATES) {
            String filePath = resolveFilePath(apiConfig, filePathTemplate);
            assertThat(new File(filePath)).isFile().exists();
        }

        if (TRUE.equals(getField(fixture, "generateSpringIntegrationFiles"))) {
            for (String filePathTemplate : SPRING_META_FILE_TEMPLATES) {
                String filePath = resolveFilePath(apiConfig, filePathTemplate);
                assertThat(new File(filePath)).isFile().exists();
            }
        }
    }

    private void assertSpecificFileContent(ApiConfig apiConfig) {
        try {
            assertEndpointName(apiConfig);
            assertTargetNamespace(apiConfig);
            assertApiType(apiConfig);
            assertSchemasInSpringSchemas(apiConfig);
            assertHandlersInSpringHandlers(apiConfig);
        } catch (IOException e) {
            throw new TestCaseFailedException(e);
        }
    }

    private void assertHandlersInSpringHandlers(ApiConfig apiConfig) throws IOException {
        String targetNamespace = replaceDynamicVarsToLowerCase(apiConfig.getTargetXmlnsNamespace(), apiConfig.getPrefix(), apiConfig.getVersion());
        targetNamespace = targetNamespace.replace(":", "\\:");
        String invokerPackage = replaceDynamicVarsToLowerCase(apiConfig.getInvokerPackage(), apiConfig.getPrefix(), apiConfig.getVersion());

        String text = String.format("%s=%s.citrus.extension.%sNamespaceHandler", targetNamespace, invokerPackage, apiConfig.getPrefix());

        assertThat(getContentOfFile(apiConfig, "spring.handlers")).contains(text);

        // Other specific meta info should be retained
        assertThat(getContentOfFile(apiConfig, "spring.handlers")).contains(OTHER_META_FILE_CONTENT);
        // Other citrus generated meta info should be deleted
        assertThat(getContentOfFile(apiConfig, "spring.handlers")).doesNotContain(OTHER_CITRUS_META_FILE_CONTENT);
    }

    private void assertSchemasInSpringSchemas(ApiConfig apiConfig) throws IOException {

        String targetNamespace = replaceDynamicVarsToLowerCase(apiConfig.getTargetXmlnsNamespace(), apiConfig.getPrefix(), apiConfig.getVersion());
        targetNamespace = targetNamespace.replace(":", "\\:");
        String schemaPath = replaceDynamicVarsToLowerCase((String)getField(fixture, "schemaFolder"), apiConfig.getPrefix(), apiConfig.getVersion());

        String text = String.format("%s.xsd=%s/%s-api.xsd", targetNamespace, schemaPath, apiConfig.getPrefix().toLowerCase());

        // Other specific meta info should be retained assertThat(getContentOfFile(apiConfig, "spring.schemas")).contains(OTHER_META_FILE_CONTENT);
        assertThat(getContentOfFile(apiConfig, "spring.schemas")).contains(String.format("%s", text));
        // Other citrus generated meta info should be deleted
        assertThat(getContentOfFile(apiConfig, "spring.schemas")).doesNotContain(OTHER_CITRUS_META_FILE_CONTENT);
    }

    private void assertApiType(ApiConfig apiConfig) throws IOException {
        String text;
        switch (apiConfig.getType()) {
            case REST -> text = "HttpClient httpClient";
            case SOAP -> text = "WebServiceClient wsClient";
            default -> throw new IllegalArgumentException(String.format("No apiTye set in ApiConfig. Expected one of %s",
                stream(ApiType.values()).map(ApiType::toString).collect(
                    Collectors.joining())));
        }
        assertThat(getContentOfFile(apiConfig, "AbstractTestRequest.java")).contains(text);
    }

    private void assertTargetNamespace(ApiConfig apiConfig) throws IOException {
        assertThat(getContentOfFile(apiConfig, "-api.xsd")).contains(
            String.format("targetNamespace=\"%s\"",
                replaceDynamicVarsToLowerCase(apiConfig.getTargetXmlnsNamespace(), apiConfig.getPrefix(), apiConfig.getVersion())));
    }

    private void assertEndpointName(ApiConfig apiConfig) throws IOException {
        assertThat(getContentOfFile(apiConfig, "AbstractTestRequest")).contains(
            String.format("@Qualifier(\"%s\")", apiConfig.qualifiedEndpoint()));
    }

    private String getContentOfFile(ApiConfig apiConfig, String fileIdentifier) throws IOException {
        String filePathTemplate = getTemplateContaining(fileIdentifier);
        String filePath = resolveFilePath(apiConfig, filePathTemplate);

        File file = new File(filePath);

        assertThat(file).exists();
        Path path = Paths.get(filePath);
        byte[] bytes = Files.readAllBytes(path);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private String getTemplateContaining(String text) {
        return concat(stream(STANDARD_FILE_PATH_TEMPLATES), stream(SPRING_META_FILE_TEMPLATES))
            .filter(path -> path.contains(text)).findFirst()
            .orElseThrow(() -> new AssertionError(String.format("Can't find file template with content: '%s'", text)));
    }

    @NotNull
    private String resolveFilePath(ApiConfig apiConfig, String filePathTemplate) {

        String lowerCasePrefix = apiConfig.getPrefix().toLowerCase();
        char[] prefixCharArray = apiConfig.getPrefix().toCharArray();
        prefixCharArray[0] = Character.toUpperCase(prefixCharArray[0]);
        String camelCasePrefix = new String(prefixCharArray);

        String invokerFolder = toFolder(
            replaceDynamicVarsToLowerCase(apiConfig.getInvokerPackage(), apiConfig.getPrefix(), apiConfig.getVersion()));
        String modelFolder = toFolder(
            replaceDynamicVarsToLowerCase(apiConfig.getModelPackage(), apiConfig.getPrefix(), apiConfig.getVersion()));
        String requestFolder = toFolder(
            replaceDynamicVarsToLowerCase(apiConfig.getApiPackage(), apiConfig.getPrefix(), apiConfig.getVersion()));
        String schemaFolder = toFolder(
            replaceDynamicVars((String)getField(fixture, "schemaFolder"), apiConfig.getPrefix(), apiConfig.getVersion()));
        String generatedSourcesFolder = toFolder(
            replaceDynamicVars((String)getField(fixture, "sourceFolder"), apiConfig.getPrefix(), apiConfig.getVersion()));
        String generatedResourcesFolder = toFolder(
            replaceDynamicVars((String)getField(fixture, "resourceFolder"), apiConfig.getPrefix(), apiConfig.getVersion()));

        return filePathTemplate
            .replace("%BASE_FOLDER%", fixture.getMavenProject().getBasedir().getPath())
            .replace("%TARGET_FOLDER%", fixture.getMavenProject().getBuild().getDirectory())
            .replace("%SOURCE_FOLDER%", fixture.getMavenProject().getBuild().getSourceDirectory())
            .replace("%GENERATED_SOURCES_FOLDER%", generatedSourcesFolder)
            .replace("%GENERATED_RESOURCES_FOLDER%", generatedResourcesFolder)
            .replace("%INVOKER_FOLDER%", invokerFolder)
            .replace("%MODEL_FOLDER%", modelFolder)
            .replace("%REQUEST_FOLDER%", requestFolder)
            .replace("%SCHEMA_FOLDER%", schemaFolder)
            .replace("%LOWER_PREFIX%", lowerCasePrefix)
            .replace("%CAMEL_PREFIX%", camelCasePrefix)
            .replace("%META_INF_FOLDER%", toFolder((String) getField(fixture, "metaInfFolder")));
    }

    private String toFolder(String text) {

        if (text == null) {
            return "";
        }

        return text.replace(".", "/");
    }

    private TestApiGeneratorMojo fixtureFromPom(String configName) throws Exception {
        String goal = "create-test-api";

        File pomFile = new File(getBasedir(), String.format("src/test/resources/%s/%s", getClass().getSimpleName(), configName + ".xml"));
        assertThat(pomFile).exists();

        MavenProject mavenProject = new CitrusOpenApiGeneratorMavenProjectStub(configName);

        TestApiGeneratorMojo testApiGeneratorMojo = (TestApiGeneratorMojo) lookupMojo(goal, pomFile);
        testApiGeneratorMojo.setMavenProject(mavenProject);
        testApiGeneratorMojo.setMojoExecution(newMojoExecution(goal));

        return testApiGeneratorMojo;
    }

}
