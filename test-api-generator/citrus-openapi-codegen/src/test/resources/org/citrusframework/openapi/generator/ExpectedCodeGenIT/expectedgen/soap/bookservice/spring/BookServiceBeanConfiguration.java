package org.citrusframework.openapi.generator.soap.bookservice.spring;

import static org.citrusframework.openapi.generator.soap.bookservice.BookServiceOpenApi.bookServiceSpecification;

import java.util.List;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.openapi.OpenApiRepository;
import org.citrusframework.openapi.OpenApiSpecification;
import org.citrusframework.openapi.testapi.ApiActionBuilderCustomizer;
import org.citrusframework.openapi.generator.soap.bookservice.request.BookServiceSoapApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.citrusframework.openapi.generator.soap.bookservice.BookServiceOpenApi;

@Configuration
@jakarta.annotation.Generated(value = "org.citrusframework.openapi.generator.CitrusJavaCodegen", date = "2025-06-29T17:00:48.787357800+02:00[Europe/Zurich]", comments = "Generator version: 7.14.0")
public class BookServiceBeanConfiguration {

    @Bean
    public OpenApiRepository bookServiceOpenApiRepository() {
        var openApiRepository = new OpenApiRepository();
        openApiRepository.getOpenApiSpecifications().add(bookServiceSpecification);
        return openApiRepository;
    }

    @Bean
    public BookServiceSoapApi bookServiceSoapApi(@Autowired(required = false) @Qualifier("bookstore.endpoint") Endpoint defaultEndpoint, @Autowired(required = false) List<ApiActionBuilderCustomizer> customizers) {
        return new BookServiceSoapApi(defaultEndpoint, customizers);
    }

}
