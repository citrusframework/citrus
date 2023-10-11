/*
 * Copyright 2021 the original author or authors.
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

package org.citrusframework.config.xml.parser;

import org.citrusframework.message.builder.script.GroovyFileResourcePayloadBuilder;
import org.citrusframework.message.builder.script.GroovyScriptPayloadBuilder;
import org.citrusframework.util.StringUtils;
import org.citrusframework.validation.builder.DefaultMessageBuilder;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

/**
 * @author Christoph Deppisch
 */
public class GroovyScriptMessageBuilderParser implements ScriptMessageBuilderParser {

    @Override
    public DefaultMessageBuilder parse(Element messageElement) {
        Element builderElement = getBuilderElement(messageElement);

        DefaultMessageBuilder scriptMessageBuilder = new DefaultMessageBuilder();
        String scriptResourcePath = builderElement.getAttribute("file");
        if (StringUtils.hasText(scriptResourcePath)) {
            if (builderElement.hasAttribute("charset")) {
                scriptMessageBuilder.setPayloadBuilder(new GroovyFileResourcePayloadBuilder(scriptResourcePath, builderElement.getAttribute("charset")));
            } else {
                scriptMessageBuilder.setPayloadBuilder(new GroovyFileResourcePayloadBuilder(scriptResourcePath));
            }
        } else {
            scriptMessageBuilder.setPayloadBuilder(new GroovyScriptPayloadBuilder(DomUtils.getTextValue(builderElement).trim()));
        }

        if (messageElement.hasAttribute("name")) {
            scriptMessageBuilder.setName(messageElement.getAttribute("name"));
        }

        return scriptMessageBuilder;
    }
}
