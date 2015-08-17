package com.consol.citrus.junit;

import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.junit.JUnit4CitrusTestDesigner;
import org.junit.Test;

/**
 * @author Christoph Deppisch
 */
public class EchoActionJUnit4JavaITest extends JUnit4CitrusTestDesigner {

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
    public void echoJavaTest() {
        variable("time", "citrus:currentDate()");

        echo("Hello Citrus!");

        echo("CurrentTime is: ${time}");
    }

    @Test
    @CitrusTest(name = "EchoSampleTest")
    public void echoTest() {
        variable("time", "citrus:currentDate()");

        echo("Hello Citrus!");

        echo("CurrentTime is: ${time}");
    }
}
