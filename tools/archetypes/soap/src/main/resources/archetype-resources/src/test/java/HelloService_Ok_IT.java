package ${package};

import org.testng.annotations.Test;

import com.consol.citrus.annotations.CitrusXmlTest;
import com.consol.citrus.testng.TestNGCitrusSupport;

/**
 * This is a sample Citrus integration test using SOAP client and server.
 * @author Citrus
 */
@Test
public class HelloService_Ok_IT extends TestNGCitrusSupport {

    @CitrusXmlTest(name = "HelloService_Ok_IT")
    public void helloServiceOk() {}
}
