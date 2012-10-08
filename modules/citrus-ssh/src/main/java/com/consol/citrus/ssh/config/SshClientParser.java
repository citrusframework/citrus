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

import com.consol.citrus.ssh.CitrusSshClient;

/**
 * Parse for SSH-client configuration
 *
 * @author Roland Huss
 * @since 1.3
 */
public class SshClientParser extends AbstractSshParser {

    @Override
    protected String[] getAttributePropertyMapping() {
        return new String[] {
                "host","host",
                "port","port",
                "private-key-path","privateKeyPath",
                "private-key-password","privateKeyPassword",
                "strict-host-checking","strictHostChecking",
                "known-hosts-path","knownHosts",
                "command-timeout","commandTimeout",
                "connection-timeout","connectionTimeout",
                "user","user",
                "password","password"
        };
    }

    @Override
    protected String[] getAttributePropertyReferenceMapping() {
        return new String[] {
                "actor","actor",
                "reply-handler","replyMessageHandler"
        };
    }

    @Override
    protected Class<?> getBeanClass() {
        return CitrusSshClient.class;
    }
}
