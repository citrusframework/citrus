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

package org.citrusframework.jbang.cli.commands;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.aesh.command.CommandDefinition;
import org.aesh.command.CommandResult;
import org.aesh.command.invocation.CommandInvocation;
import org.aesh.command.option.Argument;
import org.aesh.command.option.Option;
import org.aesh.command.option.ParentCommand;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.jbang.cli.CitrusJBangMain;
import org.citrusframework.util.ClassLoaderHelper;
import org.citrusframework.util.FileUtils;

import static java.nio.file.Files.writeString;

@CommandDefinition(name = "init", description = "Creates a new Citrus test", generateHelp = true)
public class Init extends CitrusCommand {

    @Argument(description = "Name of test file (or a github link)", paramLabel = "<file>", required = true)
    String file;

    @Option(name = "directory", description = "Directory where the files will be created", defaultValue = ".")
    String directory;

    @ParentCommand
    CitrusJBangMain parent;

    public Init() {
        super(null);
    }

    public Init(CitrusJBangMain main) {
        super(main);
    }

    @Override
    public CitrusJBangMain getMain() {
        if (super.getMain() == null) {
            setMain(parent);
        }
        return super.getMain();
    }

    @Override
    public CommandResult execute(CommandInvocation invocation) throws org.aesh.command.CommandException, InterruptedException {
        try {
            return result(call());
        } catch (Exception e) {
            printer().println("Error: Failed to initialize test: %s %s".formatted(e.getClass().getName(), e.getMessage()));
            return CommandResult.FAILURE;
        }
    }

    int call() throws Exception {
        String ext = FileUtils.getFileExtension(file);
        try (InputStream is = ClassLoaderHelper.getClassLoader().getResourceAsStream("templates/" + ext + ".tmpl")) {
            if (is == null) {
                printer().println("Error: Unsupported file type '%s' (supported types are: feature, java, yaml, xml, groovy)".formatted(ext));
                return 1;
            }

            String content = FileUtils.readToString(is, StandardCharsets.UTF_8);
            String name = FileUtils.getBaseName(FileUtils.getFileName(file));

            String targetDir = resolveTargetDirectory(directory);
            Path currentDir = Paths.get(".");
            Path workingDir = getWorkingDir(targetDir, currentDir);

            if (!workingDir.toFile().exists() && !workingDir.toFile().mkdirs()) {
                printer().println("Failed to create working directory in: " + workingDir);
                return 1;
            }

            File target = workingDir.resolve(file).toFile();
            content = content.replaceFirst("\\{\\{ \\.Name }}", name);

            writeString(target.toPath(), content);

            // allow subclasses to add custom files or perform some validation tasks with the working directory
            initAdditionalFiles(workingDir);
        } catch (Exception e) {
            printer().println("Error: Failed to initialize test: %s %s".formatted(e.getClass().getName(), e.getMessage()));
            return 1;
        }

        return 0;
    }

    private static Path getWorkingDir(String targetDir, Path currentDir) {
        Path targetDirPath = Paths.get(targetDir);
        Path workingDir;

        if (targetDir.equals(".") || targetDir.equals(currentDir.getFileName().toString())) {
            // current directory is already the target subfolder
            workingDir = currentDir;
        } else if (targetDirPath.isAbsolute()) {
            workingDir = targetDirPath;
        } else if (currentDir.resolve(targetDir).toFile().exists()) {
            // navigate to existing target subfolder
            workingDir = currentDir.resolve(targetDir);
        } else if (currentDir.resolve(targetDir).toFile().mkdirs()) {
            // create target subfolder and navigate to it
            workingDir = currentDir.resolve(targetDir);
        } else {
            throw new CitrusRuntimeException("Failed to create working directory in: " + currentDir);
        }

        return workingDir;
    }

    /**
     * Allows subclasses to adjust the default target directory.
     */
    protected String resolveTargetDirectory(String directory) {
        return directory;
    }

    /**
     * Subclasses may add additional files in working dir.
     */
    protected void initAdditionalFiles(Path workingDir) {
    }
}
