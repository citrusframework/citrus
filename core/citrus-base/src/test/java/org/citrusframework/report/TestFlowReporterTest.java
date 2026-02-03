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

package org.citrusframework.report;

import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.TestResult;
import org.citrusframework.actions.EchoAction;
import org.citrusframework.actions.ReceiveMessageAction;
import org.citrusframework.actions.SendMessageAction;
import org.citrusframework.container.Iterate;
import org.citrusframework.container.Sequence;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageHeaders;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

public class TestFlowReporterTest {

    @Mock
    private TestCase test;

    private final EchoAction echo = new EchoAction.Builder().build();
    private final SendMessageAction send = new SendMessageAction.Builder().build();
    private final ReceiveMessageAction receive = new ReceiveMessageAction.Builder().build();

    @Mock
    private Iterate iteration;
    @Mock
    private Sequence sequence;

    @BeforeClass
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void shouldGetJsonReport() {
        TestFlowReporter reporter = new TestFlowReporter();
        TestCaseMetaInfo metaInfo = new TestCaseMetaInfo();
        metaInfo.setAuthor("theAuthor");
        metaInfo.setStatus(TestCaseMetaInfo.Status.FINAL);

        TestResults results = new TestResults();
        TestResult result = TestResult.success("fooTest", "FooTest");
        result.withDuration(Duration.ofSeconds(2));
        results.addResult(result);
        reset(test);

        when(test.getName()).thenReturn("fooTest");
        when(test.getMetaInfo()).thenReturn(metaInfo);
        when(test.getTestResult()).thenReturn(result);
        when(test.getActionIndex(echo)).thenReturn(0);
        when(test.getActionIndex(send)).thenReturn(1);
        when(test.getActionIndex(receive)).thenReturn(2);

        reporter.onTestStart(test);

        reporter.onTestActionStart(test, echo);
        reporter.onTestActionFinish(test, echo);

        reporter.onTestActionStart(test, send);
        reporter.onTestActionFinish(test, send);

        reporter.onTestActionStart(test, receive);
        reporter.onTestActionFinish(test, receive);

        reporter.onTestSuccess(test);

        reporter.generate(results);
        assertThat(reporter.getJsonReport()).isEqualToNormalizingNewlines("""
                [
                  {
                    "name": "fooTest",
                    "result": {
                      "name": "fooTest",
                      "className": "FooTest",
                      "result": "SUCCESS",
                      "duration": 2000
                    },
                    "actions": [
                      {
                        "name": "echo",
                        "path": "actions.0.echo"
                      },
                      {
                        "name": "send",
                        "path": "actions.1.send"
                      },
                      {
                        "name": "receive",
                        "path": "actions.2.receive"
                      }
                    ]
                  }
                ]""");
    }

    @Test
    public void shouldGetJsonReportWithContainers() {
        TestFlowReporter reporter = new TestFlowReporter();
        TestCaseMetaInfo metaInfo = new TestCaseMetaInfo();
        metaInfo.setAuthor("theAuthor");
        metaInfo.setStatus(TestCaseMetaInfo.Status.FINAL);

        TestResults results = new TestResults();
        TestResult result = TestResult.success("fooTest", "FooTest");
        result.withDuration(Duration.ofSeconds(2));
        results.addResult(result);
        reset(test);

        when(test.getName()).thenReturn("fooTest");
        when(test.getMetaInfo()).thenReturn(metaInfo);
        when(test.getTestResult()).thenReturn(result);
        when(test.getActionIndex(echo)).thenReturn(0);
        when(test.getActionIndex(send)).thenReturn(1);
        when(test.getActionIndex(receive)).thenReturn(2);

        when(sequence.getName()).thenReturn("sequence");
        when(sequence.getActions()).thenReturn(List.of(send, receive));
        when(sequence.getExecutedActions()).thenReturn(List.of(send, receive));
        when(sequence.getActionIndex(send)).thenReturn(0);
        when(sequence.getActionIndex(receive)).thenReturn(1);

        reporter.onTestStart(test);

        reporter.onTestActionStart(test, echo);
        reporter.onTestActionFinish(test, echo);

        reporter.onTestActionStart(test, sequence);
        reporter.onTestActionFinish(test, sequence);

        reporter.onTestSuccess(test);

        reporter.generate(results);
        assertThat(reporter.getJsonReport()).isEqualToNormalizingNewlines("""
                [
                  {
                    "name": "fooTest",
                    "result": {
                      "name": "fooTest",
                      "className": "FooTest",
                      "result": "SUCCESS",
                      "duration": 2000
                    },
                    "actions": [
                      {
                        "name": "echo",
                        "path": "actions.0.echo"
                      },
                      {
                        "name": "sequence",
                        "path": "actions.0.sequence",
                        "actions": [
                          {
                            "name": "send",
                            "path": "actions.0.sequence.actions.0.send"
                          },
                          {
                            "name": "receive",
                            "path": "actions.0.sequence.actions.1.receive"
                          }
                        ]
                      }
                    ]
                  }
                ]""");
    }

