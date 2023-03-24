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

package org.citrusframework.ssh.server;

import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.session.ServerSession;

/**
 * Simple user/password authenticator for comparing textually.
 *
 * @author Roland Huss
 * @since 05.09.12
 */
class SimplePasswordAuthenticator implements PasswordAuthenticator {

    private String user;
    private String password;

    /**
     * Constructor
     *
     * @param pUser user to verify against
     * @param pPassword password to check
     */
    public SimplePasswordAuthenticator(String pUser, String pPassword) {
        user = pUser;
        password = pPassword;
    }

    /**
     * {@inheritDoc}
     */
    public boolean authenticate(String pUser, String pPassword, ServerSession pSession) {
        return pUser != null && pUser.equals(user) && password.equals(pPassword);
    }
}
