package org.citrusframework.openapi.generator.rest.petstore.request;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.citrusframework.util.StringUtils.isEmpty;
import static org.citrusframework.util.StringUtils.isNotEmpty;

import static org.citrusframework.openapi.generator.rest.petstore.PetStoreOpenApi.petStoreSpecification;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.UUID;
import org.citrusframework.actions.ReceiveMessageAction;
import org.citrusframework.actions.SendMessageAction;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.openapi.OpenApiSpecification;
import org.citrusframework.openapi.testapi.ApiActionBuilderCustomizer;
import org.citrusframework.openapi.testapi.GeneratedApiOperationInfo;
import org.citrusframework.openapi.testapi.ParameterStyle;
import org.citrusframework.openapi.testapi.RestApiSendMessageActionBuilder;
import org.citrusframework.openapi.testapi.RestApiReceiveMessageActionBuilder;
import org.citrusframework.openapi.testapi.TestApiUtils;
import org.citrusframework.spi.Resource;
import org.citrusframework.openapi.testapi.GeneratedApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;

import org.citrusframework.openapi.generator.rest.petstore.PetStoreOpenApi;
import org.citrusframework.openapi.generator.rest.petstore.model.ModelApiResponse;
import org.citrusframework.openapi.generator.rest.petstore.model.Pet;

@SuppressWarnings("unused")
@jakarta.annotation.Generated(value = "org.citrusframework.openapi.generator.CitrusJavaCodegen", date = "2025-01-29T23:14:47.794716200+01:00[Europe/Zurich]", comments = "Generator version: 7.9.0")
public class PetApi implements GeneratedApi
{

    @Value("${" + "petstore.base64-encode-api-key:#{false}}")
    private boolean base64EncodeApiKey;

    @Value("${" + "petstore.api-key:#{null}}")
    private String defaultApiKey;

    private final List<ApiActionBuilderCustomizer> customizers;

    /**
    * An optional default endpoint which will be passed into the requests.
    */
    private final Endpoint defaultEndpoint;

    public PetApi(@Nullable Endpoint defaultEndpoint)  {
        this(defaultEndpoint, emptyList());
    }

    public PetApi(@Nullable Endpoint defaultEndpoint, @Nullable List<ApiActionBuilderCustomizer> customizers)  {
        this.defaultEndpoint = defaultEndpoint;
        this.customizers = customizers;
    }