    @Test
    public void shouldGetJsonReportWithFailedContainer() {
        TestFlowReporter reporter = new TestFlowReporter();
        TestCaseMetaInfo metaInfo = new TestCaseMetaInfo();
        metaInfo.setAuthor("theAuthor");
        metaInfo.setStatus(TestCaseMetaInfo.Status.FINAL);

        TestResults results = new TestResults();
        TestResult result = TestResult.failed("fooTest", "FooTest", new CitrusRuntimeException("This went totally wrong!"));
        result.withDuration(Duration.ofSeconds(2));
        results.addResult(result);
        reset(test);

        when(test.getName()).thenReturn("fooTest");
        when(test.getMetaInfo()).thenReturn(metaInfo);
        when(test.getTestResult()).thenReturn(result);
        when(test.getActionIndex(echo)).thenReturn(0);
        when(test.getActionIndex(send)).thenReturn(1);
        when(test.getActionIndex(receive)).thenReturn(2);

        when(sequence.getName()).thenReturn("sequence");
        when(sequence.getActions()).thenReturn(List.of(send, receive));
        when(sequence.getExecutedActions()).thenReturn(List.of(send, receive));
        when(sequence.getActionIndex(send)).thenReturn(0);
        when(sequence.getActionIndex(receive)).thenReturn(1);

        reporter.onTestStart(test);

        reporter.onTestActionStart(test, echo);
        reporter.onTestActionFinish(test, echo);

        reporter.onTestActionStart(test, sequence);
        reporter.onTestActionFailed(test, sequence, new CitrusRuntimeException("This went totally wrong!"));

        reporter.onTestSuccess(test);

        reporter.generate(results);
        assertThat(reporter.getJsonReport()).isEqualToNormalizingNewlines("""
                [
                  {
                    "name": "fooTest",
                    "result": {
                      "name": "fooTest",
                      "className": "FooTest",
                      "result": "FAILURE",
                      "cause": "org.citrusframework.exceptions.CitrusRuntimeException: This went totally wrong!",
                      "errorMessage": "This went totally wrong!",
                      "duration": 2000
                    },
                    "actions": [
                      {
                        "name": "echo",
                        "path": "actions.0.echo"
                      },
                      {
                        "name": "sequence",
                        "path": "actions.0.sequence",
                        "error": "This went totally wrong!",
                        "actions": [
                          {
                            "name": "send",
                            "path": "actions.0.sequence.actions.0.send"
                          },
                          {
                            "name": "receive",
                            "path": "actions.0.sequence.actions.1.receive"
                          }
                        ]
                      }
                    ]
                  }
                ]""");
    }


