package org.citrusframework.citrus.http.integration;

import org.citrusframework.citrus.actions.SendMessageAction;
import org.citrusframework.citrus.annotations.CitrusXmlTest;
import org.citrusframework.citrus.testng.spring.TestNGCitrusSpringSupport;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test
public class HttpSendMessageJsonSchemaValidationIT extends TestNGCitrusSpringSupport {

    @CitrusXmlTest(name = "HttpSendMessageJsonSchemaValidationIT")
    public void testHttpSendMessageJsonSchemaValidationIT() {}

}
