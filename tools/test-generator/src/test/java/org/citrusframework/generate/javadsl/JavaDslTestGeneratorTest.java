package org.citrusframework.generate.javadsl;

import java.io.File;
import java.io.IOException;

import org.citrusframework.CitrusSettings;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.generate.UnitFramework;
import org.citrusframework.util.FileUtils;
import org.citrusframework.utils.CleanupUtils;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

public class JavaDslTestGeneratorTest {

    private final JavaDslTestGenerator<?> generatorUnderTest = new JavaDslTestGenerator<>();
    private final File testFile = new File(CitrusSettings.DEFAULT_TEST_SRC_DIRECTORY + "/java/org/citrusframework/FooTest.java");

    private final CleanupUtils cleanupUtils = new CleanupUtils();

    @AfterMethod
    public void cleanUp(){
        cleanupUtils.deleteFile(testFile);
    }

    @Test
    public void create_should_pass_with_junit5() throws IOException {


        //GIVEN
        generatorUnderTest.withName("FooTest")
                .withDisabled(false)
                .withFramework(UnitFramework.JUNIT5)
                .usePackage("org.citrusframework");


        //WHEN
        generatorUnderTest.create();

        //THEN
        String javaContent = loadTestFile();
        checkMethodParameter(javaContent, "@CitrusResource TestCaseRunner runner");
        assertContains(javaContent, "@ExtendWith(CitrusExtension.class)");
        assertContains(javaContent, "runner.run(echo(\"TODO: Code the test FooTest\"));");
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

    @Test(expectedExceptions = CitrusRuntimeException.class)
    public void create_should_fail_when_test_name_starts_with_lowercase_letter(){
        generatorUnderTest.withName("foo");

        generatorUnderTest.create();
    }

    private String loadTestFile() throws IOException {
        Assert.assertTrue(testFile.exists());
        return FileUtils.readToString(testFile);
    }

    private void assertContains(String haystack, String needle){
        Assert.assertTrue(haystack.contains(needle));
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
