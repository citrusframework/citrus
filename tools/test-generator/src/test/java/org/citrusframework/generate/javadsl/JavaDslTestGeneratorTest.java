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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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
import org.junit.jupiter.api.io.TempDir;

public class JavaDslTestGeneratorTest {

    private final JavaDslTestGenerator<?> generatorUnderTest = new JavaDslTestGenerator<>();
    private final File testFile = new File(CitrusSettings.getDefaultTestSrcDirectory() + "/java/org/citrusframework/FooTest.java");

    private final CleanupUtils cleanupUtils = new CleanupUtils();

    @AfterEach
    public void cleanUp() {
        cleanupUtils.deleteFile(testFile);
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
    public void create_should_generate_compilable_junit_jupiter_test(@TempDir Path tempDir) throws IOException {
        Path srcDir = tempDir.resolve("src");

        //GIVEN
        generatorUnderTest.withName("FooTest")
                .withDisabled(false)
                .withFramework(UnitFramework.JUNIT)
                .usePackage("com.example")
                .useSrcDirectory(srcDir.toString());

        //WHEN
        generatorUnderTest.create();

        //THEN
        Path generatedJava = srcDir.resolve("java/com/example/FooTest.java");
        Assertions.assertTrue(Files.exists(generatedJava));
        assertContains(Files.readString(generatedJava), "import org.citrusframework.TestActionRunner;");

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        Assumptions.assumeTrue(compiler != null, "No system Java compiler available");

        Path outputDirectory = tempDir.resolve("classes");
        Files.createDirectories(outputDirectory);
        ByteArrayOutputStream compilerErr = new ByteArrayOutputStream();
        int result = compiler.run(null, null, compilerErr,
                "-classpath", System.getProperty("java.class.path"),
                "-d", outputDirectory.toString(),
                generatedJava.toString());

        Assertions.assertEquals(0, result,
                () -> "Generated JUnit Jupiter test does not compile:\n" + compilerErr.toString(StandardCharsets.UTF_8));
        Assertions.assertTrue(Files.exists(outputDirectory.resolve("com/example/FooTest.class")));
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
}
