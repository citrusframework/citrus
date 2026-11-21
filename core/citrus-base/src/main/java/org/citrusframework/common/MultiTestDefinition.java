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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.citrusframework.TestActionBuilder;
import org.citrusframework.base.DefaultTestCase;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.util.StringUtils;

/**
 * Format neutral result of parsing a multi-test source: the shared declarations, the four hook action lists
 * and the test cases. Each parse creates new instances, so the builders of one definition are built at most
 * once.
 *
 * @since 5.3
 */
public final class MultiTestDefinition {

    private final String name;
    private final String description;
    private final DefaultTestCase shared;
    private final List<TestActionBuilder<?>> beforeAll;
    private final List<TestActionBuilder<?>> beforeEach;
    private final List<TestActionBuilder<?>> afterEach;
    private final List<TestActionBuilder<?>> afterAll;
    private final List<DefaultTestCase> tests;

    private MultiTestDefinition(Builder builder) {
        this.name = builder.name;
        this.description = builder.description;
        this.shared = builder.shared;
        this.beforeAll = Collections.unmodifiableList(new ArrayList<>(builder.beforeAll));
        this.beforeEach = Collections.unmodifiableList(new ArrayList<>(builder.beforeEach));
        this.afterEach = Collections.unmodifiableList(new ArrayList<>(builder.afterEach));
        this.afterAll = Collections.unmodifiableList(new ArrayList<>(builder.afterAll));
        this.tests = Collections.unmodifiableList(new ArrayList<>(builder.tests));
    }

    /**
     * Validates the definition before anything runs.
     * @param fileName the display name of the source used in error messages.
     * @throws CitrusRuntimeException when the source holds no test, a test has no name or two tests share a name.
     */
    public void validate(String fileName) {
        if (tests.isEmpty()) {
            throw new CitrusRuntimeException("Multi-test file '%s' holds no test".formatted(fileName));
        }

        Set<String> names = new HashSet<>();
        Set<String> duplicates = new TreeSet<>();
        for (int i = 0; i < tests.size(); i++) {
            String testName = tests.get(i).getName();
            if (!StringUtils.hasText(testName)) {
                throw new CitrusRuntimeException(
                        "Multi-test file '%s' has a test without name at position %d".formatted(fileName, i + 1));
            }

            if (!names.add(testName)) {
                duplicates.add(testName);
            }
        }

        if (!duplicates.isEmpty()) {
            throw new CitrusRuntimeException("Multi-test file '%s' has duplicate test names: %s"
                    .formatted(fileName, String.join(", ", duplicates)));
        }
    }

    /**
     * Gets the test names in declaration order.
     */
    public List<String> getTestNames() {
        return tests.stream().map(DefaultTestCase::getName).toList();
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Gets the shared declarations: a test case holding only the shared variables, endpoints and beans.
     */
    public DefaultTestCase getShared() {
        return shared;
    }

    public List<TestActionBuilder<?>> getBeforeAll() {
        return beforeAll;
    }

    public List<TestActionBuilder<?>> getBeforeEach() {
        return beforeEach;
    }

    public List<TestActionBuilder<?>> getAfterEach() {
        return afterEach;
    }

    public List<TestActionBuilder<?>> getAfterAll() {
        return afterAll;
    }

    public List<DefaultTestCase> getTests() {
        return tests;
    }

    /**
     * Builder for the definition.
     */
    public static final class Builder {

        private String name;
        private String description;
        private DefaultTestCase shared = new DefaultTestCase();
        private final List<TestActionBuilder<?>> beforeAll = new ArrayList<>();
        private final List<TestActionBuilder<?>> beforeEach = new ArrayList<>();
        private final List<TestActionBuilder<?>> afterEach = new ArrayList<>();
        private final List<TestActionBuilder<?>> afterAll = new ArrayList<>();
        private final List<DefaultTestCase> tests = new ArrayList<>();

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder shared(DefaultTestCase shared) {
            this.shared = shared;
            return this;
        }

        public Builder beforeAll(List<? extends TestActionBuilder<?>> actions) {
            this.beforeAll.addAll(actions);
            return this;
        }

        public Builder beforeEach(List<? extends TestActionBuilder<?>> actions) {
            this.beforeEach.addAll(actions);
            return this;
        }

        public Builder afterEach(List<? extends TestActionBuilder<?>> actions) {
            this.afterEach.addAll(actions);
            return this;
        }

        public Builder afterAll(List<? extends TestActionBuilder<?>> actions) {
            this.afterAll.addAll(actions);
            return this;
        }

        public Builder test(DefaultTestCase test) {
            this.tests.add(test);
            return this;
        }

        public Builder tests(List<DefaultTestCase> tests) {
            this.tests.addAll(tests);
            return this;
        }

        /**
         * Builds the definition. The shared declarations are named after the definition.
         */
        public MultiTestDefinition build() {
            if (name != null) {
                shared.setName(name);
            }
            return new MultiTestDefinition(this);
        }
    }
}
