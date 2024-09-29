package org.citrusframework.openapi.generator.rest.extpetstore.spring;

import java.util.List;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.openapi.OpenApiRepository;
import org.citrusframework.openapi.OpenApiSpecification;
import org.citrusframework.openapi.testapi.ApiActionBuilderCustomizer;
import org.citrusframework.openapi.generator.rest.extpetstore.request.ExtPetApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.citrusframework.openapi.generator.rest.extpetstore.ExtPetStore;


@Configuration
@jakarta.annotation.Generated(value = "org.citrusframework.openapi.generator.CitrusJavaCodegen", date = "2024-10-05T19:07:46.194751400+02:00[Europe/Zurich]", comments = "Generator version: 7.5.0")
public class ExtPetStoreBeanConfiguration {

    @Bean
    public OpenApiRepository extPetStoreOpenApiRepository() {
        var openApiRepository = new OpenApiRepository();
        openApiRepository.getOpenApiSpecifications().add(OpenApiSpecification.from(
            ExtPetStore.extPetStoreApi()));
        return openApiRepository;
    }

    @Bean(name="ExtPetApi")
    public ExtPetApi extPetApi(@Qualifier("extpetstore.endpoint") Endpoint endpoint, @Autowired(required = false) List<ApiActionBuilderCustomizer> customizers) {
        return new ExtPetApi(endpoint, customizers);
    }

}
