package com.consol.citrus.context;

import com.consol.citrus.annotations.CitrusXmlTest;
import com.consol.citrus.testng.AbstractTestNGCitrusTest;
import com.consol.citrus.testng.CitrusParameters;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class DataProviderAvailableInVariableBlockTest extends AbstractTestNGCitrusTest {

    @DataProvider(name = "someDataProvider")
    public Object[][] someDataProvider() {
        return new Object[][]{
                new Object[]{"some_value"},
        };
    }

    @Test(dataProvider = "someDataProvider")
    @CitrusParameters({"someVariable"})
    @CitrusXmlTest(name = "DataProviderAvailableInVariableBlockTest")
    @SuppressWarnings("unused")
    public void test(String someVariable) {
    }
}
