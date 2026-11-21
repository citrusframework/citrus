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

package org.citrusframework.dsl.schema;

import java.util.List;

import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.api.yaml.SchemaProperty;
import org.citrusframework.yaml.YamlTestCase;

/**
 * Schema of one test in the tests list of a multi-test file. It holds the same properties as a single-test file.
 */
public class TestEntry {

    @SchemaProperty(required = true, description = "The test name, unique in the file.")
    public void setName(String name) {
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData(key = "$comment", value = "group:metaData" ) },
            description = "The test description.")
    public void setDescription(String description) {
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData(key = "$comment", value = "group:metaData" ) },
            description = "The test author.")
    public void setAuthor(String author) {
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData(key = "$comment", value = "group:metaData" ) },
            description = "The test status.")
    public void setStatus(TestCaseMetaInfo.Status status) {
    }

    @SchemaProperty(description = "The test variables.")
    public void setVariables(List<Test.Variable> variables) {
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData(key = "$comment", value = "group:endpoints" ) },
            description = "List of endpoints for this test.")
    public void setEndpoints(List<Test.Endpoint> endpoints) {
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData(key = "$comment", value = "group:configuration" ) },
            description = "The test configuration holding bean definitions.")
    public void setConfiguration(YamlTestCase.Configuration configuration) {
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData(key = "$comment", value = "group:actions" ) },
            description = "The test actions.")
    public void setActions(List<TestActions> actions) {
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData(key = "$comment", value = "group:finally" ) },
            description = "The final test actions.")
    public void setFinally(List<TestActions> actions) {
    }
}
