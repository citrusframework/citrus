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

package org.citrusframework.common;

import java.nio.file.Files;
import java.nio.file.Path;

import org.citrusframework.Citrus;
import org.citrusframework.CitrusInstanceManager;
import org.citrusframework.TestSource;
import org.citrusframework.api.common.TestLoader;
import org.citrusframework.base.DefaultTestCaseRunner;
import org.citrusframework.base.UnitTestSupport;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class JavaTestLoaderTest extends UnitTestSupport {

    @Test
    public void shouldNotWriteClassFilesNextToSourcesWithoutPackageDeclaration() throws Exception {
        Path root = Files.createTempDirectory("citrus-java-loader-");
        Path nestedDir = root.resolve("org")
                .resolve("citrusframework")
                .resolve("junit")
                .resolve("jupiter")
                .resolve("integration")
                .resolve("java");
        Files.createDirectories(nestedDir);

        Path javaFile = nestedDir.resolve("StaleClassSourceTest.java");
        Files.writeString(javaFile, """
                public class StaleClassSourceTest {
                    public void run() {
                    }
                }
                """);

        Path classBesideSource = nestedDir.resolve("StaleClassSourceTest.class");
        assertThat(classBesideSource).doesNotExist();

        loadJavaSource("StaleClassSourceTest", "", javaFile);

        assertThat(classBesideSource)
                .as("compiled class must not pollute the source directory (breaks subsequent Maven surefire scans)")
                .doesNotExist();
    }

    @Test
    public void shouldLoadPackagedSourceFromDedicatedOutputDirectory() throws Exception {
        Path root = Files.createTempDirectory("citrus-java-loader-pkg-");
        Path nestedDir = root.resolve("com").resolve("example");
        Files.createDirectories(nestedDir);

        Path javaFile = nestedDir.resolve("PackagedJavaTest.java");
        Files.writeString(javaFile, """
                package com.example;

                public class PackagedJavaTest {
                    public void run() {
                    }
                }
                """);

        Path classBesideSource = nestedDir.resolve("PackagedJavaTest.class");
        assertThat(classBesideSource).doesNotExist();

        loadJavaSource("PackagedJavaTest", "com.example", javaFile);

        assertThat(classBesideSource).doesNotExist();
    }

    private void loadJavaSource(String testName, String packageName, Path javaFile) {
        Citrus citrus = CitrusInstanceManager.getOrDefault();

        JavaTestLoader loader = new JavaTestLoader();
        loader.setCitrus(citrus);
        loader.setContext(context);
        loader.setRunner(new DefaultTestCaseRunner(context));
        loader.setTestName(testName);
        loader.setPackageName(packageName);

        TestSource source = new TestSource(TestLoader.JAVA, testName, javaFile.toAbsolutePath().toString());
        loader.setSource(source);

        loader.load();
    }
}
