/*
 * Copyright 2006-2012 the original author or authors.
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

package com.consol.citrus.ssh.config;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import com.consol.citrus.ssh.CitrusSshServer;

/**
 * Parser for the configuration of an SSH server
 * 
 * @author Roland Huss
 */
public class SshServerParser extends AbstractSshParser {

    /** {@inheritDoc} */
    protected void parseExtra(BeanDefinitionBuilder builder,
                                                Element element,
                                                ParserContext pParserContext) {
        // TODO: Allow an inner bean for specifying the message handler
    }

    @Override
    /** {@inheritDoc} */
    protected String[] getAttributePropertyMapping() {
        return new String[] {
                "port","port",
                "auto-start","autoStart",
                "host-key-path","hostKeyPath",
                "user","user",
                "password","password",
                "allowed-key-path","allowedKeyPath",
        };

    }

    @Override
    protected String[] getAttributePropertyReferenceMapping() {
        return new String[] {
                "message-handler-ref","messageHandler"
        };
    }

    @Override
    /** {@inheritDoc} */
    protected Class<?> getBeanClass() {
        return CitrusSshServer.class;
    }
}
