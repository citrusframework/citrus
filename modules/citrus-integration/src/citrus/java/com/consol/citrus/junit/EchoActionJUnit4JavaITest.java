package com.consol.citrus.junit;

import com.consol.citrus.dsl.JUnit4CitrusTestBuilder;
import org.junit.Test;

/**
 * @author Christoph Deppisch
 */
public class EchoActionJUnit4JavaITest extends JUnit4CitrusTestBuilder {

    @Override
    protected void configure() {
        variable("time", "citrus:currentDate()");

        echo("Hello Citrus!");

        echo("CurrentTime is: ${time}");
    }

    @Test
    public void doExecute() {
        executeTest();
    }
}
