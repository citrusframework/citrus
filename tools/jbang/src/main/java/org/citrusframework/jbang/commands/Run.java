/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.citrusframework.jbang.commands;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.citrusframework.CitrusInstanceManager;
import org.citrusframework.jbang.CitrusJBangMain;
import org.citrusframework.jbang.LoggingSupport;
import org.citrusframework.main.TestEngine;
import org.citrusframework.main.TestRunConfiguration;
import org.citrusframework.report.TestReporter;
import org.citrusframework.report.TestResults;
import org.citrusframework.util.FileUtils;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "run", description = "Run as local Citrus test")
public class Run extends CitrusCommand {

    public static final String WORK_DIR = ".citrus-jbang";

    private static final String[] ACCEPTED_FILE_EXT
            = new String[] { ".feature", "test.groovy", "it.groovy", "test.yaml", "it.yaml",
            "Test.xml", "IT.xml", "test.xml", "it.xml", "Test.java", "IT.java", "TestCase.java", "ITCase.java" };

    private static final String CLIPBOARD_GENERATED_FILE = WORK_DIR + "/generated-clipboard";

    private static final Pattern PACKAGE_PATTERN = Pattern.compile(
            "^\\s*package\\s+([a-zA-Z][\\.\\w]*)\\s*;.*$", Pattern.MULTILINE);

    private static final Pattern CLASS_PATTERN = Pattern.compile(
            "^\\s*public class\\s+([a-zA-Z0-9]*)[\\s+|;].*$", Pattern.MULTILINE);

    @Option(names = { "--logging" }, defaultValue = "true", description = "Can be used to turn off logging")
    private boolean logging = true;

    @Option(names = { "--logging-level" }, completionCandidates = LoggingSupport.LoggingLevels.class,
            defaultValue = "info", description = "Logging level")
    private String loggingLevel;

    @Option(names = { "--logging-color" }, defaultValue = "true", description = "Use colored logging")
    private boolean loggingColor = true;

    @Parameters(description = "The test file(s) to run. If no files specified then application.properties is used as source for which files to run.",
                arity = "0..9", paramLabel = "<files>", parameterConsumer = FilesConsumer.class)
    Path[] filePaths; // Defined only for file path completion; the field never used

    String[] files;

    public Run(CitrusJBangMain main) {
        super(main);
    }

    @Override
    public Integer call() throws Exception {
        return run();
    }

    private int run() throws Exception {
        File work = new File(WORK_DIR);
        removeDir(work);
        if (!work.mkdirs()) {
            System.err.println("Failed to create working directory " + WORK_DIR);
            return 1;
        }

        // if no specific file to run then try to auto-detect
        if (files == null || files.length == 0) {
            // auto-detect test files
            files = new File(".").list((dir, name) -> Arrays.stream(ACCEPTED_FILE_EXT).anyMatch(name::endsWith));
        }

        // filter out duplicate files
        if (files != null && files.length > 0) {
            files = Arrays.stream(files).distinct().toArray(String[]::new);
        }

        List<String> tests = new ArrayList<>();
        if (files != null) {
            for (String file : files) {
                if (file.startsWith("clipboard") && !(new File(file).exists())) {
                    file = loadFromClipboard(file);
                } else if (skipFile(file)) {
                    continue;
                }

                // check if file exist
                File inputFile = FileUtils.getFileResource(file).getFile();
                if (!inputFile.exists() && !inputFile.isFile()) {
                    System.err.println("File does not exist: " + file);
                    return 1;
                }

                tests.add(file);
            }
        }

        if (tests.isEmpty()) {
            System.err.println("No tests to run in current directory");
            return 1;
        }

        final TestRunConfiguration configuration = getRunConfiguration(tests);
        final TestEngine engine = TestEngine.lookup(configuration);
        final ExitStatusTestReporter exitStatus = new ExitStatusTestReporter();
        CitrusInstanceManager.addInstanceProcessor(instance -> instance.addTestReporter(exitStatus));

        if (logging) {
            LoggingSupport.configureLog(loggingLevel, loggingColor, configuration.getEngine());
        } else {
            LoggingSupport.configureLog("off", false, configuration.getEngine());
        }

        engine.run();
        return exitStatus.exitStatus();
    }

