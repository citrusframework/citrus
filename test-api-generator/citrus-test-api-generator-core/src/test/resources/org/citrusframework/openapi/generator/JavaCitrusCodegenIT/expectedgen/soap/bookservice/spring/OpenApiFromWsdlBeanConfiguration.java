/*
* Copyright the original author or authors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.citrusframework.openapi.generator.soap.bookservice.spring;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

import org.citrusframework.openapi.generator.soap.bookservice.request.BookServiceSoapApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
@jakarta.annotation.Generated(value = "org.citrusframework.openapi.generator.JavaCitrusCodegen", date = "2024-07-03T15:24:46.256348400+02:00[Europe/Zurich]", comments = "Generator version: 7.5.0")
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
