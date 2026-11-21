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

package org.citrusframework.yaml;

import java.util.ArrayList;
import java.util.List;

import org.citrusframework.TestActionBuilder;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.api.yaml.SchemaProperty;
import org.citrusframework.base.DefaultTestCase;
import org.citrusframework.common.MultiTestDefinition;
import org.citrusframework.exceptions.CitrusRuntimeException;

/**
 * Root type of a YAML test file. A file without the keys {@code tests}, {@code beforeAll}, {@code beforeEach},
 * {@code afterEach} and {@code afterAll} is a single test case, exactly as {@link YamlTestCase}. A file with a
 * {@code tests} list holds several test cases that share the root {@code variables}, {@code endpoints} and
 * {@code configuration} and the four hooks.
 *
 * @since 5.3
 */
public class YamlTestFile extends YamlTestCase {

    private String name;
    private String description;

    private final List<String> singleTestKeys = new ArrayList<>();

    private List<Entry> tests;
    private List<TestActions> beforeAll;
    private List<TestActions> beforeEach;
    private List<TestActions> afterEach;
    private List<TestActions> afterAll;

    @Override
    public void setName(String name) {
        this.name = name;
        super.setName(name);
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
        super.setDescription(description);
    }

    @Override
    public void setAuthor(String author) {
        singleTestKeys.add("author");
        super.setAuthor(author);
    }

    @Override
    public void setStatus(TestCaseMetaInfo.Status status) {
        singleTestKeys.add("status");
        super.setStatus(status);
    }

    @Override
    public void setActions(List<TestActions> actions) {
        singleTestKeys.add("actions");
        super.setActions(actions);
    }

    @Override
    public void setFinally(List<TestActions> actions) {
        singleTestKeys.add("finally");
        super.setFinally(actions);
    }

    @SchemaProperty(description = "The test cases of a multi-test file.")
    public void setTests(List<Entry> tests) {
        this.tests = tests;
    }

    @SchemaProperty(description = "Test actions that run once before the first test of the file.")
    public void setBeforeAll(List<TestActions> actions) {
        this.beforeAll = actions;
    }

    @SchemaProperty(description = "Test actions that run at the start of every test of the file.")
    public void setBeforeEach(List<TestActions> actions) {
        this.beforeEach = actions;
    }

    @SchemaProperty(description = "Test actions that run at the end of every test of the file.")
    public void setAfterEach(List<TestActions> actions) {
        this.afterEach = actions;
    }

    @SchemaProperty(description = "Test actions that run once after the last test of the file.")
    public void setAfterAll(List<TestActions> actions) {
        this.afterAll = actions;
    }

    /**
     * Checks whether the file holds several test cases.
     * @return true when the file declares tests or one of the hooks.
     */
    public boolean isMultiTest() {
        return tests != null || beforeAll != null || beforeEach != null || afterEach != null || afterAll != null;
    }

    /**
     * Converts this multi-test file to its format neutral definition and validates the root keys.
     * @param defaultName the display name to use when the file has no name.
     * @return the definition.
     * @throws CitrusRuntimeException when the root combines tests with single-test keys.
     */
    public MultiTestDefinition toDefinition(String defaultName) {
        String fileName = name != null ? name : defaultName;

        if (!singleTestKeys.isEmpty()) {
            throw new CitrusRuntimeException("Multi-test file '%s' must not combine 'tests' with '%s'"
                    .formatted(fileName, singleTestKeys.get(0)));
        }

        MultiTestDefinition.Builder builder = new MultiTestDefinition.Builder()
                .name(fileName)
                .description(description)
                .shared((DefaultTestCase) getTestCase())
                .beforeAll(builders(beforeAll))
                .beforeEach(builders(beforeEach))
                .afterEach(builders(afterEach))
                .afterAll(builders(afterAll));

        if (tests != null) {
            tests.forEach(entry -> builder.test(entry.toTestCase()));
        }

        return builder.build();
    }

    private static List<TestActionBuilder<?>> builders(List<TestActions> actions) {
        if (actions == null) {
            return List.of();
        }

        return actions.stream().<TestActionBuilder<?>>map(TestActions::get).toList();
    }

    /**
     * One test case of a multi-test file. It accepts the same keys as a single-test file.
     */
    public static class Entry extends YamlTestCase {

        private boolean named;

        @Override
        public void setName(String name) {
            this.named = true;
            super.setName(name);
        }

        /**
         * Gets the test case of this entry. An entry without name gets no name, so validation can report it.
         */
        DefaultTestCase toTestCase() {
            DefaultTestCase testCase = (DefaultTestCase) getTestCase();
            if (!named) {
                testCase.setName(null);
            }
            return testCase;
        }
    }
}
