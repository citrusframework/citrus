package org.citrusframework.http.integration;

import org.citrusframework.annotations.CitrusXmlTest;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.testng.annotations.Test;

@Test
public class HttpJsonSchemaValidationIT extends TestNGCitrusSpringSupport {

    @CitrusXmlTest(name = "HttpJsonSchemaValidationIT")
    public void testHttpClientJsonValidation() {}
}
