package com.consol.citrus.ws.integration;

import com.consol.citrus.actions.SendMessageAction;
import com.consol.citrus.annotations.CitrusXmlTest;
import com.consol.citrus.testng.spring.TestNGCitrusSpringSupport;
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
