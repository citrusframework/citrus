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

package org.citrusframework.groovy.yaml;

import org.citrusframework.TestActionBuilder;
import org.citrusframework.script.GroovyAction;

public class Groovy implements TestActionBuilder<GroovyAction> {

    private final GroovyAction.Builder builder = new GroovyAction.Builder();

    public void setScript(String value) {
        if (value.length() > 0) {
            builder.script(value);
        }
    }

    public void setFile(String file) {
        builder.scriptResourcePath(file);
    }

    public void setScriptTemplate(String template) {
        builder.template(template);
    }

    public void setUseScriptTemplate(boolean enabled) {
        builder.useScriptTemplate(enabled);
    }

    @Override
    public GroovyAction build() {
        return builder.build();
    }
}
