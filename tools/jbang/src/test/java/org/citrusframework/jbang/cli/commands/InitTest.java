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

package org.citrusframework.jbang.cli.commands;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

import org.citrusframework.jbang.cli.CitrusJBangMain;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class InitTest extends CommandTest {

    @BeforeEach
    public void cleanTargetDir() throws IOException {
        Path targetDir = workingDir.resolve("test");
        if (Files.exists(targetDir)) {
            try (var walk = Files.walk(targetDir)) {
                walk.sorted(Comparator.reverseOrder())
                        .forEach(p -> p.toFile().delete());
            }
        }
    }

    @Test
    public void shouldInitJavaTest() {
        Path targetDir = workingDir.resolve("test");

        CitrusJBangMain main = createCitrusJBangMain();
        main.execute("init", "SampleIT.java",
                "--directory", targetDir.toString());

        Assertions.assertEquals("", printer.getOutput());
        Assertions.assertTrue(targetDir.resolve("SampleIT.java").toFile().exists());
    }

    @Test
    public void shouldInitYamlTest() throws IOException {
        Path targetDir = workingDir.resolve("test");

        CitrusJBangMain main = createCitrusJBangMain();
        main.execute("init", "sample.citrus.it.yaml",
                "--directory", targetDir.toString());

        Assertions.assertEquals("", printer.getOutput());
        Path yamlFile = targetDir.resolve("sample.citrus.it.yaml");
        Assertions.assertTrue(yamlFile.toFile().exists());

        List<String> lines = Files.readAllLines(yamlFile);
        Assertions.assertTrue(lines.getFirst().startsWith("# yaml-language-server: $schema="),
                "YAML test should start with schema comment line");
        Assertions.assertTrue(lines.stream().anyMatch(l -> l.startsWith("name: ")),
                "YAML test should contain the test name");
    }

    @Test
    public void shouldInitXmlTest() {
        Path targetDir = workingDir.resolve("test");

        CitrusJBangMain main = createCitrusJBangMain();
        main.execute("init", "sample.citrus.it.xml",
                "--directory", targetDir.toString());

        Assertions.assertEquals("", printer.getOutput());
        Assertions.assertTrue(targetDir.resolve("sample.citrus.it.xml").toFile().exists());
    }

    @Test
    public void shouldInitGroovyTest() {
        Path targetDir = workingDir.resolve("test");

        CitrusJBangMain main = createCitrusJBangMain();
        main.execute("init", "sample.citrus.it.groovy",
                "--directory", targetDir.toString());

        Assertions.assertEquals("", printer.getOutput());
        Assertions.assertTrue(targetDir.resolve("sample.citrus.it.groovy").toFile().exists());
    }

    @Test
    public void shouldInitFeatureTest() {
        Path targetDir = workingDir.resolve("test");

        CitrusJBangMain main = createCitrusJBangMain();
        main.execute("init", "sample.citrus.it.feature",
                "--directory", targetDir.toString());

        Assertions.assertEquals("", printer.getOutput());
        Assertions.assertTrue(targetDir.resolve("sample.citrus.it.feature").toFile().exists());
    }

    @Test
    public void shouldFailWhenFileAlreadyExists() throws IOException {
        Path targetDir = workingDir.resolve("test");
        Files.createDirectories(targetDir);
        Path existingFile = targetDir.resolve("SampleIT.java");
        Files.writeString(existingFile, "existing content");

        CitrusJBangMain main = createCitrusJBangMain(1);
        main.execute("init", "SampleIT.java",
                "--directory", targetDir.toString());

        Assertions.assertEquals("Error: File already exists: %s (use --force to overwrite)".formatted(existingFile.toFile().getPath()), printer.getOutput());
        Assertions.assertEquals("existing content", Files.readString(existingFile));
    }

    @Test
    public void shouldOverwriteWithForce() throws IOException {
        Path targetDir = workingDir.resolve("test");
        Files.createDirectories(targetDir);
        Path existingFile = targetDir.resolve("SampleIT.java");
        Files.writeString(existingFile, "existing content");

        CitrusJBangMain main = createCitrusJBangMain();
        main.execute("init", "SampleIT.java",
                "--directory", targetDir.toString(),
                "--force");

        Assertions.assertEquals("", printer.getOutput());
        Assertions.assertTrue(existingFile.toFile().exists());
        Assertions.assertNotEquals("existing content", Files.readString(existingFile));
    }

    @Test
    public void shouldHandleUnknownTestType() {
        Path targetDir = workingDir.resolve("test");

        CitrusJBangMain main = createCitrusJBangMain(1);
        main.execute("init", "sample.citrus.it.foo",
                "--directory", targetDir.toString());

        Assertions.assertEquals("Error: Unsupported file type 'foo' (supported types are: feature, java, yaml, xml, groovy)", printer.getOutput());
    }
}
