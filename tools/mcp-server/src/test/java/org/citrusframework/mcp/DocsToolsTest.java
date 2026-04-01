/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.citrusframework.mcp;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DocsToolsTest {

    private DocsTools createTools() {
        DocsData docsData = new DocsData();

        DocsTools tools = new DocsTools();
        tools.docsData = docsData;
        return tools;
    }

    @Test
    void shouldGetDocsIndex() {
        DocsTools tools = createTools();

        List<String> result = tools.citrus_docs_index(null, null);

        assertThat(result).isNotNull();
        assertThat(result.size()).isGreaterThan(0);
        assertThat(result).contains("index.adoc");
    }

    @Test
    void shouldGetDocsIndexWithFilter() {
        DocsTools tools = createTools();

        List<String> result = tools.citrus_docs_index("http", null);

        assertThat(result).isNotNull();
        assertThat(result.size()).isGreaterThan(0);
        assertThat(result).contains("endpoint-http.adoc");
    }

    @Test
    void shouldGetDocsPage() {
        DocsTools tools = createTools();

        DocsData.DocInfo result = tools.citrus_docs_page("action", "print", null);

        assertThat(result).isNotNull();
        assertThat(result.fileName()).isEqualTo("actions-print.adoc");
        assertThat(result.adoc()).isNotNull();

        result = tools.citrus_docs_page("endpoint", "kafka", null);

        assertThat(result).isNotNull();
        assertThat(result.fileName()).isEqualTo("endpoint-kafka.adoc");
        assertThat(result.adoc()).isNotNull();
    }

}
