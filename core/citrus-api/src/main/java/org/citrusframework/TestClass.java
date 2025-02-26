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

package org.citrusframework;

import org.citrusframework.exceptions.CitrusRuntimeException;

/**
 * @since 2.7
 */
public class TestClass extends TestSource {

    /** Optional test method */
    private final String method;

    private final Class<?> testClass;

    public TestClass(Class<?> testClass) {
        this(testClass, null);
    }

    public TestClass(Class<?> testClass, String method) {
        super(testClass);
        this.testClass = testClass;
        this.method = method;
    }

    /**
     * Gets the test method name to execute.
     */
    public String getMethod() {
        return method;
    }

    /**
     * Gets the test class to execute.
     */
    public Class<?> getTestClass() {
        return testClass;
    }

    /**
     * Read String representation and construct proper test class instance. Read optional method name information and class name using format
     * "fully.qualified.class.Name#optionalMethodName()"
     *
     * @param testClass
     * @return
     */
    public static TestClass fromString(String testClass) {
        try {
            String className;
            String methodName = null;
            if (testClass.contains("#")) {
                className = testClass.substring(0, testClass.indexOf("#"));
                methodName = testClass.substring(testClass.indexOf("#") + 1);
            } else {
                className = testClass;
            }

            if (methodName != null && !methodName.isBlank()) {
                return new TestClass(Class.forName(className), methodName);
            }

            return new TestClass(Class.forName(className));
        } catch (ClassNotFoundException e) {
            throw new CitrusRuntimeException("Failed to create test class", e);
        }
    }

    public static boolean isKnownToClasspath(String testClass) {
        try {
            String className;
            if (testClass.contains("#")) {
                className = testClass.substring(0, testClass.indexOf("#"));
            } else {
                className = testClass;
            }

            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
