package ${package};

import org.testng.annotations.Test;

import org.citrusframework.citrus.annotations.CitrusXmlTest;
import org.citrusframework.citrus.testng.spring.TestNGCitrusSpringSupport;

/**
 * This is a sample Citrus integration test for loading XML syntax test case.
 *
 * @author Citrus
 */
@Test
public class SampleXmlIT extends TestNGCitrusSpringSupport {

    @CitrusXmlTest(name = "SampleXmlIT")
    public void sampleXml() {}
}
