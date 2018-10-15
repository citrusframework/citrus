package com.consol.citrus.generate.javadsl;

import com.consol.citrus.Citrus;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.generate.UnitFramework;
import com.consol.citrus.util.FileUtils;
import org.springframework.core.io.FileSystemResource;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;


public class JavaDslTestGeneratorTest {

    private JavaDslTestGenerator generatorUnderTest = new JavaDslTestGenerator();

    @Test
    public void create_should_pass_with_junit5() throws IOException {
        generatorUnderTest.withName("FooTest")
                .withDisabled(false)
                .withFramework(UnitFramework.JUNIT5)
                .usePackage("com.consol.citrus");

        generatorUnderTest.create();

        File javaFile = new File(Citrus.DEFAULT_TEST_SRC_DIRECTORY + "/java/com/consol/citrus/FooTest.java");
        Assert.assertTrue(javaFile.exists());

        String javaContent = FileUtils.readToString(new FileSystemResource(javaFile));
        Assert.assertTrue(javaContent.contains("@ExtendWith(com.consol.citrus.dsl.junit.jupiter.CitrusExtension.class)"));
        Assert.assertTrue(javaContent.contains("public void fooTest(@CitrusResource TestRunner testRunner) {"));
        Assert.assertTrue(javaContent.contains("testRunner.echo(\"TODO: Code the test FooTest\");"));
    }

    @Test(expectedExceptions = CitrusRuntimeException.class)
    public void create_should_fail_when_test_name_starts_with_lowercase_letter(){
        generatorUnderTest.withName("foo");

        generatorUnderTest.create();
    }

}