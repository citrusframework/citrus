package org.citrusframework.openapi.generator;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.apache.commons.lang3.stream.Streams;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import static java.nio.file.Files.readString;
import static java.nio.file.Files.walk;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * This test case is designed to validate the consistency of the code generation process and detect
 * any discrepancies between the generated API files and the reference files stored in
 * '/ExpectedCodeGenIT/expectedgen/'. It compares the results of API generation against the
 * reference files, and a failure indicates potential changes in mustache templates or code
 * generation logic.
 * <p>
 * If this test fails, it is essential to review the code generation process and underlying
 * templates carefully. If the changes are intentional and verified, update the reference files by
 * copying the generated API sources to the '/ExpectedCodeGenIT/expectedgen/' directory. To ensure
 * accurate copying, without unwanted code formatting, use a simple File Explorer instead of relying
 * on IDE-based operations.
 */
class ExpectedCodeGenIT {

    public static final String BASE_PACKAGE = "org/citrusframework/openapi/generator";

    static Stream<Arguments> getResourcesForRest() throws IOException {
        return geClassResourcesIgnoringInnerClasses(BASE_PACKAGE + "/rest");
    }

    static Stream<Arguments> getResourcesForSoap() throws IOException {
        return geClassResourcesIgnoringInnerClasses(BASE_PACKAGE + "/soap/bookservice");
    }

    private static Stream<Arguments> geClassResourcesIgnoringInnerClasses(String path) throws IOException {
        return Streams.of(new PathMatchingResourcePatternResolver().getResources(path + "/**/*.class"))
                .filter(resource -> {
                            try {
                                return !resource.getURI().toString().contains("$");
                            } catch (Exception e) {
                                throw new CitrusRuntimeException("Unable to retrieve URL from resource!");
                            }
                        }
                ).map(Arguments::arguments);
    }

    private static long countFilesRecursively(Path dir) throws IOException {
        try (Stream<Path> walk = walk(dir)) {
            return walk.filter(Files::isRegularFile).count();
        }
    }

    /**
     * Get the absolute path to the test resources directory.
     */
    static Path getAbsoluteTestResourcePath(String pathToFileInTestResources)
        throws URISyntaxException {
        URL resourceUrl = CitrusJavaCodegenTest.class.getClassLoader().getResource(pathToFileInTestResources);
        assert resourceUrl != null;
        return Path.of(resourceUrl.toURI());

    }

    /**
     * Get the absolute path to the project's target directory.
     */
    static String getAbsoluteTargetDirectoryPath(String pathToFileInTargetDirectory) {
        String projectBaseDir = System.getProperty("user.dir"); // Base directory of the project
        File outputDirFile = new File(projectBaseDir, "target/" + pathToFileInTargetDirectory);
        return outputDirFile.getAbsolutePath();
    }

    @Test
    void noAdditionalFiles() throws IOException, URISyntaxException {
        long expectedFileCount = countFilesRecursively(
                getAbsoluteTestResourcePath(
                        BASE_PACKAGE + "/ExpectedCodeGenIT/expectedgen/rest"));
        long actualFileCount = countFilesRecursively(
                Path.of(getAbsoluteTargetDirectoryPath(
                        "generated-test-sources/" + BASE_PACKAGE + "/rest")));

        assertEquals(expectedFileCount, actualFileCount,
                "Directories do not have the same number of files.");
    }

    @ParameterizedTest
    @MethodSource("getResourcesForRest")
    void testGeneratedFiles(Resource resource) throws IOException {
        File classFile = resource.getFile();
        String absolutePath = classFile.getAbsolutePath();
        String javaFilePath = absolutePath
                .replace("test-classes", "generated-test-sources")
                .replace(".class", ".java");

        assertFileContent(new File(javaFilePath), "rest");
    }

    @ParameterizedTest
    @MethodSource("getResourcesForSoap")
    void testGeneratedSoapFiles(Resource resource) throws IOException {
        File classFile = resource.getFile();
        String absolutePath = classFile.getAbsolutePath();

        String javaFilePath = absolutePath
                .replace("test-classes", "generated-test-sources")
                .replace(".class", ".java");

        assertFileContent(new File(javaFilePath), "soap");
    }

    /*
     * NOTE: when changes have been performed to mustache templates, the expected files need to be updated.
     * Be aware that file content may change according to IDE formatting rules if the files are copied via IDE.
     * Files should therefore be copied using a file explorer which ensures that content of files does not change.
     */
    private void assertFileContent(File file, String apiDir) throws IOException {
        assertThat(file).exists();

        String expectedFilePath = BASE_PACKAGE + "/ExpectedCodeGenIT/expectedgen/" + file.getAbsolutePath().substring(file.getAbsolutePath().indexOf(apiDir));
        ClassPathResource classPathResource = new ClassPathResource(expectedFilePath);

        String actualContent = readString(file.toPath());
        String expectedContent = readString(classPathResource.getFile().toPath());

        // Replace "Generated" with a placeholder
        String generatedAnnotationPattern = "@jakarta\\.annotation\\.Generated\\(.*?\\)";
        String placeholder = "@jakarta.annotation.Generated(value = \"org.citrusframework.openapi.generator.JavaCitrusCodegen\", date = \"TIMESTAMP\", comments = \"Generator version: VERSION\")";

        actualContent = actualContent.replaceAll(generatedAnnotationPattern, placeholder);
        expectedContent = expectedContent.replaceAll(generatedAnnotationPattern, placeholder);

        assertThat(actualContent).isEqualTo(expectedContent);
    }
}
