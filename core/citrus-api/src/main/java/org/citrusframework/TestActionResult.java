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

package org.citrusframework;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

import org.citrusframework.json.JsonStringBuilder;
import org.citrusframework.message.Message;
import org.citrusframework.yaml.YamlStringBuilder;

import static java.util.Objects.nonNull;

public class TestActionResult {

    private final String name;
    private final String path;

    private String error;

    private final List<TestActionResult> actions = new ArrayList<>();
    private final List<TestActionResult> iterations = new ArrayList<>();

    private Message message;

    public TestActionResult(String name, String path) {
        this.name = name;
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getError() {
        return error;
    }

    public boolean isSuccess() {
        return error == null;
    }

    public boolean isFailed() {
        return error != null;
    }

    public void addAction(TestActionResult result) {
        this.actions.add(result);
    }

    public void setActions(List<TestActionResult> actions) {
        this.actions.addAll(actions);
    }

    public List<TestActionResult> getActions() {
        return actions;
    }

    public String toJson() {
        var builder = new JsonStringBuilder()
                .withObject()
                .withProperty("name", name)
                .withProperty("path", path);

        if (nonNull(error)) {
            builder.withPropertyEscaped("error", error);
        }

        if (nonNull(message)) {
            builder.withProperty("message")
                    .withObject()
                        .withProperty("headers")
                        .withArray(message.getHeaders())
                        .withProperty("headerData")
                        .withArray(message.getHeaderData()
                                .stream()
                                .map(String::getBytes)
                                .map(Base64.getEncoder()::encodeToString)
                                .toList())
                        .withProperty("payload", Base64.getEncoder().encodeToString(message.getPayload(byte[].class)))
                    .closeObject();
        }

        if (!actions.isEmpty()) {
            builder.withProperty("actions")
                    .withArray()
                    .append(actions.stream()
                                .map(TestActionResult::toJson)
                                .collect(Collectors.joining(",")))
                    .closeArray();
        }

        if (!iterations.isEmpty()) {
            builder.withProperty("iterations")
                    .withArray()
                    .append(iterations.stream()
                                .map(TestActionResult::toJson)
                                .collect(Collectors.joining(",")))
                    .closeArray();
        }

        return builder.closeObject().toString();
    }

    public String toYaml() {
        var builder = new YamlStringBuilder()
                .withProperty("name", name)
                .withProperty("path", path);

        if (nonNull(error)) {
            builder.withPropertyBlockStyle("error", error);
        }

        if (nonNull(message)) {
            builder.withObject("message")
                    .withObject("headers")
                        .withArray()
                            .withProperties(message.getHeaders())
                        .closeArray()
                    .closeObject()
                    .withArray("headerData", message.getHeaderData()
                            .stream()
                            .map(String::getBytes)
                            .map(Base64.getEncoder()::encodeToString)
                            .toList())
                    .withPropertyBlockStyle("payload", message.getPayload(String.class))
                    .closeObject();
        }

        if (!actions.isEmpty()) {
            builder.withObject("actions")
                    .withArray()
                    .append(actions.stream().map(TestActionResult::toYaml).toList())
                    .closeArray()
                .closeObject();
        }

        if (!iterations.isEmpty()) {
            builder.withObject("iterations")
                    .withArray()
                    .append(iterations.stream().map(TestActionResult::toYaml).toList())
                    .closeArray()
                .closeObject();
        }

        return builder.toString();
    }

    public void addIteration(TestActionResult iteration) {
        iterations.add(iteration);
    }

    public List<TestActionResult> getIterations() {
        return iterations;
    }

    public void setIterations(List<TestActionResult> iterations) {
        this.iterations.addAll(iterations);
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public Message getMessage() {
        return message;
    }
}
