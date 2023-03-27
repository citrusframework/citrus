package org.citrusframework.citrus.ws.integration;

import org.citrusframework.citrus.actions.SendMessageAction;
import org.citrusframework.citrus.annotations.CitrusXmlTest;
import org.citrusframework.citrus.testng.spring.TestNGCitrusSpringSupport;
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
