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

package org.citrusframework.openapi.generator.rest.petstore.request;

import jakarta.annotation.Generated;
import org.citrusframework.testapi.GeneratedApi;
import org.citrusframework.testapi.GeneratedApiRequest;
import jakarta.servlet.http.Cookie;
import org.apache.commons.lang3.StringUtils;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.Resources;
import org.citrusframework.http.actions.HttpActionBuilder;
import org.citrusframework.http.actions.HttpClientRequestActionBuilder;
import org.citrusframework.http.actions.HttpClientRequestActionBuilder.HttpMessageBuilderSupport;
import org.citrusframework.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import org.citrusframework.openapi.generator.rest.petstore.citrus.PetStoreAbstractTestRequest;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Generated(value = "org.citrusframework.openapi.generator.JavaCitrusCodegen")
public class StoreApi implements GeneratedApi
{

    public static final StoreApi INSTANCE = new StoreApi();

    public String getApiTitle() {
        return "OpenAPI Petstore";
    }

    public String getApiVersion() {
        return "1.0.0";
    }

    public String getApiPrefix() {
        return "PetStore";
    }

    public Map<String,String> getApiInfoExtensions() {
        Map<String, String> infoExtensionMap = new HashMap<>();
        infoExtensionMap.put("x-citrus-api-name", "petstore");
        infoExtensionMap.put("x-citrus-app", "PETS");
        return infoExtensionMap;
    }

    /** deleteOrder (DELETE /store/order/{order_id})
        Delete purchase order by ID
        
    **/
    public static class DeleteOrderRequest extends PetStoreAbstractTestRequest implements GeneratedApiRequest {

        private static final String ENDPOINT = "/store/order/{order_id}";
        private final Logger coverageLogger = LoggerFactory.getLogger(DeleteOrderRequest.class);

        private String orderId;

        
        public DeleteOrderRequest() {
            // The name will be overwritten with the tag name using the actual namespace as prefix, when the class is loaded from xml
            setName("PetStore".toLowerCase() + ":deleteOrderRequestType");
        }

        public String getOperationName() {
            return "deleteOrder";
        }

        public String getMethod() {
            return "DELETE";
        }

        public String getPath() {
            return "/store/order/{order_id}";
        }

        /**
        * This method sends the HTTP-Request
        */
        public void sendRequest(TestContext context) {
            HttpClientRequestActionBuilder httpClientRequestActionBuilder = new HttpActionBuilder().client(httpClient).send()
                .delete(replacePathParams(ENDPOINT));

            HttpMessageBuilderSupport messageBuilderSupport = httpClientRequestActionBuilder.getMessageBuilderSupport();
            messageBuilderSupport.accept(responseAcceptType);

            if (cookies != null) {
                cookies.forEach((k, v) -> messageBuilderSupport.cookie(new Cookie(k, v)));
            }

            if (headers != null) {
                headers.forEach((k, v) -> messageBuilderSupport.cookie(new Cookie(k, v)));
                headers.forEach(messageBuilderSupport::header);
            }

            String bodyLog = "";
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

            bodyLog = body.replace("\n", "\\n").replace("\r", "\\r").replace("\"", "\"\"") + "\";\"" + bodyType + "\"";

            Map<String, String> queryParams = new HashMap<>();
            
            String query = queryParams.entrySet().stream().map(e -> "\"" + e.getKey() + "\":\"" + e.getValue() + "\"").collect(Collectors.joining(",", "{", "}"));
            
            httpClientRequestActionBuilder.withReferenceResolver(context.getReferenceResolver());
            httpClientRequestActionBuilder = customizeBuilder(INSTANCE, context, httpClientRequestActionBuilder);

            httpClientRequestActionBuilder.build().execute(context);

            coverageLogger.trace(coverageMarker, "deleteOrder;DELETE;\"" +
                            query.replace("\n", "\\n").replace("\r", "\\r").replace("\"", "\"\"") + "\";\"" +
                            bodyLog);
        }

        public void setOrderId(String orderId) {
            this.orderId = orderId;
        }
        
