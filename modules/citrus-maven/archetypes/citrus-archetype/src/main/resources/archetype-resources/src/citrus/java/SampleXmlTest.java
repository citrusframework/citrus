package ${package};

import org.testng.annotations.Test;

import com.consol.citrus.annotations.CitrusXmlTest;
import com.consol.citrus.testng.AbstractTestNGCitrusTest;

/**
 * This is a sample Citrus integration test for loading XML syntax test case.
 *
 * @author Citrus
 */
@Test
public class SampleXmlTest extends AbstractTestNGCitrusTest {

    @CitrusXmlTest(name = "SampleXmlTest")
    public void sampleXmlTest() {}
}
