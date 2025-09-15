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

package org.citrusframework.cucumber.util;

import java.net.URI;

import io.cucumber.java.Scenario;
import org.citrusframework.util.FileUtils;

public class FeatureHelper {

    /**
     * Prevent instantiation of utility class.
     */
    private FeatureHelper() {
        // prevent instantiation
    }

    /**
     * Extract feature file name from given uri.
     * This utility method extracts the feature file name from the given file path.
     */
    public static String extractFeatureFileName(URI uri) {
        if (uri == null) {
            return "";
        }

        if (uri.toString().contains("/")) {
            return FileUtils.getFileName(uri.toString());
        }

        return uri.toString();
    }

    /**
     * Extract feature file path from given uri.
     * This utility method extracts the feature file path with null safe check.
     */
    public static String extractFeatureFile(URI uri) {
        if (uri == null) {
            return "";
        }

        return uri.toString();
    }

    /**
     * Extract feature file base path as package from given uri.
     * This utility method extracts the feature file path with null safe check.
     */
    public static String extractFeaturePackage(URI uri) {
        if (uri == null) {
            return "";
        }

        if (uri.getSchemeSpecificPart().contains("/")) {
            return FileUtils.getBasePath(uri.getSchemeSpecificPart()).replaceAll("/", ".");
        }

        return "";
    }

    /**
     * Extract feature file name from given scenario. The scenario URI usually holds the full qualified feature file path.
     * This utility method extracts the feature file name from this path.
     */
    public static String extractFeatureFileName(Scenario scenario) {
        return extractFeatureFileName(scenario.getUri());
    }

    /**
     * Extract feature file from given scenario. The scenario URI usually holds the full qualified feature file path.
     * This utility method extracts the feature file path.
     */
    public static String extractFeatureFile(Scenario scenario) {
        return extractFeatureFile(scenario.getUri());
    }

    /**
     * Extract feature file base path from given scenario.
     * The scenario URI usually holds the full qualified feature file path.
     * This utility method extracts the feature file path as package name.
     */
    public static String extractFeaturePackage(Scenario scenario) {
        return extractFeaturePackage(scenario.getUri());
    }
}
