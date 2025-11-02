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
package org.citrusframework.yaml.actions.script;

import org.citrusframework.yaml.SchemaProperty;

public class ScriptDefinitionType {

    protected String value;
    protected String type;
    protected String file;
    protected String charset;

    @SchemaProperty(description = "The script content.")
    public void setContent(String script) {
        this.value = script;
    }

    public String getContent() {
        return value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getType() {
        return type;
    }

    @SchemaProperty(description = "The script type.", defaultValue = "groovy")
    public void setType(String value) {
        this.type = value;
    }

    public String getFile() {
        return file;
    }

    @SchemaProperty(description = "The script content loaded as a file resource.")
    public void setFile(String value) {
        this.file = value;
    }

    @SchemaProperty(advanced = true, description = "The charset used to load the file resource.")
    public void setCharset(String charset) {
        this.charset = charset;
    }

    public String getCharset() {
        return charset;
    }
}
