package org.citrusframework.openapi.generator.soap.bookservice.request;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;

import jakarta.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.citrusframework.openapi.testapi.GeneratedApiOperationInfo;
import org.citrusframework.ws.actions.ReceiveSoapMessageAction;
import org.citrusframework.ws.actions.SendSoapMessageAction;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.openapi.testapi.ApiActionBuilderCustomizer;
import org.citrusframework.openapi.testapi.GeneratedApi;
import org.citrusframework.openapi.testapi.SoapApiReceiveMessageActionBuilder;
import org.citrusframework.openapi.testapi.SoapApiSendMessageActionBuilder;

@SuppressWarnings("unused")
@jakarta.annotation.Generated(value = "org.citrusframework.openapi.generator.CitrusJavaCodegen", date = "2025-01-29T23:14:48.920209500+01:00[Europe/Zurich]", comments = "Generator version: 7.9.0")
public class BookServiceSoapApi implements GeneratedApi
{

    /**
     * An optional default endpoint which will be passed into the requests.
     */
    private final Endpoint defaultEndpoint;

    private final List<ApiActionBuilderCustomizer> customizers;

    public BookServiceSoapApi(@Nullable Endpoint defaultEndpoint)  {
        this(defaultEndpoint, emptyList());
    }

    public BookServiceSoapApi(@Nullable Endpoint defaultEndpoint, @Nullable List<ApiActionBuilderCustomizer> customizers)  {
        this.defaultEndpoint = defaultEndpoint;
        this.customizers = customizers;
    }

    public static BookServiceSoapApi bookServiceSoapApi(Endpoint defaultEndpoint) {
        return new BookServiceSoapApi(defaultEndpoint);
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
    @Nullable
    public Endpoint getEndpoint() {
        return defaultEndpoint;
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

    public static class AddBookSendActionBuilder extends SoapApiSendMessageActionBuilder implements
        GeneratedApiOperationInfo {

        private static final String SOAP_ACTION = "http://www.citrusframework.com/BookService/AddBook";

        public AddBookSendActionBuilder(BookServiceSoapApi bookServiceSoapApi) {
            super(bookServiceSoapApi, SOAP_ACTION);
        }

        @Override
        public String getOperationName() {
            return SOAP_ACTION;
        }

        @Override
        public String getMethod() {
            return "POST";
        }

        @Override
        public String getPath() {
            return SOAP_ACTION;
        }

        @Override
        public SendSoapMessageAction doBuild() {

            if (getCustomizers() != null) {
                getCustomizers().forEach(customizer -> customizer.customizeRequestBuilder(this, this));
            }

            return super.doBuild();
        }
    }

    public static class AddBookReceiveActionBuilder extends SoapApiReceiveMessageActionBuilder implements
        GeneratedApiOperationInfo {

        private static final String SOAP_ACTION = "http://www.citrusframework.com/BookService/AddBook";

        public AddBookReceiveActionBuilder(BookServiceSoapApi bookServiceSoapApi) {
            super(bookServiceSoapApi, SOAP_ACTION);
        }

        @Override
        public String getOperationName() {
            return SOAP_ACTION;
        }

        @Override
        public String getMethod() {
          return "POST";
        }

        @Override
        public String getPath() {
            return SOAP_ACTION;
        }

        @Override
        public ReceiveSoapMessageAction doBuild() {

            if (getCustomizers() != null) {
                getCustomizers().forEach(customizer -> customizer.customizeResponseBuilder(this, this));
            }

            return super.doBuild();
        }

    }

    public static class GetAllBooksSendActionBuilder extends SoapApiSendMessageActionBuilder implements
        GeneratedApiOperationInfo {

        private static final String SOAP_ACTION = "http://www.citrusframework.com/BookService/GetAllBooks";

        public GetAllBooksSendActionBuilder(BookServiceSoapApi bookServiceSoapApi) {
            super(bookServiceSoapApi, SOAP_ACTION);
        }

        @Override
        public String getOperationName() {
            return SOAP_ACTION;
        }

        @Override
        public String getMethod() {
            return "POST";
        }

        @Override
        public String getPath() {
            return SOAP_ACTION;
        }

        @Override
        public SendSoapMessageAction doBuild() {

            if (getCustomizers() != null) {
                getCustomizers().forEach(customizer -> customizer.customizeRequestBuilder(this, this));
            }

            return super.doBuild();
        }
    }

    public static class GetAllBooksReceiveActionBuilder extends SoapApiReceiveMessageActionBuilder implements
        GeneratedApiOperationInfo {

        private static final String SOAP_ACTION = "http://www.citrusframework.com/BookService/GetAllBooks";

        public GetAllBooksReceiveActionBuilder(BookServiceSoapApi bookServiceSoapApi) {
            super(bookServiceSoapApi, SOAP_ACTION);
        }

        @Override
        public String getOperationName() {
            return SOAP_ACTION;
        }

        @Override
        public String getMethod() {
          return "POST";
        }

        @Override
        public String getPath() {
            return SOAP_ACTION;
        }

        @Override
        public ReceiveSoapMessageAction doBuild() {

            if (getCustomizers() != null) {
                getCustomizers().forEach(customizer -> customizer.customizeResponseBuilder(this, this));
            }

            return super.doBuild();
        }

    }

    public static class GetBookSendActionBuilder extends SoapApiSendMessageActionBuilder implements
        GeneratedApiOperationInfo {

        private static final String SOAP_ACTION = "http://www.citrusframework.com/BookService/GetBook";

        public GetBookSendActionBuilder(BookServiceSoapApi bookServiceSoapApi) {
            super(bookServiceSoapApi, SOAP_ACTION);
        }

        @Override
        public String getOperationName() {
            return SOAP_ACTION;
        }

        @Override
        public String getMethod() {
            return "POST";
        }

        @Override
        public String getPath() {
            return SOAP_ACTION;
        }

        @Override
        public SendSoapMessageAction doBuild() {

            if (getCustomizers() != null) {
                getCustomizers().forEach(customizer -> customizer.customizeRequestBuilder(this, this));
            }

            return super.doBuild();
        }
    }

    public static class GetBookReceiveActionBuilder extends SoapApiReceiveMessageActionBuilder implements
        GeneratedApiOperationInfo {

        private static final String SOAP_ACTION = "http://www.citrusframework.com/BookService/GetBook";

        public GetBookReceiveActionBuilder(BookServiceSoapApi bookServiceSoapApi) {
            super(bookServiceSoapApi, SOAP_ACTION);
        }

        @Override
        public String getOperationName() {
            return SOAP_ACTION;
        }

        @Override
        public String getMethod() {
          return "POST";
        }

        @Override
        public String getPath() {
            return SOAP_ACTION;
        }

        @Override
        public ReceiveSoapMessageAction doBuild() {

            if (getCustomizers() != null) {
                getCustomizers().forEach(customizer -> customizer.customizeResponseBuilder(this, this));
            }

            return super.doBuild();
        }

    }
}