    @Test
    public void shouldGetJsonReportWithIterations() {
        TestFlowReporter reporter = new TestFlowReporter();
        TestCaseMetaInfo metaInfo = new TestCaseMetaInfo();
        metaInfo.setAuthor("theAuthor");
        metaInfo.setStatus(TestCaseMetaInfo.Status.FINAL);

        TestResults results = new TestResults();
        TestResult result = TestResult.success("fooTest", "FooTest");
        result.withDuration(Duration.ofSeconds(2));
        results.addResult(result);
        reset(test);

        when(test.getName()).thenReturn("fooTest");
        when(test.getMetaInfo()).thenReturn(metaInfo);
        when(test.getTestResult()).thenReturn(result);
        when(test.getActionIndex(echo)).thenReturn(0);
        when(test.getActionIndex(send)).thenReturn(1);
        when(test.getActionIndex(receive)).thenReturn(2);

        when(iteration.getName()).thenReturn("iterate");
        when(iteration.getIterations()).thenReturn(2);
        when(iteration.getActions()).thenReturn(List.of(send, receive));
        when(iteration.getExecutedActions()).thenReturn(List.of(send, receive, send, receive));
        when(iteration.getActionIndex(send)).thenReturn(0).thenReturn(3);
        when(iteration.getActionIndex(receive)).thenReturn(1).thenReturn(4);

        reporter.onTestStart(test);

        reporter.onTestActionStart(test, echo);
        reporter.onTestActionFinish(test, echo);

        reporter.onTestActionStart(test, iteration);
        reporter.onTestActionFinish(test, iteration);

        reporter.onTestSuccess(test);

        reporter.generate(results);
        assertThat(reporter.getJsonReport()).isEqualToNormalizingNewlines("""
                [
                  {
                    "name": "fooTest",
                    "result": {
                      "name": "fooTest",
                      "className": "FooTest",
                      "result": "SUCCESS",
                      "duration": 2000
                    },
                    "actions": [
                      {
                        "name": "echo",
                        "path": "actions.0.echo"
                      },
                      {
                        "name": "iterate",
                        "path": "actions.0.iterate",
                        "iterations": [
                          {
                            "name": "0",
                            "path": "actions.0.iterate",
                            "actions": [
                              {
                                "name": "send",
                                "path": "actions.0.iterate.actions.0.send"
                              },
                              {
                                "name": "receive",
                                "path": "actions.0.iterate.actions.1.receive"
                              }
                            ]
                          },
                          {
                            "name": "1",
                            "path": "actions.0.iterate",
                            "actions": [
                              {
                                "name": "send",
                                "path": "actions.0.iterate.actions.1.send"
                              },
                              {
                                "name": "receive",
                                "path": "actions.0.iterate.actions.0.receive"
                              }
                            ]
                          }
                        ]
                      }
                    ]
                  }
                ]""");
    }

    @Test
    public void shouldGetJsonReportWithParameters() {
        TestFlowReporter reporter = new TestFlowReporter();
        TestCaseMetaInfo metaInfo = new TestCaseMetaInfo();
        metaInfo.setAuthor("theAuthor");
        metaInfo.setStatus(TestCaseMetaInfo.Status.FINAL);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("foo", "bar");
        parameters.put("baz", "baz");

        TestResults results = new TestResults();
        TestResult result = TestResult.success("fooTest", "FooTest", parameters);
        result.withDuration(Duration.ofSeconds(2));
        results.addResult(result);
        reset(test);

        when(test.getName()).thenReturn("fooTest");
        when(test.getMetaInfo()).thenReturn(metaInfo);
        when(test.getTestResult()).thenReturn(result);
        when(test.getActionIndex(echo)).thenReturn(0);

        reporter.onTestStart(test);

        reporter.onTestActionStart(test, echo);
        reporter.onTestActionFinish(test, echo);

        reporter.onTestSuccess(test);

        reporter.generate(results);
        assertThat(reporter.getJsonReport()).isEqualToNormalizingNewlines("""
                [
                  {
                    "name": "fooTest",
                    "result": {
                      "name": "fooTest",
                      "className": "FooTest",
                      "parameters": [
                        {
                          "name": "baz",
                          "value": "baz"
                        },
                        {
                          "name": "foo",
                          "value": "bar"
                        }
                      ],
                      "result": "SUCCESS",
                      "duration": 2000
                    },
                    "actions": [
                      {
                        "name": "echo",
                        "path": "actions.0.echo"
                      }
                    ]
                  }
                ]""");
    }

