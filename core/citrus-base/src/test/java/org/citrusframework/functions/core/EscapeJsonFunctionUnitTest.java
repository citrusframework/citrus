package org.citrusframework.functions.core;

import java.util.List;

import org.citrusframework.context.TestContext;
import org.citrusframework.functions.Function;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertThrows;

public class EscapeJsonFunctionUnitTest {

    @Mock
    private TestContext context;

    private EscapeJsonFunction fixture;

    @BeforeTest
    public void beforeTestSetup() {
        fixture = new EscapeJsonFunction();
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void isCitrusFunction() {
        assertThat(fixture)
                .isInstanceOf(Function.class);
    }

    @DataProvider(name = "testChangeParameterProvider")
    public Object[][] testChangeParameter() {
        return new Object[][]{
                {"{\"mySuperJson\": \"[{\"pippin\":\"nooooo\"}, {\"gandalf\":\"fly you fools\"}]\"}", "{\\\"mySuperJson\\\": \\\"[{\\\"pippin\\\":\\\"nooooo\\\"}, {\\\"gandalf\\\":\\\"fly you fools\\\"}]\\\"}"},
                {"{\"mySuperJson\": \"{\"pippin\":\"nooooo\"}\"}", "{\\\"mySuperJson\\\": \\\"{\\\"pippin\\\":\\\"nooooo\\\"}\\\"}"},
                {"{\"mySuperJson\": \"nooooo\"}", "{\\\"mySuperJson\\\": \\\"nooooo\\\"}"},
                {"[{\"mySuperJson\": \"nooooo\"},{\"mySuperJson2\": \"nooooo\"}]", "[{\\\"mySuperJson\\\": \\\"nooooo\\\"},{\\\"mySuperJson2\\\": \\\"nooooo\\\"}]"},
                {"{}", "{}"}
        };
    }

    @Test(dataProvider = "testChangeParameterProvider")
    public void testChangeParameter(String string, String expectedResult) {
        String newValue = fixture.execute(List.of(string), context);
        assertEquals(expectedResult, newValue);
    }

    @DataProvider(name = "testMalformedParameterListProvider")
    public Object[][] testMalformedParameterListProvider() {
        return new Object[][]{
                {emptyList()},
                {singletonList("")},
                {List.of("rip_bozo", "")}
        };
    }

    @Test(dataProvider = "testMalformedParameterListProvider")
    public void testMalformedParameterList(List<String> parameters) {
        assertThrows(Exception.class, () -> fixture.execute(parameters, context));
    }
}
