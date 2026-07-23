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

package org.citrusframework.util;

import org.citrusframework.variable.VariableUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

public class VariableUtilsTest {

    @Test
    public void testCutOffVariablesPrefixSuffix() {
        Assert.assertEquals(VariableUtils.cutOffVariablesPrefix(""), "");
        Assert.assertEquals(VariableUtils.cutOffVariablesPrefix("something_else"), "something_else");
        Assert.assertEquals(VariableUtils.cutOffVariablesPrefix("${}"), "");
        Assert.assertEquals(VariableUtils.cutOffVariablesPrefix("${variable}"), "variable");
        Assert.assertEquals(VariableUtils.cutOffVariablesPrefix("${incomplete"), "${incomplete");
        Assert.assertEquals(VariableUtils.cutOffVariablesPrefix("{incomplete}"), "{incomplete}");
    }

    @Test
    public void testCutOffSingleQuotes() {
        Assert.assertEquals(VariableUtils.cutOffSingleQuotes(""), "");
        Assert.assertEquals(VariableUtils.cutOffSingleQuotes("something_else"), "something_else");
        Assert.assertEquals(VariableUtils.cutOffSingleQuotes("'"), "'");
        Assert.assertEquals(VariableUtils.cutOffSingleQuotes("''"), "");
        Assert.assertEquals(VariableUtils.cutOffSingleQuotes("'variable'"), "variable");
        Assert.assertEquals(VariableUtils.cutOffSingleQuotes("'incomplete"), "'incomplete");
        Assert.assertEquals(VariableUtils.cutOffSingleQuotes("incomplete'"), "incomplete'");
    }

    @Test
    public void testCutOffDoubleQuotes() {
        Assert.assertEquals(VariableUtils.cutOffDoubleQuotes(""), "");
        Assert.assertEquals(VariableUtils.cutOffDoubleQuotes("something_else"), "something_else");
        Assert.assertEquals(VariableUtils.cutOffDoubleQuotes("\""), "\"");
        Assert.assertEquals(VariableUtils.cutOffDoubleQuotes("\"\""), "");
        Assert.assertEquals(VariableUtils.cutOffDoubleQuotes("\"variable\""), "variable");
        Assert.assertEquals(VariableUtils.cutOffDoubleQuotes("\"incomplete"), "\"incomplete");
        Assert.assertEquals(VariableUtils.cutOffDoubleQuotes("incomplete\""), "incomplete\"");
    }

    @Test
    public void testCutOffVariablesEscaping() {
        Assert.assertEquals(VariableUtils.cutOffVariablesEscaping(""), "");
        Assert.assertEquals(VariableUtils.cutOffVariablesEscaping("something_else"), "something_else");
        Assert.assertEquals(VariableUtils.cutOffVariablesEscaping("////"), "");
        Assert.assertEquals(VariableUtils.cutOffVariablesEscaping("//variable//"), "variable");
        Assert.assertEquals(VariableUtils.cutOffDoubleQuotes("//incomplete"), "//incomplete");
        Assert.assertEquals(VariableUtils.cutOffDoubleQuotes("incomplete//"), "incomplete//");
    }
}
