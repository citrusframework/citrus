package ${package};

import org.testng.annotations.Test;

import org.citrusframework.citrus.annotations.CitrusTest;
import org.citrusframework.citrus.testng.spring.TestNGCitrusSpringSupport;

import static org.citrusframework.citrus.actions.EchoAction.Builder.echo;

/**
 * This is a sample Java DSL Citrus integration test.
 *
 * @author Citrus
 */
@Test
public class SampleJavaIT extends TestNGCitrusSpringSupport {

    @CitrusTest
    public void echoToday() {
        variable("now", "citrus:currentDate()");

        run(echo("Today is: ${now}"));
    }

    @CitrusTest(name = "SampleJavaTest.sayHello")
    public void sayHello() {
        run(echo("Hello Citrus!"));
    }
}
