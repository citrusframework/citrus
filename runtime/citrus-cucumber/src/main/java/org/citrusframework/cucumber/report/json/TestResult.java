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

package org.citrusframework.cucumber.report.json;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Error details holding failure information.
 */
public class TestResult {

    private final UUID id;
    private final String name;
    private final String classname;
    private Throwable cause;

    public TestResult(UUID id, String name, String classname) {
        this.id = id;
        this.name = name;
        this.classname = classname;
    }

    public TestResult(UUID id, String name, String classname, Throwable cause) {
        this.id = id;
        this.name = name;
        this.classname = classname;
        this.cause = cause;
    }

    @JsonIgnore
    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getClassname() {
        return classname;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getErrorType() {
        if (cause == null) {
            return null;
        }

        return cause.getClass().getName();
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getErrorMessage() {
        if (cause == null) {
            return null;
        }

        return cause.getMessage();
    }

    public void setCause(Throwable cause) {
        this.cause = cause;
    }

    @JsonIgnore
    public Throwable getCause() {
        return cause;
    }
}
