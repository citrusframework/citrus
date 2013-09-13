package ${package};

import org.testng.annotations.Test;

import com.consol.citrus.dsl.annotations.CitrusTest;
import com.consol.citrus.dsl.TestNGCitrusTestBuilder;

/**
 * This is a sample Citrus integration test for loading XML syntax test case.
 *
 * @author Citrus
 */
@Test
public class SampleJavaTest extends TestNGCitrusTestBuilder {

    @CitrusTest(name = "SampleJavaTest")
    public void sampleTest() {
        variable("now", "citrus:currentDate()");

        echo("Today is: ${now}");
    }
}
