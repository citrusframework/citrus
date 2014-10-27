/*
 * Copyright 2006-2013 the original author or authors.
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

package com.consol.citrus.admin.model;

import com.consol.citrus.model.testcase.core.MetaInfoType;

import java.util.*;

/**
 * @author Christoph Deppisch
 */
public class TestCaseData {

    private String name;

    private String packageName;
    private String groups;
    private String file;
    private TestCaseType type;
    private Long lastModified;

    private MetaInfoType metaInfo;

    private String description;

    private Map<String, Object> variables = new LinkedHashMap<String, Object>();

    private Map<String, Object> parameters = new LinkedHashMap<String, Object>();

    private List<TestActionData> actions = new ArrayList<TestActionData>();

    /**
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return
     */
    public String getPackageName() {
        return packageName;
    }

    /**
     * @param value
     */
    public void setPackageName(String value) {
        this.packageName = value;
    }

    /**
     * @return
     */
    public String getGroups() {
        return groups;
    }

    /**
     * @param value
     */
    public void setGroups(String value) {
        this.groups = value;
    }

    /**
     * @return
     */
    public String getFile() {
        return file;
    }

    /**
     * @param value
     */
    public void setFile(String value) {
        this.file = value;
    }

    /**
     * Gets the test case type.
     * @return
     */
    public TestCaseType getType() {
        return type;
    }

    /**
     * Sets the test case type.
     * @param type
     */
    public void setType(TestCaseType type) {
        this.type = type;
    }

    /**
     * Gets last modified timestamp of test case file.
     * @return
     */
    public Long getLastModified() {
        return lastModified;
    }

    /**
     * Sets last modified timestamp of test case file.
     * @param lastModified
     */
    public void setLastModified(Long lastModified) {
        this.lastModified = lastModified;
    }

    /**
     * Gets the meta information.
     * @return
     */
    public MetaInfoType getMetaInfo() {
        return metaInfo;
    }

    /**
     * Sets the meta information.
     * @param metaInfo
     */
    public void setMetaInfo(MetaInfoType metaInfo) {
        this.metaInfo = metaInfo;
    }

    /**
     * Sets the description.
     * @param description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the description.
     * @return
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets the test actions.
     * @return
     */
    public List<TestActionData> getActions() {
        return actions;
    }

    /**
     * Sets the test actions.
     * @param actions
     */
    public void setActions(List<TestActionData> actions) {
        this.actions = actions;
    }

    /**
     * Adds new test action using builder pattern style.
     * @param action
     * @return
     */
    public TestCaseData addTestAction(TestActionData action) {
        this.actions.add(action);
        return this;
    }

    /**
     * Setter for variables.
     * @param variables
     */
    public void setVariables(Map<String, Object> variables) {
        this.variables = variables;
    }

    /**
     * Gets the variable definitions.
     * @return
     */
    public Map<String, Object> getVariables() {
        return variables;
    }

    /**
     * Gets the test parameters.
     * @return the parameters
     */
    public Map<String, Object> getParameters() {
        return parameters;
    }

    /**
     * Sets the test parameters.
     * @param parameters
     */
    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }
}
