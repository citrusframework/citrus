/** 
 * ==================================================
 * GENERATED CLASS, ALL CHANGES WILL BE LOST
 * ==================================================
 */

package org.citrusframework.openapi.generator.soap.bookservice.request;

import jakarta.annotation.Generated;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.testapi.GeneratedApi;
import org.citrusframework.testapi.GeneratedApiRequest;
import org.citrusframework.openapi.generator.soap.bookservice.citrus.OpenApiFromWsdlAbstractTestRequest;
import org.citrusframework.spi.Resources;
import org.citrusframework.util.FileUtils;
import org.citrusframework.ws.actions.SendSoapMessageAction;
import org.citrusframework.ws.actions.SendSoapMessageAction.Builder.SendSoapMessageBuilderSupport;
import org.citrusframework.ws.actions.SoapActionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import org.citrusframework.openapi.generator.soap.bookservice.citrus.OpenApiFromWsdlAbstractTestRequest;

@Generated(value = "org.citrusframework.openapi.generator.JavaCitrusCodegen")
public class BookServiceSoapApi implements GeneratedApi
{
    public static final BookServiceSoapApi INSTANCE = new BookServiceSoapApi();

    public String getApiTitle() {
        return "Generated api from wsdl";
    }

    public String getApiVersion() {
        return "1.0.0";
    }

    public String getApiPrefix() {
        return "OpenApiFromWsdl";
    }

    public Map<String,String> getApiInfoExtensions() {
    Map<String, String> infoExtensionMap = new HashMap<>();
    return infoExtensionMap;
    }

    /**
      addBook (POST /AddBook)
      
      
     **/
    public static class AddBookRequest extends OpenApiFromWsdlAbstractTestRequest implements GeneratedApiRequest {

        private final Logger coverageLogger = LoggerFactory.getLogger(AddBookRequest.class);

        // Query params
        

        public AddBookRequest(){
            // The name will be overwritten with the tag name using the actual namespace as prefix, when the class is loaded from xml
            setName("OpenApiFromWsdl".toLowerCase() + ":addBookRequestType");
        }

        public String getOperationName() {
            return "addBook";
        }

        public String getMethod() {
            return "POST";
        }

        public String getPath() {
            return "/AddBook";
        }

        /**
        * This method sends the HTTP-Request
        */
        public void sendRequest(TestContext context) {

            SendSoapMessageAction.Builder soapSendMessageActionBuilder = new SoapActionBuilder().client(wsClient).send();
            SendSoapMessageBuilderSupport messageBuilderSupport = soapSendMessageActionBuilder.getMessageBuilderSupport();

            messageBuilderSupport.soapAction("addBook");

            String payload = null;
            String payloadType = null;
 
            if (StringUtils.isNotBlank(this.bodyFile)) {
                try {
                    payload = FileUtils.readToString(Resources.create(this.bodyFile), FileUtils.getDefaultCharset());
                } catch (IOException e) {
                    throw new CitrusRuntimeException("Failed to read payload resource", e);
                }
                payloadType = this.bodyContentType;
            } else if (StringUtils.isNotBlank(this.bodyLiteral)) {
                payload = this.bodyLiteral;
                payloadType = this.bodyLiteralContentType;
            }
   
            String body = "";
            String bodyType = "";
            if(payload != null && payloadType != null) {
                messageBuilderSupport.body(payload).contentType(payloadType);
                body = context.replaceDynamicContentInString(payload);
                bodyType = context.replaceDynamicContentInString(payloadType);
            }

            if (!CollectionUtils.isEmpty(soapHeaders)) {
                for (Entry<String, String> entry : soapHeaders.entrySet()) {
                    messageBuilderSupport = messageBuilderSupport.header(entry.getKey(),
                            entry.getValue());
                }
            }

            if (!CollectionUtils.isEmpty(mimeHeaders)) {
                for (Entry<String, String> entry : mimeHeaders.entrySet()) {
                    messageBuilderSupport = messageBuilderSupport.header("citrus_http_" + entry.getKey(),
                            entry.getValue());
                }
            }

            Map<String, String> queryParams = new HashMap<>();
            
            String query = queryParams.entrySet().stream().map(e -> "\"" + e.getKey() + "\":\"" + e.getValue() + "\"").collect(Collectors.joining(",", "{", "}"));

            soapSendMessageActionBuilder.withReferenceResolver(context.getReferenceResolver());
            soapSendMessageActionBuilder = customizeBuilder(INSTANCE, context, soapSendMessageActionBuilder);

            soapSendMessageActionBuilder.build().execute(context);

            coverageLogger.trace(coverageMarker, "addBook;POST;\"" +
                query.replace("\n", "\\n").replace("\r", "\\r").replace("\"", "\"\"") + "\";\"" +
                body.replace("\n", "\\n").replace("\r", "\\r").replace("\"", "\"\"") + "\";\"" + bodyType + "\"");
        }

        
    }
    /**
      getAllBooks (POST /GetAllBooks)
      
      
     **/
    public static class GetAllBooksRequest extends OpenApiFromWsdlAbstractTestRequest implements GeneratedApiRequest {

