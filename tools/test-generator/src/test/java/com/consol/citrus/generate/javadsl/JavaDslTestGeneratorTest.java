package com.consol.citrus.generate.javadsl;

import com.consol.citrus.Citrus;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.generate.UnitFramework;
import com.consol.citrus.util.FileUtils;
import com.consol.citrus.utils.CleanupUtils;
import org.springframework.core.io.FileSystemResource;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;


public class JavaDslTestGeneratorTest {

    private JavaDslTestGenerator generatorUnderTest = new JavaDslTestGenerator();
    private File testFile = new File(Citrus.DEFAULT_TEST_SRC_DIRECTORY + "/java/com/consol/citrus/FooTest.java");

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
                .usePackage("com.consol.citrus");


        //WHEN
        generatorUnderTest.create();

        //THEN
        String javaContent = loadTestFile();
        checkMethodParameter(javaContent, "@CitrusResource TestRunner testRunner");
        assertContains(javaContent, "@ExtendWith(com.consol.citrus.dsl.junit.jupiter.CitrusExtension.class)");
        assertContains(javaContent, "testRunner.echo(\"TODO: Code the test FooTest\");");
    }

    @Test
    public void create_should_pass_with_junit4() throws IOException {

        //GIVEN
        generatorUnderTest.withName("FooTest")
                .withDisabled(false)
                .withFramework(UnitFramework.JUNIT4)
                .usePackage("com.consol.citrus");

        //WHEN
        generatorUnderTest.create();

        //THEN
        String javaContent = loadTestFile();
        checkExtension(javaContent, "JUnit4CitrusTestRunner");
        checkAnnotations(javaContent);
        checkMethodParameter(javaContent, "@CitrusResource TestRunner testRunner");
        assertContains(javaContent, "testRunner.echo(\"TODO: Code the test FooTest\");");
    }



    @Test
    public void create_should_pass_with_testng() throws IOException {

        //GIVEN
        generatorUnderTest.withName("FooTest")
                .withDisabled(false)
                .withFramework(UnitFramework.TESTNG)
                .usePackage("com.consol.citrus");

        //WHEN
        generatorUnderTest.create();

        //THEN
        String javaContent = loadTestFile();
        checkExtension(javaContent, "TestNGCitrusTestRunner");
        checkAnnotations(javaContent);
        checkMethodParameter(javaContent, "@CitrusResource @Optional TestRunner testRunner");
        assertContains(javaContent, "@Parameters(\"testRunner\")");
        assertContains(javaContent, "testRunner.echo(\"TODO: Code the test FooTest\");");
    }

    @Test(expectedExceptions = CitrusRuntimeException.class)
    public void create_should_fail_when_test_name_starts_with_lowercase_letter(){
        generatorUnderTest.withName("foo");

        generatorUnderTest.create();
    }

    private String loadTestFile() throws IOException {
        Assert.assertTrue(testFile.exists());
        return FileUtils.readToString(new FileSystemResource(testFile));
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