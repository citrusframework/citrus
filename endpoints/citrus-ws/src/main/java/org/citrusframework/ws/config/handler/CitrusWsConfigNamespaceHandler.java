/*
 * Copyright 2006-2010 the original author or authors.
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

package org.citrusframework.ws.config.handler;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

import org.citrusframework.ws.config.xml.*;

/**
 * Namespace handler for configuration components in Citrus ws namespace.
 * 
 * @author Christoph Deppisch
 */
public class CitrusWsConfigNamespaceHandler extends NamespaceHandlerSupport {

    public void init() {
        registerBeanDefinitionParser("server", new WebServiceServerParser());
        registerBeanDefinitionParser("jetty-server", new WebServiceServerParser());
        registerBeanDefinitionParser("client", new WebServiceClientParser());
    }

}
