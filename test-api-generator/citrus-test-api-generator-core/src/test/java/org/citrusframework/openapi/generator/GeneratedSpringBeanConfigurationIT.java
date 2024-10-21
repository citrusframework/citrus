package org.citrusframework.openapi.generator;

import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.config.CitrusSpringConfig;
import org.citrusframework.context.TestContext;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.http.client.HttpEndpointConfiguration;
import org.citrusframework.junit.jupiter.spring.CitrusSpringSupport;
import org.citrusframework.openapi.generator.GeneratedSpringBeanConfigurationIT.ClientConfiguration;
import org.citrusframework.openapi.generator.rest.petstore.request.PetApi;
import org.citrusframework.openapi.generator.rest.petstore.request.StoreApi;
import org.citrusframework.openapi.generator.rest.petstore.request.UserApi;
import org.citrusframework.openapi.generator.rest.petstore.spring.PetStoreBeanConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

@CitrusSpringSupport
@ContextConfiguration(classes = {CitrusSpringConfig.class, ClientConfiguration.class,
        PetStoreBeanConfiguration.class})
class GeneratedSpringBeanConfigurationIT {

    @Test
    @CitrusTest
    void petStoreOpenApiRepositoryIsAvailable(@CitrusResource TestContext testContext) {
        var petStoreOpenApiRepository = testContext.getReferenceResolver()
                .resolve("petStoreOpenApiRepository");
        assertThat(petStoreOpenApiRepository)
                .isNotNull();
    }

    @Test
    @CitrusTest
    void petApiRepositoryIsAvailable(@CitrusResource TestContext testContext) {
        var petApi = testContext.getReferenceResolver()
                .resolve(PetApi.class);
        assertThat(petApi)
                .isNotNull();
    }

    @Test
    @CitrusTest
    void storeApiRepositoryIsAvailable(@CitrusResource TestContext testContext) {
        var storeApi = testContext.getReferenceResolver()
                .resolve(StoreApi.class);
        assertThat(storeApi)
                .isNotNull();
    }

    @Test
    @CitrusTest
    void userApiRepositoryIsAvailable(@CitrusResource TestContext testContext) {
        var userApi = testContext.getReferenceResolver()
                .resolve(UserApi.class);
        assertThat(userApi)
                .isNotNull();
    }

    @TestConfiguration
    public static class ClientConfiguration {

        @Bean(name = {"petstore.endpoint"})
        public HttpClient applicationServiceClient() {
            var config = new HttpEndpointConfiguration();
            config.setRequestUrl("http://localhost:9000");
            return new HttpClient(config);
        }
    }
}
