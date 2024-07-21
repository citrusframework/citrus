package org.citrusframework.openapi.generator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.citrusframework.container.Assert.Builder.assertException;
import static org.citrusframework.http.actions.HttpActionBuilder.http;
import static org.citrusframework.openapi.generator.sample.OpenApiPetStore.AddressEntityValidationContext.Builder.address;
import static org.citrusframework.openapi.generator.sample.OpenApiPetStore.AggregateEntityValidationContext.Builder.allOf;
import static org.citrusframework.openapi.generator.sample.OpenApiPetStore.AggregateEntityValidationContext.Builder.anyOf;
import static org.citrusframework.openapi.generator.sample.OpenApiPetStore.AggregateEntityValidationContext.Builder.oneOf;
import static org.citrusframework.openapi.generator.sample.OpenApiPetStore.PetEntityValidationContext.Builder.pet;
import static org.citrusframework.openapi.generator.sample.OpenApiPetStore.openApiPetStore;
import static org.citrusframework.util.FileUtils.readToString;
import static org.citrusframework.validation.json.JsonPathMessageValidationContext.Builder.jsonPath;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import org.citrusframework.TestCaseRunner;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.config.CitrusSpringConfig;
import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.EndpointConfiguration;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.http.client.HttpEndpointConfiguration;
import org.citrusframework.http.server.HttpServer;
import org.citrusframework.http.server.HttpServerBuilder;
import org.citrusframework.junit.jupiter.spring.CitrusSpringExtension;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.citrusframework.messaging.Producer;
import org.citrusframework.messaging.SelectiveConsumer;
import org.citrusframework.openapi.generator.GetPetByIdIT.Config;
import org.citrusframework.openapi.generator.rest.petstore.request.PetApi.GetPetByIdRequest;
import org.citrusframework.openapi.generator.rest.petstore.spring.PetStoreBeanConfiguration;
import org.citrusframework.openapi.generator.sample.OpenApiPetStore.AddressEntityValidationContext;
import org.citrusframework.openapi.generator.sample.OpenApiPetStore.AggregateEntityValidationContext;
import org.citrusframework.openapi.generator.sample.OpenApiPetStore.PetEntityValidationContext.Builder;
import org.citrusframework.openapi.generator.sample.PetApi;
import org.citrusframework.spi.BindToRegistry;
import org.citrusframework.spi.Resources;
import org.citrusframework.util.SocketUtils;
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

    private final int port = SocketUtils.findAvailableTcpPort(8080);

    @BindToRegistry
    private final HttpServer httpServer = new HttpServerBuilder()
        .port(port)
        .timeout(5000L)
        .autoStart(true)
        .defaultStatus(HttpStatus.NO_CONTENT)
        .build();

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
    void testByEntityMatcher(@CitrusResource TestCaseRunner runner) {

        when(PetApi.openApiPetStore(httpClient)
            .getPetById()
                .withId("1234")
                .fork(true));

        runner.then(http().server(httpServer)
            .receive()
            .get("/pet/${petId}")
            .message()
            .accept("@contains('application/json')@"));

        runner.then(http().server(httpServer)
            .send()
            .response(HttpStatus.OK)
            .message()
            .body(Resources.create("classpath:org/citrusframework/openapi/petstore/pet.json"))
            .contentType("application/json"));

        runner.then(openApiPetStore(httpClient)
            .receivePetById(HttpStatus.OK)
            .message()
            .validate(pet().id("1234")
                .name("Garfield")
                .category("Cat")
                .address(address -> address
                    .street("Nina Hagen Hang")
                    .zip("12345")
                    .city("Hagen ATW"))
                .owners(anyOf(List.of(
                    owner -> owner.name("Peter Lustig"),
                    owner -> owner.name("Hans Meier")
                )))
                .owners(oneOf(List.of(
                    owner -> owner.name("Seppel Hinterhuber")
                )))
                .urls(0, "url1")
                .urls(1, "url2")
                .urls("@contains('url1', 'url2')")).
            validate(jsonPath().expression("$.name", "Garfield")));

        runner.then(openApiPetStore(httpClient)
            .receivePetById200()
                .withPet(validator -> validator.id("1234")
                .name("Garfield")
                .category("Cat")
                .urls(0,"url1")
                .urls(1,"url2")
                .urls("@contains('url1', 'url2')")).
            validate(jsonPath().expression("$.name", "Garfield"))
        );
    }

    @Test
    @CitrusTest
    void testFindByStatus(@CitrusResource TestCaseRunner runner) {

        when(openApiPetStore(httpClient)
            .findByStatus()
            .withStatus("SOLD")
            .fork(true));

        runner.then(http().server(httpServer)
            .receive()
            .get("/pet/${petId}")
            .message()
            .accept("@contains('application/json')@"));

        runner.then(http().server(httpServer)
            .send()
            .response(HttpStatus.OK)
            .message()
            .body(Resources.create("classpath:org/citrusframework/openapi/petstore/pet.json"))
            .contentType("application/json"));

        runner.then(openApiPetStore(httpClient)
            .receivePetById(HttpStatus.OK)
            .message()
            .validate(pet().id("1234")
                .name("Garfield")
                .category("Cat")
                .address(address -> address
                    .street("Nina Hagen Hang")
                    .zip("12345")
                    .city("Hagen ATW"))
                .owners(anyOf(List.of(
                    owner -> owner.name("Peter Lustig"),
                    owner -> owner.name("Hans Meier")
                )))
                .owners(oneOf(List.of(
                    owner -> owner.name("Seppel Hinterhuber")
                )))
                .urls(0, "url1")
                .urls(1, "url2")
                .urls("@contains('url1', 'url2')")).
            validate(jsonPath().expression("$.name", "Garfield")));

        runner.then(openApiPetStore(httpClient)
            .receivePetById200()
            .withPet(validator -> validator.id("1234")
                .name("Garfield")
                .category("Cat")
                .urls(0,"url1")
                .urls(1,"url2")
                .urls("@contains('url1', 'url2')")).
            validate(jsonPath().expression("$.name", "Garfield"))
        );
    }

    @Test
    @CitrusTest
    void testByJsonPath(@CitrusResource TestCaseRunner runner) {

        when(openApiPetStore(httpClient)
            .getPetById()
            .withPetId("1234")
            .fork(true));

        runner.then(http().server(httpServer)
            .receive()
            .get("/pet/${petId}")
            .message()
            .accept("@contains('application/json')@"));

        runner.then(http().server(httpServer)
            .send()
            .response(HttpStatus.OK)
            .message()
            .body(Resources.create("classpath:org/citrusframework/openapi/petstore/pet.json"))
            .contentType("application/json"));

        runner.then(openApiPetStore(httpClient)
            .receivePetById(HttpStatus.OK)
                .message().validate(jsonPath().expression("$.name", "Garfield"))
        );
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
