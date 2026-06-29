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

package org.citrusframework.jbang.commands;

import java.nio.file.Path;

import org.citrusframework.jbang.CitrusJBangMain;
import org.junit.jupiter.api.Assertions;
import org.testng.annotations.Test;

public class InitTest extends CommandTest {

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
    public void shouldInitYamlTest() {
        Path targetDir = workingDir.resolve("test");

        CitrusJBangMain main = createCitrusJBangMain();
        main.execute("init", "sample.citrus.it.yaml",
                "--directory", targetDir.toString());

        Assertions.assertEquals("", printer.getOutput());
        Assertions.assertTrue(targetDir.resolve("sample.citrus.it.yaml").toFile().exists());
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
    public void shouldHandleUnknownTestType() {
        Path targetDir = workingDir.resolve("test");

        CitrusJBangMain main = createCitrusJBangMain(1);
        main.execute("init", "sample.citrus.it.foo",
                "--directory", targetDir.toString());

        Assertions.assertEquals("Error: Unsupported file type 'foo' (supported types are: feature, java, yaml, xml, groovy)", printer.getOutput());
    }
}
