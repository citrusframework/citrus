package com.consol.citrus.ssh;

import java.io.*;
import java.security.PublicKey;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import org.apache.sshd.common.util.SecurityUtils;
import org.bouncycastle.openssl.PEMReader;
import org.springframework.util.FileCopyUtils;
import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * @author roland
 * @since 05.09.12
 */
public class SinglePublicKeyAuthenticatorTest {

    public SinglePublicKeyAuthenticatorTest() {
        assertTrue(SecurityUtils.isBouncyCastleRegistered());
    }

    @Test
    public void withClassPath() throws IOException {
        SinglePublicKeyAuthenticator auth = new SinglePublicKeyAuthenticator("roland","classpath:com/consol/citrus/ssh/allowed_test_key.pem");
        PublicKey pKey = getPublicKey("/com/consol/citrus/ssh/allowed_test_key.pem");
        assertTrue(auth.authenticate("roland", pKey, null));
        assertFalse(auth.authenticate("guenther",pKey,null));
        pKey = getPublicKey("/com/consol/citrus/ssh/forbidden_test_key.pem");
        assertFalse(auth.authenticate("roland",pKey,null));
    }

    @Test
    public void withFile() throws IOException {
        File temp = copyToTempFile("/com/consol/citrus/ssh/allowed_test_key.pem");
        SinglePublicKeyAuthenticator auth = new SinglePublicKeyAuthenticator("roland",temp.getAbsolutePath());
        PublicKey pKey = getPublicKeyFromStream(new FileInputStream(temp));
        assertTrue(auth.authenticate("roland", pKey, null));
        assertFalse(auth.authenticate("guenther",pKey,null));

        temp = copyToTempFile("/com/consol/citrus/ssh/forbidden_test_key.pem");
        pKey = getPublicKeyFromStream(new FileInputStream(temp));
        assertFalse(auth.authenticate("roland",pKey,null));
    }

    private File copyToTempFile(String pResource) throws IOException {
        File temp = File.createTempFile("citrus-ssh-test", "pem");
        FileCopyUtils.copy(getClass().getResourceAsStream(pResource),
                           new FileOutputStream(temp));
        return temp;
    }

    @Test(expectedExceptions = CitrusRuntimeException.class,expectedExceptionsMessageRegExp = ".*No key.*")
    public void invalidKeyFormat() {
        // This is a key pair
        SinglePublicKeyAuthenticator auth = new SinglePublicKeyAuthenticator("roland","classpath:com/consol/citrus/ssh/citrus.pem");
    }

    @Test(expectedExceptions = CitrusRuntimeException.class,expectedExceptionsMessageRegExp = ".*blubber\\.bla.*")
    public void notInClasspath() {
        SinglePublicKeyAuthenticator auth = new SinglePublicKeyAuthenticator("roland","classpath:com/consol/citrus/ssh/blubber.bla");
    }

    @Test(expectedExceptions = CitrusRuntimeException.class,expectedExceptionsMessageRegExp = ".*/no/valid/path.*")
    public void invalidFilePath() {
        SinglePublicKeyAuthenticator auth = new SinglePublicKeyAuthenticator("roland","/no/valid/path");

    }
    private PublicKey getPublicKey(String pResource) throws IOException {
        return getPublicKeyFromStream(getClass().getResourceAsStream(pResource));
    }

    private PublicKey getPublicKeyFromStream(InputStream is) throws IOException {
        Reader reader = new InputStreamReader(is);
        return (PublicKey) new PEMReader(reader).readObject();
    }
}
