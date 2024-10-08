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

package org.citrusframework.groovy.dsl.configuration;

import org.citrusframework.Citrus;
import org.citrusframework.context.TestContext;
import org.citrusframework.groovy.dsl.GroovyShellUtils;
import org.codehaus.groovy.control.customizers.ImportCustomizer;

public class ConfigurationScript {

    private final Citrus citrus;

    private final String basePath;

    private final String script;

    public ConfigurationScript(String script, Citrus citrus) {
        this(script, citrus, "");
    }

    public ConfigurationScript(String script, Citrus citrus, String basePath) {
        this.script = script;
        this.citrus = citrus;
        this.basePath = basePath;
    }

    public void execute(TestContext context) {
        ImportCustomizer ic = new ImportCustomizer();
        GroovyShellUtils.run(ic, new ContextConfiguration(citrus, context, basePath),
                context.replaceDynamicContentInString(script), citrus, context);
    }
}
