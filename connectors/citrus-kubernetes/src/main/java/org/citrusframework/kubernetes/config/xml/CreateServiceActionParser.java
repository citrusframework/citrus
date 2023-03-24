/*
 * Copyright 2006-2017 the original author or authors.
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

package org.citrusframework.kubernetes.config.xml;

import org.citrusframework.kubernetes.command.CreateService;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

/**
 * @author Christoph Deppisch
 * @since 2.7
 */
public class CreateServiceActionParser extends KubernetesExecuteActionParser<CreateService> {

    /**
     * Constructor using kubernetes command.
     */
    public CreateServiceActionParser() {
        super(CreateService.class);
    }

    @Override
    protected CreateService parseCommand(CreateService command, Element element, ParserContext parserContext) {
        Element templateElement = DomUtils.getChildElementByTagName(element, "template");
        if (templateElement != null) {
            command.setTemplate(templateElement.getAttribute("file"));
        }

        Element specElement = DomUtils.getChildElementByTagName(element, "spec");
        if (specElement != null) {
            Element selectorElement = DomUtils.getChildElementByTagName(specElement, "selector");
            if (selectorElement != null) {
                command.setSelector(selectorElement.getAttribute("label"));
            }

            Element portsElement = DomUtils.getChildElementByTagName(specElement, "ports");
            if (portsElement != null) {
                if (portsElement.hasAttribute("protocol")) {
                    command.setProtocol(portsElement.getAttribute("protocol"));
                }

                if (portsElement.hasAttribute("port")) {
                    command.setPort(portsElement.getAttribute("port"));
                }

                if (portsElement.hasAttribute("target-port")) {
                    command.setTargetPort(portsElement.getAttribute("target-port"));
                }

                if (portsElement.hasAttribute("node-port")) {
                    command.setNodePort(portsElement.getAttribute("node-port"));
                }
            }
        }

        return command;
    }
}
