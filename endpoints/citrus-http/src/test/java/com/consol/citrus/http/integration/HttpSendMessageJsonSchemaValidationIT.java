package com.consol.citrus.http.integration;

import com.consol.citrus.actions.SendMessageAction;
import com.consol.citrus.annotations.CitrusXmlTest;
import com.consol.citrus.testng.spring.TestNGCitrusSpringSupport;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test
public class HttpSendMessageJsonSchemaValidationIT extends TestNGCitrusSpringSupport {

    @CitrusXmlTest(name = "HttpSendMessageJsonSchemaValidationIT")
    public void testHttpSendMessageJsonSchemaValidationIT() {}

}
