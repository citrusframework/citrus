package com.consol.citrus.javadsl;

import com.consol.citrus.dsl.TestNGCitrusTestBuilder;
import org.testng.ITestContext;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class EchoActionConfigureITest extends TestNGCitrusTestBuilder {

    @Override
    protected void configure() {
        echo("Configure method call test");
    }

    @Test
    public void echoActionConfigureITest(ITestContext testContext) {
        executeTest(testContext);
    }
}
