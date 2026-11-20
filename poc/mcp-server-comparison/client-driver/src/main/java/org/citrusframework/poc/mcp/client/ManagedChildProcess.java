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

package org.citrusframework.poc.mcp.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

/**
 * A managed server child for HTTP scenarios: owns the {@link Process}, drains stderr,
 * and waits for a readiness marker on stdout within a bound. Anything else —
 * timeout, forced kill, unexpected exit — is shutdown failure, never success.
 */
public final class ManagedChildProcess implements AutoCloseable {

    private final Process process;
    private final List<String> stdoutLines = new java.util.concurrent.CopyOnWriteArrayList<>();
    private final List<String> stderrLines = new java.util.concurrent.CopyOnWriteArrayList<>();
    private final Thread stdoutDrain;
    private final Thread stderrDrain;

    private ManagedChildProcess(Process process) {
        this.process = process;
        this.stdoutDrain = drain(process.getInputStream(), stdoutLines);
        this.stderrDrain = drain(process.getErrorStream(), stderrLines);
        this.stdoutDrain.start();
        this.stderrDrain.start();
    }

    public static ManagedChildProcess launch(List<String> command) throws Exception {
        if (command == null || command.isEmpty()) {
            throw new IllegalArgumentException("child command must not be empty");
        }
        if (command.stream().anyMatch(arg -> arg.contains("&&") || arg.contains("|") || arg.contains(";"))) {
            throw new IllegalArgumentException("command must be an argument array, not a shell string");
        }
        Process process = new ProcessBuilder(command)
                .redirectInput(ProcessBuilder.Redirect.PIPE)
                .directory(new File(System.getProperty("user.dir")))
                .start();
        process.getOutputStream().close();
        return new ManagedChildProcess(process);
    }

    /** Waits for a stdout line matching the predicate; returns the line. */
    public String awaitStdoutLine(Predicate<String> match, long timeout, TimeUnit unit) throws Exception {
        long deadline = System.currentTimeMillis() + unit.toMillis(timeout);
        while (System.currentTimeMillis() < deadline) {
            for (String line : stdoutLines) {
                if (match.test(line)) {
                    return line;
                }
            }
            if (!process.isAlive()) {
                throw new IllegalStateException("child exited before readiness: exit="
                        + process.exitValue() + " stderr=" + stderrLines);
            }
            Thread.sleep(100);
        }
        throw new java.util.concurrent.TimeoutException(
                "no readiness line within bound; stdout=" + stdoutLines + " stderr=" + stderrLines);
    }

    /** Awaits natural exit within the bound; returns the exit code. */
    public int awaitExit(long timeout, TimeUnit unit) throws Exception {
        boolean exited = process.waitFor(timeout, unit);
        stdoutDrain.join(2000);
        stderrDrain.join(2000);
        if (!exited) {
            process.destroyForcibly();
            process.waitFor(10, TimeUnit.SECONDS);
            throw new java.util.concurrent.TimeoutException(
                    "child did not exit within bound (forced kill = failure)");
        }
        return process.exitValue();
    }

    public boolean isAlive() {
        return process.isAlive();
    }

    public List<String> stdoutLines() {
        return List.copyOf(stdoutLines);
    }

    public List<String> stderrLines() {
        return List.copyOf(stderrLines);
    }

    @Override
    public void close() {
        if (process.isAlive()) {
            process.destroyForcibly();
        }
    }

    private static Thread drain(java.io.InputStream in, List<String> sink) {
        Thread thread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sink.add(line);
                }
            } catch (Exception ignored) {
                // Partial capture; readiness/exit assertions report separately.
            }
        });
        thread.setDaemon(true);
        return thread;
    }
}