    @Test
    public void shouldGetJsonReportForFailedTest() {
        TestFlowReporter reporter = new TestFlowReporter();
        TestCaseMetaInfo metaInfo = new TestCaseMetaInfo();
        metaInfo.setAuthor("theAuthor");
        metaInfo.setStatus(TestCaseMetaInfo.Status.FINAL);

        TestResults results = new TestResults();
        TestResult result = TestResult.failed("fooTest", "FooTest", new CitrusRuntimeException("Something went completely wrong"));
        result.withDuration(Duration.ofSeconds(2));
        results.addResult(result);
        reset(test);

        when(test.getName()).thenReturn("fooTest");
        when(test.getMetaInfo()).thenReturn(metaInfo);
        when(test.getTestResult()).thenReturn(result);
        when(test.getActionIndex(echo)).thenReturn(0);
        when(test.getActionIndex(send)).thenReturn(1);
        when(test.getActionIndex(receive)).thenReturn(2);

        reporter.onTestStart(test);

        reporter.onTestActionStart(test, echo);
        reporter.onTestActionFinish(test, echo);

        reporter.onTestActionStart(test, send);
        reporter.onTestActionFinish(test, send);

        reporter.onTestActionStart(test, receive);
        reporter.onTestActionFailed(test, receive, new CitrusRuntimeException("Something went completely wrong"));

        reporter.onTestSuccess(test);

        reporter.generate(results);
        assertThat(reporter.getJsonReport()).isEqualToNormalizingNewlines("""
                [
                  {
                    "name": "fooTest",
                    "result": {
                      "name": "fooTest",
                      "className": "FooTest",
                      "result": "FAILURE",
                      "cause": "org.citrusframework.exceptions.CitrusRuntimeException: Something went completely wrong",
                      "errorMessage": "Something went completely wrong",
                      "duration": 2000
                    },
                    "actions": [
                      {
                        "name": "echo",
                        "path": "actions.0.echo"
                      },
                      {
                        "name": "send",
                        "path": "actions.1.send"
                      },
                      {
                        "name": "receive",
                        "path": "actions.2.receive",
                        "error": "Something went completely wrong"
                      }
                    ]
                  }
                ]""");
    }

    @Test
    public void shouldGetJsonReportWithMessages() {
        SendMessageAction sendMock = Mockito.mock(SendMessageAction.class);
        ReceiveMessageAction receiveMock = Mockito.mock(ReceiveMessageAction.class);

        TestFlowReporter reporter = new TestFlowReporter();
        TestCaseMetaInfo metaInfo = new TestCaseMetaInfo();
        metaInfo.setAuthor("theAuthor");
        metaInfo.setStatus(TestCaseMetaInfo.Status.FINAL);

        Message messageSent = new DefaultMessage("Hello from Citrus!").setHeader("foo", "bar");
        Message messageReceived = new DefaultMessage("Citrus rocks!").setHeader("foo", "baz");

        TestResults results = new TestResults();
        TestResult result = TestResult.success("fooTest", "FooTest");
        result.withDuration(Duration.ofSeconds(2));
        results.addResult(result);
        reset(test);

        when(test.getName()).thenReturn("fooTest");
        when(test.getMetaInfo()).thenReturn(metaInfo);
        when(test.getTestResult()).thenReturn(result);
        when(test.getActionIndex(echo)).thenReturn(0);
        when(test.getActionIndex(sendMock)).thenReturn(1);
        when(test.getActionIndex(receiveMock)).thenReturn(2);

        when(sendMock.getName()).thenReturn("send");
        when(sendMock.getMessage()).thenReturn(Optional.of(messageSent));
        when(receiveMock.getName()).thenReturn("receive");
        when(receiveMock.getMessage()).thenReturn(Optional.of(messageReceived));

        reporter.onTestStart(test);

        reporter.onTestActionStart(test, echo);
        reporter.onTestActionFinish(test, echo);

        reporter.onTestActionStart(test, sendMock);
        reporter.onTestActionFinish(test, sendMock);

        reporter.onTestActionStart(test, receiveMock);
        reporter.onTestActionFinish(test, receiveMock);

        reporter.onTestSuccess(test);

        reporter.generate(results);
        assertThat(reporter.getJsonReport()).isEqualToNormalizingNewlines("""
                [
                  {
                    "name": "fooTest",
                    "result": {
                      "name": "fooTest",
                      "className": "FooTest",
                      "result": "SUCCESS",
                      "duration": 2000
                    },
                    "actions": [
                      {
                        "name": "echo",
                        "path": "actions.0.echo"
                      },
                      {
                        "name": "send",
                        "path": "actions.1.send",
                        "message": {
                          "headers": [
                            {
                              "name": "citrus_message_id",
                              "value": "%s"
                            },
                            {
                              "name": "citrus_message_timestamp",
                              "value": "%s"
                            },
                            {
                              "name": "foo",
                              "value": "bar"
                            }
                          ],
                          "headerData": [
                          ],
                          "payload": "SGVsbG8gZnJvbSBDaXRydXMh"
                        }
                      },
                      {
                        "name": "receive",
                        "path": "actions.2.receive",
                        "message": {
                          "headers": [
                            {
                              "name": "citrus_message_id",
                              "value": "%s"
                            },
                            {
                              "name": "citrus_message_timestamp",
                              "value": "%s"
                            },
                            {
                              "name": "foo",
                              "value": "baz"
                            }
                          ],
                          "headerData": [
                          ],
                          "payload": "Q2l0cnVzIHJvY2tzIQ=="
                        }
                      }
                    ]
                  }
                ]""".formatted(messageSent.getId(), messageSent.getHeader(MessageHeaders.TIMESTAMP), messageReceived.getId(), messageReceived.getHeader(MessageHeaders.TIMESTAMP)));
    }

