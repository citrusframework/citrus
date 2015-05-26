package com.consol.citrus.junit;

import com.consol.citrus.annotations.CitrusTest;
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

    @Test
    @CitrusTest
    public void EchoJavaTest() {
        variable("time", "citrus:currentDate()");

        echo("Hello Citrus!");

        echo("CurrentTime is: ${time}");
    }

    @Test
    @CitrusTest(name = "EchoSampleTest")
    public void EchoTest() {
        variable("time", "citrus:currentDate()");

        echo("Hello Citrus!");

        echo("CurrentTime is: ${time}");
    }
}
