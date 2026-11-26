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

import java.util.concurrent.TimeUnit;

import org.citrusframework.context.TestContext;
import org.citrusframework.context.TestContextFactory;
import org.citrusframework.jbang.ProcessAndOutput;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CamelIntegrationExitWatcherTest {

    @Test
    void shouldDetectUnexpectedExit() throws Exception {
        TestContext context = TestContextFactory.newInstance().getObject();
        CamelIntegrationExitWatcher watcher = new CamelIntegrationExitWatcher();

        Process process = new ProcessBuilder("sh", "-c", "exit 1").start();
        ProcessAndOutput pao = new ProcessAndOutput(process);

        watcher.watch("failing-route", pao, context);

        process.waitFor(10, TimeUnit.SECONDS);
        Thread.sleep(500);

        assertTrue(context.hasExceptions());
        String message = context.getExceptions().get(0).getMessage();
        assertTrue(message.contains("failing-route"), "Should name the integration: " + message);
        assertTrue(message.contains("exited with code"), "Should mention exit code: " + message);
    }

    @Test
    void shouldNotFireWhenUnwatchedBeforeExit() throws Exception {
        TestContext context = TestContextFactory.newInstance().getObject();
        CamelIntegrationExitWatcher watcher = new CamelIntegrationExitWatcher();

        Process process = new ProcessBuilder("sh", "-c", "sleep 2 && exit 1").start();
        ProcessAndOutput pao = new ProcessAndOutput(process);

        watcher.watch("stopped-route", pao, context);
        watcher.unwatch("stopped-route");

        process.waitFor(5, TimeUnit.SECONDS);
        Thread.sleep(500);

        assertFalse(context.hasExceptions(), "No exception should be added for unwatched integration");
    }

    @Test
    void shouldNotFireWhenProcessExitsWithZero() throws Exception {
        TestContext context = TestContextFactory.newInstance().getObject();
        CamelIntegrationExitWatcher watcher = new CamelIntegrationExitWatcher();

        Process process = new ProcessBuilder("sh", "-c", "exit 0").start();
        ProcessAndOutput pao = new ProcessAndOutput(process);

        watcher.watch("clean-exit", pao, context);

        process.waitFor(5, TimeUnit.SECONDS);
        Thread.sleep(500);

        assertFalse(context.hasExceptions(), "No exception for clean exit (code 0)");
    }

    @Test
    void shouldExtractCausedByFromOutput() {
        String output = """
                Some log line
                java.lang.RuntimeException: boom
                    at com.example.Main.main(Main.java:10)
                Caused by: org.apache.camel.NoSuchEndpointException: No endpoint could be found for: nosuchcomponent://foo
                    at org.apache.camel.DefaultEndpoint.resolve(DefaultEndpoint.java:100)
                """;

        ProcessAndOutput pao = createPaoWithOutput(output);
        String detail = CamelIntegrationExitWatcher.extractErrorDetail(pao);

        assertTrue(detail.contains("NoSuchEndpointException"), "Should extract last Caused by: " + detail);
        assertTrue(detail.contains("nosuchcomponent://foo"), "Should include endpoint: " + detail);
    }

    @Test
    void shouldFallBackToTailWhenNoCausedBy() {
        String output = "ERROR: Something went wrong\nFailed to start\n";

        ProcessAndOutput pao = createPaoWithOutput(output);
        String detail = CamelIntegrationExitWatcher.extractErrorDetail(pao);

        assertTrue(detail.contains("Something went wrong"), "Should include tail output: " + detail);
    }

    @Test
    void shouldReturnEmptyForBlankOutput() {
        ProcessAndOutput pao = createPaoWithOutput("");
        assertEquals("", CamelIntegrationExitWatcher.extractErrorDetail(pao));
    }

    @Test
    void shouldGetOrCreateWatcher() {
        TestContext context = TestContextFactory.newInstance().getObject();

        CamelIntegrationExitWatcher watcher1 = CamelIntegrationExitWatcher.getOrCreate(context);
        CamelIntegrationExitWatcher watcher2 = CamelIntegrationExitWatcher.getOrCreate(context);

        assertTrue(watcher1 == watcher2, "Should return same instance for same context");
    }

    private static ProcessAndOutput createPaoWithOutput(String output) {
        try {
            Process process = new ProcessBuilder("sh", "-c", "echo done").start();
            process.waitFor(5, TimeUnit.SECONDS);
            ProcessAndOutput pao = new ProcessAndOutput(process) {
                @Override
                public String getOutput() {
                    return output;
                }
            };
            return pao;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
