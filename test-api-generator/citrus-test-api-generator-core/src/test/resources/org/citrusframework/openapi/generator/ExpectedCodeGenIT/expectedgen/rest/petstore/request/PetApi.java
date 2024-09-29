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
@jakarta.annotation.Generated(value = "org.citrusframework.openapi.generator.CitrusJavaCodegen", date = "2024-10-05T19:07:45.597236600+02:00[Europe/Zurich]", comments = "Generator version: 7.5.0")
public class PetApi implements GeneratedApi
{

    @Value("${" + "petstore.base64-encode-api-key:#{false}}")
    private boolean base64EncodeApiKey;

    @Value("${" + "petstore.api-key:#{null}}")
    private String defaultApiKey;

    private final List<ApiActionBuilderCustomizer> customizers;

    private final Endpoint endpoint;

    private final OpenApiSpecification openApiSpecification;

    public PetApi(Endpoint endpoint)  {
        this(endpoint, emptyList());
    }

    public PetApi(Endpoint endpoint, List<ApiActionBuilderCustomizer> customizers)  {
        this.endpoint = endpoint;
        this.customizers = customizers;

        URL resource = PetStore.class.getResource("petStore_openApi.yaml");
        if (resource == null) {
            throw new IllegalStateException(format("Cannot find resource '%s'. This resource is typically created during API generation and should therefore be present. Check API generation.", "petStore_openApi.yaml"));
        }
        openApiSpecification = OpenApiSpecification.from(resource);
    }

