package ${package};

import org.testng.annotations.Test;

import org.citrusframework.annotations.CitrusTestSource;
import org.citrusframework.common.TestLoader;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;

/**
 * This is a sample Citrus integration test for loading XML syntax test case.
 *
 * @author Citrus
 */
@Test
public class SampleXmlIT extends TestNGCitrusSpringSupport {

    @CitrusTestSource(type = TestLoader.SPRING, name = "SampleXmlIT")
    public void sampleXml() {}
}
