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

package org.citrusframework.base.util;

import javax.script.ScriptException;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.variable.VariableUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

public class VariableUtilsTest {

    private final String validGroovyScript = "a = 1";
    private final String validScriptEngine = "groovy";

    /**
     * Test for correct return with valid script
     */
    @Test
    public void testValidScript() {
        String result = VariableUtils.getValueFromScript(validScriptEngine, validGroovyScript);
        String groovyScriptResult = "1";
        Assert.assertEquals(result, groovyScriptResult);
    }

    /**
     * Test for correct exception with invalid script
     */
    @Test
    public void testInvalidScript() {
        try {
            String invalidGroovyScript = "a";
            VariableUtils.getValueFromScript(validScriptEngine, invalidGroovyScript);
        } catch (CitrusRuntimeException e) {
            Assert.assertTrue(e.getCause() instanceof ScriptException);
            return;
        }

        Assert.fail("Missing CitrusRuntimeException because of invalid groovy script");
    }

    /**
     * Test for correct exception with invalid script engine
     */
    @Test
    public void testInvalidScriptEngine() {
        String invalidScriptEngine = "invalidScriptEngine";
        try {
            VariableUtils.getValueFromScript(invalidScriptEngine, validGroovyScript);
        } catch (CitrusRuntimeException e) {
            Assert.assertTrue(e.getMessage().contains(invalidScriptEngine));
            return;
        }

        Assert.fail("Missing CitrusRuntimeException because of invalid script engine");
    }
}
