package com.consol.citrus.javadsl.design;

import com.consol.citrus.dsl.testng.TestNGCitrusTestDesigner;
import org.testng.ITestContext;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class EchoActionConfigureITest extends TestNGCitrusTestDesigner {

    @Override
    protected void configure() {
        echo("Configure method call test");
    }

    @Test
    public void echoAction(ITestContext testContext) {
        executeTest(testContext);
    }
}
