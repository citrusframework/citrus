package org.citrusframework.http.integration;

import jakarta.servlet.http.Cookie;

import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.annotations.CitrusTestSource;
import org.citrusframework.common.TestLoader;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.http.server.HttpServer;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.testng.annotations.Test;

import static org.citrusframework.http.actions.HttpActionBuilder.http;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class HttpHandleCookiesIT extends TestNGCitrusSpringSupport {

    @Autowired
    @Qualifier("echoHttpClient")
    private HttpClient httpClient;

    @Autowired
    @Qualifier("echoHttpServer")
    private HttpServer httpServer;

    @Test
    @CitrusTestSource(type = TestLoader.SPRING, name = "HttpCookiesIT")
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
