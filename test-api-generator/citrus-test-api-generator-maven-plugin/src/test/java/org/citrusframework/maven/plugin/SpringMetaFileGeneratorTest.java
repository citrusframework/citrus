package org.citrusframework.maven.plugin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.util.List;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.citrusframework.maven.plugin.TestApiGeneratorMojo.ApiConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SpringMetaFileGeneratorTest {

    @Mock
    private TestApiGeneratorMojo testApiGeneratorMojo = mock();

    private SpringMetaFileGenerator sut;

    @BeforeEach
    void beforeEach() {
        sut = new SpringMetaFileGenerator(testApiGeneratorMojo);
    }
    @Test
    void generateMetaFiles() throws MojoExecutionException {
        String userDir = System.getProperty("user.dir");

        if (!userDir.endsWith("target")) {
            userDir = userDir + "/target";
        }

        MavenProject mavenProject = mock();
        doReturn(new File(userDir)).when(mavenProject).getBasedir();
        doReturn(mavenProject).when(testApiGeneratorMojo).getMavenProject();
        doReturn("/test-classes/SpringMetaFileGeneratorTest/META-INF").when(testApiGeneratorMojo).getMetaInfFolder();

        ApiConfig apiConfig = new ApiConfig();
        apiConfig.setPrefix("PrefixA");
        doReturn(List.of(apiConfig)).when(testApiGeneratorMojo).getApiConfigs();

        sut.generateSpringIntegrationMetaFiles();

        assertThat(new File(userDir+"/test-classes/SpringMetaFileGeneratorTest/META-INF/spring.schemas"))
            .isFile()
            .exists()
            .hasContent("""
                http\\://www.citrusframework.org/schema/restdocs/config/citrus-restdocs-config.xsd=org/citrusframework/schema/citrus-restdocs-config.xsd
                http\\://www.citrusframework.org/citrus-test-schema/prefixa-api.xsd=null/prefixa-api.xsd""");
        assertThat(new File(userDir+"/test-classes/SpringMetaFileGeneratorTest/META-INF/spring.handlers"))
            .isFile()
            .exists()
            .hasContent("""
                http\\://www.citrusframework.org/schema/restdocs/config=org.citrusframework.restdocs.config.handler.RestDocConfigNamespaceHandler
                http\\://www.citrusframework.org/citrus-test-schema/prefixa-api=org.citrusframework.automation.prefixa.spring.PrefixANamespaceHandler""");

    }
}
