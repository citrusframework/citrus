package com.consol.citrus.ssh;

import java.io.*;
import java.security.KeyPair;
import java.security.PublicKey;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import org.apache.sshd.common.KeyPairProvider;
import org.apache.sshd.common.keyprovider.*;
import org.apache.sshd.common.util.IoUtils;
import org.apache.sshd.server.PublickeyAuthenticator;
import org.apache.sshd.server.session.ServerSession;
import org.bouncycastle.openssl.PEMReader;

/**
 * Public key authenticator which verifies a single provided public key. The public key
 * itself must be in PEM format.
 *
 * @author roland
 * @since 05.09.12
 */
class SinglePublicKeyAuthenticator implements PublickeyAuthenticator {

    public static final String CLASSPATH_PREFIX = "classpath:";
    private PublicKey allowedKey;
    private String user;

    /**
     * Constructor
     *
     * @param pUser user to verify against
     * @param pPublicKeyPath path to a single public key PEM, either in the filesystem or, if prefixed
     *                       with 'classpath:' taken from the classpath.
     */
    SinglePublicKeyAuthenticator(String pUser, String pPublicKeyPath) {
        user = pUser;
        InputStream is = null;
        try {
            if (pPublicKeyPath.startsWith(CLASSPATH_PREFIX)) {
                String resource = pPublicKeyPath.substring(CLASSPATH_PREFIX.length());
                is = getClass().getClassLoader().getResourceAsStream(resource);
                if (is == null) {
                    throw new CitrusRuntimeException("No key resource found at classpath at " + resource);
                }
            } else {
                is = new FileInputStream(pPublicKeyPath);
            }
            allowedKey = readKey(is);
            if (allowedKey == null) {
                throw new CitrusRuntimeException("No public key found at " + pPublicKeyPath + ", although the file/resource exists. " +
                                                 "It is probably not in a PEM form or contains more than only a public key.");
            }
        } catch (FileNotFoundException e) {
            throw new CitrusRuntimeException("public key file does not exist at " + pPublicKeyPath);
        } finally {
            IoUtils.closeQuietly(is);
        }
    }

    /** {@inheritDoc} */
    public boolean authenticate(String pUser, PublicKey pKey, ServerSession pSession) {
        return user != null && user.equals(pUser) && allowedKey.equals(pKey);
    }

    // Read the key with bouncycastle's PEM tools
    private PublicKey readKey(InputStream is) {
        InputStreamReader isr = new InputStreamReader(is);
        PEMReader r = new PEMReader(isr);
        try {
            Object o = r.readObject();
            if (o instanceof PublicKey) {
                return (PublicKey) o;
            }
        } catch (IOException e) {
            // Ignoring, returning null
        } finally {
            IoUtils.closeQuietly(isr,r);
        }
        return null;
    }

}
