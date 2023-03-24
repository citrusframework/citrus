/*
 * Copyright 2006-2016 the original author or authors.
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

package org.citrusframework.ssh.config.handler;

import org.citrusframework.ssh.config.xml.SshClientParser;
import org.citrusframework.ssh.config.xml.SshServerParser;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * Namespace handler implementation for components in Citrus SSH namespace.
 * 
 * @author Roland Huss
 */
public class CitrusSshConfigNamespaceHandler extends NamespaceHandlerSupport {

    /**
     * {@inheritDoc}
     */
    public void init() {
        registerBeanDefinitionParser("server", new SshServerParser());
        registerBeanDefinitionParser("client", new SshClientParser());
    }
}
