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

import java.util.stream.Stream;

import org.citrusframework.camel.UnitTestSupport;
import org.citrusframework.camel.cli.CamelCli;
import org.citrusframework.exceptions.ActionTimeoutException;
import org.citrusframework.jbang.ProcessAndOutput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CamelVerifyIntegrationActionTest extends UnitTestSupport {

    @Mock
    private ProcessAndOutput pao;

    @Mock
    private Process process;

    @BeforeEach
    public void setUp() {
        lenient().when(pao.getProcess()).thenReturn(process);
    }

    static Stream<String> commandOutputProvider() {
        return Stream.of(
            """
             PID   NAME      READY  STATUS   AGE  TOTAL  FAIL  INFLIGHT
            12345  my-route   1/1   Running  10s      0     0         0
            """,
            """
            some unexpected log output
             PID   NAME      READY  STATUS   AGE  TOTAL  FAIL  INFLIGHT
            12345  my-route   1/1   Running  10s      0     0         0
            """,
            """
             PID   NAME         READY  STATUS   AGE  TOTAL  FAIL  INFLIGHT
            11111  other-route   1/1   Running  10s      0     0         0
            12345  my-route      1/1   Running  10s      0     0         0
            """,
            """
             PID   NAME         READY  STATUS   AGE  TOTAL  FAIL  INFLIGHT
            11111  other-route   1/1   Running  10s      0     0         0
            99999  my-route      1/1   Running  10s      0     0         0
            """
        );
    }

    @ParameterizedTest
    @MethodSource("commandOutputProvider")
    public void shouldParseOutputAndVerifyRunningStatus(String output) {
        lenient().when(process.isAlive()).thenReturn(true);
        lenient().when(process.exitValue()).thenReturn(0);

        lenient().when(pao.getOutput()).thenReturn(output);

        context.getReferenceResolver().bind("camelCli", new CamelCli() {
            @Override
            public String ps(String... args) {
                if (args.length == 1 && args[0].equals("--json")) {
                    // Disable Json output on purpose
                    return "";
                }
                return super.ps(args);
            }

            @Override
            public String ps() {
                // Mock the ps() output
                return output;
            }
        });

        context.setVariable("my-route:pid", 12345);

        CamelVerifyIntegrationAction action = new CamelVerifyIntegrationAction.Builder()
                .integration("my-route")
                .maxAttempts(5)
                .delayBetweenAttempts(100)
                .withReferenceResolver(context.getReferenceResolver())
                .build();

        action.execute(context);
    }

    static Stream<String> commandJsonOutputProvider() {
        return Stream.of(
            """
            [{"pid":12345,"name":"my-route","ready":"1/1","status":"Running","age":"25s","total":0,"fail":0,"inflight":0}]
            """,
            """
            some unexpected log output
            [{"pid":12345,"name":"my-route","ready":"1/1","status":"Running","age":"25s","total":0,"fail":0,"inflight":0}]
            """,
            """
            [{"pid":11111,"name":"other-route","ready":"1/1","status":"Running","age":"25s","total":0,"fail":0,"inflight":0},{"pid":12345,"name":"my-route","ready":"1/1","status":"Running","age":"25s","total":0,"fail":0,"inflight":0}]
            """,
            """
            [{"pid":11111,"name":"other-route","ready":"1/1","status":"Running","age":"25s","total":0,"fail":0,"inflight":0},{"pid":99999,"name":"my-route","ready":"1/1","status":"Running","age":"25s","total":0,"fail":0,"inflight":0}]
            """
        );
    }

    @ParameterizedTest
    @MethodSource("commandJsonOutputProvider")
    public void shouldVerifyRunningStatus(String output) {
        lenient().when(process.isAlive()).thenReturn(true);
        lenient().when(process.exitValue()).thenReturn(0);

        lenient().when(pao.getOutput()).thenReturn(output);

        context.getReferenceResolver().bind("camelCli", new CamelCli() {
            @Override
            public String ps(String... args) {
                if (args.length == 1 && args[0].equals("--json")) {
                    return output;
                }
                return super.ps(args);
            }

            @Override
            public String ps() {
                return "";
            }
        });

        context.setVariable("my-route:pid", 12345);

        CamelVerifyIntegrationAction action = new CamelVerifyIntegrationAction.Builder()
                .integration("my-route")
                .maxAttempts(5)
                .delayBetweenAttempts(100)
                .withReferenceResolver(context.getReferenceResolver())
                .build();

        action.execute(context);
    }

    static Stream<String> failedOutputProvider() {
        return Stream.of(
            "",
            "Something completely different",
            """
             PID   NAME   READY  STATUS   AGE  TOTAL  FAIL  INFLIGHT
            """,
            """
             PID   NAME         READY  STATUS   AGE  TOTAL  FAIL  INFLIGHT
            11111  other-route   1/1   Running  10s      0     0         0
            """,
            """
             PID   NAME      READY  STATUS   AGE  TOTAL  FAIL  INFLIGHT
            12345  my-route   0/1   Pending  10s      0     0         0
            """
        );
    }

    @ParameterizedTest
    @MethodSource("failedOutputProvider")
    public void shouldParseOutputAndFailVerification(String output) {
        lenient().when(process.isAlive()).thenReturn(true);
        lenient().when(process.exitValue()).thenReturn(0);

        lenient().when(pao.getOutput()).thenReturn(output);

        context.getReferenceResolver().bind("camelCli", new CamelCli() {
            @Override
            public String ps(String... args) {
                if (args.length == 1 && args[0].equals("--json")) {
                    // Disable Json output on purpose
                    return "";
                }
                return super.ps(args);
            }

            @Override
            public String ps() {
                // Mock the ps() output
                return output;
            }
        });

        context.setVariable("my-route:pid", 12345);

        CamelVerifyIntegrationAction action = new CamelVerifyIntegrationAction.Builder()
                .integration("my-route")
                .maxAttempts(5)
                .delayBetweenAttempts(100)
                .withReferenceResolver(context.getReferenceResolver())
                .build();

        assertThrows(ActionTimeoutException.class, () -> action.execute(context));
    }

    static Stream<String> failedJsonOutputProvider() {
        return Stream.of(
            "",
            "Something completely different",
            """
            []
            """,
            """
            [{"pid":11111,"name":"other-route","ready":"1/1","status":"Running","age":"25s","total":0,"fail":0,"inflight":0}]
            """,
            """
            [{"pid":12345,"name":"my-route","ready":"0/1","status":"Pending","age":"25s","total":0,"fail":0,"inflight":0}]
            """
        );
    }

    @ParameterizedTest
    @MethodSource("failedOutputProvider")
    public void shouldFailVerification(String output) {
        lenient().when(process.isAlive()).thenReturn(true);
        lenient().when(process.exitValue()).thenReturn(0);

        lenient().when(pao.getOutput()).thenReturn(output);

        context.getReferenceResolver().bind("camelCli", new CamelCli() {
            @Override
            public String ps(String... args) {
                if (args.length == 1 && args[0].equals("--json")) {
                    return output;
                }
                return super.ps(args);
            }

            @Override
            public String ps() {
                return "";
            }
        });

        context.setVariable("my-route:pid", 12345);

        CamelVerifyIntegrationAction action = new CamelVerifyIntegrationAction.Builder()
                .integration("my-route")
                .maxAttempts(5)
                .delayBetweenAttempts(100)
                .withReferenceResolver(context.getReferenceResolver())
                .build();

        assertThrows(ActionTimeoutException.class, () -> action.execute(context));
    }

}
