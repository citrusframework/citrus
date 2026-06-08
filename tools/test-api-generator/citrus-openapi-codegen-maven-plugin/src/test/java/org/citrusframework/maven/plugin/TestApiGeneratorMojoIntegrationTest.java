package org.citrusframework.maven.plugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import jakarta.validation.constraints.NotNull;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.project.MavenProject;
import org.assertj.core.api.Assertions;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.TestCaseFailedException;
import org.citrusframework.maven.plugin.TestApiGeneratorMojo.ApiConfig;
import org.citrusframework.maven.plugin.stubs.CitrusOpenApiGeneratorMavenProjectStub;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.openapitools.codegen.plugin.CodeGenMojo;
import org.springframework.test.util.ReflectionTestUtils;

import static com.google.common.collect.Streams.concat;
import static java.lang.Boolean.TRUE;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.citrusframework.maven.plugin.TestApiGeneratorMojo.CITRUS_TEST_SCHEMA;
import static org.citrusframework.maven.plugin.TestApiGeneratorMojo.replaceDynamicVars;
import static org.citrusframework.maven.plugin.TestApiGeneratorMojo.replaceDynamicVarsToLowerCase;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.springframework.test.util.ReflectionTestUtils.getField;

public class TestApiGeneratorMojoIntegrationTest extends AbstractMojoTestCase {

    public static final String OTHER_META_FILE_CONTENT = "somenamespace=somevalue";

    public static final String OTHER_CITRUS_META_FILE_CONTENT = String.format(
        "somenamespace/%s/aa=somevalue", CITRUS_TEST_SCHEMA);

    /**
     * Array containing path templates for each generated file, specified with tokens. Tokens can be
     * replaced with values of the respective testing scenario.
     */
    private static final String[] STANDARD_FILE_PATH_TEMPLATES = new String[]{
        "%TARGET_FOLDER%/%GENERATED_SOURCES_FOLDER%/%INVOKER_FOLDER%/%CAMEL_PREFIX%OpenApi.java",
        "%TARGET_FOLDER%/%GENERATED_SOURCES_FOLDER%/%INVOKER_FOLDER%/spring/%CAMEL_PREFIX%NamespaceHandler.java",
        "%TARGET_FOLDER%/%GENERATED_SOURCES_FOLDER%/%INVOKER_FOLDER%/spring/%CAMEL_PREFIX%BeanConfiguration.java",
        "%TARGET_FOLDER%/%GENERATED_SOURCES_FOLDER%/%MODEL_FOLDER%/PingReqType.java",
        "%TARGET_FOLDER%/%GENERATED_SOURCES_FOLDER%/%MODEL_FOLDER%/PingRespType.java",
        "%TARGET_FOLDER%/%GENERATED_SOURCES_FOLDER%/%REQUEST_FOLDER%/PingApi.java",
        "%TARGET_FOLDER%/%GENERATED_SOURCES_FOLDER%/%REQUEST_FOLDER%/PungApi.java",
        "%TARGET_FOLDER%/%GENERATED_RESOURCES_FOLDER%/%SCHEMA_FOLDER%/%LOWER_PREFIX%-api.xsd"
    };

    /**
     * Array containing path templates for each generated spring meta file, specified with tokens.
     * Tokens can be replaced with values of the respective testing scenario.
     */
    private static final String[] SPRING_META_FILE_TEMPLATES = new String[]{
        "%BASE_FOLDER%/%META_INF_FOLDER%/spring.handlers",
        "%BASE_FOLDER%/%META_INF_FOLDER%/spring.schemas"
    };

    private TestApiGeneratorMojo fixture;

