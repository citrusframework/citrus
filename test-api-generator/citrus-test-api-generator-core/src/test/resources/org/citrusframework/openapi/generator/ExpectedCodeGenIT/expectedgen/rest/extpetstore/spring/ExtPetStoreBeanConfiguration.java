package org.citrusframework.openapi.generator.rest.extpetstore.spring;

import static org.citrusframework.openapi.generator.rest.extpetstore.ExtPetStoreOpenApi.extPetStoreSpecification;

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
import org.citrusframework.openapi.generator.rest.extpetstore.ExtPetStoreOpenApi;

@Configuration
@jakarta.annotation.Generated(value = "org.citrusframework.openapi.generator.CitrusJavaCodegen", date = "2025-01-29T23:14:48.524898+01:00[Europe/Zurich]", comments = "Generator version: 7.9.0")
public class ExtPetStoreBeanConfiguration {

    @Bean
    public OpenApiRepository extPetStoreOpenApiRepository() {
        var openApiRepository = new OpenApiRepository();
        openApiRepository.getOpenApiSpecifications().add(extPetStoreSpecification);
        return openApiRepository;
    }

    @Bean(name="ExtPetApi")
    public ExtPetApi extPetApi(@Autowired(required = false) @Qualifier("extpetstore.endpoint") Endpoint defaultEndpoint, @Autowired(required = false) List<ApiActionBuilderCustomizer> customizers) {
        return new ExtPetApi(defaultEndpoint, customizers);
    }

}