    @Test
    public void shouldGetYamlReport() {
        TestFlowReporter reporter = new TestFlowReporter();
        TestCaseMetaInfo metaInfo = new TestCaseMetaInfo();
        metaInfo.setAuthor("theAuthor");
        metaInfo.setStatus(TestCaseMetaInfo.Status.FINAL);

        TestResults results = new TestResults();
        TestResult result = TestResult.success("fooTest", "FooTest");
        result.withDuration(Duration.ofSeconds(2));
        results.addResult(result);
        reset(test);

        when(test.getName()).thenReturn("fooTest");
        when(test.getMetaInfo()).thenReturn(metaInfo);
        when(test.getTestResult()).thenReturn(result);
        when(test.getActionIndex(echo)).thenReturn(0);
        when(test.getActionIndex(send)).thenReturn(1);
        when(test.getActionIndex(receive)).thenReturn(2);

        reporter.onTestStart(test);

        reporter.onTestActionStart(test, echo);
        reporter.onTestActionFinish(test, echo);

        reporter.onTestActionStart(test, send);
        reporter.onTestActionFinish(test, send);

        reporter.onTestActionStart(test, receive);
        reporter.onTestActionFinish(test, receive);

        reporter.onTestSuccess(test);

        reporter.generate(results);
        Assert.assertEquals(reporter.getYamlReport(), """
                - name: "fooTest"
                  result:
                    name: "fooTest"
                    className: "FooTest"
                    result: "SUCCESS"
                    duration: 2000
                  actions:
                  - name: "echo"
                    path: "actions.0.echo"
                  - name: "send"
                    path: "actions.1.send"
                  - name: "receive"
                    path: "actions.2.receive"
                """);
    }

    @Test
    public void shouldGetYamlReportWithContainers() {
        TestFlowReporter reporter = new TestFlowReporter();
        TestCaseMetaInfo metaInfo = new TestCaseMetaInfo();
        metaInfo.setAuthor("theAuthor");
        metaInfo.setStatus(TestCaseMetaInfo.Status.FINAL);

        TestResults results = new TestResults();
        TestResult result = TestResult.success("fooTest", "FooTest");
        result.withDuration(Duration.ofSeconds(2));
        results.addResult(result);
        reset(test);

        when(test.getName()).thenReturn("fooTest");
        when(test.getMetaInfo()).thenReturn(metaInfo);
        when(test.getTestResult()).thenReturn(result);
        when(test.getActionIndex(echo)).thenReturn(0);
        when(test.getActionIndex(send)).thenReturn(1);
        when(test.getActionIndex(receive)).thenReturn(2);

        when(sequence.getName()).thenReturn("sequence");
        when(sequence.getActions()).thenReturn(List.of(send, receive));
        when(sequence.getExecutedActions()).thenReturn(List.of(send, receive));
        when(sequence.getActionIndex(send)).thenReturn(0);
        when(sequence.getActionIndex(receive)).thenReturn(1);

        reporter.onTestStart(test);

        reporter.onTestActionStart(test, echo);
        reporter.onTestActionFinish(test, echo);

        reporter.onTestActionStart(test, sequence);
        reporter.onTestActionFinish(test, sequence);

        reporter.onTestSuccess(test);

        reporter.generate(results);
        Assert.assertEquals(reporter.getYamlReport(), """
                - name: "fooTest"
                  result:
                    name: "fooTest"
                    className: "FooTest"
                    result: "SUCCESS"
                    duration: 2000
                  actions:
                  - name: "echo"
                    path: "actions.0.echo"
                  - name: "sequence"
                    path: "actions.0.sequence"
                    actions:
                    - name: "send"
                      path: "actions.0.sequence.actions.0.send"
                    - name: "receive"
                      path: "actions.0.sequence.actions.1.receive"
                """);
    }

