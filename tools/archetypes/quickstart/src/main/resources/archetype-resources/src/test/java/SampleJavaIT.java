package ${package};

import org.testng.annotations.Test;

import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.testng.TestNGCitrusTestDesigner;

/**
 * This is a sample Java DSL Citrus integration test.
 *
 * @author Citrus
 */
@Test
public class SampleJavaIT extends TestNGCitrusTestDesigner {

    @CitrusTest
    public void echoToday() {
        variable("now", "citrus:currentDate()");

        echo("Today is: ${now}");
    }

    @CitrusTest(name = "SampleJavaTest.sayHello")
    public void sayHello() {
        echo("Hello Citrus!");
    }
}
