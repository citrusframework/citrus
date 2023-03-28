/*
 * Copyright 2006-2018 the original author or authors.
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

import org.springframework.util.StringUtils;

/**
 * @author Christoph Deppisch
 * @since 2.7
 */
public class TestClass {

    /** Test name and optional method */
    private String name;
    private String method;

    public TestClass() {
        super();
    }

    public TestClass(String name) {
        this.name = name;
    }

    public TestClass(String name, String method) {
        this(name);
        this.method = method;
    }

    /**
     * Gets the method.
     *
     * @return
     */
    public String getMethod() {
        return method;
    }

    /**
     * Sets the method.
     *
     * @param method
     */
    public void setMethod(String method) {
        this.method = method;
    }

    /**
     * Gets the name.
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name.
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Read String representation and construct proper test class instance. Read optional method name information and class name using format
     * "fully.qualified.class.Name#optionalMethodName()"
     *
     * @param testClass
     * @return
     */
    public static TestClass fromString(String testClass) {
        String className;
        String methodName = null;
        if (testClass.contains("#")) {
            className = testClass.substring(0, testClass.indexOf("#"));
            methodName = testClass.substring(testClass.indexOf("#") + 1);
        } else {
            className = testClass;
        }

        TestClass test = new TestClass(className);
        if (StringUtils.hasText(methodName)) {
            test.setMethod(methodName);
        }

        return test;
    }
}
