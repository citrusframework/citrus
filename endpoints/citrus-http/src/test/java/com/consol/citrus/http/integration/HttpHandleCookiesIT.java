package com.consol.citrus.http.integration;

import javax.servlet.http.Cookie;

import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.annotations.CitrusXmlTest;
import com.consol.citrus.http.client.HttpClient;
import com.consol.citrus.http.server.HttpServer;
import com.consol.citrus.testng.TestNGCitrusSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.testng.annotations.Test;

import static com.consol.citrus.http.actions.HttpActionBuilder.http;

public class HttpHandleCookiesIT extends TestNGCitrusSupport {

    @Autowired
    @Qualifier("echoHttpClient")
    private HttpClient httpClient;

    @Autowired
    @Qualifier("echoHttpServer")
    private HttpServer httpServer;

    @Test
    @CitrusXmlTest(name = "HttpCookiesIT")
    public void testCookies() {}

    @Test
    @CitrusTest
    public void testClientSideCookie() {

        //GIVEN
        final Cookie aCookie = new Cookie("a", "a");
        final Cookie bCookie = new Cookie("b", "b");

        //WHEN
        when(http().client(httpClient)
                .send()
                .delete()
                .cookie(aCookie)
                .cookie(bCookie));

        //THEN
        then(http().server(httpServer)
                .receive()
                .delete()
                .cookie(aCookie)
                .cookie(bCookie));
    }

    @Test
    @CitrusTest
    public void testServerSideCookie(){

        //GIVEN
        final Cookie loginCookie = new Cookie("JSESSIONID", "asd");

        given(http().client(httpClient)
                .send()
                .get()
                .fork(true));

        given(http().server(httpServer)
                .receive()
                .get());

        //WHEN
        when(http().server(httpServer)
                .respond()
                .cookie(loginCookie));

        //THEN
        then(http().client(httpClient)
                .receive()
                .response()
                .cookie(loginCookie));
    }
}
