package com.consol.citrus.http;

import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.testng.TestNGCitrusTestRunner;
import com.consol.citrus.http.client.HttpClient;
import com.consol.citrus.http.server.HttpServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.testng.annotations.Test;

import javax.servlet.http.Cookie;

public class HttpCookieIT extends TestNGCitrusTestRunner{

    @Autowired
    @Qualifier("echoHttpClient")
    private HttpClient httpClient;

    @Autowired
    @Qualifier("echoHttpServer")
    private HttpServer httpServer;

    @Test
    @CitrusTest
    public void testCookiesAreTransmittedToServer() {

        //GIVEN
        final Cookie aCookie = new Cookie("a", "a");
        final Cookie bCookie = new Cookie("b", "b");

        //WHEN
        http(http -> http.client(httpClient)
                .send()
                .delete()
                .cookie(aCookie)
                .cookie(bCookie));

        //THEN
        http(http -> http.server(httpServer)
                .receive()
                .delete()
                .cookie(aCookie)
                .cookie(bCookie));
    }

    @Test
    @CitrusTest
    public void testClientProcessesReceivedCookiesCorrectly(){

        //GIVEN
        final Cookie loginCookie = new Cookie("JSESSIONID", "asd");

        http(http -> http
                .client(httpClient)
                .send()
                .get()
                .fork(true));

        http(http -> http
                .server(httpServer)
                .receive()
                .get());


        //WHEN
        http(http -> http
                .server(httpServer)
                .respond()
                .cookie(loginCookie));

        //THEN
        http(http -> http.client(httpClient)
                .receive()
                .response()
                .cookie(loginCookie));
    }
}