    protected TestRunConfiguration getRunConfiguration(List<String> files) {
        TestRunConfiguration configuration = new TestRunConfiguration();

        String ext = FileUtils.getFileExtension(files.get(0));
        if (ext.equals("feature")) {
            configuration.setEngine("cucumber");
        } else {
            configuration.setEngine("testng");
        }

        configuration.setTestSources(files.stream()
                .map(FileUtils::getTestSource)
                .collect(Collectors.toList()));

        return configuration;
    }

    private String loadFromClipboard(String file) throws UnsupportedFlavorException, IOException {
        // run from clipboard (not real file exists)
        String ext = FileUtils.getFileExtension(file);
        if (ext.isEmpty()) {
            throw new IllegalArgumentException(
                    "When running from clipboard, an extension is required to let Citrus know what kind of file to use");
        }
        Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
        Object t = c.getData(DataFlavor.stringFlavor);
        if (t != null) {
            String fn = CLIPBOARD_GENERATED_FILE + "." + ext;
            if ("java".equals(ext)) {
                String fqn = determineClassName(t.toString());
                if (fqn == null) {
                    throw new IllegalArgumentException(
                            "Cannot determine the Java class name from the source in the clipboard");
                }
                fn = fqn + ".java";
            }
            Files.writeString(Paths.get(fn), t.toString());
            file = "file:" + fn;
        }
        return file;
    }

    private boolean skipFile(String name) {
        if (name.startsWith(".")) {
            return true;
        }
        if ("pom.xml".equalsIgnoreCase(name)) {
            return true;
        }
        if ("build.gradle".equalsIgnoreCase(name)) {
            return true;
        }

        // skip dirs
        File f = new File(name);
        if (f.exists() && f.isDirectory()) {
            return true;
        }

        String on = FileUtils.getBaseName(name);
        on = on.toLowerCase(Locale.ROOT);
        if (on.endsWith("readme")) {
            return true;
        }

        return false;
    }

    private static void removeDir(File d) {
        String[] list = d.list();
        if (list == null) {
            list = new String[0];
        }
        for (String s : list) {
            File f = new File(d, s);
            if (f.isDirectory()) {
                removeDir(f);
            } else {
                delete(f);
            }
        }
        delete(d);
    }

    private static void delete(File f) {
        if (!f.delete()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException ex) {
                // Ignore Exception
            }
            if (!f.delete()) {
                f.deleteOnExit();
            }
        }
    }

    private static String determineClassName(String content) {
        Matcher matcher = PACKAGE_PATTERN.matcher(content);
        String pn = matcher.find() ? matcher.group(1) : null;

        matcher = CLASS_PATTERN.matcher(content);
        String cn = matcher.find() ? matcher.group(1) : null;

        String fqn;
        if (pn != null) {
            fqn = pn + "." + cn;
        } else {
            fqn = cn;
        }
        return fqn;
    }

    static class FilesConsumer extends ParameterConsumer<Run> {
        @Override
        protected void doConsumeParameters(Stack<String> args, Run cmd) {
            List<String> files = new ArrayList<>();
            while (!args.isEmpty()) {
                String arg = args.pop();
                files.add(arg);
            }
            cmd.files = files.toArray(String[]::new);
        }
    }

    /**
     * Special test reporter provides proper exit status based on successful/failed tests.
     */
    private static class ExitStatusTestReporter implements TestReporter {
        private static final int DEFAULT = 0;
        private static final int ERRORS = 1;

        private int exitStatus = DEFAULT;

        @Override
        public void generateReport(TestResults testResults) {
            if (testResults.getFailed() > 0) {
                exitStatus = ERRORS;
            }
        }

        int exitStatus() {
            return exitStatus;
        }
    }
}
