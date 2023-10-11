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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.PublicKey;

import org.apache.sshd.server.auth.pubkey.PublickeyAuthenticator;
import org.apache.sshd.server.session.ServerSession;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Public key authenticator which verifies a single provided public key. The public key
 * itself must be in PEM format.
 *
 * @author Roland Huss
 * @since 05.09.12
 */
class SinglePublicKeyAuthenticator implements PublickeyAuthenticator {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(SinglePublicKeyAuthenticator.class);

    private final PublicKey allowedKey;
    private final String user;

    /**
     * Constructor
     *
     * @param username user to verify against
     * @param publicKeyPath path to a single public key PEM, either in the filesystem or, if prefixed
     *                       with 'classpath:' taken from the classpath.
     */
    public SinglePublicKeyAuthenticator(String username, String publicKeyPath) {
        this.user = username;
        try (InputStream is = FileUtils.getFileResource(publicKeyPath).getInputStream()){
            if (is == null) {
                throw new CitrusRuntimeException(String.format("Failed to read public key - no public key found at %s", publicKeyPath));
            }
            allowedKey = readKey(is);
            if (allowedKey == null) {
                throw new CitrusRuntimeException("No public key found at " + publicKeyPath + ", although the file/resource exists. " +
                                                 "It is probably not in a PEM form or contains more than only a public key.");
            }
        } catch (IOException e) {
            throw new CitrusRuntimeException(String.format("Failed to read public key file at %s", publicKeyPath),e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean authenticate(String pUser, PublicKey pKey, ServerSession pSession) {
        return user != null && user.equals(pUser) && allowedKey.equals(pKey);
    }

    /**
     * Read the key with bouncycastle's PEM tools
     * @param is
     * @return
     */
    private PublicKey readKey(InputStream is) {
        try (InputStreamReader isr = new InputStreamReader(is);
             PEMParser r = new PEMParser(isr)) {
            Object o = r.readObject();
            if (o instanceof PEMKeyPair) {
                PEMKeyPair keyPair = (PEMKeyPair) o;
                if (keyPair.getPublicKeyInfo() != null &&
                        keyPair.getPublicKeyInfo().getEncoded().length > 0) {
                    return BouncyCastleProvider.getPublicKey(keyPair.getPublicKeyInfo());
                }
            } else if (o instanceof SubjectPublicKeyInfo) {
                return BouncyCastleProvider.getPublicKey((SubjectPublicKeyInfo) o);
            }
        } catch (IOException e) {
            // Ignoring, returning null
            logger.warn("Failed to get key from PEM file", e);
        }

        return null;
    }

}
