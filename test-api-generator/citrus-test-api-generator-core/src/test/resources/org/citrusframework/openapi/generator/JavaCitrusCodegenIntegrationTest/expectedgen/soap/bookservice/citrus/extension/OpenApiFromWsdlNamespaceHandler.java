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

/**
 * ==================================================
 * GENERATED CLASS, ALL CHANGES WILL BE LOST
 * ==================================================
 */

package org.citrusframework.openapi.generator.soap.bookservice.citrus.extension;

import org.citrusframework.openapi.generator.soap.bookservice.request.BookServiceSoapApi;
import org.citrusframework.openapi.generator.soap.bookservice.citrus.OpenApiFromWsdlBeanDefinitionParser;

import javax.annotation.processing.Generated;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

@Generated(value = "org.citrusframework.openapi.generator.JavaCitrusCodegen")
public class OpenApiFromWsdlNamespaceHandler extends NamespaceHandlerSupport {

    @Override
    public void init() {
        registerBeanDefinitionParser("addBookRequest", new OpenApiFromWsdlBeanDefinitionParser(BookServiceSoapApi.AddBookRequest.class));
        registerBeanDefinitionParser("getAllBooksRequest", new OpenApiFromWsdlBeanDefinitionParser(BookServiceSoapApi.GetAllBooksRequest.class));
        registerBeanDefinitionParser("getBookRequest", new OpenApiFromWsdlBeanDefinitionParser(BookServiceSoapApi.GetBookRequest.class));
    }
}
