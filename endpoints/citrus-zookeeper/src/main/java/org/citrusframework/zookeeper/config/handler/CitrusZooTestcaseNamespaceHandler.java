/*
 * Copyright 2006-2015 the original author or authors.
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

package org.citrusframework.zookeeper.config.handler;

import org.citrusframework.zookeeper.command.*;
import org.citrusframework.zookeeper.config.xml.ZooExecuteActionParser;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * @author Martin Maher
 * @since 2.5
 */
public class CitrusZooTestcaseNamespaceHandler extends NamespaceHandlerSupport {

    public void init() {
        registerBeanDefinitionParser("info", new ZooExecuteActionParser(Info.class));
        registerBeanDefinitionParser("create", new ZooExecuteActionParser(Create.class));
        registerBeanDefinitionParser("delete", new ZooExecuteActionParser(Delete.class));
        registerBeanDefinitionParser("exists", new ZooExecuteActionParser(Exists.class));
        registerBeanDefinitionParser("get", new ZooExecuteActionParser(GetData.class));
        registerBeanDefinitionParser("set", new ZooExecuteActionParser(SetData.class));
        registerBeanDefinitionParser("children", new ZooExecuteActionParser(GetChildren.class));
    }
}
