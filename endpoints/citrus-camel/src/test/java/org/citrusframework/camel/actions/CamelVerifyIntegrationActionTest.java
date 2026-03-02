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

import org.citrusframework.camel.UnitTestSupport;
import org.citrusframework.camel.jbang.CamelJBang;
import org.citrusframework.exceptions.ActionTimeoutException;
import org.citrusframework.jbang.ProcessAndOutput;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.mockito.Mockito.when;

public class CamelVerifyIntegrationActionTest extends UnitTestSupport {

    @Mock
    private ProcessAndOutput pao;

    @Mock
    private Process process;

    @BeforeTest
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(pao.getProcess()).thenReturn(process);
    }

    @DataProvider(name = "commandOutputProvider")
    public Object[][] commandOutputProvider() {
        return new Object[][] {
            {
            """
             PID   NAME      READY  STATUS   AGE  TOTAL  FAIL  INFLIGHT
            12345  my-route   1/1   Running  10s      0     0         0
            """
            },
            {
            """
            some unexpected log output
             PID   NAME      READY  STATUS   AGE  TOTAL  FAIL  INFLIGHT
            12345  my-route   1/1   Running  10s      0     0         0
            """
            },
            {
            """
             PID   NAME         READY  STATUS   AGE  TOTAL  FAIL  INFLIGHT
            11111  other-route   1/1   Running  10s      0     0         0
            12345  my-route      1/1   Running  10s      0     0         0
            """
            },
            {
            """
             PID   NAME         READY  STATUS   AGE  TOTAL  FAIL  INFLIGHT
            11111  other-route   1/1   Running  10s      0     0         0
            99999  my-route      1/1   Running  10s      0     0         0
            """
            }
        };
    }

    @Test(dataProvider = "commandOutputProvider")
    public void shouldParseOutputAndVerifyRunningStatus(String output) {
        when(process.isAlive()).thenReturn(true);
        when(process.exitValue()).thenReturn(0);

        when(pao.getOutput()).thenReturn(output);

        context.getReferenceResolver().bind("camelJBang", new CamelJBang() {
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

    @DataProvider(name = "commandJsonOutputProvider")
    public Object[][] commandJsonOutputProvider() {
        return new Object[][] {
            {
            """
            [{"pid":12345,"name":"my-route","ready":"1/1","status":"Running","age":"25s","total":0,"fail":0,"inflight":0}]
            """
            },
            {
            """
            some unexpected log output
            [{"pid":12345,"name":"my-route","ready":"1/1","status":"Running","age":"25s","total":0,"fail":0,"inflight":0}]
            """
            },
            {
            """
            [{"pid":11111,"name":"other-route","ready":"1/1","status":"Running","age":"25s","total":0,"fail":0,"inflight":0},{"pid":12345,"name":"my-route","ready":"1/1","status":"Running","age":"25s","total":0,"fail":0,"inflight":0}]
            """
            },
            {
            """
            [{"pid":11111,"name":"other-route","ready":"1/1","status":"Running","age":"25s","total":0,"fail":0,"inflight":0},{"pid":99999,"name":"my-route","ready":"1/1","status":"Running","age":"25s","total":0,"fail":0,"inflight":0}]
            """
            }
        };
    }

    @Test(dataProvider = "commandJsonOutputProvider")
    public void shouldVerifyRunningStatus(String output) {
        when(process.isAlive()).thenReturn(true);
        when(process.exitValue()).thenReturn(0);

        when(pao.getOutput()).thenReturn(output);

        context.getReferenceResolver().bind("camelJBang", new CamelJBang() {
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

    @DataProvider(name = "failedOutputProvider")
    public Object[][] failedOutputProvider() {
        return new Object[][] {
            {""},
            {"Something completely different"},
            {
            """
             PID   NAME   READY  STATUS   AGE  TOTAL  FAIL  INFLIGHT
            """
            },
            {
            """
             PID   NAME         READY  STATUS   AGE  TOTAL  FAIL  INFLIGHT
            11111  other-route   1/1   Running  10s      0     0         0
            """
            },
            {
            """
             PID   NAME      READY  STATUS   AGE  TOTAL  FAIL  INFLIGHT
            12345  my-route   0/1   Pending  10s      0     0         0
            """
            },
        };
    }

    @Test(dataProvider = "failedOutputProvider", expectedExceptions = ActionTimeoutException.class)
    public void shouldParseOutputAndFailVerification(String output) {
        when(process.isAlive()).thenReturn(true);
        when(process.exitValue()).thenReturn(0);

        when(pao.getOutput()).thenReturn(output);

        context.getReferenceResolver().bind("camelJBang", new CamelJBang() {
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

    @DataProvider(name = "failedJsonOutputProvider")
    public Object[][] failedJsonOutputProvider() {
        return new Object[][] {
            {""},
            {"Something completely different"},
            {
            """
            []
            """
            },
            {
            """
            [{"pid":11111,"name":"other-route","ready":"1/1","status":"Running","age":"25s","total":0,"fail":0,"inflight":0}]
            """
            },
            {
            """
            [{"pid":12345,"name":"my-route","ready":"0/1","status":"Pending","age":"25s","total":0,"fail":0,"inflight":0}]
            """
            }
        };
    }

    @Test(dataProvider = "failedOutputProvider", expectedExceptions = ActionTimeoutException.class)
    public void shouldFailVerification(String output) {
        when(process.isAlive()).thenReturn(true);
        when(process.exitValue()).thenReturn(0);

        when(pao.getOutput()).thenReturn(output);

        context.getReferenceResolver().bind("camelJBang", new CamelJBang() {
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

}
