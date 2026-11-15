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

package org.citrusframework.generate.javadsl;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import org.citrusframework.CitrusSettings;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.generate.UnitFramework;
import org.citrusframework.generate.util.CleanupUtils;
import org.citrusframework.util.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

public class JavaDslTestGeneratorTest {

    private final JavaDslTestGenerator<?> generatorUnderTest = new JavaDslTestGenerator<>();
    private final File testFile = new File(CitrusSettings.getDefaultTestSrcDirectory() + "/java/org/citrusframework/FooTest.java");
    private final File exampleTestFile = new File(CitrusSettings.getDefaultTestSrcDirectory() + "/java/com/example/FooTest.java");

    private final CleanupUtils cleanupUtils = new CleanupUtils();

    @AfterEach
    public void cleanUp() {
        cleanupUtils.deleteFile(testFile);
        cleanupUtils.deleteFile(exampleTestFile);
    }

    @Test
    public void create_should_pass_with_junit_jupiter() throws IOException {

        //GIVEN
        generatorUnderTest.withName("FooTest")
                .withDisabled(false)
                .withFramework(UnitFramework.JUNIT)
                .usePackage("org.citrusframework");

        //WHEN
        generatorUnderTest.create();

        //THEN
        String javaContent = loadTestFile();
        checkMethodParameter(javaContent, "@CitrusResource TestActionRunner runner");
        assertContains(javaContent, "@CitrusSupport");
        assertContains(javaContent, "public class FooTest implements TestActionSupport");
        Assertions.assertFalse(javaContent.contains("extends TestNGCitrusSupport"));
        Assertions.assertFalse(javaContent.contains("@ExtendWith(CitrusExtension.class)"));
        Assertions.assertFalse(javaContent.contains("CitrusExtension"));
        Assertions.assertFalse(javaContent.contains("TestCaseRunner"));
        assertContains(javaContent, "@Test");
        assertContains(javaContent, "runner.run(echo(\"TODO: Code the test FooTest\"));");
    }

    @Test
    public void create_should_add_disabled_annotation_for_junit_jupiter() throws IOException {

        //GIVEN
        generatorUnderTest.withName("FooTest")
                .withDisabled(true)
                .withFramework(UnitFramework.JUNIT)
                .usePackage("org.citrusframework");

        //WHEN
        generatorUnderTest.create();

        //THEN
        assertContains(loadTestFile(), "@Disabled");
    }

    @Test
    public void create_should_generate_compilable_junit_jupiter_test() throws IOException {

        //GIVEN
        generatorUnderTest.withName("FooTest")
                .withDisabled(false)
                .withFramework(UnitFramework.JUNIT)
                .usePackage("com.example");

        //WHEN
        generatorUnderTest.create();

        //THEN
        Assertions.assertTrue(exampleTestFile.exists());
        assertContains(FileUtils.readToString(exampleTestFile), "import org.citrusframework.TestActionRunner;");

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        Assumptions.assumeTrue(compiler != null, "No system Java compiler available");

        Path outputDirectory = Files.createTempDirectory("citrus-test-generator");
        try {
            int result = compiler.run(null, null, null,
                    "-classpath", System.getProperty("java.class.path"),
                    "-d", outputDirectory.toString(),
                    exampleTestFile.getAbsolutePath());

            Assertions.assertEquals(0, result, "Generated JUnit Jupiter test does not compile");
            Assertions.assertTrue(Files.exists(outputDirectory.resolve("com/example/FooTest.class")));
        } finally {
            deleteRecursively(outputDirectory);
        }
    }

    @Test
    public void create_should_pass_with_junit4() throws IOException {

        //GIVEN
        generatorUnderTest.withName("FooTest")
                .withDisabled(false)
                .withFramework(UnitFramework.JUNIT4)
                .usePackage("org.citrusframework");

        //WHEN
        generatorUnderTest.create();

        //THEN
        String javaContent = loadTestFile();
        checkExtension(javaContent, "JUnit4CitrusSupport");
        checkAnnotations(javaContent);
        checkMethodParameter(javaContent, "@CitrusResource TestCaseRunner runner");
        assertContains(javaContent, "runner.run(echo(\"TODO: Code the test FooTest\"));");
    }


    @Test
    public void create_should_pass_with_testng() throws IOException {

        //GIVEN
        generatorUnderTest.withName("FooTest")
                .withDisabled(false)
                .withFramework(UnitFramework.TESTNG)
                .usePackage("org.citrusframework");

        //WHEN
        generatorUnderTest.create();

        //THEN
        String javaContent = loadTestFile();
        checkExtension(javaContent, "TestNGCitrusSupport");
        checkAnnotations(javaContent);
        checkMethodParameter(javaContent, "@CitrusResource @Optional TestCaseRunner runner");
        assertContains(javaContent, "@Parameters(\"runner\")");
        assertContains(javaContent, "runner.run(echo(\"TODO: Code the test FooTest\"));");
    }

    @Test
    public void create_should_fail_when_test_name_starts_with_lowercase_letter() {
        generatorUnderTest.withName("foo");

        Assertions.assertThrows(CitrusRuntimeException.class, () -> generatorUnderTest.create());
    }

    private String loadTestFile() throws IOException {
        Assertions.assertTrue(testFile.exists());
        return FileUtils.readToString(testFile);
    }

    private void assertContains(String haystack, String needle) {
        Assertions.assertTrue(haystack.contains(needle));
    }

    private void checkExtension(String javaContent, String extension) {
        assertContains(javaContent, "public class FooTest extends " + extension);
    }

    private void checkMethodParameter(String javaContent, String parameter) {
        assertContains(javaContent, "public void fooTest(" + parameter + ") {");
    }

    private void checkAnnotations(String javaContent) {
        assertContains(javaContent, "@CitrusTest");
        assertContains(javaContent, "@Test");
    }

    private void deleteRecursively(Path directory) throws IOException {
        try (Stream<Path> paths = Files.walk(directory)) {
            paths.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        }
    }
}
