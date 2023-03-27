package org.citrusframework.citrus.http.integration;

import jakarta.servlet.http.Cookie;

import org.citrusframework.citrus.annotations.CitrusTest;
import org.citrusframework.citrus.annotations.CitrusXmlTest;
import org.citrusframework.citrus.http.client.HttpClient;
import org.citrusframework.citrus.http.server.HttpServer;
import org.citrusframework.citrus.testng.spring.TestNGCitrusSpringSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.testng.annotations.Test;

import static org.citrusframework.citrus.http.actions.HttpActionBuilder.http;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class HttpHandleCookiesIT extends TestNGCitrusSpringSupport {

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
                .message()
                .cookie(aCookie)
                .cookie(bCookie));

        //THEN
        then(http().server(httpServer)
                .receive()
                .delete()
                .message()
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
                .message()
                .cookie(loginCookie));

        //THEN
        then(http().client(httpClient)
                .receive()
                .response()
                .message()
                .cookie(loginCookie));
    }
}