    static Stream<Arguments> executeMojoWithConfigurations() {
        return Stream.of(
            arguments("pom-missing-prefix",
                new MojoExecutionException(
                    "Required parameter 'prefix' not set for api at index '0'!"),
                emptyMap(), emptyMap(), emptyList(), emptyList()),
            arguments("pom-missing-source",
                new MojoExecutionException(
                    "Required parameter 'source' not set for api at index '0'!"),
                emptyMap(), emptyMap(), emptyList(), emptyList()),
            arguments("pom-minimal-config", null,
                emptyMap(), emptyMap(), emptyList(), emptyList()),
            arguments("pom-minimal-with-version-config", null,
                emptyMap(), emptyMap(), emptyList(), emptyList()),
            arguments("pom-multi-config", null,
                emptyMap(), emptyMap(), emptyList(), emptyList()),
            arguments("pom-full-config", null,
                Map.of("debugOpenAPI", "true", "debugModels", "true"),
                Map.of("a", "b", "other", "otherOption"),
                List.of("a=b", "c=d", "rootContextPath=/a/b/c/d"), emptyList()),
            arguments("pom-full-with-version-config", null,
                emptyMap(), emptyMap(), emptyList(), emptyList()),
            arguments("pom-soap-config", null,
                emptyMap(), emptyMap(), emptyList(), emptyList()),
            arguments("pom-with-global-properties", null,
                Map.of("debugOpenAPI", "true", "debugModels", "true"),
                emptyMap(), emptyList(), emptyList()),
            arguments("pom-with-global-config", null,
                emptyMap(),
                Map.of("a", "b", "other", "otherOption"), emptyList(), emptyList()),
            arguments("pom-with-overriding-config", null,
                Map.of("debugOpenAPI", "true", "debugModels", "true"),
                Map.of("a", "b", "c", "d", "other", "otherOption"), emptyList(), emptyList()),
            arguments("pom-with-additional-properties", null,
                emptyMap(), emptyMap(),
                List.of("a=b", "c=d"), emptyList()),
            arguments("pom-soap-from-wsdl-config", null,
                emptyMap(), emptyMap(), emptyList(), emptyList()),
            arguments("pom-no-model-config", null,
                emptyMap(), emptyMap(), emptyList(), List.of(
                    "%TARGET_FOLDER%/%GENERATED_SOURCES_FOLDER%/%MODEL_FOLDER%/PingReqType.java",
                    "%TARGET_FOLDER%/%GENERATED_SOURCES_FOLDER%/%MODEL_FOLDER%/PingRespType.java"
                    )),
            arguments("pom-suppress-validation-error-config", null,
                emptyMap(), emptyMap(), emptyList(), emptyList())

        );
    }

    @BeforeEach
    @SuppressWarnings("JUnitMixedFramework")
    void beforeEachSetup() throws Exception {
        setUp();
    }

    @ParameterizedTest
    @MethodSource
    void executeMojoWithConfigurations(String configName, Exception expectedException,
        Map<?, ?> expectedGlobalProperties, Map<?, ?> expectedConfigOptions,
        List<String> expectedAdditionalProperties, List<String> unexpectedFiles) throws Exception {
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
                assertFilesGenerated(apiConfig, unexpectedFiles);
                assertSpecificFileContent(apiConfig);
            }

            ArgumentCaptor<CodeGenMojo> codegenMojoCaptor = ArgumentCaptor.captor();
            verify(fixture, atLeastOnce()).delegateExecution(codegenMojoCaptor.capture());

            CodeGenMojo codeGenMojo = codegenMojoCaptor.getValue();
            @SuppressWarnings("rawtypes")
            Map globalProperties = (Map) ReflectionTestUtils.getField(codeGenMojo,
                "globalProperties");
            @SuppressWarnings("rawtypes")
            Map configOptions = (Map) ReflectionTestUtils.getField(codeGenMojo, "configOptions");

            //noinspection unchecked
            assertThat(globalProperties).containsExactlyInAnyOrderEntriesOf(expectedGlobalProperties);
            //noinspection unchecked
            assertThat(configOptions).containsAllEntriesOf(expectedConfigOptions);
            //noinspection unchecked
            List<String> additionalPropertyList = (List<String>) ReflectionTestUtils.getField(
                codeGenMojo, "additionalProperties");

