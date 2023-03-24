/*
 * Copyright 2006-2018 the original author or authors.
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

package org.citrusframework.ftp.config.handler;

import org.citrusframework.ftp.config.xml.SftpClientParser;
import org.citrusframework.ftp.config.xml.SftpServerParser;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * @author Christoph Deppisch
 * @since 2.7.5
 */
public class CitrusSftpConfigNamespaceHandler extends NamespaceHandlerSupport {

    /**
     * @see org.springframework.beans.factory.xml.NamespaceHandler#init()
     */
    public void init() {
        registerBeanDefinitionParser("client", new SftpClientParser());
        registerBeanDefinitionParser("server", new SftpServerParser());
    }
}
