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
import java.util.List;

import io.quarkiverse.mcp.server.Tool;
import io.quarkiverse.mcp.server.ToolArg;
import io.quarkiverse.mcp.server.ToolCallException;
import jakarta.enterprise.context.ApplicationScoped;
import org.citrusframework.xml.migration.SpringXmlTestMigration;

/**
 * MCP Tools for migrating Citrus Spring Bean XML test cases to the plain XML DSL.
 */
@ApplicationScoped
public class MigrationTools {

    @Tool(description = "Migrate Citrus Spring Bean XML test cases to the plain XML DSL in batch. " +
            "Accepts a source path (single file or directory) and a target directory. " +
            "Returns migration results including migrated files, skipped files, and any warnings or errors.")
    public MigrationResult citrus_migrate_spring_bean_xml_dsl_batch(
            @ToolArg(description = "Path to the Spring Bean XML test file or directory to migrate") String source,
            @ToolArg(description = "Path to the target directory for migrated XML files") String target) {

        if (source == null || source.isBlank()) {
            throw new ToolCallException("Source path is required", null);
        }

        if (target == null || target.isBlank()) {
            throw new ToolCallException("Target directory is required", null);
        }

        try {
            Path sourcePath = Path.of(source);
            Path targetPath = Path.of(target);

            SpringXmlTestMigration migration = new SpringXmlTestMigration();
            SpringXmlTestMigration.MigrationResult result = migration.migrate(sourcePath, targetPath);

            List<MigratedFileInfo> migrated = result.migrated().stream()
                    .map(f -> new MigratedFileInfo(
                            f.source().toString(),
                            f.target().toString(),
                            f.warnings()))
                    .toList();

            List<String> skipped = result.skipped().stream()
                    .map(Path::toString)
                    .toList();

            List<FailedFileInfo> failed = result.failed().stream()
                    .map(f -> new FailedFileInfo(f.source().toString(), f.error()))
                    .toList();

            return new MigrationResult(migrated, skipped, failed);
        } catch (ToolCallException e) {
            throw e;
        } catch (Throwable e) {
            throw new ToolCallException("Migration failed: " + e.getMessage(), e);
        }
    }

    @Tool(description = "Migrate a single Citrus Spring Bean XML test case provided as a string to the plain XML DSL. " +
            "Returns the migrated XML content and any migration warnings.")
    public MigratedContentResult citrus_migrate_spring_bean_xml_dsl(
            @ToolArg(description = "The Spring Bean XML test case content to migrate") String content) {

        if (content == null || content.isBlank()) {
            throw new ToolCallException("XML content is required", null);
        }

        try {
            SpringXmlTestMigration migration = new SpringXmlTestMigration();
            String migrated = migration.migrateTest(content);
            List<String> warnings = SpringXmlTestMigration.getWarnings(migrated);

            return new MigratedContentResult(migrated, warnings);
        } catch (ToolCallException e) {
            throw e;
        } catch (Throwable e) {
            throw new ToolCallException("Migration failed: " + e.getMessage(), e);
        }
    }

    public record MigrationResult(
            List<MigratedFileInfo> migrated,
            List<String> skipped,
            List<FailedFileInfo> failed) {
    }

    public record MigratedFileInfo(String source, String target, List<String> warnings) {
    }

    public record FailedFileInfo(String source, String error) {
    }

    public record MigratedContentResult(String content, List<String> warnings) {
    }
}