        private String replacePathParams(String endpoint) {
            endpoint = endpoint.replace("{" + "order_id" + "}", orderId);
            return endpoint;
        }
    }
    /** getInventory (GET /store/inventory)
        Returns pet inventories by status
        
    **/
    public static class GetInventoryRequest extends PetStoreAbstractTestRequest implements GeneratedApiRequest {

        private static final String ENDPOINT = "/store/inventory";
        private final Logger coverageLogger = LoggerFactory.getLogger(GetInventoryRequest.class);

        
        public GetInventoryRequest() {
            // The name will be overwritten with the tag name using the actual namespace as prefix, when the class is loaded from xml
            setName("PetStore".toLowerCase() + ":getInventoryRequestType");
        }

        public String getOperationName() {
            return "getInventory";
        }

        public String getMethod() {
            return "GET";
        }

        public String getPath() {
            return "/store/inventory";
        }

        /**
        * This method sends the HTTP-Request
        */
        public void sendRequest(TestContext context) {
            HttpClientRequestActionBuilder httpClientRequestActionBuilder = new HttpActionBuilder().client(httpClient).send()
                .get(replacePathParams(ENDPOINT));

            HttpMessageBuilderSupport messageBuilderSupport = httpClientRequestActionBuilder.getMessageBuilderSupport();
            messageBuilderSupport.accept(responseAcceptType);

            if (cookies != null) {
                cookies.forEach((k, v) -> messageBuilderSupport.cookie(new Cookie(k, v)));
            }

            if (headers != null) {
                headers.forEach((k, v) -> messageBuilderSupport.cookie(new Cookie(k, v)));
                headers.forEach(messageBuilderSupport::header);
            }

            String bodyLog = "";
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

            bodyLog = body.replace("\n", "\\n").replace("\r", "\\r").replace("\"", "\"\"") + "\";\"" + bodyType + "\"";

            Map<String, String> queryParams = new HashMap<>();
            
            String query = queryParams.entrySet().stream().map(e -> "\"" + e.getKey() + "\":\"" + e.getValue() + "\"").collect(Collectors.joining(",", "{", "}"));
            
            httpClientRequestActionBuilder.withReferenceResolver(context.getReferenceResolver());
            httpClientRequestActionBuilder = customizeBuilder(INSTANCE, context, httpClientRequestActionBuilder);

            httpClientRequestActionBuilder.build().execute(context);

            coverageLogger.trace(coverageMarker, "getInventory;GET;\"" +
                            query.replace("\n", "\\n").replace("\r", "\\r").replace("\"", "\"\"") + "\";\"" +
                            bodyLog);
        }
        
        private String replacePathParams(String endpoint) {
            
            return endpoint;
        }
    }
    /** getOrderById (GET /store/order/{order_id})
        Find purchase order by ID
        
    **/
    public static class GetOrderByIdRequest extends PetStoreAbstractTestRequest implements GeneratedApiRequest {

        private static final String ENDPOINT = "/store/order/{order_id}";
        private final Logger coverageLogger = LoggerFactory.getLogger(GetOrderByIdRequest.class);

        private String orderId;

        
        public GetOrderByIdRequest() {
            // The name will be overwritten with the tag name using the actual namespace as prefix, when the class is loaded from xml
            setName("PetStore".toLowerCase() + ":getOrderByIdRequestType");
        }

        public String getOperationName() {
            return "getOrderById";
        }

        public String getMethod() {
            return "GET";
        }

        public String getPath() {
            return "/store/order/{order_id}";
        }

