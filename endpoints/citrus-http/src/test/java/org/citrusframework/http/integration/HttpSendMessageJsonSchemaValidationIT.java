package org.citrusframework.http.integration;

import org.citrusframework.annotations.CitrusTestSource;
import org.citrusframework.common.TestLoader;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.testng.annotations.Test;

@Test
public class HttpSendMessageJsonSchemaValidationIT extends TestNGCitrusSpringSupport {

    @CitrusTestSource(type = TestLoader.SPRING, name = "HttpSendMessageJsonSchemaValidationIT")
    public void testHttpSendMessageJsonSchemaValidationIT() {}

}
