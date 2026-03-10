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

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Stack;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.camel.tooling.maven.MavenArtifact;
import org.citrusframework.CitrusInstanceManager;
import org.citrusframework.agent.CitrusAgentConfiguration;
import org.citrusframework.agent.util.ConfigurationHelper;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.jbang.CitrusJBangMain;
import org.citrusframework.jbang.LoggingSupport;
import org.citrusframework.jbang.maven.MavenDependencyResolver;
import org.citrusframework.main.TestEngine;
import org.citrusframework.main.TestRunConfiguration;
import org.citrusframework.report.TestReporter;
import org.citrusframework.report.TestReporterSettings;
import org.citrusframework.report.TestResults;
import org.citrusframework.util.ClassLoaderHelper;
import org.citrusframework.util.FileUtils;
import org.citrusframework.util.StringUtils;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "run", description = "Run as local Citrus test")
public class Run extends CitrusCommand {

    @Option(names = { "--engine" }, description = "Name of the test engine that is used to run tests. One of junit, junit5, testng, cucumber")
    private String engine;

    @Option(names = { "--verbose" }, defaultValue = "true", description = "Should the test engine print verbose test summary information.")
    private String verbose;

    @Option(names = { "--reset" }, defaultValue = "true", description = "Should the test engine reset the suite state for this run.")
    private String reset;

    @Option(names = { "--includes" }, arity = "0..*", description = "Includes test name pattern.")
    private String[] includes;

    @Option(names = { "--work-directory" }, description = "The working directory used by the file based test engines to load file resources from.")
    private String workDir;

    @Option(names = { "--repository" }, arity = "0..*", description = "Set of Maven repositories that should be used to resolve dependencies.")
    private String[] repositories;

    @Option(names = { "--modules" }, description = "Comma delimited list of additional Citrus modules that must be loaded to run the test.")
    private String modules;

    @Option(names = { "--dep" }, arity = "0..*", description = "Set of additional Maven dependencies that must be loaded to run the test.")
    private String[] dependencies;

    @Option(names = { "--offline" }, defaultValue = "false", description = "When enabled there will be no attempts to resolve Maven artifacts via internet connection.")
    private boolean offline;

    @Option(names = { "--property" }, arity = "0..*", description = "Default System property to set before the test run.")
    private String[] properties;

    @Option(names = { "--logging" }, defaultValue = "true", description = "Can be used to turn off logging")
    private boolean logging = true;

    @Option(names = { "--logging-level" }, completionCandidates = LoggingSupport.LoggingLevels.class,
            defaultValue = "info", description = "Logging level")
    private String loggingLevel = "info";

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