    public static PetApi petApi(Endpoint defaultEndpoint) {
        return new PetApi(defaultEndpoint);
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
    @Nullable
    public Endpoint getEndpoint() {
        return defaultEndpoint;
    }

    @Override
    public List<ApiActionBuilderCustomizer> getCustomizers() {
        return customizers;
    }

    /**
     * Builder with type safe required parameters.
     */
    public AddPetSendActionBuilder sendAddPet()   {
            return new AddPetSendActionBuilder(this);
    }

    public AddPetReceiveActionBuilder receiveAddPet(@NotNull HttpStatus statusCode)   {
        return new AddPetReceiveActionBuilder(this, Integer.toString(statusCode.value()));
    }

    public AddPetReceiveActionBuilder receiveAddPet(@NotNull String statusCode)   {
        return new AddPetReceiveActionBuilder(this,  statusCode);
    }

    /**
     * Builder with type safe required parameters.
     */
    public DeletePetSendActionBuilder sendDeletePet(Long petId)   {
            return new DeletePetSendActionBuilder(this, petId);
    }

    /**
     * Builder with required parameters as string, allowing dynamic content using citrus expressions.
     */
    public DeletePetSendActionBuilder sendDeletePet$(String petIdExpression )   {
            return new DeletePetSendActionBuilder(petIdExpression, this);
    }

    public DeletePetReceiveActionBuilder receiveDeletePet(@NotNull HttpStatus statusCode)   {
        return new DeletePetReceiveActionBuilder(this, Integer.toString(statusCode.value()));
    }

    public DeletePetReceiveActionBuilder receiveDeletePet(@NotNull String statusCode)   {
        return new DeletePetReceiveActionBuilder(this,  statusCode);
    }

    /**
     * Builder with type safe required parameters.
     */
    public FindPetsByStatusSendActionBuilder sendFindPetsByStatus()   {
            return new FindPetsByStatusSendActionBuilder(this);
    }

    public FindPetsByStatusReceiveActionBuilder receiveFindPetsByStatus(@NotNull HttpStatus statusCode)   {
        return new FindPetsByStatusReceiveActionBuilder(this, Integer.toString(statusCode.value()));
    }

    public FindPetsByStatusReceiveActionBuilder receiveFindPetsByStatus(@NotNull String statusCode)   {
        return new FindPetsByStatusReceiveActionBuilder(this,  statusCode);
    }

    /**
     * Builder with type safe required parameters.
     */
    public FindPetsByTagsSendActionBuilder sendFindPetsByTags()   {
            return new FindPetsByTagsSendActionBuilder(this);
    }

    public FindPetsByTagsReceiveActionBuilder receiveFindPetsByTags(@NotNull HttpStatus statusCode)   {
        return new FindPetsByTagsReceiveActionBuilder(this, Integer.toString(statusCode.value()));
    }

    public FindPetsByTagsReceiveActionBuilder receiveFindPetsByTags(@NotNull String statusCode)   {
        return new FindPetsByTagsReceiveActionBuilder(this,  statusCode);
    }

    /**
     * Builder with type safe required parameters.
     */
    public GetPetByIdSendActionBuilder sendGetPetById(Long petId)   {
            GetPetByIdSendActionBuilder builder =  new GetPetByIdSendActionBuilder(this, petId);
            builder.setBase64EncodeApiKey(base64EncodeApiKey);
            return builder;
    }

    /**
     * Builder with required parameters as string, allowing dynamic content using citrus expressions.
     */
    public GetPetByIdSendActionBuilder sendGetPetById$(String petIdExpression )   {
            GetPetByIdSendActionBuilder builder =  new GetPetByIdSendActionBuilder(petIdExpression, this);
            builder.setBase64EncodeApiKey(base64EncodeApiKey);
            builder.setApiKey(defaultApiKey);
            return builder;
    }

    public GetPetByIdReceiveActionBuilder receiveGetPetById(@NotNull HttpStatus statusCode)   {
        return new GetPetByIdReceiveActionBuilder(this, Integer.toString(statusCode.value()));
    }

    public GetPetByIdReceiveActionBuilder receiveGetPetById(@NotNull String statusCode)   {
        return new GetPetByIdReceiveActionBuilder(this,  statusCode);
    }

    /**
     * Builder with type safe required parameters.
     */
    public UpdatePetSendActionBuilder sendUpdatePet()   {
            return new UpdatePetSendActionBuilder(this);
    }

    public UpdatePetReceiveActionBuilder receiveUpdatePet(@NotNull HttpStatus statusCode)   {
        return new UpdatePetReceiveActionBuilder(this, Integer.toString(statusCode.value()));
    }

    public UpdatePetReceiveActionBuilder receiveUpdatePet(@NotNull String statusCode)   {
        return new UpdatePetReceiveActionBuilder(this,  statusCode);
    }

    /**
     * Builder with type safe required parameters.
     */
    public UpdatePetWithFormSendActionBuilder sendUpdatePetWithForm(Long petId)   {
            return new UpdatePetWithFormSendActionBuilder(this, petId);
    }

    /**
     * Builder with required parameters as string, allowing dynamic content using citrus expressions.
     */
    public UpdatePetWithFormSendActionBuilder sendUpdatePetWithForm$(String petIdExpression )   {
            return new UpdatePetWithFormSendActionBuilder(petIdExpression, this);
    }

    public UpdatePetWithFormReceiveActionBuilder receiveUpdatePetWithForm(@NotNull HttpStatus statusCode)   {
        return new UpdatePetWithFormReceiveActionBuilder(this, Integer.toString(statusCode.value()));
    }

    public UpdatePetWithFormReceiveActionBuilder receiveUpdatePetWithForm(@NotNull String statusCode)   {
        return new UpdatePetWithFormReceiveActionBuilder(this,  statusCode);
    }

    /**
     * Builder with type safe required parameters.
     */
    public UploadFileSendActionBuilder sendUploadFile(Long petId)   {
            return new UploadFileSendActionBuilder(this, petId);
    }

    /**
     * Builder with required parameters as string, allowing dynamic content using citrus expressions.
     */
    public UploadFileSendActionBuilder sendUploadFile$(String petIdExpression )   {
            return new UploadFileSendActionBuilder(petIdExpression, this);
    }

    public UploadFileReceiveActionBuilder receiveUploadFile(@NotNull HttpStatus statusCode)   {
        return new UploadFileReceiveActionBuilder(this, Integer.toString(statusCode.value()));
    }

    public UploadFileReceiveActionBuilder receiveUploadFile(@NotNull String statusCode)   {
        return new UploadFileReceiveActionBuilder(this,  statusCode);
    }

    public static class AddPetSendActionBuilder extends
                RestApiSendMessageActionBuilder implements GeneratedApiOperationInfo {

        private static final String METHOD = "POST";

        private static final String ENDPOINT = "/api/v3/pet";

        private static final String OPERATION_NAME = "addPet";

        /**
         * Constructor with type safe required parameters.
         */
        public AddPetSendActionBuilder(PetApi petApi) {
            super(petApi, petStoreSpecification, METHOD, ENDPOINT, OPERATION_NAME);
        }

        @Override
        public String getOperationName() {
            return OPERATION_NAME;
        }

        @Override
        public String getMethod() {
            return METHOD;
        }

        @Override
        public String getPath() {
            return ENDPOINT;
        }

        public AddPetSendActionBuilder(PetApi petApi, TestApiClientRequestMessageBuilder messageBuilder) {
            super(petApi, petStoreSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
        }

        @Override
        public SendMessageAction doBuild() {

            if (getCustomizers() != null) {
                getCustomizers().forEach(customizer -> customizer.customizeRequestBuilder(this, this));
            }

            return super.doBuild();
        }
    }

    public static class AddPetReceiveActionBuilder extends
                        RestApiReceiveMessageActionBuilder implements GeneratedApiOperationInfo {

        private static final String METHOD = "POST";

        private static final String ENDPOINT = "/api/v3/pet";

        private static final String OPERATION_NAME = "addPet";

        public AddPetReceiveActionBuilder(PetApi petApi,  String statusCode) {
            super(petApi, petStoreSpecification, METHOD, ENDPOINT, OPERATION_NAME, statusCode);
        }

        public AddPetReceiveActionBuilder(PetApi petApi, OpenApiClientResponseMessageBuilder messageBuilder) {
            super(petApi, petStoreSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
        }

        @Override
        public String getOperationName() {
            return OPERATION_NAME;
        }

        @Override
        public String getMethod() {
            return METHOD;
        }

        @Override
        public String getPath() {
            return ENDPOINT;
        }

        @Override
        public ReceiveMessageAction doBuild() {

            if (getCustomizers() != null) {
                getCustomizers().forEach(customizer -> customizer.customizeResponseBuilder(this, this));
            }

            return super.doBuild();
        }

    }

    public static class DeletePetSendActionBuilder extends
                RestApiSendMessageActionBuilder implements GeneratedApiOperationInfo {

        private static final String METHOD = "DELETE";

        private static final String ENDPOINT = "/api/v3/pet/{petId}";

        private static final String OPERATION_NAME = "deletePet";

        /**
         * Constructor with type safe required parameters.
         */
        public DeletePetSendActionBuilder(PetApi petApi, Long petId) {
            super(petApi, petStoreSpecification, METHOD, ENDPOINT, OPERATION_NAME);
            pathParameter("petId", petId, ParameterStyle.SIMPLE, false, false);
        }

        /**
         * Constructor with required parameters as string to allow for dynamic content.
         */
            public DeletePetSendActionBuilder(String petIdExpression, PetApi petApi) {
            super(petApi, petStoreSpecification,  METHOD, ENDPOINT, OPERATION_NAME);
            pathParameter("petId", petIdExpression, ParameterStyle.SIMPLE, false, false);
        }

        @Override
        public String getOperationName() {
            return OPERATION_NAME;
        }

        @Override
        public String getMethod() {
            return METHOD;
        }

        @Override
        public String getPath() {
            return ENDPOINT;
        }

        /**
         * Constructor with required parameters as string to allow for dynamic content.
         */
        public DeletePetSendActionBuilder(PetApi petApi, TestApiClientRequestMessageBuilder messageBuilder, String petIdExpression) {
            super(petApi, petStoreSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
            pathParameter("petId", petIdExpression, ParameterStyle.SIMPLE, false, false);
        }

        public DeletePetSendActionBuilder petId(Long petId) {
            pathParameter("petId", petId, ParameterStyle.SIMPLE, false, false);
            return this;
        }

        public DeletePetSendActionBuilder petId(String petIdExpression) {
            pathParameter("petId", petIdExpression, ParameterStyle.SIMPLE, false, false);
                return this;
        }

        public DeletePetSendActionBuilder apiKey(String apiKey) {
            headerParameter("api_key", apiKey, ParameterStyle.SIMPLE, false, false);
            return this;
        }

        public void setApiKey(String apiKey) {
            headerParameter("api_key", apiKey, ParameterStyle.SIMPLE, false, false);
        }

        @Override
        public SendMessageAction doBuild() {

            if (getCustomizers() != null) {
                getCustomizers().forEach(customizer -> customizer.customizeRequestBuilder(this, this));
            }

            return super.doBuild();
        }
    }

    public static class DeletePetReceiveActionBuilder extends
                        RestApiReceiveMessageActionBuilder implements GeneratedApiOperationInfo {

        private static final String METHOD = "DELETE";

        private static final String ENDPOINT = "/api/v3/pet/{petId}";

        private static final String OPERATION_NAME = "deletePet";

        public DeletePetReceiveActionBuilder(PetApi petApi,  String statusCode) {
            super(petApi, petStoreSpecification, METHOD, ENDPOINT, OPERATION_NAME, statusCode);
        }

        public DeletePetReceiveActionBuilder(PetApi petApi, OpenApiClientResponseMessageBuilder messageBuilder) {
            super(petApi, petStoreSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
        }

        @Override
        public String getOperationName() {
            return OPERATION_NAME;
        }

        @Override
        public String getMethod() {
            return METHOD;
        }

        @Override
        public String getPath() {
            return ENDPOINT;
        }

        @Override
        public ReceiveMessageAction doBuild() {

            if (getCustomizers() != null) {
                getCustomizers().forEach(customizer -> customizer.customizeResponseBuilder(this, this));
            }

            return super.doBuild();
        }

    }

    public static class FindPetsByStatusSendActionBuilder extends
                RestApiSendMessageActionBuilder implements GeneratedApiOperationInfo {

        private static final String METHOD = "GET";

        private static final String ENDPOINT = "/api/v3/pet/findByStatus";

        private static final String OPERATION_NAME = "findPetsByStatus";

        /**
         * Constructor with type safe required parameters.
         */
        public FindPetsByStatusSendActionBuilder(PetApi petApi) {
            super(petApi, petStoreSpecification, METHOD, ENDPOINT, OPERATION_NAME);
        }

        @Override
        public String getOperationName() {
            return OPERATION_NAME;
        }

        @Override
        public String getMethod() {
            return METHOD;
        }

        @Override
        public String getPath() {
            return ENDPOINT;
        }

        public FindPetsByStatusSendActionBuilder(PetApi petApi, TestApiClientRequestMessageBuilder messageBuilder) {
            super(petApi, petStoreSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
        }

        public FindPetsByStatusSendActionBuilder status(String status) {
            queryParameter("status", status, ParameterStyle.FORM, true, false);
            return this;
        }

        public void setStatus(String status) {
            queryParameter("status", status, ParameterStyle.FORM, true, false);
        }

        @Override
        public SendMessageAction doBuild() {

            if (getCustomizers() != null) {
                getCustomizers().forEach(customizer -> customizer.customizeRequestBuilder(this, this));
            }

            return super.doBuild();
        }
    }

    public static class FindPetsByStatusReceiveActionBuilder extends
                        RestApiReceiveMessageActionBuilder implements GeneratedApiOperationInfo {

        private static final String METHOD = "GET";

        private static final String ENDPOINT = "/api/v3/pet/findByStatus";

        private static final String OPERATION_NAME = "findPetsByStatus";

        public FindPetsByStatusReceiveActionBuilder(PetApi petApi,  String statusCode) {
            super(petApi, petStoreSpecification, METHOD, ENDPOINT, OPERATION_NAME, statusCode);
        }

        public FindPetsByStatusReceiveActionBuilder(PetApi petApi, OpenApiClientResponseMessageBuilder messageBuilder) {
            super(petApi, petStoreSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
        }

        @Override
        public String getOperationName() {
            return OPERATION_NAME;
        }

        @Override
        public String getMethod() {
            return METHOD;
        }

        @Override
        public String getPath() {
            return ENDPOINT;
        }

        @Override
        public ReceiveMessageAction doBuild() {

            if (getCustomizers() != null) {
                getCustomizers().forEach(customizer -> customizer.customizeResponseBuilder(this, this));
            }

            return super.doBuild();
        }

    }

    public static class FindPetsByTagsSendActionBuilder extends
                RestApiSendMessageActionBuilder implements GeneratedApiOperationInfo {

        private static final String METHOD = "GET";

        private static final String ENDPOINT = "/api/v3/pet/findByTags";

        private static final String OPERATION_NAME = "findPetsByTags";

        /**
         * Constructor with type safe required parameters.
         */
        public FindPetsByTagsSendActionBuilder(PetApi petApi) {
            super(petApi, petStoreSpecification, METHOD, ENDPOINT, OPERATION_NAME);
        }

        @Override
        public String getOperationName() {
            return OPERATION_NAME;
        }

        @Override
        public String getMethod() {
            return METHOD;
        }

        @Override
        public String getPath() {
            return ENDPOINT;
        }

        public FindPetsByTagsSendActionBuilder(PetApi petApi, TestApiClientRequestMessageBuilder messageBuilder) {
            super(petApi, petStoreSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
        }

        public FindPetsByTagsSendActionBuilder tags(String...tags) {
            queryParameter("tags", tags, ParameterStyle.FORM, true, false);
            return this;
        }

        public void setTags(String...tags) {
            queryParameter("tags", tags, ParameterStyle.FORM, true, false);
        }

        @Override
        public SendMessageAction doBuild() {

            if (getCustomizers() != null) {
                getCustomizers().forEach(customizer -> customizer.customizeRequestBuilder(this, this));
            }

            return super.doBuild();
        }
    }

    public static class FindPetsByTagsReceiveActionBuilder extends
                        RestApiReceiveMessageActionBuilder implements GeneratedApiOperationInfo {

        private static final String METHOD = "GET";

        private static final String ENDPOINT = "/api/v3/pet/findByTags";

        private static final String OPERATION_NAME = "findPetsByTags";

        public FindPetsByTagsReceiveActionBuilder(PetApi petApi,  String statusCode) {
            super(petApi, petStoreSpecification, METHOD, ENDPOINT, OPERATION_NAME, statusCode);
        }

        public FindPetsByTagsReceiveActionBuilder(PetApi petApi, OpenApiClientResponseMessageBuilder messageBuilder) {
            super(petApi, petStoreSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
        }

        @Override
        public String getOperationName() {
            return OPERATION_NAME;
        }

        @Override
        public String getMethod() {
            return METHOD;
        }

        @Override
        public String getPath() {
            return ENDPOINT;
        }

        @Override
        public ReceiveMessageAction doBuild() {

            if (getCustomizers() != null) {
                getCustomizers().forEach(customizer -> customizer.customizeResponseBuilder(this, this));
            }

            return super.doBuild();
        }

    }

    public static class GetPetByIdSendActionBuilder extends
                RestApiSendMessageActionBuilder implements GeneratedApiOperationInfo {

        private static final String METHOD = "GET";

        private static final String ENDPOINT = "/api/v3/pet/{petId}";

        private static final String OPERATION_NAME = "getPetById";

        @Value("${" + "petstore.base64-encode-api-key:#{false}}")
        private boolean base64EncodeApiKey;

        @Value("${" + "petstore.api-key:#{null}}")
        private String defaultApiKey;

        private String apiKey;

        /**
         * Constructor with type safe required parameters.
         */
        public GetPetByIdSendActionBuilder(PetApi petApi, Long petId) {
            super(petApi, petStoreSpecification, METHOD, ENDPOINT, OPERATION_NAME);
            pathParameter("petId", petId, ParameterStyle.SIMPLE, false, false);
        }

        /**
         * Constructor with required parameters as string to allow for dynamic content.
         */
            public GetPetByIdSendActionBuilder(String petIdExpression, PetApi petApi) {
            super(petApi, petStoreSpecification,  METHOD, ENDPOINT, OPERATION_NAME);
            pathParameter("petId", petIdExpression, ParameterStyle.SIMPLE, false, false);
        }

        @Override
        public String getOperationName() {
            return OPERATION_NAME;
        }

        @Override
        public String getMethod() {
            return METHOD;
        }

        @Override
        public String getPath() {
            return ENDPOINT;
        }

        /**
         * Constructor with required parameters as string to allow for dynamic content.
         */
        public GetPetByIdSendActionBuilder(PetApi petApi, TestApiClientRequestMessageBuilder messageBuilder, String petIdExpression) {
            super(petApi, petStoreSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
            pathParameter("petId", petIdExpression, ParameterStyle.SIMPLE, false, false);
        }

        public GetPetByIdSendActionBuilder petId(Long petId) {
            pathParameter("petId", petId, ParameterStyle.SIMPLE, false, false);
            return this;
        }

        public GetPetByIdSendActionBuilder petId(String petIdExpression) {
            pathParameter("petId", petIdExpression, ParameterStyle.SIMPLE, false, false);
                return this;
        }

        public void setBase64EncodeApiKey(boolean encode) {
            this.base64EncodeApiKey = encode;
        }

        public GetPetByIdSendActionBuilder apiKey(String apiKey) {
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
                getCustomizers().forEach(customizer -> customizer.customizeRequestBuilder(this, this));
            }

            return super.doBuild();
        }
    }

    public static class GetPetByIdReceiveActionBuilder extends
                        RestApiReceiveMessageActionBuilder implements GeneratedApiOperationInfo {

        private static final String METHOD = "GET";

        private static final String ENDPOINT = "/api/v3/pet/{petId}";

        private static final String OPERATION_NAME = "getPetById";

        public GetPetByIdReceiveActionBuilder(PetApi petApi,  String statusCode) {
            super(petApi, petStoreSpecification, METHOD, ENDPOINT, OPERATION_NAME, statusCode);
        }

        public GetPetByIdReceiveActionBuilder(PetApi petApi, OpenApiClientResponseMessageBuilder messageBuilder) {
            super(petApi, petStoreSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
        }

        @Override
        public String getOperationName() {
            return OPERATION_NAME;
        }

        @Override
        public String getMethod() {
            return METHOD;
        }

        @Override
        public String getPath() {
            return ENDPOINT;
        }

        @Override
        public ReceiveMessageAction doBuild() {

            if (getCustomizers() != null) {
                getCustomizers().forEach(customizer -> customizer.customizeResponseBuilder(this, this));
            }

            return super.doBuild();
        }

    }

    public static class UpdatePetSendActionBuilder extends
                RestApiSendMessageActionBuilder implements GeneratedApiOperationInfo {

        private static final String METHOD = "PUT";

        private static final String ENDPOINT = "/api/v3/pet";

        private static final String OPERATION_NAME = "updatePet";

        /**
         * Constructor with type safe required parameters.
         */
        public UpdatePetSendActionBuilder(PetApi petApi) {
            super(petApi, petStoreSpecification, METHOD, ENDPOINT, OPERATION_NAME);
        }

        @Override
        public String getOperationName() {
            return OPERATION_NAME;
        }

        @Override
        public String getMethod() {
            return METHOD;
        }

        @Override
        public String getPath() {
            return ENDPOINT;
        }

        public UpdatePetSendActionBuilder(PetApi petApi, TestApiClientRequestMessageBuilder messageBuilder) {
            super(petApi, petStoreSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
        }

        @Override
        public SendMessageAction doBuild() {

            if (getCustomizers() != null) {
                getCustomizers().forEach(customizer -> customizer.customizeRequestBuilder(this, this));
            }

            return super.doBuild();
        }
    }

    public static class UpdatePetReceiveActionBuilder extends
                        RestApiReceiveMessageActionBuilder implements GeneratedApiOperationInfo {

        private static final String METHOD = "PUT";

        private static final String ENDPOINT = "/api/v3/pet";

        private static final String OPERATION_NAME = "updatePet";

        public UpdatePetReceiveActionBuilder(PetApi petApi,  String statusCode) {
            super(petApi, petStoreSpecification, METHOD, ENDPOINT, OPERATION_NAME, statusCode);
        }

        public UpdatePetReceiveActionBuilder(PetApi petApi, OpenApiClientResponseMessageBuilder messageBuilder) {
            super(petApi, petStoreSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
        }

        @Override
        public String getOperationName() {
            return OPERATION_NAME;
        }

        @Override
        public String getMethod() {
            return METHOD;
        }

        @Override
        public String getPath() {
            return ENDPOINT;
        }

        @Override
        public ReceiveMessageAction doBuild() {

            if (getCustomizers() != null) {
                getCustomizers().forEach(customizer -> customizer.customizeResponseBuilder(this, this));
            }

            return super.doBuild();
        }

    }

    public static class UpdatePetWithFormSendActionBuilder extends
                RestApiSendMessageActionBuilder implements GeneratedApiOperationInfo {

        private static final String METHOD = "POST";

        private static final String ENDPOINT = "/api/v3/pet/{petId}";

        private static final String OPERATION_NAME = "updatePetWithForm";

        /**
         * Constructor with type safe required parameters.
         */
        public UpdatePetWithFormSendActionBuilder(PetApi petApi, Long petId) {
            super(petApi, petStoreSpecification, METHOD, ENDPOINT, OPERATION_NAME);
            pathParameter("petId", petId, ParameterStyle.SIMPLE, false, false);
        }

        /**
         * Constructor with required parameters as string to allow for dynamic content.
         */
            public UpdatePetWithFormSendActionBuilder(String petIdExpression, PetApi petApi) {
            super(petApi, petStoreSpecification,  METHOD, ENDPOINT, OPERATION_NAME);
            pathParameter("petId", petIdExpression, ParameterStyle.SIMPLE, false, false);
        }

        @Override
        public String getOperationName() {
            return OPERATION_NAME;
        }

        @Override
        public String getMethod() {
            return METHOD;
        }

        @Override
        public String getPath() {
            return ENDPOINT;
        }

        /**
         * Constructor with required parameters as string to allow for dynamic content.
         */
        public UpdatePetWithFormSendActionBuilder(PetApi petApi, TestApiClientRequestMessageBuilder messageBuilder, String petIdExpression) {
            super(petApi, petStoreSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
            pathParameter("petId", petIdExpression, ParameterStyle.SIMPLE, false, false);
        }

        public UpdatePetWithFormSendActionBuilder petId(Long petId) {
            pathParameter("petId", petId, ParameterStyle.SIMPLE, false, false);
            return this;
        }

        public UpdatePetWithFormSendActionBuilder petId(String petIdExpression) {
            pathParameter("petId", petIdExpression, ParameterStyle.SIMPLE, false, false);
                return this;
        }

        public UpdatePetWithFormSendActionBuilder _name(String _name) {
            queryParameter("name", _name, ParameterStyle.FORM, true, false);
            return this;
        }

        public void set_name(String _name) {
            queryParameter("name", _name, ParameterStyle.FORM, true, false);
        }

        public UpdatePetWithFormSendActionBuilder status(String status) {
            queryParameter("status", status, ParameterStyle.FORM, true, false);
            return this;
        }

        public void setStatus(String status) {
            queryParameter("status", status, ParameterStyle.FORM, true, false);
        }

        @Override
        public SendMessageAction doBuild() {

            if (getCustomizers() != null) {
                getCustomizers().forEach(customizer -> customizer.customizeRequestBuilder(this, this));
            }

            return super.doBuild();
        }
    }

    public static class UpdatePetWithFormReceiveActionBuilder extends
                        RestApiReceiveMessageActionBuilder implements GeneratedApiOperationInfo {

        private static final String METHOD = "POST";

        private static final String ENDPOINT = "/api/v3/pet/{petId}";

        private static final String OPERATION_NAME = "updatePetWithForm";

        public UpdatePetWithFormReceiveActionBuilder(PetApi petApi,  String statusCode) {
            super(petApi, petStoreSpecification, METHOD, ENDPOINT, OPERATION_NAME, statusCode);
        }

        public UpdatePetWithFormReceiveActionBuilder(PetApi petApi, OpenApiClientResponseMessageBuilder messageBuilder) {
            super(petApi, petStoreSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
        }

        @Override
        public String getOperationName() {
            return OPERATION_NAME;
        }

        @Override
        public String getMethod() {
            return METHOD;
        }

        @Override
        public String getPath() {
            return ENDPOINT;
        }

        @Override
        public ReceiveMessageAction doBuild() {

            if (getCustomizers() != null) {
                getCustomizers().forEach(customizer -> customizer.customizeResponseBuilder(this, this));
            }

            return super.doBuild();
        }

    }

    public static class UploadFileSendActionBuilder extends
                RestApiSendMessageActionBuilder implements GeneratedApiOperationInfo {

        private static final String METHOD = "POST";

        private static final String ENDPOINT = "/api/v3/pet/{petId}/uploadImage";

        private static final String OPERATION_NAME = "uploadFile";

        /**
         * Constructor with type safe required parameters.
         */
        public UploadFileSendActionBuilder(PetApi petApi, Long petId) {
            super(petApi, petStoreSpecification, METHOD, ENDPOINT, OPERATION_NAME);
            pathParameter("petId", petId, ParameterStyle.SIMPLE, false, false);
        }

        /**
         * Constructor with required parameters as string to allow for dynamic content.
         */
            public UploadFileSendActionBuilder(String petIdExpression, PetApi petApi) {
            super(petApi, petStoreSpecification,  METHOD, ENDPOINT, OPERATION_NAME);
            pathParameter("petId", petIdExpression, ParameterStyle.SIMPLE, false, false);
        }

        @Override
        public String getOperationName() {
            return OPERATION_NAME;
        }

        @Override
        public String getMethod() {
            return METHOD;
        }

        @Override
        public String getPath() {
            return ENDPOINT;
        }

        /**
         * Constructor with required parameters as string to allow for dynamic content.
         */
        public UploadFileSendActionBuilder(PetApi petApi, TestApiClientRequestMessageBuilder messageBuilder, String petIdExpression) {
            super(petApi, petStoreSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
            pathParameter("petId", petIdExpression, ParameterStyle.SIMPLE, false, false);
        }

        public UploadFileSendActionBuilder petId(Long petId) {
            pathParameter("petId", petId, ParameterStyle.SIMPLE, false, false);
            return this;
        }

        public UploadFileSendActionBuilder petId(String petIdExpression) {
            pathParameter("petId", petIdExpression, ParameterStyle.SIMPLE, false, false);
                return this;
        }

        public UploadFileSendActionBuilder additionalMetadata(String additionalMetadata) {
            queryParameter("additionalMetadata", additionalMetadata, ParameterStyle.FORM, true, false);
            return this;
        }

        public void setAdditionalMetadata(String additionalMetadata) {
            queryParameter("additionalMetadata", additionalMetadata, ParameterStyle.FORM, true, false);
        }

        public UploadFileSendActionBuilder body(org.citrusframework.spi.Resource body) {
            return this;
        }

        public void setBody(org.citrusframework.spi.Resource body) {
        }

        public UploadFileSendActionBuilder body(String bodyExpression) {
            return this;
        }

        public void setBody(String bodyExpression) {
        }

        @Override
        public SendMessageAction doBuild() {

            if (getCustomizers() != null) {
                getCustomizers().forEach(customizer -> customizer.customizeRequestBuilder(this, this));
            }

            return super.doBuild();
        }
    }

    public static class UploadFileReceiveActionBuilder extends
                        RestApiReceiveMessageActionBuilder implements GeneratedApiOperationInfo {

        private static final String METHOD = "POST";

        private static final String ENDPOINT = "/api/v3/pet/{petId}/uploadImage";

        private static final String OPERATION_NAME = "uploadFile";

        public UploadFileReceiveActionBuilder(PetApi petApi,  String statusCode) {
            super(petApi, petStoreSpecification, METHOD, ENDPOINT, OPERATION_NAME, statusCode);
        }

        public UploadFileReceiveActionBuilder(PetApi petApi, OpenApiClientResponseMessageBuilder messageBuilder) {
            super(petApi, petStoreSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
        }

        @Override
        public String getOperationName() {
            return OPERATION_NAME;
        }

        @Override
        public String getMethod() {
            return METHOD;
        }

        @Override
        public String getPath() {
            return ENDPOINT;
        }

        @Override
        public ReceiveMessageAction doBuild() {

            if (getCustomizers() != null) {
                getCustomizers().forEach(customizer -> customizer.customizeResponseBuilder(this, this));
            }

            return super.doBuild();
        }

    }
}
