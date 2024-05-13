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