    @Test
    public void shouldGetYamlReportWithFailedContainer() {
        TestFlowReporter reporter = new TestFlowReporter();
        TestCaseMetaInfo metaInfo = new TestCaseMetaInfo();
        metaInfo.setAuthor("theAuthor");
        metaInfo.setStatus(TestCaseMetaInfo.Status.FINAL);

        TestResults results = new TestResults();
        TestResult result = TestResult.failed("fooTest", "FooTest", new CitrusRuntimeException("This went totally wrong!"));
        result.withDuration(Duration.ofSeconds(2));
        results.addResult(result);
        reset(test);

        when(test.getName()).thenReturn("fooTest");
        when(test.getMetaInfo()).thenReturn(metaInfo);
        when(test.getTestResult()).thenReturn(result);
        when(test.getActionIndex(echo)).thenReturn(0);
        when(test.getActionIndex(send)).thenReturn(1);
        when(test.getActionIndex(receive)).thenReturn(2);

        when(sequence.getName()).thenReturn("sequence");
        when(sequence.getActions()).thenReturn(List.of(send, receive));
        when(sequence.getExecutedActions()).thenReturn(List.of(send, receive));
        when(sequence.getActionIndex(send)).thenReturn(0);
        when(sequence.getActionIndex(receive)).thenReturn(1);

        reporter.onTestStart(test);

        reporter.onTestActionStart(test, echo);
        reporter.onTestActionFinish(test, echo);

        reporter.onTestActionStart(test, sequence);
        reporter.onTestActionFailed(test, sequence, new CitrusRuntimeException("This went totally wrong!"));

        reporter.onTestSuccess(test);

        reporter.generate(results);
        Assert.assertEquals(reporter.getYamlReport(), """
                - name: "fooTest"
                  result:
                    name: "fooTest"
                    className: "FooTest"
                    result: "FAILURE"
                    cause: |
                      org.citrusframework.exceptions.CitrusRuntimeException: This went totally wrong!
                    errorMessage: |
                      This went totally wrong!
                    duration: 2000
                  actions:
                  - name: "echo"
                    path: "actions.0.echo"
                  - name: "sequence"
                    path: "actions.0.sequence"
                    error: |
                      This went totally wrong!
                    actions:
                    - name: "send"
                      path: "actions.0.sequence.actions.0.send"
                    - name: "receive"
                      path: "actions.0.sequence.actions.1.receive"
                """);
    }


