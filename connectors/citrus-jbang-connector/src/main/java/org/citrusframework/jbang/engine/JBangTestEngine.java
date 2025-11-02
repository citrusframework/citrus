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

package org.citrusframework.jbang.engine;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.citrusframework.TestSource;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.jbang.CitrusJBang;
import org.citrusframework.jbang.ProcessOutputListener;
import org.citrusframework.main.AbstractTestEngine;
import org.citrusframework.main.TestRunConfiguration;
import org.citrusframework.spi.Resource;
import org.citrusframework.spi.Resources;
import org.citrusframework.util.FileUtils;
import org.citrusframework.util.StringUtils;

/**
 * Test engine implementation runs tests via Citrus JBang as a separate JVM process.
 */
public class JBangTestEngine extends AbstractTestEngine {

    private Path workingDir;
    private ProcessOutputListener outputListener;

    public JBangTestEngine(TestRunConfiguration configuration) {
        super(configuration);
    }

    @Override
    public void run() {
        CitrusJBang citrus = new CitrusJBang()
                .workingDir(Optional.ofNullable(getConfiguration().getWorkDir())
                        .filter(StringUtils::isNotEmpty)
                        .map(FileUtils::getFileResource)
                        .map(Resource::getFile)
                        .map(File::toPath)
                        .orElse(workingDir));

        if (outputListener != null) {
            citrus.withOutputListener(outputListener);
        }

        if (getConfiguration().getTestSources().isEmpty()) {
            runTestPackages(citrus, getConfiguration());
        } else {
            runTestSources(citrus, getConfiguration());
        }
    }

    private void runTestPackages(CitrusJBang citrus, TestRunConfiguration configuration) {
        List<String> packagesToRun = Optional.ofNullable(configuration.getPackages())
                .orElseGet(Collections::emptyList)
                .stream()
                .map(packageName -> packageName.replace(".", "/"))
                .collect(Collectors.toList());

        if (packagesToRun.isEmpty()) {
            packagesToRun = Collections.singletonList("");
            logger.info("Running all tests in project");
        }

        for (String packageName : packagesToRun) {
            if (StringUtils.hasText(packageName)) {
                logger.info(String.format("Running tests in directory %s", packageName));
            } else {
                logger.info(String.format("Running tests in current working directory %s",
                        Optional.ofNullable(workingDir).map(Path::toString).orElse(".")));
            }

            citrus.run(packageName, Collections.emptyMap());
        }
    }

    private void runTestSources(CitrusJBang citrus, TestRunConfiguration configuration) {
        List<TestSource> directories = configuration.getTestSources().stream()
                .filter(source -> "directory".equals(source.getType()))
                .toList();

        for (TestSource directory : directories) {
            logger.info(String.format("Running tests in directory %s", directory.getName()));
            citrus.run(directory.getFilePath(), Collections.emptyMap());
        }

        List<TestSource> sources = configuration.getTestSources().stream()
                .filter(source -> !"directory".equals(source.getType()))
                .toList();

        for (TestSource source : sources) {
            try {
                logger.info(String.format("Running test source %s", source.getName()));

                if (source.getSourceFile() instanceof Resources.ByteArrayResource) {
                    Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
                    c.setContents(new StringSelection(FileUtils.readToString(source.getSourceFile())), null);

                    String fileExt = Optional.of(FileUtils.getFileExtension(source.getName()))
                            .filter(ext -> !ext.isEmpty())
                            .orElse(".yaml");
                    citrus.run("clipboard" + fileExt, Collections.emptyMap());
                } else if (StringUtils.hasText(source.getFilePath())) {
                    citrus.run(source.getFilePath(), Collections.emptyMap());
                } else {
                    citrus.run(source.getSourceFile().getLocation(), Collections.emptyMap());
                }
            } catch (IOException e) {
                throw new CitrusRuntimeException("Failed to run test source: %s".formatted(source.getName()), e);
            }
        }
    }

    public JBangTestEngine withWorkingDir(Path workingDir) {
        this.workingDir = workingDir;
        return this;
    }

    public JBangTestEngine withOutputListener(ProcessOutputListener outputListener) {
        this.outputListener = outputListener;
        return this;
    }
}
