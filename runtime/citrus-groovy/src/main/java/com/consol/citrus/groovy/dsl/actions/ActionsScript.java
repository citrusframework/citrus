/*
 * Copyright 2022 the original author or authors.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.consol.citrus.groovy.dsl.actions;

import com.consol.citrus.Citrus;
import com.consol.citrus.TestActionRunner;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.groovy.dsl.GroovyShellUtils;
import org.codehaus.groovy.control.customizers.ImportCustomizer;

/**
 * @author Christoph Deppisch
 */
public class ActionsScript {

    private final String script;
    private final Citrus citrus;
    private final TestContext context;

    public ActionsScript(String script, Citrus citrus, TestContext context) {
        this.script = script;
        this.citrus = citrus;
        this.context = context;
    }

    public void execute(TestActionRunner runner) {
        ImportCustomizer ic = new ImportCustomizer();
        GroovyShellUtils.run(ic, new ActionsConfiguration(runner, context), normalize(script), citrus, context);
    }

    private String normalize(String script) {
        String normalized = GroovyShellUtils.removeComments(script);

        if (!isActionScript(normalized)) {
            return String.format("$(%s)", normalized);
        }

        return normalized;
    }

    public static boolean isActionScript(String script) {
        return script.startsWith("$actions {") || script.startsWith("$actions{") ||
                script.startsWith("$finally {") || script.startsWith("$finally{") ||
                script.startsWith("$(");
    }

}
