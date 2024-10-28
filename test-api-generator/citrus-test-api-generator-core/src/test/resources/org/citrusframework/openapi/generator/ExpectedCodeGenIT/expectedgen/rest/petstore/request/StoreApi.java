package org.citrusframework.openapi.generator.rest.petstore.request;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.citrusframework.util.StringUtils.isEmpty;
import static org.citrusframework.util.StringUtils.isNotEmpty;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import org.citrusframework.actions.ReceiveMessageAction;
import org.citrusframework.actions.SendMessageAction;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.openapi.OpenApiSpecification;
import org.citrusframework.openapi.testapi.ApiActionBuilderCustomizer;
import org.citrusframework.openapi.testapi.ParameterStyle;
import org.citrusframework.openapi.testapi.RestApiSendMessageActionBuilder;
import org.citrusframework.openapi.testapi.RestApiReceiveMessageActionBuilder;
import org.citrusframework.openapi.testapi.TestApiUtils;
import org.citrusframework.spi.Resource;
import org.citrusframework.openapi.testapi.GeneratedApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;

import org.citrusframework.openapi.generator.rest.petstore.PetStore;
import org.citrusframework.openapi.generator.rest.petstore.model.*;

@SuppressWarnings("unused")
@jakarta.annotation.Generated(value = "org.citrusframework.openapi.generator.CitrusJavaCodegen", date = "2024-10-28T13:20:43.309584600+01:00[Europe/Zurich]", comments = "Generator version: 7.5.0")
public class StoreApi implements GeneratedApi
{

    @Value("${" + "petstore.base64-encode-api-key:#{false}}")
    private boolean base64EncodeApiKey;

    @Value("${" + "petstore.api-key:#{null}}")
    private String defaultApiKey;

    private final List<ApiActionBuilderCustomizer> customizers;

    private final Endpoint endpoint;

    private final OpenApiSpecification openApiSpecification;

    public StoreApi(Endpoint endpoint)  {
        this(endpoint, emptyList());
    }

    public StoreApi(Endpoint endpoint, List<ApiActionBuilderCustomizer> customizers)  {
        this.endpoint = endpoint;
        this.customizers = customizers;

        URL resource = PetStore.class.getResource("petStore_openApi.yaml");
        if (resource == null) {
            throw new IllegalStateException(format("Cannot find resource '%s'. This resource is typically created during API generation and should therefore be present. Check API generation.", "petStore_openApi.yaml"));
        }
        openApiSpecification = OpenApiSpecification.from(resource);
    }

    public static StoreApi storeApi(Endpoint endpoint) {
        return new StoreApi(endpoint);
    }

    @Override
    public String getApiTitle() {
        return "Swagger Petstore - OpenAPI 3.0";
    }

    @Override
    public String getApiVersion() {
        return "1.0.19";
    }