    @Test
    public void shouldGetYamlReportWithIterations() {
        TestFlowReporter reporter = new TestFlowReporter();
        TestCaseMetaInfo metaInfo = new TestCaseMetaInfo();
        metaInfo.setAuthor("theAuthor");
        metaInfo.setStatus(TestCaseMetaInfo.Status.FINAL);

        TestResults results = new TestResults();
        TestResult result = TestResult.success("fooTest", "FooTest");
        result.withDuration(Duration.ofSeconds(2));
        results.addResult(result);
        reset(test);

        when(test.getName()).thenReturn("fooTest");
        when(test.getMetaInfo()).thenReturn(metaInfo);
        when(test.getTestResult()).thenReturn(result);
        when(test.getActionIndex(echo)).thenReturn(0);
        when(test.getActionIndex(send)).thenReturn(1);
        when(test.getActionIndex(receive)).thenReturn(2);

        when(iteration.getName()).thenReturn("iterate");
        when(iteration.getIterations()).thenReturn(2);
        when(iteration.getActions()).thenReturn(List.of(send, receive));
        when(iteration.getExecutedActions()).thenReturn(List.of(send, receive, send, receive));
        when(iteration.getActionIndex(send)).thenReturn(0).thenReturn(3);
        when(iteration.getActionIndex(receive)).thenReturn(1).thenReturn(4);

        reporter.onTestStart(test);

        reporter.onTestActionStart(test, echo);
        reporter.onTestActionFinish(test, echo);

        reporter.onTestActionStart(test, iteration);
        reporter.onTestActionFinish(test, iteration);

        reporter.onTestSuccess(test);

        reporter.generate(results);
        Assert.assertEquals(reporter.getYamlReport(), """
                - name: "fooTest"
                  result:
                    name: "fooTest"
                    className: "FooTest"
                    result: "SUCCESS"
                    duration: 2000
                  actions:
                  - name: "echo"
                    path: "actions.0.echo"
                  - name: "iterate"
                    path: "actions.0.iterate"
                    iterations:
                    - name: "0"
                      path: "actions.0.iterate"
                      actions:
                      - name: "send"
                        path: "actions.0.iterate.actions.0.send"
                      - name: "receive"
                        path: "actions.0.iterate.actions.1.receive"
                    - name: "1"
                      path: "actions.0.iterate"
                      actions:
                      - name: "send"
                        path: "actions.0.iterate.actions.1.send"
                      - name: "receive"
                        path: "actions.0.iterate.actions.0.receive"
                """);
    }

    @Test
    public void shouldGetYamlReportWithParameters() {
        TestFlowReporter reporter = new TestFlowReporter();
        TestCaseMetaInfo metaInfo = new TestCaseMetaInfo();
        metaInfo.setAuthor("theAuthor");
        metaInfo.setStatus(TestCaseMetaInfo.Status.FINAL);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("foo", "bar");
        parameters.put("baz", "baz");

        TestResults results = new TestResults();
        TestResult result = TestResult.success("fooTest", "FooTest", parameters);
        result.withDuration(Duration.ofSeconds(2));
        results.addResult(result);
        reset(test);

        when(test.getName()).thenReturn("fooTest");
        when(test.getMetaInfo()).thenReturn(metaInfo);
        when(test.getTestResult()).thenReturn(result);
        when(test.getActionIndex(echo)).thenReturn(0);

        reporter.onTestStart(test);

        reporter.onTestActionStart(test, echo);
        reporter.onTestActionFinish(test, echo);

        reporter.onTestSuccess(test);

        reporter.generate(results);
        Assert.assertEquals(reporter.getYamlReport(), """
                - name: "fooTest"
                  result:
                    name: "fooTest"
                    className: "FooTest"
                    parameters:
                      name: "baz"
                      value: "baz"
                      name: "foo"
                      value: "bar"
                    result: "SUCCESS"
                    duration: 2000
                  actions:
                  - name: "echo"
                    path: "actions.0.echo"
                """);
    }

    @Test
    public void shouldGetYamlReportForFailedTest() {
        TestFlowReporter reporter = new TestFlowReporter();
        TestCaseMetaInfo metaInfo = new TestCaseMetaInfo();
        metaInfo.setAuthor("theAuthor");
        metaInfo.setStatus(TestCaseMetaInfo.Status.FINAL);

        TestResults results = new TestResults();
        TestResult result = TestResult.failed("fooTest", "FooTest", new CitrusRuntimeException("Something went completely wrong"));
        result.withDuration(Duration.ofSeconds(2));
        results.addResult(result);
        reset(test);

        when(test.getName()).thenReturn("fooTest");
        when(test.getMetaInfo()).thenReturn(metaInfo);
        when(test.getTestResult()).thenReturn(result);
        when(test.getActionIndex(echo)).thenReturn(0);
        when(test.getActionIndex(send)).thenReturn(1);
        when(test.getActionIndex(receive)).thenReturn(2);

        reporter.onTestStart(test);

        reporter.onTestActionStart(test, echo);
        reporter.onTestActionFinish(test, echo);

        reporter.onTestActionStart(test, send);
        reporter.onTestActionFinish(test, send);

        reporter.onTestActionStart(test, receive);
        reporter.onTestActionFailed(test, receive, new CitrusRuntimeException("Something went completely wrong"));

        reporter.onTestSuccess(test);

        reporter.generate(results);
        Assert.assertEquals(reporter.getYamlReport(), """
                - name: "fooTest"
                  result:
                    name: "fooTest"
                    className: "FooTest"
                    result: "FAILURE"
                    cause: |
                      org.citrusframework.exceptions.CitrusRuntimeException: Something went completely wrong
                    errorMessage: |
                      Something went completely wrong
                    duration: 2000
                  actions:
                  - name: "echo"
                    path: "actions.0.echo"
                  - name: "send"
                    path: "actions.1.send"
                  - name: "receive"
                    path: "actions.2.receive"
                    error: |
                      Something went completely wrong
                """);
    }

