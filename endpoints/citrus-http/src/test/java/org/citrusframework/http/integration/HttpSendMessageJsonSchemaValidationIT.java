package org.citrusframework.http.integration;

import org.citrusframework.actions.SendMessageAction;
import org.citrusframework.annotations.CitrusXmlTest;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test
public class HttpSendMessageJsonSchemaValidationIT extends TestNGCitrusSpringSupport {

    @CitrusXmlTest(name = "HttpSendMessageJsonSchemaValidationIT")
    public void testHttpSendMessageJsonSchemaValidationIT() {}

}
