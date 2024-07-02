package org.citrusframework.openapi.generator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.citrusframework.container.Assert.Builder.assertException;
import static org.citrusframework.util.FileUtils.readToString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.citrusframework.TestCaseRunner;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.config.CitrusSpringConfig;
import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.EndpointConfiguration;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.http.client.HttpEndpointConfiguration;
import org.citrusframework.junit.jupiter.spring.CitrusSpringExtension;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.citrusframework.messaging.Producer;
import org.citrusframework.messaging.SelectiveConsumer;
import org.citrusframework.openapi.generator.GetPetByIdIT.Config;
import org.citrusframework.openapi.generator.rest.petstore.request.PetApi.GetPetByIdRequest;
import org.citrusframework.openapi.generator.rest.petstore.spring.PetStoreBeanConfiguration;
import org.citrusframework.spi.Resources;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;

@ExtendWith(CitrusSpringExtension.class)
@SpringBootTest(classes = {PetStoreBeanConfiguration.class, CitrusSpringConfig.class, Config.class})
class GetPetByIdIT {

    @Autowired
    private GetPetByIdRequest getPetByIdRequest;

    @Autowired
    @Qualifier("petStoreEndpoint")
    private HttpClient httpClient;

    private String defaultResponse;

    @BeforeEach
    public void beforeTest() throws IOException {
        defaultResponse = readToString(Resources.create(
                "org/citrusframework/openapi/generator/GeneratedApiTest/payloads/getPetByIdControlMessage1.json"),
            StandardCharsets.UTF_8) ;

        mockProducer();
        mockConsumer();
    }

    /**
     * TODO #1161 - Improve with builder pattern
     */
    @Test
    @CitrusTest
    void testByJsonPath(@CitrusResource TestCaseRunner runner) {

        // Given
        getPetByIdRequest.setPetId("1234");

        // Then
        getPetByIdRequest.setResponseStatus(HttpStatus.OK.value());
        getPetByIdRequest.setResponseReasonPhrase(HttpStatus.OK.getReasonPhrase());

        // Assert body by json path
        getPetByIdRequest.setResponseValue(Map.of("$.name", "Snoopy"));

        // When
        runner.$(getPetByIdRequest);
    }

    /**
     * TODO #1161 - Improve with builder pattern
     */
    @Test
    @CitrusTest
    void testValidationFailureByJsonPath(@CitrusResource TestCaseRunner runner) {

        // Given
        getPetByIdRequest.setPetId("1234");

        // Then
        getPetByIdRequest.setResponseStatus(HttpStatus.OK.value());
        getPetByIdRequest.setResponseReasonPhrase(HttpStatus.OK.getReasonPhrase());

        // Assert body by json path
        getPetByIdRequest.setResponseValue(Map.of("$.name", "Garfield"));

        // When
        runner.$(assertException()
            .exception(org.citrusframework.exceptions.CitrusRuntimeException.class)
            .message("Values not equal for element '$.name', expected 'Garfield' but was 'Snoopy'")
            .when(
                getPetByIdRequest
            )
        );
        // When

    }

    /**
     * TODO #1161 - Improve with builder pattern
     */
    @Test
    @CitrusTest
    void testByResource(@CitrusResource TestCaseRunner runner) {

        // Given
        getPetByIdRequest.setPetId("1234");

        // Then
        getPetByIdRequest.setResponseStatus(HttpStatus.OK.value());
        getPetByIdRequest.setResponseReasonPhrase(HttpStatus.OK.getReasonPhrase());
        // Assert body by resource
        getPetByIdRequest.setResource(
            "org/citrusframework/openapi/generator/GeneratedApiTest/payloads/getPetByIdControlMessage1.json");

        // When
        runner.$(getPetByIdRequest);
    }