    @Test
    public void shouldGetYamlReportWithMessages() {
        SendMessageAction sendMock = Mockito.mock(SendMessageAction.class);
        ReceiveMessageAction receiveMock = Mockito.mock(ReceiveMessageAction.class);

        TestFlowReporter reporter = new TestFlowReporter();
        TestCaseMetaInfo metaInfo = new TestCaseMetaInfo();
        metaInfo.setAuthor("theAuthor");
        metaInfo.setStatus(TestCaseMetaInfo.Status.FINAL);

        Message messageSent = new DefaultMessage("Hello from Citrus!").setHeader("foo", "bar");
        Message messageReceived = new DefaultMessage("Citrus rocks!").setHeader("foo", "baz");

        TestResults results = new TestResults();
        TestResult result = TestResult.success("fooTest", "FooTest");
        result.withDuration(Duration.ofSeconds(2));
        results.addResult(result);
        reset(test);

        when(test.getName()).thenReturn("fooTest");
        when(test.getMetaInfo()).thenReturn(metaInfo);
        when(test.getTestResult()).thenReturn(result);
        when(test.getActionIndex(echo)).thenReturn(0);
        when(test.getActionIndex(sendMock)).thenReturn(1);
        when(test.getActionIndex(receiveMock)).thenReturn(2);

        when(sendMock.getName()).thenReturn("send");
        when(sendMock.getMessage()).thenReturn(Optional.of(messageSent));
        when(receiveMock.getName()).thenReturn("receive");
        when(receiveMock.getMessage()).thenReturn(Optional.of(messageReceived));

        reporter.onTestStart(test);

        reporter.onTestActionStart(test, echo);
        reporter.onTestActionFinish(test, echo);

        reporter.onTestActionStart(test, sendMock);
        reporter.onTestActionFinish(test, sendMock);

        reporter.onTestActionStart(test, receiveMock);
        reporter.onTestActionFinish(test, receiveMock);

        reporter.onTestSuccess(test);

        reporter.generate(results);
        Assert.assertEquals(reporter.getYamlReport(), """
                - name: "fooTest"
                  result:
                    name: "fooTest"
                    className: "FooTest"
                    result: "SUCCESS"
                    duration: 2000
                  actions:
                  - name: "echo"
                    path: "actions.0.echo"
                  - name: "send"
                    path: "actions.1.send"
                    message:
                      headers:
                        - name: "citrus_message_id"
                          value: "%s"
                        - name: "citrus_message_timestamp"
                          value: "%s"
                        - name: "foo"
                          value: "bar"
                      headerData: []
                      payload: |
                        Hello from Citrus!
                  - name: "receive"
                    path: "actions.2.receive"
                    message:
                      headers:
                        - name: "citrus_message_id"
                          value: "%s"
                        - name: "citrus_message_timestamp"
                          value: "%s"
                        - name: "foo"
                          value: "baz"
                      headerData: []
                      payload: |
                        Citrus rocks!
                """.formatted(messageSent.getId(), messageSent.getHeader(MessageHeaders.TIMESTAMP), messageReceived.getId(), messageReceived.getHeader(MessageHeaders.TIMESTAMP)));
    }

}
