package ${package};

import org.testng.annotations.Test;

import org.citrusframework.annotations.CitrusTestSource;
import org.citrusframework.common.TestLoader;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;

/**
 * This is a sample Citrus integration test using SOAP client and server.
 * @author Citrus
 */
@Test
public class HelloService_Ok_IT extends TestNGCitrusSpringSupport {

    @CitrusTestSource(type = TestLoader.SPRING, name = "HelloService_Ok_IT")
    public void helloServiceOk() {}
}
