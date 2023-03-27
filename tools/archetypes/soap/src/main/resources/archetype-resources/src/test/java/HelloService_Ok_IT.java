package ${package};

import org.testng.annotations.Test;

import org.citrusframework.citrus.annotations.CitrusXmlTest;
import org.citrusframework.citrus.testng.spring.TestNGCitrusSpringSupport;

/**
 * This is a sample Citrus integration test using SOAP client and server.
 * @author Citrus
 */
@Test
public class HelloService_Ok_IT extends TestNGCitrusSpringSupport {

    @CitrusXmlTest(name = "HelloService_Ok_IT")
    public void helloServiceOk() {}
}
