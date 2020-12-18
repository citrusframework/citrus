package com.consol.citrus.http.integration;

import com.consol.citrus.annotations.CitrusXmlTest;
import com.consol.citrus.testng.spring.TestNGCitrusSpringSupport;
import org.testng.annotations.Test;

@Test
public class HttpJsonSchemaValidationIT extends TestNGCitrusSpringSupport {

    @CitrusXmlTest(name = "HttpJsonSchemaValidationIT")
    public void testHttpClientJsonValidation() {}
}
