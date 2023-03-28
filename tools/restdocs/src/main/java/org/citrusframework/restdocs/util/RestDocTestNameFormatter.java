/*
 * Copyright 2006-2016 the original author or authors.
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

package org.citrusframework.restdocs.util;

/**
 * @author Christoph Deppisch
 * @since 2.6
 */
public final class RestDocTestNameFormatter {

    /**
     * Prevent instantiation.
     */
    private RestDocTestNameFormatter() {
        super();
    }

    /**
     * Format test name so we get a good RestDoc snippet output.
     * @param testClass
     * @param testName
     * @return
     */
    public static String format(Class<?> testClass, String testName) {
        String formatted = testName;
        if (testName.contains(".") && testClass.getSimpleName().endsWith(testName.substring(0, testName.indexOf(".")))) {
            formatted = formatted.substring(formatted.indexOf(".") + 1);
        }

        if (formatted.endsWith("IT")) {
            formatted = formatted.substring(0, formatted.length() - 1) + "t";
        }

        return formatted;
    }
}