    /**
     * TODO #1161 - Improve with builder pattern
     */
    @Test
    @CitrusTest
    void testValidationFailureByResource(@CitrusResource TestCaseRunner runner) {

        // Given
        getPetByIdRequest.setPetId("1234");

        // Then
        getPetByIdRequest.setResponseStatus(HttpStatus.OK.value());
        getPetByIdRequest.setResponseReasonPhrase(HttpStatus.OK.getReasonPhrase());
        // Assert body by resource
        getPetByIdRequest.setResource(
            "org/citrusframework/openapi/generator/GeneratedApiTest/payloads/getPetByIdControlMessage2.json");

        // When
        runner.$(assertException()
            .exception(org.citrusframework.exceptions.CitrusRuntimeException.class)
            .message("Values not equal for entry: '$['name']', expected 'Garfield' but was 'Snoopy'")
            .when(
                getPetByIdRequest
            )
        );
    }

    /**
     * TODO #1161 - Improve with builder pattern
     */
    @Test
    @CitrusTest
    void validateByVariable(@CitrusResource TestContext testContext,
        @CitrusResource TestCaseRunner runner) {

        // Given
        getPetByIdRequest.setPetId("1234");

        // Then
        getPetByIdRequest.setResponseStatus(HttpStatus.OK.value());
        getPetByIdRequest.setResponseReasonPhrase(HttpStatus.OK.getReasonPhrase());

        // Assert load data into variables
        getPetByIdRequest.setResponseVariable(Map.of("$", "RESPONSE", "$.id", "ID"));

        // When
        runner.$(getPetByIdRequest);

        // Then
        assertThat(testContext)
            .satisfies(
                c -> assertThat(c.getVariable("RESPONSE"))
                    .isNotNull(),
                c -> assertThat(c.getVariable("ID"))
                    .isNotNull()
                    .isEqualTo("12")
            );
    }

    /**
     * TODO #1161 - Improve with builder pattern
     */
    @Test
    @CitrusTest
    void validateReceivedResponse(@CitrusResource TestContext testContext) {

        // Given
        getPetByIdRequest.setPetId("1234");

        // When
        getPetByIdRequest.sendRequest(testContext);

        // Then
        Message receiveResponse = getPetByIdRequest.receiveResponse(testContext);
        assertThat(receiveResponse)
            .isNotNull()
            .extracting(Message::getPayload)
            .asString()
            .isEqualToIgnoringWhitespace(defaultResponse);
        assertThat(receiveResponse.getHeaders())
            .containsEntry("citrus_http_status_code", 200)
            .containsEntry("citrus_http_reason_phrase", "OK");
    }

    private void mockProducer() {
        Producer producerMock = mock();
        when(httpClient.createProducer()).thenReturn(producerMock);
    }

    private void mockConsumer() {
        Message receiveMessage = createReceiveMessage();

        SelectiveConsumer consumer = mock(SelectiveConsumer.class);
        when(httpClient.createConsumer()).thenReturn(consumer);
        when(consumer.receive(any(), eq(5000L))).thenReturn(receiveMessage);
    }

    private Message createReceiveMessage() {
        Message receiveMessage = new DefaultMessage();
        receiveMessage.setPayload(defaultResponse);
        receiveMessage.getHeaders().put("citrus_http_reason_phrase", "OK");
        receiveMessage.getHeaders().put("citrus_http_version", "HTTP/1.1");
        receiveMessage.getHeaders().put("Content-Type", 200);
        receiveMessage.getHeaders().put("citrus_http_status_code", 200);
        return receiveMessage;
    }

    @TestConfiguration
    public static class Config {

        @Bean(name = {"applicationServiceClient", "petStoreEndpoint"})
        public HttpClient applicationServiceClient() {
            HttpClient client = mock(HttpClient.class);
            EndpointConfiguration endpointConfiguration = mock(EndpointConfiguration.class);
            when(client.getEndpointConfiguration()).thenReturn(new HttpEndpointConfiguration());
            when(endpointConfiguration.getTimeout()).thenReturn(5000L);
            return client;
        }
    }
}