        /**
        * This method sends the HTTP-Request
        */
        public void sendRequest(TestContext context) {
            HttpClientRequestActionBuilder httpClientRequestActionBuilder = new HttpActionBuilder().client(httpClient).send()
                .get(replacePathParams(ENDPOINT));

            HttpMessageBuilderSupport messageBuilderSupport = httpClientRequestActionBuilder.getMessageBuilderSupport();
            messageBuilderSupport.accept(responseAcceptType);

            if (cookies != null) {
                cookies.forEach((k, v) -> messageBuilderSupport.cookie(new Cookie(k, v)));
            }

            if (headers != null) {
                headers.forEach((k, v) -> messageBuilderSupport.cookie(new Cookie(k, v)));
                headers.forEach(messageBuilderSupport::header);
            }

            String bodyLog = "";
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

            bodyLog = body.replace("\n", "\\n").replace("\r", "\\r").replace("\"", "\"\"") + "\";\"" + bodyType + "\"";

            Map<String, String> queryParams = new HashMap<>();
            
            String query = queryParams.entrySet().stream().map(e -> "\"" + e.getKey() + "\":\"" + e.getValue() + "\"").collect(Collectors.joining(",", "{", "}"));
            
            httpClientRequestActionBuilder.withReferenceResolver(context.getReferenceResolver());
            httpClientRequestActionBuilder = customizeBuilder(INSTANCE, context, httpClientRequestActionBuilder);

            httpClientRequestActionBuilder.build().execute(context);

            coverageLogger.trace(coverageMarker, "getOrderById;GET;\"" +
                            query.replace("\n", "\\n").replace("\r", "\\r").replace("\"", "\"\"") + "\";\"" +
                            bodyLog);
        }

        public void setOrderId(String orderId) {
            this.orderId = orderId;
        }
        
        private String replacePathParams(String endpoint) {
            endpoint = endpoint.replace("{" + "order_id" + "}", orderId);
            return endpoint;
        }
    }
    /** placeOrder (POST /store/order)
        Place an order for a pet
        
    **/
    public static class PlaceOrderRequest extends PetStoreAbstractTestRequest implements GeneratedApiRequest {

        private static final String ENDPOINT = "/store/order";
        private final Logger coverageLogger = LoggerFactory.getLogger(PlaceOrderRequest.class);

        
        public PlaceOrderRequest() {
            // The name will be overwritten with the tag name using the actual namespace as prefix, when the class is loaded from xml
            setName("PetStore".toLowerCase() + ":placeOrderRequestType");
        }

        public String getOperationName() {
            return "placeOrder";
        }

        public String getMethod() {
            return "POST";
        }

        public String getPath() {
            return "/store/order";
        }

        /**
        * This method sends the HTTP-Request
        */
        public void sendRequest(TestContext context) {
            HttpClientRequestActionBuilder httpClientRequestActionBuilder = new HttpActionBuilder().client(httpClient).send()
                .post(replacePathParams(ENDPOINT));

            HttpMessageBuilderSupport messageBuilderSupport = httpClientRequestActionBuilder.getMessageBuilderSupport();
            messageBuilderSupport.accept(responseAcceptType);

            if (cookies != null) {
                cookies.forEach((k, v) -> messageBuilderSupport.cookie(new Cookie(k, v)));
            }

            if (headers != null) {
                headers.forEach((k, v) -> messageBuilderSupport.cookie(new Cookie(k, v)));
                headers.forEach(messageBuilderSupport::header);
            }

            String bodyLog = "";
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

            bodyLog = body.replace("\n", "\\n").replace("\r", "\\r").replace("\"", "\"\"") + "\";\"" + bodyType + "\"";

            Map<String, String> queryParams = new HashMap<>();
            
            String query = queryParams.entrySet().stream().map(e -> "\"" + e.getKey() + "\":\"" + e.getValue() + "\"").collect(Collectors.joining(",", "{", "}"));
            
            httpClientRequestActionBuilder.withReferenceResolver(context.getReferenceResolver());
            httpClientRequestActionBuilder = customizeBuilder(INSTANCE, context, httpClientRequestActionBuilder);

            httpClientRequestActionBuilder.build().execute(context);

            coverageLogger.trace(coverageMarker, "placeOrder;POST;\"" +
                            query.replace("\n", "\\n").replace("\r", "\\r").replace("\"", "\"\"") + "\";\"" +
                            bodyLog);
        }
        
        private String replacePathParams(String endpoint) {
            
            return endpoint;
        }
    }
}
