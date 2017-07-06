package works.integration;

import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.annotations.JIRAMetaInfo;
import com.consol.citrus.annotations.TestMetaInfo;
import com.consol.citrus.dsl.testng.TestNGCitrusTestDesigner;
import org.testng.annotations.Test;

/**
 * Created by sudeep.r on 21/11/2016.
 */

@Test
public class SampleTest1 extends TestNGCitrusTestDesigner {

    @CitrusTest(name = "Test_1")
    @TestMetaInfo(  description = "This is Test 1",
                    requirementID = "Requirement 101",
                    scenario = "Run Test_1, SampleTestMethodRunner",
                    author = "Sunny" )
    @JIRAMetaInfo(project = "TPKO")
    public void run() {
        System.out.println("Done!");
    }

    @CitrusTest(name = "Test_2")
    @TestMetaInfo(  description = "This is Test 2",
                    requirementID = "Requirement 201",
                    scenario = "Run Test_2",
                    author = "Steve" )
    @JIRAMetaInfo(project = "TPKO")
    public void run2() {

        description("hello run2");
        System.out.println("Done!");
        fail("87877");
    }


}
