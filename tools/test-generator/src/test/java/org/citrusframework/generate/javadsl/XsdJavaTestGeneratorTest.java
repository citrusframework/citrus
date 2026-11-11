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
import java.util.stream.Stream;

import org.citrusframework.CitrusSettings;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.generate.UnitFramework;
import org.citrusframework.util.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * @since 2.7
 */
public class XsdJavaTestGeneratorTest {

    @ParameterizedTest
    @MethodSource("nameProvider")
    public void testCreateTest(String requestName, String responseName, String generatedResponseName) throws IOException {
        XsdJavaTestGenerator generator = new XsdJavaTestGenerator();

        generator.withAuthor("Christoph")
                .withDescription("This is a sample test")
                .usePackage("org.citrusframework")
                .withFramework(UnitFramework.TESTNG);

        generator.withXsd("org/citrusframework/xsd/HelloService.xsd");
        generator.withRequestMessage(requestName);
        generator.withResponseMessage(responseName);

        generator.create();

        File javaFile = new File(CitrusSettings.getDefaultTestSrcDirectory() + "java/org/citrusframework/HelloIT.java");
        Assertions.assertTrue(javaFile.exists());

        String javaContent = FileUtils.readToString(javaFile);
        Assertions.assertTrue(javaContent.contains("public class HelloIT"));
        Assertions.assertTrue(javaContent.contains("* This is a sample test"));
        Assertions.assertTrue(javaContent.contains("package org.citrusframework;"));
        Assertions.assertTrue(javaContent.contains("extends TestNGCitrusSupport"));
    }

    static Stream<Arguments> nameProvider() {
        return Stream.of(
            Arguments.of("Hello", "HelloResponse", "HelloResponse"),
            Arguments.of("HelloRequest", "HelloResponse", "HelloResponse"),
            Arguments.of("HelloRequest", "", "HelloResponse"),
            Arguments.of("HelloRequestMessage", "HelloResponseMessage", "HelloResponseMessage"),
            Arguments.of("HelloRequestMessage", "", "HelloResponseMessage"),
            Arguments.of("HelloReq", "HelloRes", "HelloRes"),
            Arguments.of("HelloReq", "", "HelloRes")
        );
    }

    @Test
    public void testCreateTestWithoutResponse() throws IOException {
        XsdJavaTestGenerator generator = new XsdJavaTestGenerator();

        generator.withAuthor("Christoph")
                .withDescription("This is a sample test")
                .usePackage("org.citrusframework")
                .withFramework(UnitFramework.TESTNG);

        generator.withXsd("org/citrusframework/xsd/HelloService.xsd");
        generator.withRequestMessage("Hello");
        generator.withResponseMessage("");

        generator.create();

        File javaFile = new File(CitrusSettings.getDefaultTestSrcDirectory() + "java/org/citrusframework/HelloIT.java");
        Assertions.assertTrue(javaFile.exists());

        String javaContent = FileUtils.readToString(javaFile);
        Assertions.assertTrue(javaContent.contains("public class HelloIT"));
        Assertions.assertTrue(javaContent.contains("* This is a sample test"));
        Assertions.assertTrue(javaContent.contains("package org.citrusframework;"));
        Assertions.assertTrue(javaContent.contains("extends TestNGCitrusSupport"));
    }

    @Test
    public void testUnknownRequest() {
        XsdJavaTestGenerator generator = new XsdJavaTestGenerator();

        generator.withAuthor("Christoph")
                .withDescription("This is a sample test")
                .usePackage("org.citrusframework")
                .withFramework(UnitFramework.TESTNG);

        generator.withXsd("org/citrusframework/xsd/HelloService.xsd");
        generator.withRequestMessage("HiRequest");
        generator.withResponseMessage("HiResponse");

        CitrusRuntimeException thrown = Assertions.assertThrows(CitrusRuntimeException.class, generator::create);
        Assertions.assertTrue(thrown.getMessage().matches("Unable to find element with name 'HiRequest'.*"));
    }
}