    @Override
    public String getApiPrefix() {
        return "petStore";
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

    /**
     * Builder with type safe required parameters.
     */
    public DeleteOrderSendActionBuilder sendDeleteOrder(Long orderId)   {
            return new DeleteOrderSendActionBuilder(this, openApiSpecification, orderId);
    }

    /**
     * Builder with required parameters as string, allowing dynamic content using citrus expressions.
     */
    public DeleteOrderSendActionBuilder sendDeleteOrder$(String orderIdExpression )   {
            return new DeleteOrderSendActionBuilder(openApiSpecification, this, orderIdExpression);
    }

    public DeleteOrderReceiveActionBuilder receiveDeleteOrder(@NotNull HttpStatus statusCode)   {
        return new DeleteOrderReceiveActionBuilder(this, openApiSpecification, Integer.toString(statusCode.value()));
    }

    public DeleteOrderReceiveActionBuilder receiveDeleteOrder(@NotNull String statusCode)   {
        return new DeleteOrderReceiveActionBuilder(this, openApiSpecification,  statusCode);
    }

    /**
     * Builder with type safe required parameters.
     */
    public GetInventorySendActionBuilder sendGetInventory()   {
            GetInventorySendActionBuilder builder =  new GetInventorySendActionBuilder(this, openApiSpecification);
            builder.setBase64EncodeApiKey(base64EncodeApiKey);
            return builder;
    }

    public GetInventoryReceiveActionBuilder receiveGetInventory(@NotNull HttpStatus statusCode)   {
        return new GetInventoryReceiveActionBuilder(this, openApiSpecification, Integer.toString(statusCode.value()));
    }

    public GetInventoryReceiveActionBuilder receiveGetInventory(@NotNull String statusCode)   {
        return new GetInventoryReceiveActionBuilder(this, openApiSpecification,  statusCode);
    }

    /**
     * Builder with type safe required parameters.
     */
    public GetOrderByIdSendActionBuilder sendGetOrderById(Long orderId)   {
            return new GetOrderByIdSendActionBuilder(this, openApiSpecification, orderId);
    }

    /**
     * Builder with required parameters as string, allowing dynamic content using citrus expressions.
     */
    public GetOrderByIdSendActionBuilder sendGetOrderById$(String orderIdExpression )   {
            return new GetOrderByIdSendActionBuilder(openApiSpecification, this, orderIdExpression);
    }

    public GetOrderByIdReceiveActionBuilder receiveGetOrderById(@NotNull HttpStatus statusCode)   {
        return new GetOrderByIdReceiveActionBuilder(this, openApiSpecification, Integer.toString(statusCode.value()));
    }

    public GetOrderByIdReceiveActionBuilder receiveGetOrderById(@NotNull String statusCode)   {
        return new GetOrderByIdReceiveActionBuilder(this, openApiSpecification,  statusCode);
    }

    /**
     * Builder with type safe required parameters.
     */
    public PlaceOrderSendActionBuilder sendPlaceOrder()   {
            return new PlaceOrderSendActionBuilder(this, openApiSpecification);
    }

    public PlaceOrderReceiveActionBuilder receivePlaceOrder(@NotNull HttpStatus statusCode)   {
        return new PlaceOrderReceiveActionBuilder(this, openApiSpecification, Integer.toString(statusCode.value()));
    }

    public PlaceOrderReceiveActionBuilder receivePlaceOrder(@NotNull String statusCode)   {
        return new PlaceOrderReceiveActionBuilder(this, openApiSpecification,  statusCode);
    }

    public static class DeleteOrderSendActionBuilder extends
                RestApiSendMessageActionBuilder {

        private static final String METHOD = "DELETE";

        private static final String ENDPOINT = "/api/v3/store/order/{orderId}";

        private static final String OPERATION_NAME = "deleteOrder";

        /**
         * Constructor with type safe required parameters.
         */
        public DeleteOrderSendActionBuilder(StoreApi storeApi, OpenApiSpecification openApiSpecification, Long orderId) {
            super(storeApi, openApiSpecification, METHOD, ENDPOINT, OPERATION_NAME);
            pathParameter("orderId", orderId, ParameterStyle.SIMPLE, false, false);
        }

        /**
         * Constructor with required parameters as string to allow for dynamic content.
         */
            public DeleteOrderSendActionBuilder(OpenApiSpecification openApiSpecification, StoreApi storeApi, String orderIdExpression) {
            super(storeApi, openApiSpecification,  METHOD, ENDPOINT, OPERATION_NAME);
            pathParameter("orderId", orderIdExpression, ParameterStyle.SIMPLE, false, false);
        }

        /**
         * Constructor with required parameters as string to allow for dynamic content.
         */
        public DeleteOrderSendActionBuilder(StoreApi storeApi, OpenApiSpecification openApiSpecification, TestApiClientRequestMessageBuilder messageBuilder, String orderIdExpression) {
            super(storeApi, openApiSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
            pathParameter("orderId", orderIdExpression, ParameterStyle.SIMPLE, false, false);
        }

        public DeleteOrderSendActionBuilder orderId(Long orderId) {
            pathParameter("orderId", orderId, ParameterStyle.SIMPLE, false, false);
            return this;
        }

        public DeleteOrderSendActionBuilder orderId(String orderIdExpression) {
            pathParameter("orderId", orderIdExpression, ParameterStyle.SIMPLE, false, false);
                return this;
        }

        @Override
        public SendMessageAction doBuild() {

            if (getCustomizers() != null) {
                getCustomizers().forEach(customizer -> customizer.customizeRequestBuilder(getGeneratedApi(), this));
            }

            return super.doBuild();
        }
    }

    public static class DeleteOrderReceiveActionBuilder extends
                        RestApiReceiveMessageActionBuilder {

        private static final String METHOD = "DELETE";

        private static final String ENDPOINT = "/api/v3/store/order/{orderId}";

        private static final String OPERATION_NAME = "deleteOrder";

        public DeleteOrderReceiveActionBuilder(StoreApi storeApi, OpenApiSpecification openApiSpecification,  String statusCode) {
            super(storeApi, openApiSpecification, METHOD, ENDPOINT, OPERATION_NAME, statusCode);
        }

        public DeleteOrderReceiveActionBuilder(StoreApi storeApi, OpenApiSpecification openApiSpecification, OpenApiClientResponseMessageBuilder messageBuilder) {
            super(storeApi, openApiSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
        }

        @Override
        public ReceiveMessageAction doBuild() {

            if (getCustomizers() != null) {
                getCustomizers().forEach(customizer -> customizer.customizeResponseBuilder(getGeneratedApi(), this));
            }

            return super.doBuild();
        }

    }

    public static class GetInventorySendActionBuilder extends
                RestApiSendMessageActionBuilder {

        private static final String METHOD = "GET";

        private static final String ENDPOINT = "/api/v3/store/inventory";

        private static final String OPERATION_NAME = "getInventory";

        @Value("${" + "petstore.base64-encode-api-key:#{false}}")
        private boolean base64EncodeApiKey;

        @Value("${" + "petstore.api-key:#{null}}")
        private String defaultApiKey;

        private String apiKey;

        /**
         * Constructor with type safe required parameters.
         */
        public GetInventorySendActionBuilder(StoreApi storeApi, OpenApiSpecification openApiSpecification) {
            super(storeApi, openApiSpecification, METHOD, ENDPOINT, OPERATION_NAME);
        }

        public GetInventorySendActionBuilder(StoreApi storeApi, OpenApiSpecification openApiSpecification, TestApiClientRequestMessageBuilder messageBuilder) {
            super(storeApi, openApiSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
        }

        public void setBase64EncodeApiKey(boolean encode) {
            this.base64EncodeApiKey = encode;
        }

        public GetInventorySendActionBuilder apiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        @Override
        public SendMessageAction doBuild() {
            headerParameter("api_key", getOrDefault(apiKey, defaultApiKey, base64EncodeApiKey));

            if (getCustomizers() != null) {
                getCustomizers().forEach(customizer -> customizer.customizeRequestBuilder(getGeneratedApi(), this));
            }

            return super.doBuild();
        }
    }

    public static class GetInventoryReceiveActionBuilder extends
                        RestApiReceiveMessageActionBuilder {

        private static final String METHOD = "GET";

        private static final String ENDPOINT = "/api/v3/store/inventory";

        private static final String OPERATION_NAME = "getInventory";

        public GetInventoryReceiveActionBuilder(StoreApi storeApi, OpenApiSpecification openApiSpecification,  String statusCode) {
            super(storeApi, openApiSpecification, METHOD, ENDPOINT, OPERATION_NAME, statusCode);
        }

        public GetInventoryReceiveActionBuilder(StoreApi storeApi, OpenApiSpecification openApiSpecification, OpenApiClientResponseMessageBuilder messageBuilder) {
            super(storeApi, openApiSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
        }

        @Override
        public ReceiveMessageAction doBuild() {

            if (getCustomizers() != null) {
                getCustomizers().forEach(customizer -> customizer.customizeResponseBuilder(getGeneratedApi(), this));
            }

            return super.doBuild();
        }

    }

    public static class GetOrderByIdSendActionBuilder extends
                RestApiSendMessageActionBuilder {

        private static final String METHOD = "GET";

        private static final String ENDPOINT = "/api/v3/store/order/{orderId}";

        private static final String OPERATION_NAME = "getOrderById";

        /**
         * Constructor with type safe required parameters.
         */
        public GetOrderByIdSendActionBuilder(StoreApi storeApi, OpenApiSpecification openApiSpecification, Long orderId) {
            super(storeApi, openApiSpecification, METHOD, ENDPOINT, OPERATION_NAME);
            pathParameter("orderId", orderId, ParameterStyle.SIMPLE, false, false);
        }

        /**
         * Constructor with required parameters as string to allow for dynamic content.
         */
            public GetOrderByIdSendActionBuilder(OpenApiSpecification openApiSpecification, StoreApi storeApi, String orderIdExpression) {
            super(storeApi, openApiSpecification,  METHOD, ENDPOINT, OPERATION_NAME);
            pathParameter("orderId", orderIdExpression, ParameterStyle.SIMPLE, false, false);
        }

        /**
         * Constructor with required parameters as string to allow for dynamic content.
         */
        public GetOrderByIdSendActionBuilder(StoreApi storeApi, OpenApiSpecification openApiSpecification, TestApiClientRequestMessageBuilder messageBuilder, String orderIdExpression) {
            super(storeApi, openApiSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
            pathParameter("orderId", orderIdExpression, ParameterStyle.SIMPLE, false, false);
        }

        public GetOrderByIdSendActionBuilder orderId(Long orderId) {
            pathParameter("orderId", orderId, ParameterStyle.SIMPLE, false, false);
            return this;
        }

        public GetOrderByIdSendActionBuilder orderId(String orderIdExpression) {
            pathParameter("orderId", orderIdExpression, ParameterStyle.SIMPLE, false, false);
                return this;
        }

        @Override
        public SendMessageAction doBuild() {

            if (getCustomizers() != null) {
                getCustomizers().forEach(customizer -> customizer.customizeRequestBuilder(getGeneratedApi(), this));
            }

            return super.doBuild();
        }
    }

    public static class GetOrderByIdReceiveActionBuilder extends
                        RestApiReceiveMessageActionBuilder {

        private static final String METHOD = "GET";

        private static final String ENDPOINT = "/api/v3/store/order/{orderId}";

        private static final String OPERATION_NAME = "getOrderById";

        public GetOrderByIdReceiveActionBuilder(StoreApi storeApi, OpenApiSpecification openApiSpecification,  String statusCode) {
            super(storeApi, openApiSpecification, METHOD, ENDPOINT, OPERATION_NAME, statusCode);
        }

        public GetOrderByIdReceiveActionBuilder(StoreApi storeApi, OpenApiSpecification openApiSpecification, OpenApiClientResponseMessageBuilder messageBuilder) {
            super(storeApi, openApiSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
        }

        @Override
        public ReceiveMessageAction doBuild() {

            if (getCustomizers() != null) {
                getCustomizers().forEach(customizer -> customizer.customizeResponseBuilder(getGeneratedApi(), this));
            }

            return super.doBuild();
        }

    }

    public static class PlaceOrderSendActionBuilder extends
                RestApiSendMessageActionBuilder {

        private static final String METHOD = "POST";

        private static final String ENDPOINT = "/api/v3/store/order";

        private static final String OPERATION_NAME = "placeOrder";

        /**
         * Constructor with type safe required parameters.
         */
        public PlaceOrderSendActionBuilder(StoreApi storeApi, OpenApiSpecification openApiSpecification) {
            super(storeApi, openApiSpecification, METHOD, ENDPOINT, OPERATION_NAME);
        }

        public PlaceOrderSendActionBuilder(StoreApi storeApi, OpenApiSpecification openApiSpecification, TestApiClientRequestMessageBuilder messageBuilder) {
            super(storeApi, openApiSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
        }

        public PlaceOrderSendActionBuilder order(Order order) {
            return this;
        }

        public void setOrder(Order order) {
        }

        public PlaceOrderSendActionBuilder order(String orderExpression) {
            return this;
        }

        public void setOrder(String orderExpression) {
        }

        @Override
        public SendMessageAction doBuild() {

            if (getCustomizers() != null) {
                getCustomizers().forEach(customizer -> customizer.customizeRequestBuilder(getGeneratedApi(), this));
            }

            return super.doBuild();
        }
    }

    public static class PlaceOrderReceiveActionBuilder extends
                        RestApiReceiveMessageActionBuilder {

        private static final String METHOD = "POST";

        private static final String ENDPOINT = "/api/v3/store/order";

        private static final String OPERATION_NAME = "placeOrder";

        public PlaceOrderReceiveActionBuilder(StoreApi storeApi, OpenApiSpecification openApiSpecification,  String statusCode) {
            super(storeApi, openApiSpecification, METHOD, ENDPOINT, OPERATION_NAME, statusCode);
        }

        public PlaceOrderReceiveActionBuilder(StoreApi storeApi, OpenApiSpecification openApiSpecification, OpenApiClientResponseMessageBuilder messageBuilder) {
            super(storeApi, openApiSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
        }

        @Override
        public ReceiveMessageAction doBuild() {

            if (getCustomizers() != null) {
                getCustomizers().forEach(customizer -> customizer.customizeResponseBuilder(getGeneratedApi(), this));
            }

            return super.doBuild();
        }

    }
}
