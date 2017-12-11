package com.consol.citrus.http.integration;

import com.consol.citrus.annotations.CitrusXmlTest;
import com.consol.citrus.testng.AbstractTestNGCitrusTest;
import org.testng.annotations.Test;

@Test
public class HttpJsonSchemaValidationIT extends AbstractTestNGCitrusTest {

    @CitrusXmlTest(name = "HttpJsonSchemaValidationIT")
    public void testHttpClientJsonValidation() {}
}