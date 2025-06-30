package org.citrusframework.openapi.generator.soap.bookservice.spring;

import org.citrusframework.openapi.OpenApiSpecification;
import org.citrusframework.openapi.testapi.SoapApiSendMessageActionBuilder;
import org.citrusframework.openapi.testapi.SoapApiReceiveMessageActionBuilder;
import org.citrusframework.openapi.generator.soap.bookservice.request.BookServiceSoapApi;
import org.citrusframework.openapi.testapi.spring.SoapApiReceiveMessageActionParser;
import org.citrusframework.openapi.testapi.spring.SoapApiSendMessageActionParser;
import org.citrusframework.openapi.generator.soap.bookservice.BookServiceOpenApi;
import org.citrusframework.openapi.testapi.GeneratedApi;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

@jakarta.annotation.Generated(value = "org.citrusframework.openapi.generator.CitrusJavaCodegen", date = "2025-06-29T17:00:48.787357800+02:00[Europe/Zurich]", comments = "Generator version: 7.14.0")
public class BookServiceNamespaceHandler extends NamespaceHandlerSupport {

    @Override
    public void init() {

            registerOperationParsers(BookServiceSoapApi.class,"add-book",
                BookServiceSoapApi.AddBookSendActionBuilder.class,
                BookServiceSoapApi.AddBookReceiveActionBuilder.class);

            registerOperationParsers(BookServiceSoapApi.class,"get-all-books",
                BookServiceSoapApi.GetAllBooksSendActionBuilder.class,
                BookServiceSoapApi.GetAllBooksReceiveActionBuilder.class);

            registerOperationParsers(BookServiceSoapApi.class,"get-book",
                BookServiceSoapApi.GetBookSendActionBuilder.class,
                BookServiceSoapApi.GetBookReceiveActionBuilder.class);
    }

    private void registerOperationParsers(Class<? extends GeneratedApi> apiClass, String elementName,
        Class<? extends SoapApiSendMessageActionBuilder> sendBeanClass,
        Class<? extends SoapApiReceiveMessageActionBuilder> receiveBeanClass) {

    SoapApiSendMessageActionParser sendParser = new SoapApiSendMessageActionParser(
            apiClass,
            sendBeanClass,
            receiveBeanClass,
            "bookstore.endpoint");
        registerBeanDefinitionParser("send-"+elementName, sendParser);

        SoapApiReceiveMessageActionParser receiveParser = new SoapApiReceiveMessageActionParser(
        apiClass, receiveBeanClass, "bookstore.endpoint");
        registerBeanDefinitionParser("receive-"+elementName, receiveParser);
    }
}
