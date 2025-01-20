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

package org.citrusframework.jbang;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Support class prepares JBang executable and runs commands via spawned process using the JBang binary.
 */
public class JBangSupport {

    /** Logger */
    private static final Logger LOG = LoggerFactory.getLogger(JBangSupport.class);

    private static final boolean IS_OS_WINDOWS = System.getProperty("os.name").toLowerCase(Locale.ENGLISH).contains("windows");

    public static final int OK_EXIT_CODE = 0;

    private static Path installDir;

    private static final AtomicBoolean initialized = new AtomicBoolean(false);

    private static final Set<String> trustUrls = new HashSet<>();

    private final Map<String, String> systemProperties = new HashMap<>();

    private final Map<String, String> envVars = new HashMap<>();

    private String app;

    private Path workingDir;

    /**
     * Prevent direct instantiation.
     */
    private JBangSupport() {
    }

    public static JBangSupport jbang() {
        if (!initialized.getAndSet(true)) {
            detectJBang();
            Arrays.stream(JBangSettings.getTrustUrls())
                    .forEach(JBangSupport::addTrust);
        }

        return new JBangSupport();
    }

    /**
     * Get the JBang version.
     */
    public String version() {
        ProcessAndOutput p = execute(jBang("version"), null, null);
        return p.getOutput();
    }

    /**
     * Adds JBang trust for given URL.
     */
    public JBangSupport trust(String url) {
        addTrust(url);
        return this;
    }

    /**
     * Adds system property to command line.
     */
    public JBangSupport withSystemProperty(String name, String value) {
        this.systemProperties.put(name, value);
        return this;
    }

    /**
     * Adds system properties to command line.
     */
    public JBangSupport withSystemProperties(Map<String, String> systemProperties) {
        this.systemProperties.putAll(systemProperties);
        return this;
    }

    /**
     * Adds environment variables to command line.
     */
    public JBangSupport withEnv(String name, String value) {
        this.systemProperties.put(name, value);
        return this;
    }

    /**
     * Adds environment variables to command line.
     */
    public JBangSupport withEnvs(Map<String, String> envVars) {
        this.envVars.putAll(envVars);
        return this;
    }

    /**
     * Sets the JBang application name to call.
     * @param name
     */
    public JBangSupport app(String name) {
        this.app = name;
        return this;
    }

    /**
     * Sets the working directory of the JBang process.
     * @param workingDir
     */
    public JBangSupport workingDir(Path workingDir) {
        this.workingDir = workingDir;
        return this;
    }

    /**
     * Runs JBang command.
     */
    public ProcessAndOutput run(String command, String... args) {
        return run(command, Arrays.asList(args));
    }

    /**
     * Runs JBang command and waits for the command to complete.
     * Command can be a script file or an app command.
     */
    public ProcessAndOutput run(String command, List<String> args) {
        return execute(jBang(systemProperties, constructAllArgs(command, args)), workingDir, envVars);
    }

    /**
     * Runs JBang command - does not wait for the command to complete.
     * Command can be a script file or an app command.
     */
    public ProcessAndOutput runAsync(String command, String... args) {
        return runAsync(command, Arrays.asList(args));
    }

    /**
     * Runs JBang command - does not wait for the command to complete.
     * Command can be a script file or an app command.
     */
    public ProcessAndOutput runAsync(String command, List<String> args) {
        return executeAsync(jBang(systemProperties, constructAllArgs(command, args)), workingDir, envVars);
    }

    /**
     * Runs JBang command - does not wait for the command to complete.
     * Command can be a script file or an app command.
     * Redirect the process output to given file.
     */
    public ProcessAndOutput runAsync(String command, File output, String... args) {
        return runAsync(command, output, Arrays.asList(args));
    }

    /**
     * Runs JBang command - does not wait for the command to complete.
     * Command can be a script file or an app command.
     * Redirect the process output to given file.
     */
    public ProcessAndOutput runAsync(String command, File output, List<String> args) {
        return executeAsync(jBang(systemProperties, constructAllArgs(command, args)), workingDir, output, envVars);
    }

    private List<String> constructAllArgs(String command, List<String> args) {
        List<String> allArgs = new ArrayList<>();

        // JBang app name
        if (app != null) {
            allArgs.add(app);
        }

        allArgs.add(command);
        allArgs.addAll(args);

        return allArgs;
    }

