/*
 * Copyright 2006-2015 the original author or authors.
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

package org.citrusframework;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.citrusframework.container.AbstractSuiteActionContainer;

/**
 * Abstract container builder takes care on calling the container runner when actions are placed in the container.
 * @author Christoph Deppisch
 */
public abstract class AbstractSuiteContainerBuilder<T extends AbstractSuiteActionContainer, S extends AbstractSuiteContainerBuilder<T, S>> extends AbstractTestContainerBuilder<T, S> {

    private final List<String> suiteNames = new ArrayList<>();
    private final List<String> testGroups = new ArrayList<>();
    private final Map<String, String> env = new HashMap<>();
    private final Map<String, String> systemProperties = new HashMap<>();

    /**
     * Condition on suite name. The before test logic will only run when this condition matches.
     * @param suiteNames
     */
    public S onSuite(String... suiteNames) {
        return onSuites(Arrays.asList(suiteNames));
    }

    /**
     * Condition on suite names. The before test logic will only run when this condition matches.
     * @param suiteNames
     */
    public S onSuites(List<String> suiteNames) {
        this.suiteNames.addAll(suiteNames);
        return self;
    }

    /**
     * Condition on test group name. The before test logic will only run when this condition matches.
     * @param testGroups
     */
    public S onTestGroup(String... testGroups) {
        return onTestGroups(Arrays.asList(testGroups));
    }

    /**
     * Condition on test group names. The before test logic will only run when this condition matches.
     * @param testGroups
     */
    public S onTestGroups(List<String> testGroups) {
        this.testGroups.addAll(testGroups);
        return self;
    }

    /**
     * Condition on system property with value. The before test logic will only run when this condition matches.
     * @param name
     * @param value
     */
    public S whenSystemProperty(String name, String value) {
        this.systemProperties.put(name, value);
        return self;
    }

    /**
     * Condition on system properties. The before test logic will only run when this condition matches.
     * @param systemProperties
     */
    public S whenSystemProperties(Map<String, String> systemProperties) {
        this.systemProperties.putAll(systemProperties);
        return self;
    }


    /**
     * Condition on environment variable with value. The before test logic will only run when this condition matches.
     * @param name
     * @param value
     */
    public S whenEnv(String name, String value) {
        this.env.put(name, value);
        return self;
    }

    /**
     * Condition on environment variables. The before test logic will only run when this condition matches.
     * @param envs
     */
    public S whenEnv(Map<String, String> envs) {
        this.env.putAll(envs);
        return self;
    }

    @Override
    public T build() {
        T container = super.build();

        container.setSuiteNames(suiteNames);
        container.setTestGroups(testGroups);
        container.setSystemProperties(systemProperties);
        container.setEnv(env);

        return container;
    }
}
