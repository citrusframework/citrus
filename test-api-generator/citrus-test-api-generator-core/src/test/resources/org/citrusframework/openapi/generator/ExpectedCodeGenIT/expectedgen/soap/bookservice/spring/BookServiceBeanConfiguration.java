package org.citrusframework.openapi.generator.soap.bookservice.spring;

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
import org.citrusframework.openapi.generator.soap.bookservice.BookService;

@Configuration
@jakarta.annotation.Generated(value = "org.citrusframework.openapi.generator.CitrusJavaCodegen", date = "2024-10-28T13:20:44.612584400+01:00[Europe/Zurich]", comments = "Generator version: 7.5.0")
public class BookServiceBeanConfiguration {

    @Bean
    public OpenApiRepository bookServiceOpenApiRepository() {
        var openApiRepository = new OpenApiRepository();
        openApiRepository.getOpenApiSpecifications().add(OpenApiSpecification.from(
            BookService.bookServiceApi()));
        return openApiRepository;
    }

    @Bean(name="BookServiceSoapApi")
    public BookServiceSoapApi bookServiceSoapApi(@Qualifier("bookstore.endpoint") Endpoint endpoint, @Autowired(required = false) List<ApiActionBuilderCustomizer> customizers) {
        return new BookServiceSoapApi(endpoint, customizers);
    }

}