    private static void detectJBang() {
        ProcessAndOutput result = getVersion();
        if (result.getProcess().exitValue() == OK_EXIT_CODE) {
            LOG.info("Found JBang v" + result.getOutput());
        } else if (JBangSettings.isAutoDownload()){
            LOG.warn("JBang not found. Downloading ...");
            download();
            result = getVersion();
            if (result.getProcess().exitValue() == OK_EXIT_CODE) {
                LOG.info("Using JBang v" + result.getOutput());
            }
        } else {
            throw new CitrusRuntimeException("Missing JBang installation on host - make sure to install JBang");
        }
    }

    private static void download() {
        String homePath = "jbang";

        Path installPath = Paths.get(System.getProperty("user.home")).toAbsolutePath().resolve(".jbang").toAbsolutePath();

        if (installPath.resolve(homePath).toFile().exists()) {
            LOG.info("Using local JBang in " + installPath);
            installDir = installPath.resolve(homePath);
            return;
        }

        LOG.info("Downloading JBang from " + JBangSettings.getJBangDownloadUrl() + " and installing in " + installPath);

        try {
            Files.createDirectories(installPath);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(JBangSettings.getJBangDownloadUrl()))
                    .GET()
                    .build();

            HttpClient client = HttpClient.newBuilder()
                    .followRedirects(HttpClient.Redirect.ALWAYS)
                    .build();
            HttpResponse<Path> response = client.send(request, HttpResponse.BodyHandlers.ofFileDownload(installPath,
                    StandardOpenOption.WRITE,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING));

            if (response.statusCode() != 200) {
                throw new CitrusRuntimeException(String.format("Failed to download JBang - response code %d", response.statusCode()));
            }

            unzip(response.body(), installPath);
        } catch (URISyntaxException | IOException | InterruptedException e) {
            throw new CitrusRuntimeException("Failed to download JBang", e);
        }

