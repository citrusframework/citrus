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

import org.citrusframework.exceptions.CitrusRuntimeException;

/**
 * @author Christoph Deppisch
 * @since 2.7
 */
public class TestClass extends TestSource {

    /** Optional test method */
    private final String method;

    public TestClass(Class<?> type) {
        this(type, null);
    }

    public TestClass(Class<?> type, String method) {
        super(type);
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
}
