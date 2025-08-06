package ${package};

import org.testng.annotations.Test;

import org.citrusframework.TestActionSupport;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;

/**
 * This is a sample Java DSL Citrus integration test.
 *
 */
@Test
public class SampleJavaIT extends TestNGCitrusSpringSupport implements TestActionSupport {

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
