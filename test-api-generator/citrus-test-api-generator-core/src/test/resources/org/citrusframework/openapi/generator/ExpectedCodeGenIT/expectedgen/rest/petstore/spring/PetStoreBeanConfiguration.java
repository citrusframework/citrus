package org.citrusframework.openapi.generator.rest.petstore.spring;

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
import org.citrusframework.openapi.generator.rest.petstore.PetStore;


@Configuration
@jakarta.annotation.Generated(value = "org.citrusframework.openapi.generator.CitrusJavaCodegen", date = "2024-10-05T19:07:45.597236600+02:00[Europe/Zurich]", comments = "Generator version: 7.5.0")
public class PetStoreBeanConfiguration {

    @Bean
    public OpenApiRepository petStoreOpenApiRepository() {
        var openApiRepository = new OpenApiRepository();
        openApiRepository.getOpenApiSpecifications().add(OpenApiSpecification.from(
            PetStore.petStoreApi()));
        return openApiRepository;
    }

    @Bean(name="PetApi")
    public PetApi petApi(@Qualifier("petstore.endpoint") Endpoint endpoint, @Autowired(required = false) List<ApiActionBuilderCustomizer> customizers) {
        return new PetApi(endpoint, customizers);
    }

    @Bean(name="StoreApi")
    public StoreApi storeApi(@Qualifier("petstore.endpoint") Endpoint endpoint, @Autowired(required = false) List<ApiActionBuilderCustomizer> customizers) {
        return new StoreApi(endpoint, customizers);
    }

    @Bean(name="UserApi")
    public UserApi userApi(@Qualifier("petstore.endpoint") Endpoint endpoint, @Autowired(required = false) List<ApiActionBuilderCustomizer> customizers) {
        return new UserApi(endpoint, customizers);
    }

}
