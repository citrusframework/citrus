package com.consol.citrus.ssh;

import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * @author roland
 * @since 05.09.12
 */
public class SimplePasswordAuthenticatorTest {

    @Test
    public void simple() {
        SimplePasswordAuthenticator auth = new SimplePasswordAuthenticator("roland","secret");
        assertTrue(auth.authenticate("roland","secret",null));
        assertFalse(auth.authenticate("guenther","uebel",null));

    }
}
