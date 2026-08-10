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

package org.citrusframework.config.metadata;

import org.testng.Assert;
import org.testng.annotations.Test;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class CitrusConfigMetadataProcessorTest {

    @Test
    public void shouldGenerateMetadataForAnnotatedClass() throws IOException {
        String source = """
                package test;

                import org.citrusframework.config.CitrusConfigProperties;
                import org.citrusframework.config.CitrusConfigProperty;

                @CitrusConfigProperties(prefix = "citrus.test", description = "Test settings")
                public class TestSettings {

                    @CitrusConfigProperty(
                        description = "Enable test feature",
                        type = "java.lang.Boolean",
                        defaultValue = "true"
                    )
                    public static final String FEATURE_ENABLED_PROPERTY = "citrus.test.feature.enabled";

                    @CitrusConfigProperty(
                        description = "Test name",
                        defaultValue = "default"
                    )
                    public static final String NAME_PROPERTY = "citrus.test.name";

                    @CitrusConfigProperty(
                        description = "Test timeout in milliseconds",
                        type = "java.lang.Long",
                        defaultValue = "5000"
                    )
                    public static final String TIMEOUT_PROPERTY = "citrus.test.timeout";

                    // Field without annotation — should be ignored
                    public static final String TIMEOUT_ENV = "CITRUS_TEST_TIMEOUT";
                }
                """;

        String metadata = compileAndReadMetadata(source);
        Assert.assertNotNull(metadata, "Metadata file should be generated");

        Assert.assertTrue(metadata.contains("\"citrus.test\""), "Should contain group name");
        Assert.assertTrue(metadata.contains("test.TestSettings"), "Should contain source type");
        Assert.assertTrue(metadata.contains("Test settings"), "Should contain group description");

        Assert.assertTrue(metadata.contains("\"citrus.test.feature.enabled\""), "Should contain boolean property");
        Assert.assertTrue(metadata.contains("\"java.lang.Boolean\""), "Should contain Boolean type");
        Assert.assertTrue(metadata.contains("true"), "Should contain boolean default value");

        Assert.assertTrue(metadata.contains("\"citrus.test.name\""), "Should contain string property");
        Assert.assertTrue(metadata.contains("\"default\""), "Should contain string default value");

        Assert.assertTrue(metadata.contains("\"citrus.test.timeout\""), "Should contain long property");
        Assert.assertTrue(metadata.contains("5000"), "Should contain numeric default value");

        Assert.assertFalse(metadata.contains("CITRUS_TEST_TIMEOUT"), "Should not contain env var constant");
    }

    @Test
    public void shouldGenerateDeprecationMetadata() throws IOException {
        String source = """
                package test;

                import org.citrusframework.config.CitrusConfigProperties;
                import org.citrusframework.config.CitrusConfigProperty;

                @CitrusConfigProperties(prefix = "citrus.old", description = "Old settings")
                public class OldSettings {

                    @CitrusConfigProperty(
                        description = "Old property",
                        deprecated = true,
                        replacement = "citrus.new.property"
                    )
                    public static final String OLD_PROPERTY = "citrus.old.property";
                }
                """;

        String metadata = compileAndReadMetadata("test.OldSettings", source);
        Assert.assertNotNull(metadata);

        Assert.assertTrue(metadata.contains("\"deprecation\""), "Should contain deprecation");
        Assert.assertTrue(metadata.contains("citrus.new.property"), "Should contain replacement");
    }

    @Test
    public void shouldHandleClassWithNoAnnotatedFields() throws IOException {
        String source = """
                package test;

                import org.citrusframework.config.CitrusConfigProperties;

                @CitrusConfigProperties(prefix = "citrus.empty", description = "Empty group")
                public class EmptySettings {
                    public static final String SOME_CONSTANT = "value";
                }
                """;

        String metadata = compileAndReadMetadata("test.EmptySettings", source);
        // No properties annotated, so no metadata file should be generated
        Assert.assertNull(metadata, "Should not generate metadata when no properties are annotated");
    }

    private String compileAndReadMetadata(String source) throws IOException {
        return compileAndReadMetadata("test.TestSettings", source);
    }

    private String compileAndReadMetadata(String className, String source) throws IOException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new IllegalStateException("No system Java compiler available; run tests with a JDK, not a JRE");
        }

        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);

        Path outputDir = Files.createTempDirectory("citrus-config-metadata-test");
        fileManager.setLocation(StandardLocation.CLASS_OUTPUT, List.of(outputDir.toFile()));

        String classpath = System.getProperty("java.class.path");
        List<String> options = List.of("-classpath", classpath);

        JavaFileObject sourceFile = new InMemoryJavaSource(className, source);

        JavaCompiler.CompilationTask task = compiler.getTask(
                null, fileManager, diagnostics, options, null, List.of(sourceFile));
        task.setProcessors(List.of(new CitrusConfigMetadataProcessor()));

        boolean success = task.call();
        if (!success) {
            StringBuilder errors = new StringBuilder("Compilation failed:\n");
            diagnostics.getDiagnostics().forEach(d -> errors.append(d).append('\n'));
            Assert.fail(errors.toString());
        }

        File metadataFile = new File(outputDir.toFile(),
                "META-INF/spring-configuration-metadata.json");

        if (!metadataFile.exists()) {
            return null;
        }

        return Files.readString(metadataFile.toPath());
    }

    private static class InMemoryJavaSource extends SimpleJavaFileObject {
        private final String code;

        InMemoryJavaSource(String className, String code) {
            super(URI.create("string:///" + className.replace('.', '/') + Kind.SOURCE.extension),
                    Kind.SOURCE);
            this.code = code;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return code;
        }
    }
}
