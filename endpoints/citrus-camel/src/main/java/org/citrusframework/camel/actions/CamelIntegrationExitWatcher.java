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

package org.citrusframework.camel.actions;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.jbang.JBangSupport;
import org.citrusframework.jbang.ProcessAndOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Watches Camel integration processes started by a test and adds an exception to the test context
 * when an integration exits unexpectedly. Uses {@link ProcessHandle#onExit()} to detect
 * process termination in the background without blocking or interrupting the test thread.
 */
public class CamelIntegrationExitWatcher {

    private static final Logger logger = LoggerFactory.getLogger(CamelIntegrationExitWatcher.class);

    static final String CONTEXT_VARIABLE = "citrus:integration-exit-watcher";

    private final Map<String, CompletableFuture<?>> watched = new ConcurrentHashMap<>();

    /**
     * Registers an exit callback for the given integration process. When the process exits
     * unexpectedly (i.e. not unwatched first via {@link #unwatch}), the callback reads the
     * process output and adds an exception to the test context.
     */
    public void watch(String name, ProcessAndOutput pao, TestContext context) {
        Long pid = pao.getProcessId();
        Process process = pao.getProcess();
        CompletableFuture<?> future = process.toHandle().onExit().thenAccept(handle -> {
            if (!watched.containsKey(name)) {
                return;
            }
            watched.remove(name);

            int exitCode;
            try {
                exitCode = process.exitValue();
            } catch (IllegalThreadStateException e) {
                exitCode = -1;
            }

            if (exitCode == JBangSupport.OK_EXIT_CODE) {
                return;
            }

            String errorDetail = extractErrorDetail(pao);
            String message = "Camel integration '%s' (pid %d) exited with code %d while the test was running"
                    .formatted(name, pid, exitCode);
            if (!errorDetail.isEmpty()) {
                message += ": " + errorDetail;
            }

            logger.error(message);
            context.addException(new CitrusRuntimeException(message));
        });

        watched.put(name, future);
    }

    /**
     * Marks the integration as intentionally stopped. Any pending exit callback is cancelled
     * so it does not fire when the process terminates.
     */
    public void unwatch(String name) {
        CompletableFuture<?> future = watched.remove(name);
        if (future != null) {
            future.cancel(false);
        }
    }

    /**
     * Cancels all pending watchers.
     */
    public void unwatchAll() {
        watched.keySet().forEach(this::unwatch);
    }

    /**
     * Retrieves or creates the watcher for the current test context.
     */
    static CamelIntegrationExitWatcher getOrCreate(TestContext context) {
        if (context.getVariables().containsKey(CONTEXT_VARIABLE)) {
            return context.getVariable(CONTEXT_VARIABLE, CamelIntegrationExitWatcher.class);
        }

        CamelIntegrationExitWatcher watcher = new CamelIntegrationExitWatcher();
        context.setVariable(CONTEXT_VARIABLE, watcher);
        return watcher;
    }

    static String extractErrorDetail(ProcessAndOutput pao) {
        try {
            String output = pao.getOutput();
            if (output == null || output.isBlank()) {
                return "";
            }

            String[] lines = output.split("\\R");

            String lastCausedBy = null;
            for (String line : lines) {
                String trimmed = line.trim();
                if (trimmed.startsWith("Caused by:")) {
                    lastCausedBy = trimmed;
                }
            }
            if (lastCausedBy != null) {
                return lastCausedBy.substring("Caused by: ".length()).trim();
            }

            int tailStart = Math.max(0, lines.length - 20);
            StringBuilder tail = new StringBuilder();
            for (int i = tailStart; i < lines.length; i++) {
                if (!lines[i].isBlank()) {
                    if (!tail.isEmpty()) {
                        tail.append(" | ");
                    }
                    tail.append(lines[i].trim());
                }
            }
            return tail.toString();
        } catch (Exception e) {
            logger.debug("Failed to extract error detail from integration output", e);
            return "";
        }
    }
}
