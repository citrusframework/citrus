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

package org.citrusframework.junit.jupiter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.citrusframework.CitrusInstanceManager;
import org.citrusframework.CitrusSettings;
import org.citrusframework.TestCase;
import org.citrusframework.api.common.MultiTestLoader;
import org.citrusframework.api.common.MultiTestRun;
import org.citrusframework.base.annotations.CitrusAnnotations;
import org.citrusframework.api.common.TestLoader;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.ClasspathResourceResolver;
import org.citrusframework.util.FileUtils;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.opentest4j.TestAbortedException;

public final class CitrusTestFactorySupport {

    private final String type;
    private final Consumer<TestLoader> handler;

    public CitrusTestFactorySupport(String type, Consumer<TestLoader> handler) {
        this.type = type;
        this.handler = handler;
    }

    public static CitrusTestFactorySupport factory(String type) {
        return new CitrusTestFactorySupport(type, TestLoader::load);
    }

    public static CitrusTestFactorySupport java() {
        return factory(TestLoader.JAVA);
    }

    public static CitrusTestFactorySupport xml() {
        return factory(TestLoader.XML);
    }

    public static CitrusTestFactorySupport yaml() {
        return factory(TestLoader.YAML);
    }

    public static CitrusTestFactorySupport groovy() {
        return factory(TestLoader.GROOVY);
    }

    public static CitrusTestFactorySupport springXml() {
        return factory(TestLoader.SPRING);
    }

    /**
     * Creates stream of dynamic tests based on package scan. Scans package for all Xml test case files and creates dynamic test instance for it.
     * @param packagesToScan
     * @return
     */
    public Stream<DynamicTest> packageScan(String ... packagesToScan) {
        List<DynamicTest> tests = new ArrayList<>();

        for (ScannedTest scannedTest : scan(packagesToScan)) {
            TestLoader testLoader = createTestLoader(scannedTest.testName(), scannedTest.packageName());
            tests.add(DynamicTest.dynamicTest(scannedTest.testName(), () -> handler.accept(testLoader)));
        }

        return tests.stream();
    }

    /**
     * Creates dynamic nodes based on package scan. Like {@link #packageScan(String...)}, but a test source holding
     * several test cases (e.g. a YAML or XML multi-test file) becomes one dynamic container with one dynamic test
     * per test case. Other test sources become one dynamic test each.
     * @param packagesToScan the packages to scan for test files.
     * @return the dynamic nodes.
     * @since 5.3
     */
    public Stream<DynamicNode> packageScanNodes(String ... packagesToScan) {
        List<DynamicNode> nodes = new ArrayList<>();

        for (ScannedTest scannedTest : scan(packagesToScan)) {
            nodes.add(dynamicNode(scannedTest.testName(), createTestLoader(scannedTest.testName(), scannedTest.packageName())));
        }

        return nodes.stream();
    }

    /**
     * Finds all test files of this factory's type in the given packages.
     */
    private List<ScannedTest> scan(String ... packagesToScan) {
        List<ScannedTest> scannedTests = new ArrayList<>();

        for (String packageScan : packagesToScan) {
            try {
                for (String fileNamePattern : CitrusSettings.getTestFileNamePattern(type)) {
                    Set<Path> fileResources = new ClasspathResourceResolver().getResources(packageScan.replace('.', File.separatorChar), fileNamePattern);
                    for (Path fileResource : fileResources) {
                        String filePath = fileResource.getParent().toFile().getCanonicalPath();

                        if (packageScan.startsWith("file:")) {
                            filePath = "file:" + filePath;
                        }

                        filePath = filePath.substring(filePath.indexOf(packageScan.replace('.', File.separatorChar)));

                        String testName = FileUtils.getBaseName(String.valueOf(fileResource.getFileName()));
                        scannedTests.add(new ScannedTest(testName, filePath));
                    }
                }
            } catch (IOException e) {
                throw new CitrusRuntimeException("Unable to locate file resources for test package '" + packageScan + "'", e);
            }
        }

        return scannedTests;
    }

    /**
     * Creates dynamic test that executes Xml test given by name and package.
     * @param packageName
     * @param testNames
     * @return
     */
    public Stream<DynamicTest> dynamicTests(String packageName, String ... testNames) {
        return Stream.of(testNames).map(testName -> {
            TestLoader testLoader = createTestLoader(testName, packageName);
            return DynamicTest.dynamicTest(testName, () -> handler.accept(testLoader));
        });
    }

