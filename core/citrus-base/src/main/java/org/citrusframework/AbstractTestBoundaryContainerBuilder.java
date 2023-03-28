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

import org.citrusframework.container.AbstractTestBoundaryActionContainer;

/**
 * Abstract container builder takes care on calling the container runner when actions are placed in the container.
 * @author Christoph Deppisch
 */
public abstract class AbstractTestBoundaryContainerBuilder<T extends AbstractTestBoundaryActionContainer, S extends AbstractTestBoundaryContainerBuilder<T, S>> extends AbstractTestContainerBuilder<T, S> {

    private String namePattern;
    private String packageNamePattern;
    private final List<String> testGroups = new ArrayList<>();
    private final Map<String, String> env = new HashMap<>();
    private final Map<String, String> systemProperties = new HashMap<>();

    /**
     * Condition on test names. The before test logic will only run when this condition matches.
     * @param namePattern
     */
    public S onTests(String namePattern) {
        this.namePattern = namePattern;
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
     * Condition on package names. The before test logic will only run when this condition matches.
     * @param packageNamePattern
     */
    public S onPackage(String packageNamePattern) {
        this.packageNamePattern = packageNamePattern;
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

        container.setNamePattern(namePattern);
        container.setPackageNamePattern(packageNamePattern);
        container.setTestGroups(testGroups);
        container.setSystemProperties(systemProperties);
        container.setEnv(env);

        return container;
    }
}
