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

/**
 * @author Christoph Deppisch
 */
public class TestCaseInfo {

    private String name;

    private String packageName;
    private String groups;
    private String file;
    private TestCaseType type;
    private Long lastModified;

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
}
