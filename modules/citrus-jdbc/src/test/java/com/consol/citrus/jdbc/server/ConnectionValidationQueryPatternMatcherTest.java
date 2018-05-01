package com.consol.citrus.jdbc.server;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class ConnectionValidationQueryPatternMatcherTest {

    @DataProvider
    public Object[][] matchDP() {
        return new Object[][] {
                {"Select 1", true},
                {"Select 1 from", false},
                {"SELECT USER", true},
                {"SELECT USER from DUAL", true},
                {"SELECT 1 from DUAL", true},
                {"SELECT USER FROM SYSIBM.SYSDUMMY1", true},
                {"SELECT 1 FROM SYSIBM.SYSDUMMY1", true},
                {"SELECT 1 FROM SYSIBM.SYSDUMMY1 where", false},
        };
    }

    @Test(dataProvider = "matchDP")
    public void match(String query, boolean isMatching) {
        ConnectionValidationQueryPatternMatcher matcher = new ConnectionValidationQueryPatternMatcher();
        assertEquals(matcher.match(query), isMatching);
    }

    @DataProvider
    public Object[][] matchUsingSystemPropertyDP() {
        return new Object[][] {
                {"Select 1", true},
                {"Select 1 from", false},
                {"SELECT USER", false},
                {"SELECT 1", true},
        };
    }

    @Test(dataProvider = "matchUsingSystemPropertyDP")
    public void matchUsingSystemProperty(String query, boolean isMatching) {
        System.setProperty(ConnectionValidationQueryPatternMatcher.CONNECTION_VALIDATION_QUERIES_PROPERTY, "select 1;;");
        ConnectionValidationQueryPatternMatcher matcher = new ConnectionValidationQueryPatternMatcher();
        assertEquals(matcher.match(query), isMatching);
    }

}