        private final Logger coverageLogger = LoggerFactory.getLogger(GetAllBooksRequest.class);

        // Query params
        

        public GetAllBooksRequest(){
            // The name will be overwritten with the tag name using the actual namespace as prefix, when the class is loaded from xml
            setName("OpenApiFromWsdl".toLowerCase() + ":getAllBooksRequestType");
        }

        public String getOperationName() {
            return "getAllBooks";
        }

        public String getMethod() {
            return "POST";
        }

        public String getPath() {
            return "/GetAllBooks";
        }

        /**
        * This method sends the HTTP-Request
        */
        public void sendRequest(TestContext context) {

            SendSoapMessageAction.Builder soapSendMessageActionBuilder = new SoapActionBuilder().client(wsClient).send();
            SendSoapMessageBuilderSupport messageBuilderSupport = soapSendMessageActionBuilder.getMessageBuilderSupport();

            messageBuilderSupport.soapAction("getAllBooks");

            String payload = null;
            String payloadType = null;
 
            if (StringUtils.isNotBlank(this.bodyFile)) {
                try {
                    payload = FileUtils.readToString(Resources.create(this.bodyFile), FileUtils.getDefaultCharset());
                } catch (IOException e) {
                    throw new CitrusRuntimeException("Failed to read payload resource", e);
                }
                payloadType = this.bodyContentType;
            } else if (StringUtils.isNotBlank(this.bodyLiteral)) {
                payload = this.bodyLiteral;
                payloadType = this.bodyLiteralContentType;
            }
   
            String body = "";
            String bodyType = "";
            if(payload != null && payloadType != null) {
                messageBuilderSupport.body(payload).contentType(payloadType);
                body = context.replaceDynamicContentInString(payload);
                bodyType = context.replaceDynamicContentInString(payloadType);
            }

            if (!CollectionUtils.isEmpty(soapHeaders)) {
                for (Entry<String, String> entry : soapHeaders.entrySet()) {
                    messageBuilderSupport = messageBuilderSupport.header(entry.getKey(),
                            entry.getValue());
                }
            }

            if (!CollectionUtils.isEmpty(mimeHeaders)) {
                for (Entry<String, String> entry : mimeHeaders.entrySet()) {
                    messageBuilderSupport = messageBuilderSupport.header("citrus_http_" + entry.getKey(),
                            entry.getValue());
                }
            }

            Map<String, String> queryParams = new HashMap<>();
            
            String query = queryParams.entrySet().stream().map(e -> "\"" + e.getKey() + "\":\"" + e.getValue() + "\"").collect(Collectors.joining(",", "{", "}"));

            soapSendMessageActionBuilder.withReferenceResolver(context.getReferenceResolver());
            soapSendMessageActionBuilder = customizeBuilder(INSTANCE, context, soapSendMessageActionBuilder);

            soapSendMessageActionBuilder.build().execute(context);

            coverageLogger.trace(coverageMarker, "getAllBooks;POST;\"" +
                query.replace("\n", "\\n").replace("\r", "\\r").replace("\"", "\"\"") + "\";\"" +
                body.replace("\n", "\\n").replace("\r", "\\r").replace("\"", "\"\"") + "\";\"" + bodyType + "\"");
        }

        
    }
    /**
      getBook (POST /GetBook)
      
      
     **/
    public static class GetBookRequest extends OpenApiFromWsdlAbstractTestRequest implements GeneratedApiRequest {

