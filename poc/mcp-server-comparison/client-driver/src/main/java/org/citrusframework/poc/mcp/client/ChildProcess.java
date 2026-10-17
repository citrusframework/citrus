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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Bounded child-process launcher for stdio scenarios. Captures stdout/stderr
 * separately; protocol parsing uses stdout only. A forced kill after the bound
 * is recorded as shutdown failure, never success.
 */
public final class ChildProcess {

    public record Result(int exitCode, String stdout, String stderr, boolean timedOut, boolean forcedKill) {
    }

    private final List<String> args;
    private final long timeoutSeconds;

    public ChildProcess(List<String> args, long timeoutSeconds) {
        this.args = List.copyOf(Objects.requireNonNull(args, "args"));
        if (args.isEmpty()) {
            throw new IllegalArgumentException("child args must not be empty");
        }
        if (timeoutSeconds <= 0) {
            throw new IllegalArgumentException("timeoutSeconds must be positive");
        }
        this.timeoutSeconds = timeoutSeconds;
    }

    public Result run() throws Exception {
        ProcessBuilder builder = new ProcessBuilder(args);
        builder.redirectInput(ProcessBuilder.Redirect.PIPE);
        Process process = builder.start();
        // No stdin payload for the fixture probes; close to signal EOF where the child expects it.
        process.getOutputStream().close();

        StreamGobbler out = new StreamGobbler(process.getInputStream());
        StreamGobbler err = new StreamGobbler(process.getErrorStream());
        Thread outThread = new Thread(out, "poc-child-stdout");
        Thread errThread = new Thread(err, "poc-child-stderr");
        outThread.start();
        errThread.start();

        boolean exited = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
        if (!exited) {
            process.destroyForcibly();
            boolean gone = process.waitFor(10, TimeUnit.SECONDS);
            outThread.join(2000);
            errThread.join(2000);
            return new Result(gone ? process.exitValue() : -1, out.text(), err.text(), true, true);
        }
        outThread.join(2000);
        errThread.join(2000);
        return new Result(process.exitValue(), out.text(), err.text(), false, false);
    }

    private static final class StreamGobbler implements Runnable {
        private final InputStream in;
        private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        StreamGobbler(InputStream in) {
            this.in = in;
        }

        @Override
        public void run() {
            try {
                in.transferTo(buffer);
            } catch (Exception ignored) {
                // Capture partial output; the Result records timeout/kill separately.
            }
        }

        String text() {
            return buffer.toString(java.nio.charset.StandardCharsets.UTF_8);
        }
    }
}
