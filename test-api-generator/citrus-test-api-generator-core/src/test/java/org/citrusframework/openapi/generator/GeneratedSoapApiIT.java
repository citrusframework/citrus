package org.citrusframework.openapi.generator;

import org.citrusframework.TestCaseRunner;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.annotations.CitrusTestSource;
import org.citrusframework.common.TestLoader;
import org.citrusframework.config.CitrusSpringConfig;
import org.citrusframework.junit.jupiter.spring.CitrusSpringExtension;
import org.citrusframework.openapi.generator.GeneratedSoapApiIT.Config;
import org.citrusframework.openapi.generator.soap.bookservice.request.BookServiceSoapApi;
import org.citrusframework.openapi.generator.soap.bookservice.spring.BookServiceBeanConfiguration;
import org.citrusframework.util.SocketUtils;
import org.citrusframework.ws.client.WebServiceClient;
import org.citrusframework.ws.client.WebServiceClientBuilder;
import org.citrusframework.ws.endpoint.builder.WebServiceEndpoints;
import org.citrusframework.ws.server.WebServiceServer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import static org.citrusframework.ws.actions.SoapActionBuilder.soap;

/**
 * This integration test class for the generated TestAPI aims to comprehensively test all aspects of
 * accessing the API using both Java and XML. In addition to serving as a test suite, it also acts
 * as a reference example.
 *
 * <p>Therefore, each test is designed to be self-contained and straightforward, allowing
 * anyone reviewing the code to easily grasp the purpose and context of the test without needing to
 * rely on shared setup or utility methods.
 */

@ExtendWith(CitrusSpringExtension.class)
@SpringBootTest(classes = {BookServiceBeanConfiguration.class, CitrusSpringConfig.class, Config.class}
)
class GeneratedSoapApiIT {


    @Autowired
    private WebServiceServer soapServer;

    @Autowired
    private BookServiceSoapApi bookServiceSoapApi;

    @TestConfiguration
    public static class Config {

        private final int wsPort = SocketUtils.findAvailableTcpPort(8090);

        @Bean(name = {"bookstore.endpoint"})
        public WebServiceClient soapClient() {
            return new WebServiceClientBuilder()
                    .defaultUri("http://localhost:%d".formatted(wsPort))
                    .build();
        }

        @Bean
        public WebServiceServer soapServer() {
            return WebServiceEndpoints.soap().server()
                    .port(wsPort)
                    .timeout(5000)
                    .autoStart(true)
                    .build();
        }

    }

    @Nested
    class SoapApi {

        @Test
        @CitrusTestSource(type = TestLoader.SPRING, packageName = "org.citrusframework.openapi.generator.GeneratedApiTest", name = "withSoapTest")
        void xml() {
        }

        @Test
        void java(@CitrusResource TestCaseRunner runner) {
            String request = """
                    <AddBook>
                        <book>
                            <title>Lord of the Rings</title>
                            <author>J.R.R. Tolkien</author>
                        </book>
                    </AddBook>
                    """;
            runner.when(bookServiceSoapApi.sendAddBook().fork(true).message().body(request));

            runner.then(soap().server(soapServer)
                    .receive()
                    .message()
                    .body(request));

            String response = """
                    <AddBookResponse>
                        <book>
                            <title>Lord of the Rings</title>
                            <author>J.R.R. Tolkien</author>
                        </book>
                    </AddBookResponse>
                    """;

            runner.then(soap().server(soapServer)
                    .send()
                    .message()
                    .body(response));

            runner.then(bookServiceSoapApi.receiveAddBook()
                    .message()
                    .body(response));
        }
    }
}

