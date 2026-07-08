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

package org.citrusframework.jbang;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

public class JBangSettingsTest {

    @Test
    public void shouldReturnEmptyListForNullInput() {
        Assert.assertTrue(JBangSettings.parseArgs(null).isEmpty());
    }

    @Test
    public void shouldReturnEmptyListForEmptyInput() {
        Assert.assertTrue(JBangSettings.parseArgs("").isEmpty());
    }

    @Test
    public void shouldReturnEmptyListForBlankInput() {
        Assert.assertTrue(JBangSettings.parseArgs("   ").isEmpty());
    }

    @Test
    public void shouldParseSingleArgWithValue() {
        List<String> result = JBangSettings.parseArgs("deps=org.foo:bar:1.0");
        Assert.assertEquals(result, List.of("--deps=org.foo:bar:1.0"));
    }

    @Test
    public void shouldParseMultipleArgs() {
        List<String> result = JBangSettings.parseArgs("deps=org.foo:bar:1.0,fresh=");
        Assert.assertEquals(result, List.of("--deps=org.foo:bar:1.0", "--fresh"));
    }

    @Test
    public void shouldMergeRepeatedArgNames() {
        List<String> result = JBangSettings.parseArgs("deps=org.foo:bar:1.0,deps=org.baz:qux:2.0");
        Assert.assertEquals(result, List.of("--deps=org.foo:bar:1.0,org.baz:qux:2.0"));
    }

    @Test
    public void shouldHandleEmptyValueAsFlag() {
        List<String> result = JBangSettings.parseArgs("fresh=");
        Assert.assertEquals(result, List.of("--fresh"));
    }

    @Test
    public void shouldHandleArgWithoutEqualsAsFlag() {
        List<String> result = JBangSettings.parseArgs("fresh");
        Assert.assertEquals(result, List.of("--fresh"));
    }

    @Test
    public void shouldNotAddDoubleDashPrefix() {
        List<String> result = JBangSettings.parseArgs("--deps=org.foo:bar:1.0");
        Assert.assertEquals(result, List.of("--deps=org.foo:bar:1.0"));
    }

    @Test
    public void shouldHandleFullExampleFromIssue() {
        List<String> result = JBangSettings.parseArgs("deps=org.foo:bar:1.0,deps=org.baz:qux:2.0,fresh=");
        Assert.assertEquals(result, List.of("--deps=org.foo:bar:1.0,org.baz:qux:2.0", "--fresh"));
    }

    @Test
    public void shouldPreserveArgOrder() {
        List<String> result = JBangSettings.parseArgs("fresh=,deps=org.foo:bar:1.0,verbose=");
        Assert.assertEquals(result, List.of("--fresh", "--deps=org.foo:bar:1.0", "--verbose"));
    }

    @Test
    public void shouldTrimWhitespace() {
        List<String> result = JBangSettings.parseArgs(" deps = org.foo:bar:1.0 , fresh = ");
        Assert.assertEquals(result, List.of("--deps=org.foo:bar:1.0", "--fresh"));
    }
}
