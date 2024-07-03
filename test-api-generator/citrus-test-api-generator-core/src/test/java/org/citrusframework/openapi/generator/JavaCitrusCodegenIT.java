package org.citrusframework.openapi.generator;

import static java.nio.file.Files.readString;
import static java.nio.file.Files.walk;
import static org.assertj.core.api.Assertions.assertThat;
import static org.citrusframework.openapi.generator.JavaCitrusCodegenTest.getAbsoluteTargetDirectoryPath;
import static org.citrusframework.openapi.generator.JavaCitrusCodegenTest.getAbsoluteTestResourcePath;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
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

/**
 * This test case is designed to validate the consistency of the code generation process and detect
 * any discrepancies between the generated API files and the reference files stored in
 * '/JavaCitrusCodegenIT/expectedgen/'. It compares the results of API generation against the
 * reference files, and a failure indicates potential changes in mustache templates or code
 * generation logic.
 * <p>
 * If this test fails, it is essential to review the code generation process and underlying
 * templates carefully. If the changes are intentional and verified, update the reference files by
 * copying the generated API sources to the '/JavaCitrusCodegenIT/expectedgen/' directory. To ensure
 * accurate copying, without unwanted code formatting, use a simple File Explorer instead of relying
 * on IDE-based operations.
 */
class JavaCitrusCodegenIT {

    public static final String BASE_PACKAGE = "org/citrusframework/openapi/generator";

    private static long countFilesRecursively(Path dir) throws IOException {
        try (Stream<Path> walk = walk(dir)) {
            return walk.filter(Files::isRegularFile).count();
        }
    }

    @Test
    void noAdditionalFiles() throws IOException {
        long expectedFileCount = countFilesRecursively(
            Path.of(getAbsoluteTestResourcePath(
                BASE_PACKAGE + "/JavaCitrusCodegenIT/expectedgen/rest")));
        long actualFileCount = countFilesRecursively(
            Path.of(getAbsoluteTargetDirectoryPath(
                "generated-test-sources/" + BASE_PACKAGE + "/rest")));

        assertEquals(expectedFileCount, actualFileCount,
            "Directories do not have the same number of files.");
    }

    static Stream<Arguments> getResourcesForRest() throws IOException {
        return geClassResourcesIgnoringInnerClasses(BASE_PACKAGE + "/rest");
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

    static Stream<Arguments> getResourcesForSoap() throws IOException {
        return geClassResourcesIgnoringInnerClasses(
            BASE_PACKAGE + "/soap/bookservice");
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

    private static Stream<Arguments> geClassResourcesIgnoringInnerClasses(String path)
        throws IOException {
        return Streams.of(
                new PathMatchingResourcePatternResolver().getResources(path + "/**/*.class"))
            .filter(resource -> {
                    try {
                        return !resource.getURI().toString().contains("$");
                    } catch (Exception e) {
                        throw new CitrusRuntimeException("Unable to retrieve URL from resource!");
                    }
                }
            ).map(Arguments::arguments);
    }

    /*
     * NOTE: when changes have been performed to mustache templates, the expected files need to be updated.
     * Be aware that file content may change according to IDE formatting rules if the files are copied via IDE.
     * Files should therefore be copied using a file explorer which ensures that content of files does not change.
     */
    private void assertFileContent(File file, String apiDir) throws IOException {
        assertThat(file).exists();

        String expectedFilePath = BASE_PACKAGE + "/JavaCitrusCodegenIT/expectedgen/" + file.getAbsolutePath().substring(file.getAbsolutePath().indexOf(apiDir));
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
