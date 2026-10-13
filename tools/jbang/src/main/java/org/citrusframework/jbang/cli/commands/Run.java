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

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.camel.tooling.maven.MavenArtifact;
import org.citrusframework.CitrusInstanceManager;
import org.citrusframework.CitrusSettings;
import org.citrusframework.api.agent.CitrusAgentConfiguration;
import org.citrusframework.common.TestSourceHelper;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.jbang.cli.CitrusJBangMain;
import org.citrusframework.jbang.cli.LoggingSupport;
import org.citrusframework.jbang.cli.maven.MavenDependencyResolver;
import org.citrusframework.jbang.cli.util.CodeAnalyzer;
import org.citrusframework.jbang.cli.util.DelegatingCodeAnalyzer;
import org.citrusframework.log.CitrusLogSettings;
import org.citrusframework.api.main.TestEngine;
import org.citrusframework.api.main.TestRunConfiguration;
import org.citrusframework.message.MessageType;
import org.citrusframework.report.TestReporter;
import org.citrusframework.report.TestReporterSettings;
import org.citrusframework.report.TestResults;
import org.citrusframework.util.ClassLoaderHelper;
import org.citrusframework.util.FileUtils;
import org.citrusframework.util.StringUtils;
import org.aesh.command.CommandDefinition;
import org.aesh.command.CommandResult;
import org.aesh.command.invocation.CommandInvocation;
import org.aesh.command.option.Arguments;
import org.aesh.command.option.Option;
import org.aesh.command.option.OptionList;
import org.aesh.command.option.ParentCommand;

@CommandDefinition(name = "run", description = "Run as local Citrus test", generateHelp = true)
public class Run extends CitrusCommand {

    @Option(name = "engine", description = "Name of the test engine that is used to run tests. One of junit, junit-jupiter, junit4, testng, cucumber")
    String engine;

    @Option(name = "verbose", defaultValue = "true", description = "Should the test engine print verbose test summary information.")
    String verbose;

    @Option(name = "reset", defaultValue = "true", description = "Should the test engine reset the suite state for this run.")
    String reset;

    @OptionList(name = "includes", valueSeparator = ',', description = "Includes test name pattern.")
    List<String> includes;

    @Option(name = "work-directory", description = "The working directory used by the file based test engines to load file resources from.")
    String workDir;

    @OptionList(name = "repository", aliases = {"repositories"}, valueSeparator = ',', description = "Set of Maven repositories that should be used to resolve dependencies.")
    List<String> repositories;

    @Option(name = "modules", description = "Comma delimited list of additional Citrus modules that must be loaded to run the test.")
    String modules;

    @OptionList(name = "dep", aliases = {"dependency"}, valueSeparator = ',', description = "Comma delimited list of additional Maven GAV dependencies that must be loaded to run the test.")
    List<String> dependencies;

    @Option(name = "offline", defaultValue = "false", optionalValue = true, fallbackValue = "true", description = "When enabled there will be no attempts to resolve Maven artifacts via internet connection.")
    boolean offline;

    @Option(name = "inspect-code", defaultValue = "true", optionalValue = true, fallbackValue = "true", description = "When enabled the source code gets analyzed for required modules and dependencies that are added to the classpath.")
    boolean inspectCode = true;

    @OptionList(name = "property", aliases = {"properties"}, valueSeparator = ',', description = "Default System property to set before the test run.")
    List<String> properties;

    @Option(name = "logging", defaultValue = "true", optionalValue = true, fallbackValue = "true", description = "Can be used to turn off logging")
    boolean logging = true;

    @Option(name = "logging-level", defaultValue = "info", description = "Logging level")
    String loggingLevel = "info";

    @Option(name = "logging-color", defaultValue = "true", optionalValue = true, fallbackValue = "true", description = "Use colored logging")
    boolean loggingColor = true;

    @Arguments(description = "The test file(s) to run. If no files specified then application.properties is used as source for which files to run.",
                paramLabel = "<files>")
    List<String> files;

    @ParentCommand
    CitrusJBangMain parent;

    public Run() {
        super(null);
    }

    public Run(CitrusJBangMain main) {
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
        return result(run());
    }