            if (expectedAdditionalProperties.isEmpty()) {
                assertThat(additionalPropertyList).isNull();
            } else {
                assertThat(additionalPropertyList).containsExactlyElementsOf(expectedAdditionalProperties);
            }

        } else {
            // When/Then
            assertThatThrownBy(() -> fixture.execute())
                .isInstanceOf(expectedException.getClass())
                .hasMessage(expectedException.getMessage());
        }
    }

    /**
     * Writes values to spring meta files, to make sure existing non generated and existing
     * generated values are treated properly.
     */
    private void writeSomeValuesToSpringMetaFiles(List<ApiConfig> apiConfigs) {
        for (ApiConfig apiConfig : apiConfigs) {
            for (String filePathTemplate : SPRING_META_FILE_TEMPLATES) {
                String filePath = resolveFilePath(apiConfig, filePathTemplate);
                File file = new File(filePath);
                if (!file.getParentFile().exists() && !new File(filePath).getParentFile()
                    .mkdirs()) {
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

    private void assertFilesGenerated(ApiConfig apiConfig, List<String> unexpectedFiles) {

        List<String> allExpected = new ArrayList<>(Arrays.asList(STANDARD_FILE_PATH_TEMPLATES));
        allExpected.removeAll(unexpectedFiles);

        if (apiConfig.getSource().contains("test-api.yml")) {
            for (String filePathTemplate : allExpected) {
                String filePath = resolveFilePath(apiConfig, filePathTemplate);
                assertThat(new File(filePath)).isFile().exists();
            }

            for (String filePathTemplate : unexpectedFiles) {
                String filePath = resolveFilePath(apiConfig, filePathTemplate);
                assertThat(new File(filePath)).doesNotExist();
            }

            if (TRUE.equals(getField(fixture, "generateSpringIntegrationFiles"))) {
                for (String filePathTemplate : SPRING_META_FILE_TEMPLATES) {
                    String filePath = resolveFilePath(apiConfig, filePathTemplate);
                    assertThat(new File(filePath)).isFile().exists();
                }
            }
        }
    }

    private void assertSpecificFileContent(ApiConfig apiConfig) {
        try {
            assertEndpointName(apiConfig);
            assertTargetNamespace(apiConfig);
            assertSchemasInSpringSchemas(apiConfig);
            assertHandlersInSpringHandlers(apiConfig);
        } catch (IOException e) {
            throw new TestCaseFailedException(e);
        }
    }

    private void assertHandlersInSpringHandlers(ApiConfig apiConfig) throws IOException {
        String targetNamespace = replaceDynamicVarsToLowerCase(apiConfig.getTargetXmlnsNamespace(),
            apiConfig.getPrefix(), apiConfig.getVersion());
        targetNamespace = targetNamespace.replace(":", "\\:");
        String invokerPackage = replaceDynamicVarsToLowerCase(apiConfig.getInvokerPackage(),
            apiConfig.getPrefix(), apiConfig.getVersion());

        String text = String.format("%s=%s.spring.%sNamespaceHandler", targetNamespace,
            invokerPackage, apiConfig.getPrefix());

        assertThat(getContentOfFile(apiConfig, "spring.handlers")).contains(text);

        // Other specific meta info should be retained
        assertThat(getContentOfFile(apiConfig, "spring.handlers")).contains(
            OTHER_META_FILE_CONTENT);
        // Other citrus generated meta info should be deleted
        assertThat(getContentOfFile(apiConfig, "spring.handlers")).doesNotContain(
            OTHER_CITRUS_META_FILE_CONTENT);
    }

    private void assertSchemasInSpringSchemas(ApiConfig apiConfig) throws IOException {
        String targetNamespace = replaceDynamicVarsToLowerCase(apiConfig.getTargetXmlnsNamespace(),
            apiConfig.getPrefix(), apiConfig.getVersion());
        targetNamespace = targetNamespace.replace(":", "\\:");
        String schemaPath = replaceDynamicVarsToLowerCase(
            (String) getField(fixture, "schemaFolder"), apiConfig.getPrefix(),
            apiConfig.getVersion());

        String text = String.format("%s.xsd=%s/%s-api.xsd", targetNamespace, schemaPath,
            apiConfig.getPrefix().toLowerCase());

        // Other specific meta info should be retained assertThat(getContentOfFile(apiConfig, "spring.schemas")).contains(OTHER_META_FILE_CONTENT);
        assertThat(getContentOfFile(apiConfig, "spring.schemas")).contains(
            String.format("%s", text));
        // Other citrus generated meta info should be deleted
        assertThat(getContentOfFile(apiConfig, "spring.schemas")).doesNotContain(
            OTHER_CITRUS_META_FILE_CONTENT);
    }

    private void assertTargetNamespace(ApiConfig apiConfig) throws IOException {
        assertThat(getContentOfFile(apiConfig, "-api.xsd"))
            .contains(String.format("targetNamespace=\"%s\"",
                replaceDynamicVarsToLowerCase(apiConfig.getTargetXmlnsNamespace(),
                    apiConfig.getPrefix(), apiConfig.getVersion())));
    }

    private void assertEndpointName(ApiConfig apiConfig) throws IOException {
        assertThat(getContentOfFile(apiConfig, "BeanConfiguration"))
            .contains(String.format("@Qualifier(\"%s\")", apiConfig.qualifiedEndpoint()));
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
            .filter(path -> path.contains(text))
            .findFirst()
            .orElseThrow(() -> new AssertionError(
                String.format("Can't find file template with content: '%s'", text)));
    }

    @NotNull
    private String resolveFilePath(ApiConfig apiConfig, String filePathTemplate) {
        String lowerCasePrefix = apiConfig.getPrefix().toLowerCase();
        char[] prefixCharArray = apiConfig.getPrefix().toCharArray();
        prefixCharArray[0] = Character.toUpperCase(prefixCharArray[0]);
        String camelCasePrefix = new String(prefixCharArray);

        String invokerFolder = toFolder(
            replaceDynamicVarsToLowerCase(apiConfig.getInvokerPackage(), apiConfig.getPrefix(),
                apiConfig.getVersion()));
        String modelFolder = toFolder(
            replaceDynamicVarsToLowerCase(apiConfig.getModelPackage(), apiConfig.getPrefix(),
                apiConfig.getVersion()));
        String requestFolder = toFolder(
            replaceDynamicVarsToLowerCase(apiConfig.getApiPackage(), apiConfig.getPrefix(),
                apiConfig.getVersion()));
        String schemaFolder = toFolder(
            replaceDynamicVars((String) getField(fixture, "schemaFolder"), apiConfig.getPrefix(),
                apiConfig.getVersion()));


        Map<String, String> apiConfigOptions = apiConfig.getApiConfigOptions();

        String targetFolder = fixture.getMavenProject().getBuild().getDirectory();
        String generatedSourcesFolder = "generated-sources/openapi/src/main/java";
        String generatedResourcesFolder = "generated-sources/openapi/src/main/resources";

        if (apiConfigOptions != null) {
            String output = apiConfigOptions.get("output");
            if (output != null) {
                // Due to the special nature of the nested build directories, explicit output paths need
                // to be prefixed with the path to the nested target folder - e.g. target/pom-full-config.
                // These two segments are also specified in the baseFolderPath and thus need to be removed
                // for proper resolution.
                Path baseFolderPath = Path.of(fixture.getMavenProject().getBasedir().getPath());
                Path outputPath = Path.of(output);
                targetFolder = baseFolderPath.getParent().getParent().resolve(outputPath).toString();
            }

            String sourceFolder = apiConfigOptions.get("sourceFolder");
            if (sourceFolder != null) {
                generatedSourcesFolder = sourceFolder;
            }

            String resourceFolder = apiConfigOptions.get("resourceFolder");
            if (resourceFolder != null) {
                generatedResourcesFolder = resourceFolder;
            }

        }

        return filePathTemplate
            .replace("%BASE_FOLDER%", fixture.getMavenProject().getBasedir().getPath())
            .replace("%TARGET_FOLDER%", targetFolder)
            .replace("%SOURCE_FOLDER%", fixture.getMavenProject().getBuild().getSourceDirectory())
            .replace("%GENERATED_SOURCES_FOLDER%", generatedSourcesFolder)
            .replace("%GENERATED_RESOURCES_FOLDER%", generatedResourcesFolder)
            .replace("%INVOKER_FOLDER%", invokerFolder)
            .replace("%MODEL_FOLDER%", modelFolder)
            .replace("%REQUEST_FOLDER%", requestFolder)
            .replace("%SCHEMA_FOLDER%", schemaFolder)
            .replace("%LOWER_PREFIX%", lowerCasePrefix)
            .replace("%CAMEL_PREFIX%", camelCasePrefix)
            .replace("%META_INF_FOLDER%", fixture.getMetaInfFolder());
    }

    private String toFolder(String text) {
        if (text == null) {
            return "";
        }

        return text.replace(".", "/");
    }

    private TestApiGeneratorMojo fixtureFromPom(String configName) throws Exception {
        String goal = "create-test-api";

        File pomFile = new File(getBasedir(),
            String.format("src/test/resources/%s/%s", getClass().getSimpleName(),
                configName + ".xml"));
        assertThat(pomFile).exists();

        MavenProject mavenProject = new CitrusOpenApiGeneratorMavenProjectStub(configName);

        TestApiGeneratorMojo testApiGeneratorMojo = (TestApiGeneratorMojo) lookupMojo(goal,
            pomFile);
        testApiGeneratorMojo.setMavenProject(mavenProject);
        testApiGeneratorMojo.setMojoExecution(newMojoExecution(goal));

        return spy(testApiGeneratorMojo);
    }
}
