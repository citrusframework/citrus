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

package org.citrusframework.container;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.citrusframework.AbstractTestContainerBuilder;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.citrusframework.util.StringUtils.hasText;

/**
 * Abstract suit container actions executed before and after test suite run. Container decides
 * weather to execute according to given suite name and included test groups if any.
 *
 * @since 2.0
 */
public abstract class AbstractSuiteActionContainer extends AbstractActionContainer implements ReferenceResolverAware {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(AbstractSuiteActionContainer.class);

    /** List of suite names that match for this container */
    private List<String> suiteNames = new ArrayList<>();

    /** List of test group names that match for this container */
    private List<String> testGroups = new ArrayList<>();

    /** Optional env parameters */
    private Map<String, String> env = new HashMap<>();

    /** Optional system properties */
    private Map<String, String> systemProperties = new HashMap<>();

    protected AbstractSuiteActionContainer(String name, AbstractTestContainerBuilder<?, ?> builder) {
        super(name, builder);
    }

    /**
     * Checks if this suite actions should execute according to suite name and included test groups.
     * @param suiteName
     * @param includedGroups
     * @return
     */
    public boolean shouldExecute(String suiteName, String[] includedGroups) {
        String baseErrorMessage = "Skip before/after suite container because of %s restriction - do not execute container '%s'";

        if (hasText(suiteName)
                && suiteNames != null && !suiteNames.isEmpty()
                && !suiteNames.contains(suiteName)) {
            logger.warn("{} suite name {}", baseErrorMessage, getName());
            return false;
        }

        if (!checkTestGroups(includedGroups)) {
            logger.warn("{} test groups {}", baseErrorMessage, getName());
            return false;
        }

        for (Map.Entry<String, String> envEntry : env.entrySet()) {
            if (!System.getenv().containsKey(envEntry.getKey())
                    || (hasText(envEntry.getValue()) && !System.getenv().get(envEntry.getKey()).equals(envEntry.getValue()))) {
                logger.warn("{} env properties {}", baseErrorMessage, getName());
                return false;
            }
        }

        for (Map.Entry<String, String> systemProperty : systemProperties.entrySet()) {
            if (!System.getProperties().containsKey(systemProperty.getKey())
                    || (hasText(systemProperty.getValue()) && !System.getProperties().get(systemProperty.getKey()).equals(systemProperty.getValue()))) {
                logger.warn("{} system properties {}", baseErrorMessage, getName());
                return false;
            }
        }

        return true;
    }

    /**
     * Checks on included test groups if we should execute sequence. Included group list should have
     * at least one entry matching the sequence test groups restriction.
     *
     * @param includedGroups
     * @return
     */
    private boolean checkTestGroups(String[] includedGroups) {
        if (testGroups.isEmpty()) {
            return true;
        }

        if (includedGroups != null) {
            for (String includedGroup : includedGroups) {
                if (testGroups.contains(includedGroup)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Gets the test groups that restrict the container execution.
     * @return
     */
    public List<String> getTestGroups() {
        return testGroups;
    }

    /**
     * Sets the test groups that restrict the container execution.
     * @param testGroups
     */
    public void setTestGroups(List<String> testGroups) {
        this.testGroups = testGroups;
    }

    /**
     * Gets the suite names that restrict the container execution.
     * @return
     */
    public List<String> getSuiteNames() {
        return suiteNames;
    }

    /**
     * Sets the suite names that restrict the container execution.
     * @param suiteNames
     */
    public void setSuiteNames(List<String> suiteNames) {
        this.suiteNames = suiteNames;
    }

    /**
     * Gets the env.
     *
     * @return
     */
    public Map<String, String> getEnv() {
        return env;
    }

    /**
     * Sets the env.
     *
     * @param env
     */
    public void setEnv(Map<String, String> env) {
        this.env = env;
    }

    /**
     * Gets the systemProperties.
     *
     * @return
     */
    public Map<String, String> getSystemProperties() {
        return systemProperties;
    }

    /**
     * Sets the systemProperties.
     *
     * @param systemProperties
     */
    public void setSystemProperties(Map<String, String> systemProperties) {
        this.systemProperties = systemProperties;
    }

    @Override
    public void setReferenceResolver(ReferenceResolver referenceResolver) {
        for (TestActionBuilder<?> builder : actions) {
            if (builder instanceof ReferenceResolverAware referenceResolverAwareBuilder) {
                referenceResolverAwareBuilder.setReferenceResolver(referenceResolver);
            }
        }
    }
}
