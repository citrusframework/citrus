package org.citrusframework.openapi.generator.soap.bookservice.request;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.citrusframework.ws.actions.ReceiveSoapMessageAction;
import org.citrusframework.ws.actions.SendSoapMessageAction;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.openapi.testapi.ApiActionBuilderCustomizer;
import org.citrusframework.openapi.testapi.GeneratedApi;
import org.citrusframework.openapi.testapi.SoapApiReceiveMessageActionBuilder;
import org.citrusframework.openapi.testapi.SoapApiSendMessageActionBuilder;

@SuppressWarnings("unused")
@jakarta.annotation.Generated(value = "org.citrusframework.openapi.generator.CitrusJavaCodegen", date = "2024-10-05T19:07:46.419751700+02:00[Europe/Zurich]", comments = "Generator version: 7.5.0")
public class BookServiceSoapApi implements GeneratedApi
{

    private final Endpoint endpoint;

    private final List<ApiActionBuilderCustomizer> customizers;

    public BookServiceSoapApi(Endpoint endpoint)  {
        this(endpoint, emptyList());
    }

    public BookServiceSoapApi(Endpoint endpoint, List<ApiActionBuilderCustomizer> customizers)  {
        this.endpoint = endpoint;
        this.customizers = customizers;
    }

    public static BookServiceSoapApi bookServiceSoapApi(Endpoint endpoint) {
        return new BookServiceSoapApi(endpoint);
    }

    @Override
    public String getApiTitle() {
        return "Generated api from wsdl";
    }

    @Override
    public String getApiVersion() {
        return "1.0.0";
    }

    @Override
    public String getApiPrefix() {
        return "BookService";
    }

    @Override
    public Map<String, String> getApiInfoExtensions() {
        return emptyMap();
    }

    @Override
    public Endpoint getEndpoint() {
        return endpoint;
    }

    @Override
    public List<ApiActionBuilderCustomizer> getCustomizers() {
        return customizers;
    }

    public AddBookSendActionBuilder sendAddBook()   {
        return new AddBookSendActionBuilder(this);
    }

    public AddBookReceiveActionBuilder receiveAddBook()   {
        return new AddBookReceiveActionBuilder(this);
    }

    public GetAllBooksSendActionBuilder sendGetAllBooks()   {
        return new GetAllBooksSendActionBuilder(this);
    }

    public GetAllBooksReceiveActionBuilder receiveGetAllBooks()   {
        return new GetAllBooksReceiveActionBuilder(this);
    }

    public GetBookSendActionBuilder sendGetBook()   {
        return new GetBookSendActionBuilder(this);
    }

    public GetBookReceiveActionBuilder receiveGetBook()   {
        return new GetBookReceiveActionBuilder(this);
    }

    public static class AddBookSendActionBuilder extends SoapApiSendMessageActionBuilder {

        private static final String SOAP_ACTION = "http://www.citrusframework.com/BookService/AddBook";

        public AddBookSendActionBuilder(BookServiceSoapApi bookServiceSoapApi) {
            super(bookServiceSoapApi, SOAP_ACTION);
        }

        @Override
        public SendSoapMessageAction doBuild() {

            if (getCustomizers() != null) {
                getCustomizers().forEach(customizer -> customizer.customizeRequestBuilder(getGeneratedApi(), this));
            }

            return super.doBuild();
        }
    }

    public static class AddBookReceiveActionBuilder extends SoapApiReceiveMessageActionBuilder {

        private static final String SOAP_ACTION = "http://www.citrusframework.com/BookService/AddBook";

        public AddBookReceiveActionBuilder(BookServiceSoapApi bookServiceSoapApi) {
            super(bookServiceSoapApi, SOAP_ACTION);
        }

        @Override
        public ReceiveSoapMessageAction doBuild() {

            if (getCustomizers() != null) {
                getCustomizers().forEach(customizer -> customizer.customizeResponseBuilder(getGeneratedApi(), this));
            }

            return super.doBuild();
        }

    }

    public static class GetAllBooksSendActionBuilder extends SoapApiSendMessageActionBuilder {

        private static final String SOAP_ACTION = "http://www.citrusframework.com/BookService/GetAllBooks";

        public GetAllBooksSendActionBuilder(BookServiceSoapApi bookServiceSoapApi) {
            super(bookServiceSoapApi, SOAP_ACTION);
        }

        @Override
        public SendSoapMessageAction doBuild() {

            if (getCustomizers() != null) {
                getCustomizers().forEach(customizer -> customizer.customizeRequestBuilder(getGeneratedApi(), this));
            }

            return super.doBuild();
        }
    }

    public static class GetAllBooksReceiveActionBuilder extends SoapApiReceiveMessageActionBuilder {

        private static final String SOAP_ACTION = "http://www.citrusframework.com/BookService/GetAllBooks";

        public GetAllBooksReceiveActionBuilder(BookServiceSoapApi bookServiceSoapApi) {
            super(bookServiceSoapApi, SOAP_ACTION);
        }

        @Override
        public ReceiveSoapMessageAction doBuild() {

            if (getCustomizers() != null) {
                getCustomizers().forEach(customizer -> customizer.customizeResponseBuilder(getGeneratedApi(), this));
            }

            return super.doBuild();
        }

    }

    public static class GetBookSendActionBuilder extends SoapApiSendMessageActionBuilder {

        private static final String SOAP_ACTION = "http://www.citrusframework.com/BookService/GetBook";

        public GetBookSendActionBuilder(BookServiceSoapApi bookServiceSoapApi) {
            super(bookServiceSoapApi, SOAP_ACTION);
        }

        @Override
        public SendSoapMessageAction doBuild() {

            if (getCustomizers() != null) {
                getCustomizers().forEach(customizer -> customizer.customizeRequestBuilder(getGeneratedApi(), this));
            }

            return super.doBuild();
        }
    }

    public static class GetBookReceiveActionBuilder extends SoapApiReceiveMessageActionBuilder {

        private static final String SOAP_ACTION = "http://www.citrusframework.com/BookService/GetBook";

        public GetBookReceiveActionBuilder(BookServiceSoapApi bookServiceSoapApi) {
            super(bookServiceSoapApi, SOAP_ACTION);
        }

        @Override
        public ReceiveSoapMessageAction doBuild() {

            if (getCustomizers() != null) {
                getCustomizers().forEach(customizer -> customizer.customizeResponseBuilder(getGeneratedApi(), this));
            }

            return super.doBuild();
        }

    }
}