    public static PetApi petApi(Endpoint endpoint) {
        return new PetApi(endpoint);
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
    public AddPetSendActionBuilder sendAddPet()   {
            return new AddPetSendActionBuilder(this, openApiSpecification);
    }

    public AddPetReceiveActionBuilder receiveAddPet(@NotNull HttpStatus statusCode)   {
        return new AddPetReceiveActionBuilder(this, openApiSpecification, Integer.toString(statusCode.value()));
    }

    public AddPetReceiveActionBuilder receiveAddPet(@NotNull String statusCode)   {
        return new AddPetReceiveActionBuilder(this, openApiSpecification,  statusCode);
    }

    /**
     * Builder with type safe required parameters.
     */
    public DeletePetSendActionBuilder sendDeletePet(Long petId)   {
            return new DeletePetSendActionBuilder(this, openApiSpecification, petId);
    }

    /**
     * Builder with required parameters as string to allow for dynamic content.
     */
    public DeletePetSendActionBuilder sendDeletePet$(String petIdExpression )   {
            return new DeletePetSendActionBuilder(openApiSpecification, this, petIdExpression);
    }

    public DeletePetReceiveActionBuilder receiveDeletePet(@NotNull HttpStatus statusCode)   {
        return new DeletePetReceiveActionBuilder(this, openApiSpecification, Integer.toString(statusCode.value()));
    }

    public DeletePetReceiveActionBuilder receiveDeletePet(@NotNull String statusCode)   {
        return new DeletePetReceiveActionBuilder(this, openApiSpecification,  statusCode);
    }

    /**
     * Builder with type safe required parameters.
     */
    public FindPetsByStatusSendActionBuilder sendFindPetsByStatus()   {
            return new FindPetsByStatusSendActionBuilder(this, openApiSpecification);
    }

    public FindPetsByStatusReceiveActionBuilder receiveFindPetsByStatus(@NotNull HttpStatus statusCode)   {
        return new FindPetsByStatusReceiveActionBuilder(this, openApiSpecification, Integer.toString(statusCode.value()));
    }

    public FindPetsByStatusReceiveActionBuilder receiveFindPetsByStatus(@NotNull String statusCode)   {
        return new FindPetsByStatusReceiveActionBuilder(this, openApiSpecification,  statusCode);
    }

    /**
     * Builder with type safe required parameters.
     */
    public FindPetsByTagsSendActionBuilder sendFindPetsByTags()   {
            return new FindPetsByTagsSendActionBuilder(this, openApiSpecification);
    }

    public FindPetsByTagsReceiveActionBuilder receiveFindPetsByTags(@NotNull HttpStatus statusCode)   {
        return new FindPetsByTagsReceiveActionBuilder(this, openApiSpecification, Integer.toString(statusCode.value()));
    }

    public FindPetsByTagsReceiveActionBuilder receiveFindPetsByTags(@NotNull String statusCode)   {
        return new FindPetsByTagsReceiveActionBuilder(this, openApiSpecification,  statusCode);
    }

    /**
     * Builder with type safe required parameters.
     */
    public GetPetByIdSendActionBuilder sendGetPetById(Long petId)   {
            GetPetByIdSendActionBuilder builder =  new GetPetByIdSendActionBuilder(this, openApiSpecification, petId);
            builder.setBase64EncodeApiKey(base64EncodeApiKey);
            return builder;
    }

    /**
     * Builder with required parameters as string to allow for dynamic content.
     */
    public GetPetByIdSendActionBuilder sendGetPetById$(String petIdExpression )   {
            GetPetByIdSendActionBuilder builder =  new GetPetByIdSendActionBuilder(openApiSpecification, this, petIdExpression);
            builder.setBase64EncodeApiKey(base64EncodeApiKey);
            builder.setApiKey(defaultApiKey);
            return builder;
    }

    public GetPetByIdReceiveActionBuilder receiveGetPetById(@NotNull HttpStatus statusCode)   {
        return new GetPetByIdReceiveActionBuilder(this, openApiSpecification, Integer.toString(statusCode.value()));
    }

    public GetPetByIdReceiveActionBuilder receiveGetPetById(@NotNull String statusCode)   {
        return new GetPetByIdReceiveActionBuilder(this, openApiSpecification,  statusCode);
    }

    /**
     * Builder with type safe required parameters.
     */
    public UpdatePetSendActionBuilder sendUpdatePet()   {
            return new UpdatePetSendActionBuilder(this, openApiSpecification);
    }

    public UpdatePetReceiveActionBuilder receiveUpdatePet(@NotNull HttpStatus statusCode)   {
        return new UpdatePetReceiveActionBuilder(this, openApiSpecification, Integer.toString(statusCode.value()));
    }

    public UpdatePetReceiveActionBuilder receiveUpdatePet(@NotNull String statusCode)   {
        return new UpdatePetReceiveActionBuilder(this, openApiSpecification,  statusCode);
    }

    /**
     * Builder with type safe required parameters.
     */
    public UpdatePetWithFormSendActionBuilder sendUpdatePetWithForm(Long petId)   {
            return new UpdatePetWithFormSendActionBuilder(this, openApiSpecification, petId);
    }

    /**
     * Builder with required parameters as string to allow for dynamic content.
     */
    public UpdatePetWithFormSendActionBuilder sendUpdatePetWithForm$(String petIdExpression )   {
            return new UpdatePetWithFormSendActionBuilder(openApiSpecification, this, petIdExpression);
    }

    public UpdatePetWithFormReceiveActionBuilder receiveUpdatePetWithForm(@NotNull HttpStatus statusCode)   {
        return new UpdatePetWithFormReceiveActionBuilder(this, openApiSpecification, Integer.toString(statusCode.value()));
    }

    public UpdatePetWithFormReceiveActionBuilder receiveUpdatePetWithForm(@NotNull String statusCode)   {
        return new UpdatePetWithFormReceiveActionBuilder(this, openApiSpecification,  statusCode);
    }

    /**
     * Builder with type safe required parameters.
     */
    public UploadFileSendActionBuilder sendUploadFile(Long petId)   {
            return new UploadFileSendActionBuilder(this, openApiSpecification, petId);
    }

    /**
     * Builder with required parameters as string to allow for dynamic content.
     */
    public UploadFileSendActionBuilder sendUploadFile$(String petIdExpression )   {
            return new UploadFileSendActionBuilder(openApiSpecification, this, petIdExpression);
    }

    public UploadFileReceiveActionBuilder receiveUploadFile(@NotNull HttpStatus statusCode)   {
        return new UploadFileReceiveActionBuilder(this, openApiSpecification, Integer.toString(statusCode.value()));
    }

    public UploadFileReceiveActionBuilder receiveUploadFile(@NotNull String statusCode)   {
        return new UploadFileReceiveActionBuilder(this, openApiSpecification,  statusCode);
    }

    public static class AddPetSendActionBuilder extends
                RestApiSendMessageActionBuilder {

        private static final String METHOD = "POST";

        private static final String ENDPOINT = "/api/v3/pet";

        private static final String OPERATION_NAME = "addPet";

        /**
         * Constructor with type safe required parameters.
         */
        public AddPetSendActionBuilder(PetApi petApi, OpenApiSpecification openApiSpecification) {
            super(petApi, openApiSpecification, METHOD, ENDPOINT, OPERATION_NAME);
        }

        public AddPetSendActionBuilder(PetApi petApi, OpenApiSpecification openApiSpecification, TestApiClientRequestMessageBuilder messageBuilder) {
            super(petApi, openApiSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
        }

        @Override
        public SendMessageAction doBuild() {

            if (getCustomizers() != null) {
                getCustomizers().forEach(customizer -> customizer.customizeRequestBuilder(getGeneratedApi(), this));
            }

            return super.doBuild();
        }
    }

    public static class AddPetReceiveActionBuilder extends
                        RestApiReceiveMessageActionBuilder {

        private static final String METHOD = "POST";

        private static final String ENDPOINT = "/api/v3/pet";

        private static final String OPERATION_NAME = "addPet";

        public AddPetReceiveActionBuilder(PetApi petApi, OpenApiSpecification openApiSpecification,  String statusCode) {
            super(petApi, openApiSpecification, METHOD, ENDPOINT, OPERATION_NAME, statusCode);
        }

        public AddPetReceiveActionBuilder(PetApi petApi, OpenApiSpecification openApiSpecification, OpenApiClientResponseMessageBuilder messageBuilder) {
            super(petApi, openApiSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
        }

        @Override
        public ReceiveMessageAction doBuild() {

            if (getCustomizers() != null) {
                getCustomizers().forEach(customizer -> customizer.customizeResponseBuilder(getGeneratedApi(), this));
            }

            return super.doBuild();
        }

    }

    public static class DeletePetSendActionBuilder extends
                RestApiSendMessageActionBuilder {

        private static final String METHOD = "DELETE";

        private static final String ENDPOINT = "/api/v3/pet/{petId}";

        private static final String OPERATION_NAME = "deletePet";

        /**
         * Constructor with type safe required parameters.
         */
        public DeletePetSendActionBuilder(PetApi petApi, OpenApiSpecification openApiSpecification, Long petId) {
            super(petApi, openApiSpecification, METHOD, ENDPOINT, OPERATION_NAME);
            pathParameter("petId", petId, ParameterStyle.SIMPLE, false, false);
        }

        /**
         * Constructor with required parameters as string to allow for dynamic content.
         */
            public DeletePetSendActionBuilder(OpenApiSpecification openApiSpecification, PetApi petApi, String petIdExpression) {
            super(petApi, openApiSpecification,  METHOD, ENDPOINT, OPERATION_NAME);
            pathParameter("petId", petIdExpression, ParameterStyle.SIMPLE, false, false);
        }

        /**
         * Constructor with required parameters as string to allow for dynamic content.
         */
        public DeletePetSendActionBuilder(PetApi petApi, OpenApiSpecification openApiSpecification, TestApiClientRequestMessageBuilder messageBuilder, String petIdExpression) {
            super(petApi, openApiSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
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
                getCustomizers().forEach(customizer -> customizer.customizeRequestBuilder(getGeneratedApi(), this));
            }

            return super.doBuild();
        }
    }

    public static class DeletePetReceiveActionBuilder extends
                        RestApiReceiveMessageActionBuilder {

        private static final String METHOD = "DELETE";

        private static final String ENDPOINT = "/api/v3/pet/{petId}";

        private static final String OPERATION_NAME = "deletePet";

        public DeletePetReceiveActionBuilder(PetApi petApi, OpenApiSpecification openApiSpecification,  String statusCode) {
            super(petApi, openApiSpecification, METHOD, ENDPOINT, OPERATION_NAME, statusCode);
        }

        public DeletePetReceiveActionBuilder(PetApi petApi, OpenApiSpecification openApiSpecification, OpenApiClientResponseMessageBuilder messageBuilder) {
            super(petApi, openApiSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
        }

        @Override
        public ReceiveMessageAction doBuild() {

            if (getCustomizers() != null) {
                getCustomizers().forEach(customizer -> customizer.customizeResponseBuilder(getGeneratedApi(), this));
            }

            return super.doBuild();
        }

    }

    public static class FindPetsByStatusSendActionBuilder extends
                RestApiSendMessageActionBuilder {

        private static final String METHOD = "GET";

        private static final String ENDPOINT = "/api/v3/pet/findByStatus";

        private static final String OPERATION_NAME = "findPetsByStatus";

        /**
         * Constructor with type safe required parameters.
         */
        public FindPetsByStatusSendActionBuilder(PetApi petApi, OpenApiSpecification openApiSpecification) {
            super(petApi, openApiSpecification, METHOD, ENDPOINT, OPERATION_NAME);
        }

        public FindPetsByStatusSendActionBuilder(PetApi petApi, OpenApiSpecification openApiSpecification, TestApiClientRequestMessageBuilder messageBuilder) {
            super(petApi, openApiSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
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
                getCustomizers().forEach(customizer -> customizer.customizeRequestBuilder(getGeneratedApi(), this));
            }

            return super.doBuild();
        }
    }

    public static class FindPetsByStatusReceiveActionBuilder extends
                        RestApiReceiveMessageActionBuilder {

        private static final String METHOD = "GET";

        private static final String ENDPOINT = "/api/v3/pet/findByStatus";

        private static final String OPERATION_NAME = "findPetsByStatus";

        public FindPetsByStatusReceiveActionBuilder(PetApi petApi, OpenApiSpecification openApiSpecification,  String statusCode) {
            super(petApi, openApiSpecification, METHOD, ENDPOINT, OPERATION_NAME, statusCode);
        }

        public FindPetsByStatusReceiveActionBuilder(PetApi petApi, OpenApiSpecification openApiSpecification, OpenApiClientResponseMessageBuilder messageBuilder) {
            super(petApi, openApiSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
        }

        @Override
        public ReceiveMessageAction doBuild() {

            if (getCustomizers() != null) {
                getCustomizers().forEach(customizer -> customizer.customizeResponseBuilder(getGeneratedApi(), this));
            }

            return super.doBuild();
        }

    }

    public static class FindPetsByTagsSendActionBuilder extends
                RestApiSendMessageActionBuilder {

        private static final String METHOD = "GET";

        private static final String ENDPOINT = "/api/v3/pet/findByTags";

        private static final String OPERATION_NAME = "findPetsByTags";

        /**
         * Constructor with type safe required parameters.
         */
        public FindPetsByTagsSendActionBuilder(PetApi petApi, OpenApiSpecification openApiSpecification) {
            super(petApi, openApiSpecification, METHOD, ENDPOINT, OPERATION_NAME);
        }

        public FindPetsByTagsSendActionBuilder(PetApi petApi, OpenApiSpecification openApiSpecification, TestApiClientRequestMessageBuilder messageBuilder) {
            super(petApi, openApiSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
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
                getCustomizers().forEach(customizer -> customizer.customizeRequestBuilder(getGeneratedApi(), this));
            }

            return super.doBuild();
        }
    }

    public static class FindPetsByTagsReceiveActionBuilder extends
                        RestApiReceiveMessageActionBuilder {

        private static final String METHOD = "GET";

        private static final String ENDPOINT = "/api/v3/pet/findByTags";

        private static final String OPERATION_NAME = "findPetsByTags";

        public FindPetsByTagsReceiveActionBuilder(PetApi petApi, OpenApiSpecification openApiSpecification,  String statusCode) {
            super(petApi, openApiSpecification, METHOD, ENDPOINT, OPERATION_NAME, statusCode);
        }

        public FindPetsByTagsReceiveActionBuilder(PetApi petApi, OpenApiSpecification openApiSpecification, OpenApiClientResponseMessageBuilder messageBuilder) {
            super(petApi, openApiSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
        }

        @Override
        public ReceiveMessageAction doBuild() {

            if (getCustomizers() != null) {
                getCustomizers().forEach(customizer -> customizer.customizeResponseBuilder(getGeneratedApi(), this));
            }

            return super.doBuild();
        }

    }

    public static class GetPetByIdSendActionBuilder extends
                RestApiSendMessageActionBuilder {

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
        public GetPetByIdSendActionBuilder(PetApi petApi, OpenApiSpecification openApiSpecification, Long petId) {
            super(petApi, openApiSpecification, METHOD, ENDPOINT, OPERATION_NAME);
            pathParameter("petId", petId, ParameterStyle.SIMPLE, false, false);
        }

        /**
         * Constructor with required parameters as string to allow for dynamic content.
         */
            public GetPetByIdSendActionBuilder(OpenApiSpecification openApiSpecification, PetApi petApi, String petIdExpression) {
            super(petApi, openApiSpecification,  METHOD, ENDPOINT, OPERATION_NAME);
            pathParameter("petId", petIdExpression, ParameterStyle.SIMPLE, false, false);
        }

        /**
         * Constructor with required parameters as string to allow for dynamic content.
         */
        public GetPetByIdSendActionBuilder(PetApi petApi, OpenApiSpecification openApiSpecification, TestApiClientRequestMessageBuilder messageBuilder, String petIdExpression) {
            super(petApi, openApiSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
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
                getCustomizers().forEach(customizer -> customizer.customizeRequestBuilder(getGeneratedApi(), this));
            }

            return super.doBuild();
        }
    }

    public static class GetPetByIdReceiveActionBuilder extends
                        RestApiReceiveMessageActionBuilder {

        private static final String METHOD = "GET";

        private static final String ENDPOINT = "/api/v3/pet/{petId}";

        private static final String OPERATION_NAME = "getPetById";

        public GetPetByIdReceiveActionBuilder(PetApi petApi, OpenApiSpecification openApiSpecification,  String statusCode) {
            super(petApi, openApiSpecification, METHOD, ENDPOINT, OPERATION_NAME, statusCode);
        }

        public GetPetByIdReceiveActionBuilder(PetApi petApi, OpenApiSpecification openApiSpecification, OpenApiClientResponseMessageBuilder messageBuilder) {
            super(petApi, openApiSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
        }

        @Override
        public ReceiveMessageAction doBuild() {

            if (getCustomizers() != null) {
                getCustomizers().forEach(customizer -> customizer.customizeResponseBuilder(getGeneratedApi(), this));
            }

            return super.doBuild();
        }

    }

    public static class UpdatePetSendActionBuilder extends
                RestApiSendMessageActionBuilder {

        private static final String METHOD = "PUT";

        private static final String ENDPOINT = "/api/v3/pet";

        private static final String OPERATION_NAME = "updatePet";

        /**
         * Constructor with type safe required parameters.
         */
        public UpdatePetSendActionBuilder(PetApi petApi, OpenApiSpecification openApiSpecification) {
            super(petApi, openApiSpecification, METHOD, ENDPOINT, OPERATION_NAME);
        }

        public UpdatePetSendActionBuilder(PetApi petApi, OpenApiSpecification openApiSpecification, TestApiClientRequestMessageBuilder messageBuilder) {
            super(petApi, openApiSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
        }

        @Override
        public SendMessageAction doBuild() {

            if (getCustomizers() != null) {
                getCustomizers().forEach(customizer -> customizer.customizeRequestBuilder(getGeneratedApi(), this));
            }

            return super.doBuild();
        }
    }

    public static class UpdatePetReceiveActionBuilder extends
                        RestApiReceiveMessageActionBuilder {

        private static final String METHOD = "PUT";

        private static final String ENDPOINT = "/api/v3/pet";

        private static final String OPERATION_NAME = "updatePet";

        public UpdatePetReceiveActionBuilder(PetApi petApi, OpenApiSpecification openApiSpecification,  String statusCode) {
            super(petApi, openApiSpecification, METHOD, ENDPOINT, OPERATION_NAME, statusCode);
        }

        public UpdatePetReceiveActionBuilder(PetApi petApi, OpenApiSpecification openApiSpecification, OpenApiClientResponseMessageBuilder messageBuilder) {
            super(petApi, openApiSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
        }

        @Override
        public ReceiveMessageAction doBuild() {

            if (getCustomizers() != null) {
                getCustomizers().forEach(customizer -> customizer.customizeResponseBuilder(getGeneratedApi(), this));
            }

            return super.doBuild();
        }

    }

    public static class UpdatePetWithFormSendActionBuilder extends
                RestApiSendMessageActionBuilder {

        private static final String METHOD = "POST";

        private static final String ENDPOINT = "/api/v3/pet/{petId}";

        private static final String OPERATION_NAME = "updatePetWithForm";

        /**
         * Constructor with type safe required parameters.
         */
        public UpdatePetWithFormSendActionBuilder(PetApi petApi, OpenApiSpecification openApiSpecification, Long petId) {
            super(petApi, openApiSpecification, METHOD, ENDPOINT, OPERATION_NAME);
            pathParameter("petId", petId, ParameterStyle.SIMPLE, false, false);
        }

        /**
         * Constructor with required parameters as string to allow for dynamic content.
         */
            public UpdatePetWithFormSendActionBuilder(OpenApiSpecification openApiSpecification, PetApi petApi, String petIdExpression) {
            super(petApi, openApiSpecification,  METHOD, ENDPOINT, OPERATION_NAME);
            pathParameter("petId", petIdExpression, ParameterStyle.SIMPLE, false, false);
        }

        /**
         * Constructor with required parameters as string to allow for dynamic content.
         */
        public UpdatePetWithFormSendActionBuilder(PetApi petApi, OpenApiSpecification openApiSpecification, TestApiClientRequestMessageBuilder messageBuilder, String petIdExpression) {
            super(petApi, openApiSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
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
                getCustomizers().forEach(customizer -> customizer.customizeRequestBuilder(getGeneratedApi(), this));
            }

            return super.doBuild();
        }
    }

    public static class UpdatePetWithFormReceiveActionBuilder extends
                        RestApiReceiveMessageActionBuilder {

        private static final String METHOD = "POST";

        private static final String ENDPOINT = "/api/v3/pet/{petId}";

        private static final String OPERATION_NAME = "updatePetWithForm";

        public UpdatePetWithFormReceiveActionBuilder(PetApi petApi, OpenApiSpecification openApiSpecification,  String statusCode) {
            super(petApi, openApiSpecification, METHOD, ENDPOINT, OPERATION_NAME, statusCode);
        }

        public UpdatePetWithFormReceiveActionBuilder(PetApi petApi, OpenApiSpecification openApiSpecification, OpenApiClientResponseMessageBuilder messageBuilder) {
            super(petApi, openApiSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
        }

        @Override
        public ReceiveMessageAction doBuild() {

            if (getCustomizers() != null) {
                getCustomizers().forEach(customizer -> customizer.customizeResponseBuilder(getGeneratedApi(), this));
            }

            return super.doBuild();
        }

    }

    public static class UploadFileSendActionBuilder extends
                RestApiSendMessageActionBuilder {

        private static final String METHOD = "POST";

        private static final String ENDPOINT = "/api/v3/pet/{petId}/uploadImage";

        private static final String OPERATION_NAME = "uploadFile";

        /**
         * Constructor with type safe required parameters.
         */
        public UploadFileSendActionBuilder(PetApi petApi, OpenApiSpecification openApiSpecification, Long petId) {
            super(petApi, openApiSpecification, METHOD, ENDPOINT, OPERATION_NAME);
            pathParameter("petId", petId, ParameterStyle.SIMPLE, false, false);
        }

        /**
         * Constructor with required parameters as string to allow for dynamic content.
         */
            public UploadFileSendActionBuilder(OpenApiSpecification openApiSpecification, PetApi petApi, String petIdExpression) {
            super(petApi, openApiSpecification,  METHOD, ENDPOINT, OPERATION_NAME);
            pathParameter("petId", petIdExpression, ParameterStyle.SIMPLE, false, false);
        }

        /**
         * Constructor with required parameters as string to allow for dynamic content.
         */
        public UploadFileSendActionBuilder(PetApi petApi, OpenApiSpecification openApiSpecification, TestApiClientRequestMessageBuilder messageBuilder, String petIdExpression) {
            super(petApi, openApiSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
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

        public UploadFileSendActionBuilder body(Resource body) {
            return this;
        }

        public void setBody(Resource body) {
        }

        public UploadFileSendActionBuilder body(String bodyExpression) {
            return this;
        }

        public void setBody(String bodyExpression) {
        }

        @Override
        public SendMessageAction doBuild() {

            if (getCustomizers() != null) {
                getCustomizers().forEach(customizer -> customizer.customizeRequestBuilder(getGeneratedApi(), this));
            }

            return super.doBuild();
        }
    }

    public static class UploadFileReceiveActionBuilder extends
                        RestApiReceiveMessageActionBuilder {

        private static final String METHOD = "POST";

        private static final String ENDPOINT = "/api/v3/pet/{petId}/uploadImage";

        private static final String OPERATION_NAME = "uploadFile";

        public UploadFileReceiveActionBuilder(PetApi petApi, OpenApiSpecification openApiSpecification,  String statusCode) {
            super(petApi, openApiSpecification, METHOD, ENDPOINT, OPERATION_NAME, statusCode);
        }

        public UploadFileReceiveActionBuilder(PetApi petApi, OpenApiSpecification openApiSpecification, OpenApiClientResponseMessageBuilder messageBuilder) {
            super(petApi, openApiSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
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