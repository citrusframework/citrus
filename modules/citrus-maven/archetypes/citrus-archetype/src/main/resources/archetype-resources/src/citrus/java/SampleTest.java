package ${package};

import org.testng.annotations.Test;

import com.consol.citrus.annotations.CitrusXmlTest;
import com.consol.citrus.testng.AbstractTestNGCitrusTest;

/**
 * This is a sample Citrus integration test
 *
 * @author Unknown
 */
@Test
public class SampleXmlTest extends AbstractTestNGCitrusTest {

    @CitrusXmlTest
    public void sampleXmlTest() {}
}
