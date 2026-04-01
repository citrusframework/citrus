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

package org.citrusframework.mcp;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import org.citrusframework.CitrusSettings;
import org.citrusframework.spi.Resources;
import org.citrusframework.util.FileUtils;

@ApplicationScoped
public class DocsData {

    public static final String CITRUS_MANUAL_DOC = "manual";

    /**
     * Gets all documentation file names.
     */
    public List<String> getDocsIndex(String version) throws IOException {
        String index = FileUtils.readToString(
                Resources.fromClasspath(CITRUS_MANUAL_DOC + "/index.txt"), StandardCharsets.UTF_8);
        return Arrays.asList(index.split("\n"));
    }

    /**
     * Gets the documentation Ascii doc file content for the given component type and name search.
     */
    public DocInfo getDocsPage(String kind, String searchKey, String version) throws IOException {
        Optional<String> docFile = getDocsIndex(version).stream()
                .filter(fileName -> fileName.contains(kind))
                .filter(fileName -> fileName.contains(searchKey))
                .findFirst();

        if (docFile.isPresent()) {
            String markdown = FileUtils.readToString(
                    Resources.fromClasspath(CITRUS_MANUAL_DOC + "/" + docFile.get()));
            return new DocInfo(docFile.get(), version, markdown);
        }

        return null;
    }

    /**
     * Gets a collection of best practices when writing Citrus tests.
     */
    public List<String> getBestPractices() {
        List<String> bestPractices = new ArrayList<>();

        bestPractices.add("Set Citrus configuration properties in an application.properties file named: " + CitrusSettings.getApplicationPropertiesFile());
        bestPractices.add("For Java test files use one of the file name pattern: " + CitrusSettings.getJavaTestFileNamePattern());
        bestPractices.add("For YAML DSL test files use one of the file name pattern: " + CitrusSettings.getYamlTestFileNamePattern());
        bestPractices.add("For XML DSL test files use one of the file name pattern: " + CitrusSettings.getXmlTestFileNamePattern());
        bestPractices.add("For Groovy DSL test files use one of the file name pattern: " + CitrusSettings.getGroovyTestFileNamePattern());
        bestPractices.add("Evaluate the usage of test variables for values that are used multiple times in a test");
        bestPractices.add("When sending and receiving messages load large message payloads from a file resource");

        return bestPractices;
    }

    public record DocInfo(String fileName, String version, String adoc) {
    }
}