        private final Logger coverageLogger = LoggerFactory.getLogger(GetBookRequest.class);

        // Query params
        

        public GetBookRequest(){
            // The name will be overwritten with the tag name using the actual namespace as prefix, when the class is loaded from xml
            setName("OpenApiFromWsdl".toLowerCase() + ":getBookRequestType");
        }

        public String getOperationName() {
            return "getBook";
        }

        public String getMethod() {
            return "POST";
        }

        public String getPath() {
            return "/GetBook";
        }

        /**
        * This method sends the HTTP-Request
        */
        public void sendRequest(TestContext context) {

            SendSoapMessageAction.Builder soapSendMessageActionBuilder = new SoapActionBuilder().client(wsClient).send();
            SendSoapMessageBuilderSupport messageBuilderSupport = soapSendMessageActionBuilder.getMessageBuilderSupport();

            messageBuilderSupport.soapAction("getBook");

            String payload = null;
            String payloadType = null;
 
            if (StringUtils.isNotBlank(this.bodyFile)) {
                try {
                    payload = FileUtils.readToString(Resources.create(this.bodyFile), FileUtils.getDefaultCharset());
                } catch (IOException e) {
                    throw new CitrusRuntimeException("Failed to read payload resource", e);
                }
                payloadType = this.bodyContentType;
            } else if (StringUtils.isNotBlank(this.bodyLiteral)) {
                payload = this.bodyLiteral;
                payloadType = this.bodyLiteralContentType;
            }
   
            String body = "";
            String bodyType = "";
            if(payload != null && payloadType != null) {
                messageBuilderSupport.body(payload).contentType(payloadType);
                body = context.replaceDynamicContentInString(payload);
                bodyType = context.replaceDynamicContentInString(payloadType);
            }

            if (!CollectionUtils.isEmpty(soapHeaders)) {
                for (Entry<String, String> entry : soapHeaders.entrySet()) {
                    messageBuilderSupport = messageBuilderSupport.header(entry.getKey(),
                            entry.getValue());
                }
            }

            if (!CollectionUtils.isEmpty(mimeHeaders)) {
                for (Entry<String, String> entry : mimeHeaders.entrySet()) {
                    messageBuilderSupport = messageBuilderSupport.header("citrus_http_" + entry.getKey(),
                            entry.getValue());
                }
            }

            Map<String, String> queryParams = new HashMap<>();
            
            String query = queryParams.entrySet().stream().map(e -> "\"" + e.getKey() + "\":\"" + e.getValue() + "\"").collect(Collectors.joining(",", "{", "}"));

            soapSendMessageActionBuilder.withReferenceResolver(context.getReferenceResolver());
            soapSendMessageActionBuilder = customizeBuilder(INSTANCE, context, soapSendMessageActionBuilder);

            soapSendMessageActionBuilder.build().execute(context);

            coverageLogger.trace(coverageMarker, "getBook;POST;\"" +
                query.replace("\n", "\\n").replace("\r", "\\r").replace("\"", "\"\"") + "\";\"" +
                body.replace("\n", "\\n").replace("\r", "\\r").replace("\"", "\"\"") + "\";\"" + bodyType + "\"");
        }

        
    }
}
