package org.citrusframework.openapi.generator.rest.petstore.spring;

import static org.citrusframework.openapi.generator.rest.petstore.PetStoreOpenApi.petStoreSpecification;

import java.util.List;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.openapi.OpenApiRepository;
import org.citrusframework.openapi.OpenApiSpecification;
import org.citrusframework.openapi.testapi.ApiActionBuilderCustomizer;
import org.citrusframework.openapi.generator.rest.petstore.request.PetApi;
import org.citrusframework.openapi.generator.rest.petstore.request.StoreApi;
import org.citrusframework.openapi.generator.rest.petstore.request.UserApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.citrusframework.openapi.generator.rest.petstore.PetStoreOpenApi;

@Configuration
@jakarta.annotation.Generated(value = "org.citrusframework.openapi.generator.CitrusJavaCodegen", date = "2025-06-29T17:00:42.828969400+02:00[Europe/Zurich]", comments = "Generator version: 7.14.0")
public class PetStoreBeanConfiguration {

    @Bean
    public OpenApiRepository petStoreOpenApiRepository() {
        var openApiRepository = new OpenApiRepository();
        openApiRepository.getOpenApiSpecifications().add(petStoreSpecification);
        return openApiRepository;
    }

    @Bean
    public PetApi petApi(@Autowired(required = false) @Qualifier("petstore.endpoint") Endpoint defaultEndpoint, @Autowired(required = false) List<ApiActionBuilderCustomizer> customizers) {
        return new PetApi(defaultEndpoint, customizers);
    }

    @Bean
    public StoreApi storeApi(@Autowired(required = false) @Qualifier("petstore.endpoint") Endpoint defaultEndpoint, @Autowired(required = false) List<ApiActionBuilderCustomizer> customizers) {
        return new StoreApi(defaultEndpoint, customizers);
    }

    @Bean
    public UserApi userApi(@Autowired(required = false) @Qualifier("petstore.endpoint") Endpoint defaultEndpoint, @Autowired(required = false) List<ApiActionBuilderCustomizer> customizers) {
        return new UserApi(defaultEndpoint, customizers);
    }

}
