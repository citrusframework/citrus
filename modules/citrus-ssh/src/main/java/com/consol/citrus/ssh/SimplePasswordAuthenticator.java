package com.consol.citrus.ssh;

import org.apache.sshd.server.PasswordAuthenticator;
import org.apache.sshd.server.session.ServerSession;

/**
 * Simple user/password authenticator for comparing textually.
 *
 * @author roland
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
