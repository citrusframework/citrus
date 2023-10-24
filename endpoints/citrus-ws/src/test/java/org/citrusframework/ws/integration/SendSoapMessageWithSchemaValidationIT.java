package org.citrusframework.ws.integration;

import org.citrusframework.annotations.CitrusTestSource;
import org.citrusframework.common.TestLoader;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.testng.annotations.Test;

/**
 * @author Thorsten Schlathoelter
 * @since 3.1.3
 */
public class SendSoapMessageWithSchemaValidationIT extends TestNGCitrusSpringSupport {

    @Test
    @CitrusTestSource(type = TestLoader.SPRING)
    public void SendSoapMessageWithSchemaValidationIT() {}

}
