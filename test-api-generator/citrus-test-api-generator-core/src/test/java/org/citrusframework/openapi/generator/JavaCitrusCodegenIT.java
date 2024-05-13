package org.citrusframework.openapi.generator;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.List;
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
 * '/JavaCitrusCodegenIntegrationTest/expectedgen/'. It compares the results of API generation
 * against the reference files, and a failure indicates potential changes in mustache templates or
 * code generation logic.
 * <p>
 * If this test fails, it is essential to review the code generation process and underlying
 * templates carefully. If the changes are intentional and verified, update the reference files by
 * copying the generated API sources to the '/JavaCitrusCodegenIntegrationTest/expectedgen/'
 * directory. To ensure accurate copying, without unwanted code formatting, use a simple File
 * Explorer instead of relying on IDE-based operations.
 */
class JavaCitrusCodegenIT {

    static Stream<Arguments> getResourcesForRest() throws IOException {
        return geClassResourcesIgnoringInnerClasses("org/citrusframework/openapi/generator/rest");
    }

    @ParameterizedTest
    @MethodSource("getResourcesForRest")
    void testGeneratedFiles(Resource resource) throws IOException {
        File classFile = resource.getFile();
        String absolutePath = classFile.getAbsolutePath();
        String javaFilePath = absolutePath.replace("test-classes", "generated-test-sources")
            .replace(".class", ".java");

        assertFileContent(new File(javaFilePath), "rest");
    }

    static Stream<Arguments> getResourcesForSoap() throws IOException {
        return geClassResourcesIgnoringInnerClasses(
            "org/citrusframework/openapi/generator/soap/bookservice");
    }

    @ParameterizedTest
    @MethodSource("getResourcesForSoap")
    void testGeneratedSoapFiles(Resource resource) throws IOException {
        File classFile = resource.getFile();
        String absolutePath = classFile.getAbsolutePath();

        String javaFilePath = absolutePath.replace("test-classes", "generated-test-sources")
            .replace(".class", ".java");

        assertFileContent(new File(javaFilePath), "soap");
    }

    private static Stream<Arguments> geClassResourcesIgnoringInnerClasses(String path)
        throws IOException {
        return Streams.of(new PathMatchingResourcePatternResolver().getResources(
            path + "/**/*.class")).filter(resource -> {
            try {
                return !resource.getURI().toString().contains("$");
            } catch (Exception e) {
                throw new CitrusRuntimeException("Unable to retrieve URL from resource!");
            }
        }).map(Arguments::arguments);
    }

    private void assertFileContent(File file, String apiDir) throws IOException {
        assertThat(file).exists();
        String expectedFilePath =
            "org/citrusframework/openapi/generator/JavaCitrusCodegenIntegrationTest/expectedgen/"
                + file.getAbsolutePath().substring(file.getAbsolutePath().indexOf(apiDir));

        ClassPathResource classPathResource = new ClassPathResource(expectedFilePath);

        /*
         * NOTE: when changes have been performed to mustache templates, the expected files need to be updated.
         * Be aware that file content may change according to IDE formatting rules if the files are copied via IDE.
         * Files should therefore be copied using a file explorer which ensures that content of files does not change.
         */
        assertThat(file).hasSameTextualContentAs(classPathResource.getFile());
    }
}
