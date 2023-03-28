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

package org.citrusframework.generate;

/**
 * Unit testing framework can be either JUnit or TestNG. Test case generator
 * will create different Java classes according to the unit test framework.
 */
public enum UnitFramework {
    TESTNG,
    JUNIT4,
    JUNIT5;

    public static UnitFramework fromString(String value) {
        if (value.equalsIgnoreCase("testng")) {
            return TESTNG;
        } else if (value.equalsIgnoreCase("junit") || value.equalsIgnoreCase("junit4")) {
            return JUNIT4;
        } else if (value.equalsIgnoreCase("junit5")) {
            return JUNIT5;
        } else {
            throw new IllegalArgumentException("Found unsupported unit test framework '" + value + "'");
        }
    }
}
