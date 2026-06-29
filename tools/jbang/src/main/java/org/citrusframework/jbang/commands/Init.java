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

package org.citrusframework.jbang.commands;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Stack;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.jbang.CitrusJBangMain;
import org.citrusframework.util.ClassLoaderHelper;
import org.citrusframework.util.FileUtils;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import static java.nio.file.Files.writeString;

@Command(name = "init", description = "Creates a new Citrus test")
public class Init extends CitrusCommand {

    @Parameters(description = "Name of test file (or a github link)", arity = "1",
                paramLabel = "<file>", parameterConsumer = FileConsumer.class)
    private Path filePath; // Defined only for file path completion; the field never used

    private String file;

    @Option(names = {"--directory" }, description = "Directory where the files will be created", defaultValue = ".")
    private String directory;

    public Init(CitrusJBangMain main) {
        super(main);
    }

    @Override
    public Integer call() throws Exception {
        String ext = FileUtils.getFileExtension(file);
        String name = FileUtils.getBaseName(file);
        String content;
        try (InputStream is = ClassLoaderHelper.getClassLoader().getResourceAsStream("templates/" + ext + ".tmpl")) {
            if (is == null) {
                printer().println("Error: Unsupported file type '%s' (supported types are: feature, java, yaml, xml, groovy)".formatted(ext));
                return 1;
            }
            content = FileUtils.readToString(is, StandardCharsets.UTF_8);

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

    static class FileConsumer extends ParameterConsumer<Init> {
        @Override
        protected void doConsumeParameters(Stack<String> args, Init cmd) {
            cmd.file = args.pop();
        }
    }
}