        installDir = installPath.resolve(homePath);
    }

    private static ProcessAndOutput getVersion() {
        return execute(jBang("version"), null, null);
    }

    /**
     * Execute "jbang trust add URL..."
     *
     * @throws CitrusRuntimeException if the exit value is different from
     *                                0: success
     *                                1: Already trusted source(s)
     */
    private static void addTrust(String url) {
        if (trustUrls.add(url)) {
            ProcessAndOutput result = execute(jBang("trust", "add", url), null, null);
            int exitValue = result.getProcess().exitValue();
            if (exitValue != OK_EXIT_CODE && exitValue != 1) {
                throw new CitrusRuntimeException("Error while trusting JBang URLs. Exit code: " + exitValue);
            }
        }
    }

    /**
     * @return JBang command with given arguments.
     */
    private static List<String> jBang(String... args) {
        return jBang(List.of(args));
    }

    /**
     * @return JBang command with given arguments.
     */
    private static List<String> jBang(List<String> args) {
        return jBang(Collections.emptyMap(), args);
    }

    /**
     * @return JBang command with given arguments.
     */
    private static List<String> jBang(Map<String, String> systemProperties, List<String> args) {
        List<String> command = new ArrayList<>();
        if (IS_OS_WINDOWS) {
            command.add("cmd.exe");
            command.add("/c");
        } else {
            command.add("sh");
            command.add("-c");
        }

        String jBangCommand = getJBangExecutable() + " " + getSystemPropertyArgs(systemProperties) + String.join(" ", args);
        command.add(jBangCommand);

        return command;
    }

    /**
     * Construct command line arguments from given map of system properties.
     * @param systemProperties
     * @return
     */
    private static String getSystemPropertyArgs(Map<String, String> systemProperties) {
        if (systemProperties.isEmpty()) {
            return "";
        }

        return systemProperties.entrySet()
                .stream()
                .map(entry -> "-D%s=\"%s\"".formatted(entry.getKey(), entry.getValue()))
                .collect(Collectors.joining(" ")) + " ";
    }

    /**
     * Execute JBang command using the process API. Waits for the process to complete and returns the process instance so
     * caller is able to access the exit code and process output.
     * @param command
     * @param workingDir
     * @param envVars
     * @return
     */
    private static ProcessAndOutput execute(List<String> command, Path workingDir, Map<String, String> envVars) {
        try {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Executing JBang command: %s".formatted(String.join(" ", command)));
            }

            ProcessBuilder pBuilder = new ProcessBuilder(command)
                    .redirectErrorStream(true);

            if (envVars != null) {
                pBuilder.environment().putAll(envVars);
            }

            if (workingDir != null) {
                pBuilder.directory(workingDir.toFile());
            }

            Process p = pBuilder.start();

            String output = FileUtils.readToString(p.getInputStream(), StandardCharsets.UTF_8);
            p.waitFor();

            if (JBangSettings.isDumpProcessOutput()) {
                Path workDir = JBangSettings.getWorkDir();
                FileUtils.writeToFile(output, workDir.resolve(String.format("%s-output.txt", p.pid())).toFile());
            }

            if (LOG.isDebugEnabled() && p.exitValue() != OK_EXIT_CODE) {
                LOG.debug("Command failed: " + String.join(" ", command));
                LOG.debug(output);
            }

            return new ProcessAndOutput(p, output);
        } catch (IOException | InterruptedException e) {
            throw new CitrusRuntimeException("Error while executing JBang", e);
        }
    }

    /**
     * Execute JBang command using the process API. Waits for the process to complete and returns the process instance so
     * caller is able to access the exit code and process output.
     * @param command
     * @param workingDir
     * @param envVars
     * @return
     */
    private static ProcessAndOutput executeAsync(List<String> command, Path workingDir, Map<String, String> envVars) {
        try {
            ProcessBuilder pBuilder = new ProcessBuilder(command)
                    .redirectErrorStream(true);

            if (envVars != null) {
                pBuilder.environment().putAll(envVars);
            }

            if (workingDir != null) {
                pBuilder.directory(workingDir.toFile());
            }

            Process p = pBuilder.start();
            return new ProcessAndOutput(p);
        } catch (IOException e) {
            throw new CitrusRuntimeException("Error while executing JBang", e);
        }
    }

    /**
     * Execute JBang command using the process API. Waits for the process to complete and returns the process instance so
     * caller is able to access the exit code and process output.
     * @param command
     * @param outputFile
     * @param envVars
     * @return
     */
    private static ProcessAndOutput executeAsync(List<String> command, Path workingDir, File outputFile, Map<String, String> envVars) {
        try {
            if (!outputFile.getParentFile().exists() && !outputFile.getParentFile().mkdirs()) {
                throw new CitrusRuntimeException("Unable to create process output directory: " + outputFile.getParent());
            }

            ProcessBuilder pBuilder = new ProcessBuilder(command)
                .redirectErrorStream(true)
                .redirectOutput(outputFile);

            if (envVars != null) {
                pBuilder.environment().putAll(envVars);
            }

            if (workingDir != null) {
                pBuilder.directory(workingDir.toFile());
            }

            Process p = pBuilder.start();
            return new ProcessAndOutput(p, outputFile);
        } catch (IOException e) {
            throw new CitrusRuntimeException("Error while executing JBang", e);
        }
    }

    /**
     * Gets the JBang executable name.
     * @return
     */
    private static String getJBangExecutable() {
        if (installDir != null) {
            if (IS_OS_WINDOWS) {
                return installDir.resolve("bin/jbang.cmd").toString();
            } else {
                return installDir.resolve("bin/jbang").toString();
            }
        } else {
            if (IS_OS_WINDOWS) {
                return "jbang.cmd";
            } else {
                return "jbang";
            }
        }
    }

    /**
     * Extract JBang download.zip to install directory.
     * @param downloadZip
     * @param installPath
     * @throws IOException
     */
    private static void unzip(Path downloadZip, Path installPath) throws IOException {
        ZipInputStream zis = new ZipInputStream(new FileInputStream(downloadZip.toFile()));
        ZipEntry zipEntry = zis.getNextEntry();
        while (zipEntry != null) {
            Path filePath = newFile(installPath, zipEntry);
            File newFile = filePath.toFile();
            if (zipEntry.isDirectory()) {
                if (!newFile.isDirectory() && !newFile.mkdirs()) {
                    throw new IOException("Failed to create directory " + newFile);
                }
            } else {
                // fix for Windows-created archives
                File parent = newFile.getParentFile();
                if (!parent.isDirectory() && !parent.mkdirs()) {
                    throw new IOException("Failed to create directory " + parent);
                }

                // write file content
                Files.copy(zis, filePath, StandardCopyOption.REPLACE_EXISTING);

                if ("jbang".equals(filePath.getFileName().toString())) {
                    Files.setPosixFilePermissions(filePath, PosixFilePermissions.fromString("rwxr--r--"));
                }
            }
            zipEntry = zis.getNextEntry();
        }

        zis.closeEntry();
        zis.close();
    }

    /**
     * Guards against writing files to the file system outside the target folder also known as Zip slip vulnerability.
     * @param destinationDir
     * @param zipEntry
     * @return
     * @throws IOException
     */
    private static Path newFile(Path destinationDir, ZipEntry zipEntry) throws IOException {
        Path destFile = destinationDir.resolve(zipEntry.getName());

        String destDirPath = destinationDir.toFile().getCanonicalPath();
        String destFilePath = destFile.toFile().getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }
}