    int run() {
        File work = new File(CitrusJBangMain.Settings.getWorkDir());
        TestReporterSettings.setReportDirectory(CitrusJBangMain.Settings.getReportDirectory());
        removeDir(work);
        if (!work.mkdirs()) {
            printer().printErr("Failed to create working directory " + CitrusJBangMain.Settings.getWorkDir());
            return 1;
        }

        // if no specific file to run then try to auto-detect
        if (files == null || files.isEmpty()) {
            // auto-detect test files
            String[] detected = new File(".").list((dir, name) -> {
                if (new File(dir, name).isDirectory()) {
                    return true;
                }

                return Arrays.stream(CitrusJBangMain.Settings.getTestSourceFileExt()).anyMatch(name::endsWith);
            });
            files = detected != null ? new ArrayList<>(Arrays.asList(detected)) : new ArrayList<>();
        }

        // filter out duplicate files
        if (files != null && !files.isEmpty()) {
            files = files.stream().distinct().collect(Collectors.toList());
        }

        List<String> tests = new ArrayList<>();
        try {
            resolveTests(files != null ? files.toArray(String[]::new) : new String[] {}, tests);
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

        final List<TestRunConfiguration> configurations = getRunConfigurations(tests);
        if (configurations.isEmpty()) {
            printer().printErr("Failed to construct run configurations");
            return 1;
        }

        final ExitStatusTestReporter exitStatus = new ExitStatusTestReporter();
        CitrusInstanceManager.addInstanceProcessor(instance -> instance.addTestReporter(exitStatus));

        String testEngine = configurations.get(0).getEngine();
        if (logging) {
            LoggingSupport.configureLog(loggingLevel, loggingColor, testEngine);
        } else {
            LoggingSupport.configureLog("off", false, testEngine);
        }

        if (!loggingColor) {
            System.setProperty(CitrusLogSettings.LOG_COLOR_PROPERTY, "never");
        }

        if (!offline) {
            resolveArtifacts(tests);
        }

        int exitCode = 0;
        for (TestRunConfiguration configuration : configurations) {
            // Set properties as System properties
            configuration.setDefaultProperties();

            final TestEngine engine = TestEngine.lookup(configuration);
            if (!testEngine.equals(configuration.getEngine())) {
                testEngine = configuration.getEngine();
                if (logging) {
                    LoggingSupport.configureLog(loggingLevel, loggingColor, testEngine);
                } else {
                    LoggingSupport.configureLog("off", false, testEngine);
                }
            }

            engine.run();

            if (exitStatus.exitStatus() != 0) {
                // make sure to save failed state in exit status
                exitCode = exitStatus.exitStatus();
            }
        }

        return exitCode;
    }

    private void resolveArtifacts(List<String> tests) {
        MavenDependencyResolver resolver = getMavenDependencyResolver();
        Set<String> allModules = new HashSet<>();
        Set<String> allDependencies = new HashSet<>();

        List<MavenArtifact> additionalArtifacts = new ArrayList<>();

        // Handle DSL test loaders according to file extensions
        tests.stream().map(FileUtils::getFileExtension).distinct().forEach(ext -> {
            if (StringUtils.hasText(ext)) {
                if ("java".equals(ext)) {
                    // Java DSL is on the classpath by default - no dynamic module needed
                } else if ("feature".equals(ext)) {
                    allModules.add("citrus-cucumber");
                    allModules.add("citrus-cucumber-core");
                } else {
                    allModules.add("citrus-" + ext);
                }
            }
        });

        // Handle Citrus modules from envVar settings
        Arrays.stream(CitrusJBangMain.Settings.getModules())
                .map(String::trim)
                .filter(StringUtils::hasText)
                .forEach(allModules::add);

        // Handle Citrus modules from command line options
        if (StringUtils.hasText(modules)) {
            Arrays.stream(modules.split(","))
                    .map(String::trim)
                    .filter(StringUtils::hasText)
                    .forEach(allModules::add);
        }

        // Handle Citrus dependencies from envVar settings
        Arrays.stream(CitrusJBangMain.Settings.getDependencies())
                .map(String::trim)
                .filter(StringUtils::hasText)
                .forEach(allDependencies::add);

        // Handle Citrus dependencies from command line options
        if (dependencies != null) {
            dependencies.stream()
                    .map(String::trim)
                    .filter(StringUtils::hasText)
                    .forEach(allDependencies::add);
        }

        if (inspectCode) {
            // Analyze code for additional modules and dependencies
            CodeAnalyzer analyzer = new DelegatingCodeAnalyzer();
            for (String test : tests) {
                try {
                    CodeAnalyzer.ScanResult scanResult = analyzer.scan(FileUtils.getFileResource(test));
                    allModules.addAll(Arrays.asList(scanResult.modules()));
                    allDependencies.addAll(Arrays.asList(scanResult.dependencies()));
                } catch (Exception e) {
                    printer().printErr(String.format("Failed to analyze test file %s due to '%s'", test, e.getMessage()));
                }
            }
        }

        String systemClasspath = System.getProperty("java.class.path");
        allModules.stream()
                .map(module -> {
                    if (module.startsWith("citrus-")) {
                        return module;
                    } else {
                        return "citrus-" + module;
                    }
                })
                .filter(module -> !systemClasspath.contains(module))
                .forEach(module -> additionalArtifacts.addAll(resolver.resolveModule(module)));

        allDependencies.forEach(dependency -> additionalArtifacts.addAll(
                resolver.resolve(dependency.trim(), dependency.contains("-SNAPSHOT"), true)));

        if (!additionalArtifacts.isEmpty()) {
            additionalArtifacts.forEach(mavenArtifact -> {
                try {
                    ClassLoaderHelper.addArtifact(mavenArtifact.toString(), mavenArtifact.getFile().toURI().toURL());
                } catch (MalformedURLException e) {
                    printer().printErr(String.format("Error resolving artifact %s due to '%s'", mavenArtifact, e.getMessage()));
                }
            });

            // Adapt and set class loader in main thread
            ClassLoaderHelper.updateContextClassloader(true);
        }
    }

    private MavenDependencyResolver getMavenDependencyResolver() {
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

            File inputFile = FileUtils.getFileResource(file).file();
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

    /**
     * Construct run configurations for given list of tests.
     * Makes sure to choose the right test engine for respective test types (e.g. Cucumber BDD tests).
     * If applicable uses multiple test runt configurations for different test types.
     */
    protected List<TestRunConfiguration> getRunConfigurations(List<String> files) {
        Set<String> extensions = files.stream().map(FileUtils::getFileExtension).collect(Collectors.toSet());
        if (extensions.stream().noneMatch(this::isCucumberFeature) || extensions.stream().allMatch(this::isCucumberFeature)) {
            // Homogeneous test sources - All files are either feature files or arbitrary test DSL files
            return Collections.singletonList(getRunConfiguration(files));
        } else {
            List<TestRunConfiguration> runConfigurations = new ArrayList<>();
            // Add arbitrary test DSL files first
            runConfigurations.add(getRunConfiguration(files.stream()
                    .filter(file -> !isCucumberFeature(FileUtils.getFileExtension(file)))
                    .collect(Collectors.toList())));

            // Add Cucumber BDD feature files in a separate run configuration
            runConfigurations.add(getRunConfiguration(files.stream()
                    .filter(file -> isCucumberFeature(FileUtils.getFileExtension(file)))
                    .collect(Collectors.toList())));
            return runConfigurations;
        }
    }

    /**
     * Gets the run configuration for given list of tests.
     * The list of test sources should be homogeneous in terms of its type (e.g. Cucumber BDD feature files only, arbitrary test DSL files only).
     */
    protected TestRunConfiguration getRunConfiguration(List<String> files) {
        CitrusAgentConfiguration configuration = fromCliOptions(CitrusAgentConfiguration.fromEnvVars(TestSourceHelper::create));

        String ext = FileUtils.getFileExtension(files.get(0));
        if (ext.equals("feature")) {
            configuration.setEngine("cucumber");
        }

        configuration.setTestSources(files.stream()
                .map(TestSourceHelper::create)
                .collect(Collectors.toList()));

        Path workingDir;
        if (StringUtils.hasText(configuration.getWorkDir())) {
            workingDir = new File(configuration.getWorkDir()).toPath();
        } else {
            workingDir = new File(".").toPath();
        }

        // Read default Citrus application properties file if present
        if (workingDir.resolve(CitrusSettings.getApplicationPropertiesFile()).toFile().exists()) {
            if (logging) {
                printer().println("Reading Citrus application properties file: " + workingDir.resolve(CitrusSettings.getApplicationPropertiesFile()));
            }

            Path citrusApplicationProperties = workingDir.resolve(CitrusSettings.getApplicationPropertiesFile());
            try (InputStream is = new ByteArrayInputStream(Files.readAllBytes(citrusApplicationProperties))) {
                Properties properties = new Properties();
                properties.load(is);

                configuration.addDefaultProperties(properties.entrySet()
                        .stream()
                        .filter(entry -> entry.getValue() != null)
                        .collect(Collectors.toMap(entry -> entry.getKey().toString(), entry -> entry.getValue().toString())));
            } catch (Exception e) {
                printer().printErr("Failed to read Citrus application properties file '%s'".formatted(citrusApplicationProperties));
            }
        }

        configuration.addDefaultProperty(CitrusSettings.DEFAULT_MESSAGE_TYPE_PROPERTY, MessageType.JSON.name(), false);

        return configuration;
    }

    private boolean isCucumberFeature(String extension) {
        return "feature".equals(extension);
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
            configuration.setIncludes(includes.toArray(new String[0]));
        }

        if (workDir != null) {
            configuration.setWorkDir(workDir);
        }

        if (properties != null) {
            configuration.addDefaultProperties(properties.stream()
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
