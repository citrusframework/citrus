package com.consol.citrus.ssh;

import com.consol.citrus.testng.AbstractTestNGCitrusTest;
import org.testng.ITestContext;
import org.testng.annotations.Test;

/**
 * @author roland
 * @since 05.09.12
 */
public class SshServerITest extends AbstractTestNGCitrusTest {
    
    @Test
    public void sshServerITest(ITestContext testContext) {
        executeTest(testContext);
    }
}
