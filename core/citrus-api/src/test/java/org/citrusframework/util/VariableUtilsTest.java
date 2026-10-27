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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class VariableUtilsTest {

    @Test
    public void testCutOffVariablesPrefixSuffix() {
        Assertions.assertEquals("", VariableUtils.cutOffVariablesPrefix(""));
        Assertions.assertEquals("something_else", VariableUtils.cutOffVariablesPrefix("something_else"));
        Assertions.assertEquals("", VariableUtils.cutOffVariablesPrefix("${}"));
        Assertions.assertEquals("variable", VariableUtils.cutOffVariablesPrefix("${variable}"));
        Assertions.assertEquals("${incomplete", VariableUtils.cutOffVariablesPrefix("${incomplete"));
        Assertions.assertEquals("{incomplete}", VariableUtils.cutOffVariablesPrefix("{incomplete}"));
    }

    @Test
    public void testCutOffSingleQuotes() {
        Assertions.assertEquals("", VariableUtils.cutOffSingleQuotes(""));
        Assertions.assertEquals("something_else", VariableUtils.cutOffSingleQuotes("something_else"));
        Assertions.assertEquals("'", VariableUtils.cutOffSingleQuotes("'"));
        Assertions.assertEquals("", VariableUtils.cutOffSingleQuotes("''"));
        Assertions.assertEquals("variable", VariableUtils.cutOffSingleQuotes("'variable'"));
        Assertions.assertEquals("'incomplete", VariableUtils.cutOffSingleQuotes("'incomplete"));
        Assertions.assertEquals("incomplete'", VariableUtils.cutOffSingleQuotes("incomplete'"));
    }

    @Test
    public void testCutOffDoubleQuotes() {
        Assertions.assertEquals("", VariableUtils.cutOffDoubleQuotes(""));
        Assertions.assertEquals("something_else", VariableUtils.cutOffDoubleQuotes("something_else"));
        Assertions.assertEquals("\"", VariableUtils.cutOffDoubleQuotes("\""));
        Assertions.assertEquals("", VariableUtils.cutOffDoubleQuotes("\"\""));
        Assertions.assertEquals("variable", VariableUtils.cutOffDoubleQuotes("\"variable\""));
        Assertions.assertEquals("\"incomplete", VariableUtils.cutOffDoubleQuotes("\"incomplete"));
        Assertions.assertEquals("incomplete\"", VariableUtils.cutOffDoubleQuotes("incomplete\""));
    }

    @Test
    public void testCutOffVariablesEscaping() {
        Assertions.assertEquals("", VariableUtils.cutOffVariablesEscaping(""));
        Assertions.assertEquals("something_else", VariableUtils.cutOffVariablesEscaping("something_else"));
        Assertions.assertEquals("", VariableUtils.cutOffVariablesEscaping("////"));
        Assertions.assertEquals("variable", VariableUtils.cutOffVariablesEscaping("//variable//"));
        Assertions.assertEquals("//incomplete", VariableUtils.cutOffDoubleQuotes("//incomplete"));
        Assertions.assertEquals("incomplete//", VariableUtils.cutOffDoubleQuotes("incomplete//"));
    }
}
