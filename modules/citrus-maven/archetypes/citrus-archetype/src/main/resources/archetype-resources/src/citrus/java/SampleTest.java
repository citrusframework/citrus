package ${package};

import org.testng.ITestContext;
import org.testng.annotations.Test;

import com.consol.citrus.testng.AbstractTestNGCitrusTest;

/**
 * This is a sample Citrus integration test
 *
 * @author Unknown
 */
public class SampleTest extends AbstractTestNGCitrusTest {
    @Test
    public void sampleTest(ITestContext testContext) {
        executeTest(testContext);
    }
}
