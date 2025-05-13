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

package org.citrusframework.util;

import java.util.function.Predicate;

/**
 * Tests if a string represents a Yaml content. An empty string is considered to be a valid Yaml.
 */
public class IsYamlPredicate implements Predicate<String> {

    private static final IsYamlPredicate INSTANCE = new IsYamlPredicate();

    private IsYamlPredicate() {
        // Singleton
    }

    public static IsYamlPredicate getInstance() {
        return INSTANCE;
    }

    @Override
    public boolean test(String toTest) {
        if (toTest == null) {
            return false;
        }

        String normalized = toTest.trim().replaceAll("\\r(\\n)?", "\n");
        return normalized.isEmpty() || normalized.startsWith("- ") ||
                normalized.split("\n")[0].trim().endsWith(":")  ||
                normalized.split("\n")[0].trim().endsWith(": {}");
    }
}
