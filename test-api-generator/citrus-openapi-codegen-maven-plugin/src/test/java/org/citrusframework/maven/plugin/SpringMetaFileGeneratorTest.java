package org.citrusframework.maven.plugin;

import java.io.File;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.citrusframework.maven.plugin.TestApiGeneratorMojo.ApiConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openapitools.codegen.utils.CamelizeOption;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.openapitools.codegen.utils.StringUtils.camelize;

@ExtendWith(MockitoExtension.class)
class SpringMetaFileGeneratorTest {

    @Mock
    private TestApiGeneratorMojo testApiGeneratorMojo = mock();

    private SpringMetaFileGenerator sut;

    @BeforeEach
    void beforeEach() {
        sut = new SpringMetaFileGenerator(testApiGeneratorMojo);
    }

    @ParameterizedTest
    @CsvSource(value = {"prefixA", "PrefixB", "prefixc", "PREFIXD"})
    void generateMetaFiles(String prefix) throws MojoExecutionException {
        String userDir = System.getProperty("user.dir");

        if (!userDir.endsWith("target")) {
            userDir = userDir + "/target";
        }

        MavenProject mavenProject = mock();
        doReturn(new File(userDir)).when(mavenProject).getBasedir();
        doReturn(mavenProject).when(testApiGeneratorMojo).getMavenProject();
        doReturn("/test-classes/SpringMetaFileGeneratorTest/" + prefix + "/META-INF").when(
            testApiGeneratorMojo).getMetaInfFolder();

        ApiConfig apiConfig = new ApiConfig();
        apiConfig.setPrefix(prefix);
        doReturn(List.of(apiConfig)).when(testApiGeneratorMojo).getApiConfigs();

        sut.generateSpringIntegrationMetaFiles();

        String camelCasePrefix = camelize(apiConfig.getPrefix(),
            CamelizeOption.UPPERCASE_FIRST_CHAR);
        String lowerCasePrefix = prefix.toLowerCase();

        assertThat(new File(userDir + "/test-classes/SpringMetaFileGeneratorTest/" + prefix
            + "/META-INF/spring.schemas"))
            .isFile()
            .exists()
            .hasContent(format("""
                    http\\://www.citrusframework.org/schema/restdocs/config/citrus-restdocs-config.xsd=org/citrusframework/schema/citrus-restdocs-config.xsd
                    http\\://www.citrusframework.org/citrus-test-schema/%s-api.xsd=null/%s-api.xsd""",
                lowerCasePrefix, lowerCasePrefix));
        assertThat(new File(userDir + "/test-classes/SpringMetaFileGeneratorTest/" + prefix
            + "/META-INF/spring.handlers"))
            .isFile()
            .exists()
            .hasContent(format("""
                    http\\://www.citrusframework.org/schema/restdocs/config=org.citrusframework.restdocs.config.handler.RestDocConfigNamespaceHandler
                    http\\://www.citrusframework.org/citrus-test-schema/%s-api=org.citrusframework.automation.%s.spring.%sNamespaceHandler""",
                lowerCasePrefix, lowerCasePrefix, camelCasePrefix));

    }
}
