/** 
 * ==================================================
 * GENERATED CLASS, ALL CHANGES WILL BE LOST
 * ==================================================
 */

package org.citrusframework.openapi.generator.soap.bookservice.spring;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

import org.citrusframework.openapi.generator.soap.bookservice.request.BookServiceSoapApi;
import javax.annotation.processing.Generated;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
@Generated(value = "org.citrusframework.openapi.generator.JavaCitrusCodegen")
public class OpenApiFromWsdlBeanConfiguration {

    @Bean
    @Scope(SCOPE_PROTOTYPE)
    public BookServiceSoapApi.AddBookRequest addBookRequest() {
        return new BookServiceSoapApi.AddBookRequest();
    }

    @Bean
    @Scope(SCOPE_PROTOTYPE)
    public BookServiceSoapApi.GetAllBooksRequest getAllBooksRequest() {
        return new BookServiceSoapApi.GetAllBooksRequest();
    }

    @Bean
    @Scope(SCOPE_PROTOTYPE)
    public BookServiceSoapApi.GetBookRequest getBookRequest() {
        return new BookServiceSoapApi.GetBookRequest();
    }
}
