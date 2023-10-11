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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.security.PublicKey;

import org.apache.sshd.common.util.io.IoUtils;
import org.apache.sshd.common.util.security.SecurityUtils;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * @author Roland Huss
 * @since 05.09.12
 */
public class SinglePublicKeyAuthenticatorTest {

    /**
     * Default constructor.
     */
    public SinglePublicKeyAuthenticatorTest() {
        assertTrue(SecurityUtils.isBouncyCastleRegistered());
    }

    @Test
    public void withClassPath() throws IOException {
        SinglePublicKeyAuthenticator auth = new SinglePublicKeyAuthenticator("roland","classpath:org/citrusframework/ssh/allowed_test_key.pem");
        PublicKey pKey = getPublicKey("/org/citrusframework/ssh/allowed_test_key.pem");
        assertTrue(auth.authenticate("roland", pKey, null));
        assertFalse(auth.authenticate("guenther", pKey, null));

        pKey = getPublicKey("/org/citrusframework/ssh/forbidden_test_key.pem");
        assertFalse(auth.authenticate("roland", pKey, null));

        pKey = getPublicKey("/org/citrusframework/ssh/citrus.pem");
        assertFalse(auth.authenticate("citrus", pKey, null));
    }

    @Test
    public void withFile() throws IOException {
        File temp = copyToTempFile("/org/citrusframework/ssh/allowed_test_key.pem");
        SinglePublicKeyAuthenticator auth = new SinglePublicKeyAuthenticator("roland",temp.getAbsolutePath());
        PublicKey pKey = getPublicKeyFromStream(new FileInputStream(temp));
        assertTrue(auth.authenticate("roland", pKey, null));
        assertFalse(auth.authenticate("guenther",pKey,null));

        temp = copyToTempFile("/org/citrusframework/ssh/forbidden_test_key.pem");
        pKey = getPublicKeyFromStream(new FileInputStream(temp));
        assertFalse(auth.authenticate("roland", pKey, null));
    }

    @Test(expectedExceptions = CitrusRuntimeException.class, expectedExceptionsMessageRegExp = ".*org/citrusframework/ssh/private.key.*")
    public void invalidKeyFormat() {
        new SinglePublicKeyAuthenticator("roland", "classpath:org/citrusframework/ssh/private.key");
    }

    @Test(expectedExceptions = CitrusRuntimeException.class, expectedExceptionsMessageRegExp = ".*blubber\\.bla.*")
    public void notInClasspath() {
        new SinglePublicKeyAuthenticator("roland", "classpath:org/citrusframework/ssh/blubber.bla");
    }

    @Test(expectedExceptions = CitrusRuntimeException.class,expectedExceptionsMessageRegExp = ".*/no/valid/path.*")
    public void invalidFilePath() {
        new SinglePublicKeyAuthenticator("roland","/no/valid/path");
    }

    /**
     * Gets public key instance from resource.
     * @param pResource
     * @return
     * @throws IOException
     */
    private PublicKey getPublicKey(String pResource) throws IOException {
        return getPublicKeyFromStream(getClass().getResourceAsStream(pResource));
    }

    /**
     * Creates new temporary file from resource.
     * @param pResource
     * @return
     * @throws IOException
     */
    private File copyToTempFile(String pResource) throws IOException {
        File temp = File.createTempFile("citrus-ssh", "pem");
        try (InputStream in = getClass().getResourceAsStream(pResource);
             FileOutputStream fos = new FileOutputStream(temp)) {
            Assert.assertNotNull(in);
            fos.write(in.readAllBytes());
            fos.flush();
        }
        return temp;
    }

    /**
     * Create public key instance from file input stream.
     * @param is
     * @return
     * @throws IOException
     */
    private PublicKey getPublicKeyFromStream(InputStream is) {
        Reader reader = new InputStreamReader(is);
        try {
            Object o = new PEMParser(reader).readObject();
            if (o instanceof PEMKeyPair) {
                return new BouncyCastleProvider().getPublicKey(((PEMKeyPair) o).getPublicKeyInfo());
            } else if (o instanceof SubjectPublicKeyInfo) {
                return new BouncyCastleProvider().getPublicKey((SubjectPublicKeyInfo) o);
            } else {
                throw new CitrusRuntimeException("Unable to read public key");
            }
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to read public key", e);
        } finally {
            IoUtils.closeQuietly(is, reader);
        }
    }
}
