package org.citrusframework.ws.integration;

import org.citrusframework.actions.SendMessageAction;
import org.citrusframework.annotations.CitrusXmlTest;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author Thorsten Schlathoelter
 * @since 3.1.3
 */
public class SendSoapMessageWithSchemaValidationIT extends TestNGCitrusSpringSupport {

    @Test
    @CitrusXmlTest
    public void SendSoapMessageWithSchemaValidationIT() {}

}
