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
import java.nio.file.Files;
import java.nio.file.Path;

import io.quarkiverse.mcp.server.ToolCallException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MigrationToolsTest {

    private final MigrationTools migrationTools = new MigrationTools();

    private static final String SPRING_XML_TEST = """
            <?xml version="1.0" encoding="UTF-8"?>
            <spring:beans xmlns="http://www.citrusframework.org/schema/testcase"
                          xmlns:spring="http://www.springframework.org/schema/beans"
                          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                          xsi:schemaLocation="http://www.springframework.org/schema/beans
                              http://www.springframework.org/schema/beans/spring-beans.xsd
                              http://www.citrusframework.org/schema/testcase
                              http://www.citrusframework.org/schema/testcase/citrus-testcase.xsd">

                <testcase name="SampleIT">
                    <meta-info>
                        <author>Test</author>
                        <creationdate>2024-01-01</creationdate>
                        <status>FINAL</status>
                    </meta-info>
                    <actions>
                        <echo><message>Hello Citrus!</message></echo>
                    </actions>
                </testcase>
            </spring:beans>
            """;

    @Test
    void shouldMigrateContent() {
        MigrationTools.MigratedContentResult result = migrationTools.citrus_migrate_spring_bean_xml_dsl(SPRING_XML_TEST);

        assertThat(result).isNotNull();
        assertThat(result.content()).contains("<test");
        assertThat(result.content()).contains("name=\"SampleIT\"");
        assertThat(result.content()).contains("<echo>");
        assertThat(result.content()).doesNotContain("<spring:beans");
        assertThat(result.content()).doesNotContain("<testcase");
        assertThat(result.warnings()).isEmpty();
    }

    @Test
    void shouldMigrateContentWithWarnings() {
        String xmlWithUnsupported = """
                <?xml version="1.0" encoding="UTF-8"?>
                <spring:beans xmlns="http://www.citrusframework.org/schema/testcase"
                              xmlns:spring="http://www.springframework.org/schema/beans"
                              xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                              xsi:schemaLocation="http://www.springframework.org/schema/beans
                                  http://www.springframework.org/schema/beans/spring-beans.xsd
                                  http://www.citrusframework.org/schema/testcase
                                  http://www.citrusframework.org/schema/testcase/citrus-testcase.xsd">

                    <testcase name="UnsupportedIT">
                        <meta-info>
                            <author>Test</author>
                            <creationdate>2024-01-01</creationdate>
                            <status>FINAL</status>
                        </meta-info>
                        <actions>
                            <java class="com.example.MyClass">
                                <method name="execute"/>
                            </java>
                        </actions>
                    </testcase>
                </spring:beans>
                """;

        MigrationTools.MigratedContentResult result = migrationTools.citrus_migrate_spring_bean_xml_dsl(xmlWithUnsupported);

        assertThat(result).isNotNull();
        assertThat(result.warnings()).isNotEmpty();
        assertThat(result.warnings().getFirst()).contains("java");
    }

    @Test
    void shouldMigrateFile(@TempDir Path tempDir) throws IOException {
        Path sourceDir = tempDir.resolve("source");
        Files.createDirectories(sourceDir);
        Files.writeString(sourceDir.resolve("SampleIT.xml"), SPRING_XML_TEST, StandardCharsets.UTF_8);

        Path targetDir = tempDir.resolve("target");

        MigrationTools.MigrationResult result = migrationTools.citrus_migrate_spring_bean_xml_dsl_batch(
                sourceDir.toString(), targetDir.toString());

        assertThat(result).isNotNull();
        assertThat(result.migrated()).hasSize(1);
        assertThat(result.migrated().getFirst().source()).contains("SampleIT.xml");
        assertThat(result.migrated().getFirst().target()).contains("SampleIT.xml");
        assertThat(result.migrated().getFirst().warnings()).isEmpty();
        assertThat(result.skipped()).isEmpty();
        assertThat(result.failed()).isEmpty();

        assertThat(Files.readString(Path.of(result.migrated().getFirst().target()), StandardCharsets.UTF_8))
                .contains("<test")
                .contains("name=\"SampleIT\"");
    }

    @Test
    void shouldSkipNonTestFiles(@TempDir Path tempDir) throws IOException {
        Path sourceDir = tempDir.resolve("source");
        Files.createDirectories(sourceDir);

        String springConfig = """
                <?xml version="1.0" encoding="UTF-8"?>
                <beans xmlns="http://www.springframework.org/schema/beans"
                       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                       xsi:schemaLocation="http://www.springframework.org/schema/beans
                           http://www.springframework.org/schema/beans/spring-beans.xsd">
                    <bean id="myBean" class="com.example.MyBean"/>
                </beans>
                """;
        Files.writeString(sourceDir.resolve("context.xml"), springConfig, StandardCharsets.UTF_8);

        Path targetDir = tempDir.resolve("target");

        MigrationTools.MigrationResult result = migrationTools.citrus_migrate_spring_bean_xml_dsl_batch(
                sourceDir.toString(), targetDir.toString());

        assertThat(result.migrated()).isEmpty();
        assertThat(result.skipped()).hasSize(1);
        assertThat(result.failed()).isEmpty();
    }

    @Test
    void shouldRejectBlankSource() {
        assertThatThrownBy(() -> migrationTools.citrus_migrate_spring_bean_xml_dsl_batch("", "/tmp/target"))
                .isInstanceOf(ToolCallException.class)
                .hasMessageContaining("Source path is required");
    }

    @Test
    void shouldRejectBlankTarget() {
        assertThatThrownBy(() -> migrationTools.citrus_migrate_spring_bean_xml_dsl_batch("/tmp/source", ""))
                .isInstanceOf(ToolCallException.class)
                .hasMessageContaining("Target directory is required");
    }

    @Test
    void shouldRejectBlankContent() {
        assertThatThrownBy(() -> migrationTools.citrus_migrate_spring_bean_xml_dsl(""))
                .isInstanceOf(ToolCallException.class)
                .hasMessageContaining("XML content is required");
    }

    @Test
    void shouldReportErrorForNonExistentSource() {
        assertThatThrownBy(() -> migrationTools.citrus_migrate_spring_bean_xml_dsl_batch(
                "/nonexistent/path", "/tmp/target"))
                .isInstanceOf(ToolCallException.class)
                .hasMessageContaining("Migration failed");
    }
}
