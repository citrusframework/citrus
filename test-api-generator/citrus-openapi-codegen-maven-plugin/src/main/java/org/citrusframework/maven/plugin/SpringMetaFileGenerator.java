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

package org.citrusframework.maven.plugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import org.apache.maven.plugin.MojoExecutionException;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.maven.plugin.TestApiGeneratorMojo.ApiConfig;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static org.citrusframework.maven.plugin.TestApiGeneratorMojo.CITRUS_TEST_SCHEMA;
import static org.citrusframework.maven.plugin.TestApiGeneratorMojo.CITRUS_TEST_SCHEMA_KEEP_HINT;
import static org.citrusframework.maven.plugin.TestApiGeneratorMojo.replaceDynamicVars;
import static org.citrusframework.maven.plugin.TestApiGeneratorMojo.replaceDynamicVarsToLowerCase;

/**
 * Utility class responsible for generating the Spring meta files 'spring.handlers' and 'spring.schemas', used
 * in Spring integration testing. These meta files define mappings between XML namespace URIs and corresponding
 * handler classes. The class provides methods to generate these meta files based on the configuration provided.
 * <p>
 * The generated meta files can be created either in the generated folder or in the main resources folder. See
 * {@link TestApiGeneratorMojo#RESOURCE_FOLDER_PROPERTY} for details. The implemented algorithm carefully updates these
 * files and tries to keep non generated information unchanged. Therefore, a special segment in the namespace uri is used, namely
 * {@link TestApiGeneratorMojo#CITRUS_TEST_SCHEMA}.
 * </p>
 */
public class SpringMetaFileGenerator {

    private final TestApiGeneratorMojo testApiGeneratorMojo;

    private String lineEnding = "\n";

    public SpringMetaFileGenerator(TestApiGeneratorMojo testApiGeneratorMojo) {
        this.testApiGeneratorMojo = testApiGeneratorMojo;
    }

    /**
     * Reads the lines from the specified file and filters out lines indicating a generated test API,
     * while maintaining all non-generated test API lines. This method is used to process files
     * containing both generated and non-generated test APIs, allowing seamless integration and
     * modification of both types of APIs in the same source files.
     *
     * <p>
     * Generated test API lines are identified by the presence of the {@code CITRUS_TEST_SCHEMA}
     * string and excluded from the output of this method, while all other lines are preserved.
     * This enables the algorithm to operate on files that are not purely generated, for example,
     * when mixing generated with non-generated APIs in 'src/main/META-INF'.
     * </p>
     *
     * @param file the file to read and filter
     * @return a list of filtered lines, excluding lines indicating a generated test API
     * @throws CitrusRuntimeException if an error occurs while reading the file
     */
    private List<String> readAndFilterLines(File file) {
        if (!file.exists()) {
            return emptyList();
        }

        List<String> filteredLines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {

                if (lineEnding.contains("\r")) {
                    lineEnding = "\r\n";
                }

                if (line.contains(CITRUS_TEST_SCHEMA_KEEP_HINT)) {
                    filteredLines.add(reader.readLine());
                } else if (!line.contains(CITRUS_TEST_SCHEMA)) {
                    filteredLines.add(line);
                }
            }
        } catch (IOException e) {
            throw new CitrusRuntimeException(format("Unable to read file file: '%s'", file.getPath()), e);
        }

        return filteredLines;
    }

    public void generateSpringIntegrationMetaFiles() throws MojoExecutionException {

        File metaFolder = new File(testApiGeneratorMojo.getMavenProject().getBasedir(), testApiGeneratorMojo.getMetaInfFolder());

        if (!metaFolder.exists() && !metaFolder.mkdirs()) {
            throw new CitrusRuntimeException(
                    format("Unable to create spring meta file directory: '%s'", testApiGeneratorMojo.getMetaInfFolder()));
        }

        try {
            writeSpringSchemaMetaFile(metaFolder);
            writeSpringHandlerMetaFile(metaFolder);
        } catch (MetaFileWriteException e) {
            throw new MojoExecutionException(e);
        }
    }

    private void writeSpringSchemaMetaFile(File springMetafileDirectory) throws MojoExecutionException {
        String filename = "spring.schemas";
        writeSpringMetaFile(springMetafileDirectory, filename, (fileWriter, apiConfig) -> {
            String targetXmlnsNamespace = replaceDynamicVarsToLowerCase(apiConfig.getTargetXmlnsNamespace(), apiConfig.getPrefix(), apiConfig.getVersion());
            String schemaFolderPath = replaceDynamicVars(testApiGeneratorMojo.schemaFolder(apiConfig), apiConfig.getPrefix(), apiConfig.getVersion());
            String schemaPath = format("%s/%s-api.xsd", schemaFolderPath, apiConfig.getPrefix().toLowerCase());
            appendLine(fileWriter, format("%s.xsd=%s%n", targetXmlnsNamespace.replace("http://", "http\\://"), schemaPath), filename);
        });
    }

    private void writeSpringHandlerMetaFile(File springMetafileDirectory) throws MojoExecutionException {
        String filename = "spring.handlers";
        writeSpringMetaFile(springMetafileDirectory, filename, (fileWriter, apiConfig) -> {
            String targetXmlnsNamespace = replaceDynamicVarsToLowerCase(apiConfig.getTargetXmlnsNamespace(), apiConfig.getPrefix(), apiConfig.getVersion());
            String invokerPackage = replaceDynamicVarsToLowerCase(apiConfig.getInvokerPackage(), apiConfig.getPrefix(), apiConfig.getVersion());
            String namespaceHandlerClass = invokerPackage + ".spring." + apiConfig.getPrefix() + "NamespaceHandler";
            appendLine(fileWriter, format("%s=%s%n", targetXmlnsNamespace.replace("http://", "http\\://"), namespaceHandlerClass), filename);
        });
    }

    private void writeSpringMetaFile(File springMetafileDirectory, String filename, BiConsumer<FileWriter, ApiConfig> contentFormatter) throws MojoExecutionException {
        File handlerFile = new File(format("%s/%s", springMetafileDirectory.getPath(), filename));
        List<String> filteredLines = readAndFilterLines(handlerFile);

        try (FileWriter fileWriter = new FileWriter(handlerFile)) {
            for (String line : filteredLines) {
                fileWriter.write(format("%s%s", line, lineEnding));
            }

            for (ApiConfig apiConfig : testApiGeneratorMojo.getApiConfigs()) {
                contentFormatter.accept(fileWriter, apiConfig);
            }

        } catch (IOException e) {
            throw new MojoExecutionException("Unable to write spring meta file!", e);
        }
    }

    private void appendLine(FileWriter fileWriter, String format, String filename) {
        try {
            fileWriter.append(format);
        } catch (IOException e) {
            throw new MetaFileWriteException(format("Unable to write spring meta file '%s'!", filename), e);
        }
    }

    private static final class MetaFileWriteException extends RuntimeException {

        public MetaFileWriteException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