    /**
     * Creates dynamic test that executes Xml test given by name and package.
     * @param testClass
     * @param testNames
     * @return
     */
    public Stream<DynamicTest> dynamicTests(Class<?> testClass, String ... testNames) {
        return Stream.of(testNames).map(testName -> {
            TestLoader testLoader = createTestLoader(testName, testClass.getPackage().getName());
            return DynamicTest.dynamicTest(testName, () -> handler.accept(testLoader));
        });
    }

    /**
     * Creates dynamic test that executes Xml test given by name and package.
     * @param packageName
     * @param testName
     * @return
     */
    public DynamicTest dynamicTest(String packageName, String testName) {
        return dynamicTest(testName, createTestLoader(testName, packageName));
    }

    /**
     * Creates dynamic test that uses the given test loader to load the test.
     * @param testName
     * @param testLoader
     * @return
     */
    public DynamicTest dynamicTest(String testName, TestLoader testLoader) {
        return DynamicTest.dynamicTest(testName, () -> handler.accept(testLoader));
    }

    /**
     * Creates dynamic nodes for the given test names in the given package. A test source holding several test cases
     * becomes one dynamic container with one dynamic test per test case.
     * @since 5.3
     */
    public Stream<DynamicNode> dynamicNodes(String packageName, String ... testNames) {
        return Stream.of(testNames).map(testName -> dynamicNode(testName, createTestLoader(testName, packageName)));
    }

    /**
     * Creates dynamic nodes for the given test names in the package of the given test class. A test source holding
     * several test cases becomes one dynamic container with one dynamic test per test case.
     * @since 5.3
     */
    public Stream<DynamicNode> dynamicNodes(Class<?> testClass, String ... testNames) {
        return dynamicNodes(testClass.getPackage().getName(), testNames);
    }

    /**
     * Creates the dynamic node for the given test name in the given package.
     * @since 5.3
     */
    public DynamicNode dynamicNode(String packageName, String testName) {
        return dynamicNode(testName, createTestLoader(testName, packageName));
    }

    /**
     * Creates the dynamic node for the given test loader. When the loader opens a test source holding several test
     * cases, the node is a dynamic container named after the source with one dynamic test per test case. The test
     * cases of one container always run one after another on the container's thread, also when JUnit runs tests in
     * parallel, and afterAll runs once the last test case has finished. Otherwise the node is the same dynamic test as
     * {@link #dynamicTest(String, TestLoader)} creates. A source that cannot be opened becomes a dynamic test that
     * reports the error when it runs.
     * <p>
     * The handler of this factory applies to test sources holding a single test case only.
     * @since 5.3
     */
    public DynamicNode dynamicNode(String testName, TestLoader testLoader) {
        if (testLoader instanceof MultiTestLoader multiTestLoader) {
            Optional<MultiTestRun> run;
            try {
                run = multiTestLoader.openMultiTest();
            } catch (RuntimeException e) {
                return DynamicTest.dynamicTest(testName, testLoader::load);
            }

            if (run.isPresent()) {
                return toContainer(run.get());
            }
        }

        return dynamicTest(testName, testLoader);
    }

    /**
     * Maps a multi-test run to a dynamic container whose children run sequentially on the container's thread. The
     * children stream closes the run, which runs afterAll after the last child.
     */
    private static DynamicContainer toContainer(MultiTestRun run) {
        List<String> testNames = run.getTestNames();
        Stream<DynamicNode> children = IntStream.range(0, testNames.size())
                .<DynamicNode>mapToObj(index -> DynamicTest.dynamicTest(testNames.get(index), () -> runTest(run, index)))
                .onClose(run::close);

        return DynamicContainer.dynamicContainer(configuration -> configuration
                .displayName(run.getName())
                .childExecutionMode(ExecutionMode.SAME_THREAD)
                .children(children));
    }

    private static void runTest(MultiTestRun run, int index) {
        TestCase testCase = run.runTest(index);
        if (testCase.getTestResult() != null && testCase.getTestResult().isSkipped()) {
            throw new TestAbortedException("Test '%s' is disabled".formatted(testCase.getName()));
        }
    }

    private TestLoader createTestLoader(String testName, String packageName) {
        TestLoader testLoader = TestLoader.lookup(type)
                .orElseThrow(() -> new CitrusRuntimeException(String.format("Missing '%s' test loader in project classpath - " +
                        "please add proper Citrus module to the project", type)));

        testLoader.setTestClass(DynamicTest.class);
        testLoader.setTestName(testName);
        testLoader.setPackageName(packageName);

        CitrusAnnotations.injectAll(testLoader, CitrusInstanceManager.getOrDefault());

        return testLoader;
    }

    /**
     * Test file found by package scan.
     */
    private record ScannedTest(String testName, String packageName) {
    }
}
