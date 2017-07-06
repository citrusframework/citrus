package works.integration;

import com.consol.citrus.annotations.CitrusXmlTest;
import com.consol.citrus.testng.AbstractTestNGCitrusTest;
import org.testng.annotations.Test;

/**
 * Created by vihar.naik on 06-Jun-17.
 */
@Test
public class XMLTest extends AbstractTestNGCitrusTest{

    @CitrusXmlTest(name = "MyFirstTest", packageName = "works.integration")
    public void test1(){
    }
}
