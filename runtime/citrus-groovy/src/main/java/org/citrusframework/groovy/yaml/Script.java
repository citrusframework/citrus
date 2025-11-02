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

package org.citrusframework.groovy.yaml;

import org.citrusframework.yaml.SchemaProperty;

public class Script {

    protected String file;

    protected boolean useScriptTemplate = true;

    protected String template;

    protected String value;

    public String getFile() {
        return file;
    }

    @SchemaProperty
    public void setFile(String file) {
        this.file = file;
    }

    public String getValue() {
        return value;
    }

    @SchemaProperty
    public void setValue(String value) {
        this.value = value;
    }

    @SchemaProperty
    public void setScript(String script) {
        this.value = script;
    }

    public String getTemplate() {
        return template;
    }

    @SchemaProperty
    public void setTemplate(String template) {
        this.template = template;
    }

    public boolean isUseScriptTemplate() {
        return useScriptTemplate;
    }

    @SchemaProperty
    public void setUseScriptTemplate(boolean useScriptTemplate) {
        this.useScriptTemplate = useScriptTemplate;
    }
}