    private int run() {
        File work = new File(CitrusJBangMain.Settings.getWorkDir());
        TestReporterSettings.setReportDirectory(CitrusJBangMain.Settings.getReportDirectory());
        removeDir(work);
        if (!work.mkdirs()) {
            printer().printErr("Failed to create working directory " + CitrusJBangMain.Settings.getWorkDir());
            return 1;
        }

        // if no specific file to run then try to auto-detect
        if (files == null || files.length == 0) {
            // auto-detect test files
            files = new File(".").list((dir, name) -> {
                if (new File(dir, name).isDirectory()) {
                    return true;
                }

                return Arrays.stream(CitrusJBangMain.Settings.getTestSourceFileExt()).anyMatch(name::endsWith);
            });
        }

        // filter out duplicate files
        if (files != null && files.length > 0) {
            files = Arrays.stream(files).distinct().toArray(String[]::new);
        }

        List<String> tests = new ArrayList<>();
        try {
            resolveTests(files, tests);
        } catch (Exception e) {
            if (Optional.ofNullable(verbose).map(Boolean::parseBoolean).orElse(false)) {
                e.printStackTrace(System.err);
            }
            printer().printErr("Failed to resolve tests", e);
            return 1;
        }

        if (tests.isEmpty()) {
            printer().printErr("No tests to run in current directory");
            return 1;
        }

        String basePath = FileUtils.getBasePath(tests.get(0));
        if (StringUtils.hasText(basePath) && !StringUtils.hasText(workDir)) {
            workDir = basePath;
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

        if (!offline) {
            handleAdditionalDependencies();
        }

        engine.run();
        return exitStatus.exitStatus();
    }

    private void handleAdditionalDependencies() {
        MavenDependencyResolver resolver = getMavenDependencyResolver();
        List<MavenArtifact> additionalArtifacts = new ArrayList<>();

        // Handle Citrus modules from envVar settings
        Arrays.stream(CitrusJBangMain.Settings.getModules())
                .map(String::trim)
                .filter(StringUtils::hasText)
                .forEach(module -> additionalArtifacts.addAll(resolver.resolveModule(module.trim())));

        // Handle Citrus modules from command line options
        if (StringUtils.hasText(modules)) {
            for (String module : modules.split(",")) {
                additionalArtifacts.addAll(resolver.resolveModule(module.trim()));
            }
        }

        Predicate<String> isSnapshotArtifact = it -> it.contains("-SNAPSHOT");
        // Handle Citrus dependencies from envVar settings
        Arrays.stream(CitrusJBangMain.Settings.getDependencies())
                .map(String::trim)
                .filter(StringUtils::hasText)
                .forEach(dependency ->
                    additionalArtifacts.addAll(resolver.resolve(dependency.trim(), isSnapshotArtifact.test(dependency), true)));

        // Handle Citrus dependencies from command line options
        if (dependencies != null) {
            for (String dependency : dependencies) {
                additionalArtifacts.addAll(
                        resolver.resolve(dependency.trim(), isSnapshotArtifact.test(dependency), true));
            }
        }

        if (!additionalArtifacts.isEmpty()) {
            additionalArtifacts.forEach(mavenArtifact -> {
                try {
                    ClassLoaderHelper.addArtifact(mavenArtifact.toString(), mavenArtifact.getFile().toURI().toURL());
                } catch (MalformedURLException e) {
                    printer().printErr(String.format("Error resolving artifact %s due to '%s'", mavenArtifact, e.getMessage()));
                }
            });

            try {
                // Adapt and set class loader in main thread
                Thread.currentThread().setContextClassLoader(ClassLoaderHelper.getContextClassLoader());
            } catch (Throwable e) {
                printer().printErr("Failed to set context class loader with additional dependencies due to '%s'".formatted(e.getMessage()));
            }
        }
    }

    private @NotNull MavenDependencyResolver getMavenDependencyResolver() {
        MavenDependencyResolver resolver = new MavenDependencyResolver();
        if (repositories != null) {
            for (String repository : repositories) {
                String name;
                String url;
                if (repository.contains(":")) {
                    String[] parts = repository.split(":");
                    name = parts[0];
                    url = parts[1];
                } else {
                    name = "custom";
                    url = repository;
                }
                resolver.withRepository(name, url);
            }
        }
        return resolver;
    }

    private void resolveTests(String[] files, List<String> tests) throws Exception {
        if (files == null) {
            return;
        }

        for (String file : files) {
            File f = new File(file);
            if (file.startsWith("clipboard") && !(f.exists())) {
                file = loadFromClipboard(file);
            } else if (f.isDirectory()) {
                resolveTests(Stream.of(Optional.ofNullable(f.list()).orElseGet(() -> new String[] {}))
                        .filter(it -> !skipFile(it))
                        .map(it -> f.getPath() + "/" + it)
                        .collect(Collectors.toSet()).toArray(String[]::new), tests);
                continue;
            } else if (skipFile(file)) {
                continue;
            }

            File inputFile = FileUtils.getFileResource(file).getFile();
            if (!inputFile.isFile()) {
                continue;
            }

            // check if file exist
            if (!inputFile.exists()) {
                printer().printErr("File does not exist: " + file);
                throw new CitrusRuntimeException("File does not exist: " + file);
            }

            tests.add(file);
        }
    }

    protected TestRunConfiguration getRunConfiguration(List<String> files) {
        CitrusAgentConfiguration configuration = fromCliOptions(ConfigurationHelper.fromEnvVars());

        String ext = FileUtils.getFileExtension(files.get(0));
        if (ext.equals("feature")) {
            configuration.setEngine("cucumber");
        }

        configuration.setTestSources(files.stream()
                .map(FileUtils::getTestSource)
                .collect(Collectors.toList()));

        return configuration;
    }

    private CitrusAgentConfiguration fromCliOptions(CitrusAgentConfiguration configuration) {
        if (StringUtils.hasText(engine)) {
            configuration.setEngine(engine);
        }

        if (StringUtils.hasText(verbose)) {
            configuration.setVerbose(Boolean.parseBoolean(verbose));
        }

        if (StringUtils.hasText(reset)) {
            configuration.setReset(Boolean.parseBoolean(reset));
        }

        if (includes != null) {
            configuration.setIncludes(includes);
        }

        if (workDir != null) {
            configuration.setWorkDir(workDir);
        }

        if (properties != null) {
            configuration.addDefaultProperties(Arrays.stream(properties)
                    .filter(p -> p.contains("="))
                    .map(p -> p.split("=", 2))
                    .collect(Collectors.toMap(p -> p[0], p -> p[1])));
        }

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
            String fn = CitrusJBangMain.Settings.getClipboardGeneratedFile() + "." + ext;
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

        if (Arrays.stream(CitrusJBangMain.Settings.getTestSourceFileExt()).noneMatch(name::endsWith)) {
            return true;
        }

        if ("pom.xml".equalsIgnoreCase(name)) {
            return true;
        }

        if ("build.gradle".equalsIgnoreCase(name)) {
            return true;
        }

        if ("jbang.properties".equalsIgnoreCase(name)) {
            return true;
        }

        // skip dirs
        File f = new File(name);
        if (f.exists() && f.isDirectory()) {
            return true;
        }

        return FileUtils.getBaseName(name).equalsIgnoreCase("readme");
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
        Matcher matcher = CitrusJBangMain.Settings.getPackagePattern().matcher(content);
        String pn = matcher.find() ? matcher.group(1) : null;

        matcher = CitrusJBangMain.Settings.getClassPattern().matcher(content);
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
