package org.citrusframework.citrus.http.integration;

import org.citrusframework.citrus.annotations.CitrusXmlTest;
import org.citrusframework.citrus.testng.spring.TestNGCitrusSpringSupport;
import org.testng.annotations.Test;

@Test
public class HttpJsonSchemaValidationIT extends TestNGCitrusSpringSupport {

    @CitrusXmlTest(name = "HttpJsonSchemaValidationIT")
    public void testHttpClientJsonValidation() {}
}
