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
public class UserApi implements GeneratedApi
{

    @Value("${" + "petstore.base64-encode-api-key:#{false}}")
    private boolean base64EncodeApiKey;

    @Value("${" + "petstore.api-key:#{null}}")
    private String defaultApiKey;

    private final List<ApiActionBuilderCustomizer> customizers;

    private final Endpoint endpoint;

    private final OpenApiSpecification openApiSpecification;

    public UserApi(Endpoint endpoint)  {
        this(endpoint, emptyList());
    }

    public UserApi(Endpoint endpoint, List<ApiActionBuilderCustomizer> customizers)  {
        this.endpoint = endpoint;
        this.customizers = customizers;

        URL resource = PetStore.class.getResource("petStore_openApi.yaml");
        if (resource == null) {
            throw new IllegalStateException(format("Cannot find resource '%s'. This resource is typically created during API generation and should therefore be present. Check API generation.", "petStore_openApi.yaml"));
        }
        openApiSpecification = OpenApiSpecification.from(resource);
    }

    public static UserApi userApi(Endpoint endpoint) {
        return new UserApi(endpoint);
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
    public CreateUserSendActionBuilder sendCreateUser()   {
            return new CreateUserSendActionBuilder(this, openApiSpecification);
    }

    public CreateUserReceiveActionBuilder receiveCreateUser(@NotNull HttpStatus statusCode)   {
        return new CreateUserReceiveActionBuilder(this, openApiSpecification, Integer.toString(statusCode.value()));
    }

    public CreateUserReceiveActionBuilder receiveCreateUser(@NotNull String statusCode)   {
        return new CreateUserReceiveActionBuilder(this, openApiSpecification,  statusCode);
    }

    /**
     * Builder with type safe required parameters.
     */
    public CreateUsersWithListInputSendActionBuilder sendCreateUsersWithListInput()   {
            return new CreateUsersWithListInputSendActionBuilder(this, openApiSpecification);
    }

    public CreateUsersWithListInputReceiveActionBuilder receiveCreateUsersWithListInput(@NotNull HttpStatus statusCode)   {
        return new CreateUsersWithListInputReceiveActionBuilder(this, openApiSpecification, Integer.toString(statusCode.value()));
    }

    public CreateUsersWithListInputReceiveActionBuilder receiveCreateUsersWithListInput(@NotNull String statusCode)   {
        return new CreateUsersWithListInputReceiveActionBuilder(this, openApiSpecification,  statusCode);
    }

    /**
     * Builder with type safe required parameters.
     */
    public DeleteUserSendActionBuilder sendDeleteUser(String username)   {
            return new DeleteUserSendActionBuilder(this, openApiSpecification, username);
    }

    public DeleteUserReceiveActionBuilder receiveDeleteUser(@NotNull HttpStatus statusCode)   {
        return new DeleteUserReceiveActionBuilder(this, openApiSpecification, Integer.toString(statusCode.value()));
    }

    public DeleteUserReceiveActionBuilder receiveDeleteUser(@NotNull String statusCode)   {
        return new DeleteUserReceiveActionBuilder(this, openApiSpecification,  statusCode);
    }

    /**
     * Builder with type safe required parameters.
     */
    public GetUserByNameSendActionBuilder sendGetUserByName(String username)   {
            return new GetUserByNameSendActionBuilder(this, openApiSpecification, username);
    }

    public GetUserByNameReceiveActionBuilder receiveGetUserByName(@NotNull HttpStatus statusCode)   {
        return new GetUserByNameReceiveActionBuilder(this, openApiSpecification, Integer.toString(statusCode.value()));
    }

    public GetUserByNameReceiveActionBuilder receiveGetUserByName(@NotNull String statusCode)   {
        return new GetUserByNameReceiveActionBuilder(this, openApiSpecification,  statusCode);
    }

    /**
     * Builder with type safe required parameters.
     */
    public LoginUserSendActionBuilder sendLoginUser()   {
            return new LoginUserSendActionBuilder(this, openApiSpecification);
    }

    public LoginUserReceiveActionBuilder receiveLoginUser(@NotNull HttpStatus statusCode)   {
        return new LoginUserReceiveActionBuilder(this, openApiSpecification, Integer.toString(statusCode.value()));
    }

    public LoginUserReceiveActionBuilder receiveLoginUser(@NotNull String statusCode)   {
        return new LoginUserReceiveActionBuilder(this, openApiSpecification,  statusCode);
    }

    /**
     * Builder with type safe required parameters.
     */
    public LogoutUserSendActionBuilder sendLogoutUser()   {
            return new LogoutUserSendActionBuilder(this, openApiSpecification);
    }

    public LogoutUserReceiveActionBuilder receiveLogoutUser(@NotNull HttpStatus statusCode)   {
        return new LogoutUserReceiveActionBuilder(this, openApiSpecification, Integer.toString(statusCode.value()));
    }

    public LogoutUserReceiveActionBuilder receiveLogoutUser(@NotNull String statusCode)   {
        return new LogoutUserReceiveActionBuilder(this, openApiSpecification,  statusCode);
    }

    /**
     * Builder with type safe required parameters.
     */
    public UpdateUserSendActionBuilder sendUpdateUser(String username)   {
            return new UpdateUserSendActionBuilder(this, openApiSpecification, username);
    }

    public UpdateUserReceiveActionBuilder receiveUpdateUser(@NotNull HttpStatus statusCode)   {
        return new UpdateUserReceiveActionBuilder(this, openApiSpecification, Integer.toString(statusCode.value()));
    }

    public UpdateUserReceiveActionBuilder receiveUpdateUser(@NotNull String statusCode)   {
        return new UpdateUserReceiveActionBuilder(this, openApiSpecification,  statusCode);
    }

    public static class CreateUserSendActionBuilder extends
                RestApiSendMessageActionBuilder {

        private static final String METHOD = "POST";

        private static final String ENDPOINT = "/api/v3/user";

        private static final String OPERATION_NAME = "createUser";

        /**
         * Constructor with type safe required parameters.
         */
        public CreateUserSendActionBuilder(UserApi userApi, OpenApiSpecification openApiSpecification) {
            super(userApi, openApiSpecification, METHOD, ENDPOINT, OPERATION_NAME);
        }

        public CreateUserSendActionBuilder(UserApi userApi, OpenApiSpecification openApiSpecification, TestApiClientRequestMessageBuilder messageBuilder) {
            super(userApi, openApiSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
        }

        public CreateUserSendActionBuilder user(User user) {
            return this;
        }

        public void setUser(User user) {
        }

        public CreateUserSendActionBuilder user(String userExpression) {
            return this;
        }

        public void setUser(String userExpression) {
        }

        @Override
        public SendMessageAction doBuild() {

            if (getCustomizers() != null) {
                getCustomizers().forEach(customizer -> customizer.customizeRequestBuilder(getGeneratedApi(), this));
            }

            return super.doBuild();
        }
    }

    public static class CreateUserReceiveActionBuilder extends
                        RestApiReceiveMessageActionBuilder {

        private static final String METHOD = "POST";

        private static final String ENDPOINT = "/api/v3/user";

        private static final String OPERATION_NAME = "createUser";

        public CreateUserReceiveActionBuilder(UserApi userApi, OpenApiSpecification openApiSpecification,  String statusCode) {
            super(userApi, openApiSpecification, METHOD, ENDPOINT, OPERATION_NAME, statusCode);
        }

        public CreateUserReceiveActionBuilder(UserApi userApi, OpenApiSpecification openApiSpecification, OpenApiClientResponseMessageBuilder messageBuilder) {
            super(userApi, openApiSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
        }

        @Override
        public ReceiveMessageAction doBuild() {

            if (getCustomizers() != null) {
                getCustomizers().forEach(customizer -> customizer.customizeResponseBuilder(getGeneratedApi(), this));
            }

            return super.doBuild();
        }

    }

    public static class CreateUsersWithListInputSendActionBuilder extends
                RestApiSendMessageActionBuilder {

        private static final String METHOD = "POST";

        private static final String ENDPOINT = "/api/v3/user/createWithList";

        private static final String OPERATION_NAME = "createUsersWithListInput";

        /**
         * Constructor with type safe required parameters.
         */
        public CreateUsersWithListInputSendActionBuilder(UserApi userApi, OpenApiSpecification openApiSpecification) {
            super(userApi, openApiSpecification, METHOD, ENDPOINT, OPERATION_NAME);
        }

        public CreateUsersWithListInputSendActionBuilder(UserApi userApi, OpenApiSpecification openApiSpecification, TestApiClientRequestMessageBuilder messageBuilder) {
            super(userApi, openApiSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
        }

        public CreateUsersWithListInputSendActionBuilder user(User...user) {
            return this;
        }

        public void setUser(User...user) {
        }

        public CreateUsersWithListInputSendActionBuilder user(String...userExpression) {
            return this;
        }

        public void setUser(String...userExpression) {
        }

        @Override
        public SendMessageAction doBuild() {

            if (getCustomizers() != null) {
                getCustomizers().forEach(customizer -> customizer.customizeRequestBuilder(getGeneratedApi(), this));
            }

            return super.doBuild();
        }
    }

    public static class CreateUsersWithListInputReceiveActionBuilder extends
                        RestApiReceiveMessageActionBuilder {

        private static final String METHOD = "POST";

        private static final String ENDPOINT = "/api/v3/user/createWithList";

        private static final String OPERATION_NAME = "createUsersWithListInput";

        public CreateUsersWithListInputReceiveActionBuilder(UserApi userApi, OpenApiSpecification openApiSpecification,  String statusCode) {
            super(userApi, openApiSpecification, METHOD, ENDPOINT, OPERATION_NAME, statusCode);
        }

        public CreateUsersWithListInputReceiveActionBuilder(UserApi userApi, OpenApiSpecification openApiSpecification, OpenApiClientResponseMessageBuilder messageBuilder) {
            super(userApi, openApiSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
        }

        @Override
        public ReceiveMessageAction doBuild() {

            if (getCustomizers() != null) {
                getCustomizers().forEach(customizer -> customizer.customizeResponseBuilder(getGeneratedApi(), this));
            }

            return super.doBuild();
        }

    }

    public static class DeleteUserSendActionBuilder extends
                RestApiSendMessageActionBuilder {

        private static final String METHOD = "DELETE";

        private static final String ENDPOINT = "/api/v3/user/{username}";

        private static final String OPERATION_NAME = "deleteUser";

        /**
         * Constructor with type safe required parameters.
         */
        public DeleteUserSendActionBuilder(UserApi userApi, OpenApiSpecification openApiSpecification, String username) {
            super(userApi, openApiSpecification, METHOD, ENDPOINT, OPERATION_NAME);
            pathParameter("username", username, ParameterStyle.SIMPLE, false, false);
        }

        /**
         * Constructor with required parameters as string to allow for dynamic content.
         */
        public DeleteUserSendActionBuilder(UserApi userApi, OpenApiSpecification openApiSpecification, TestApiClientRequestMessageBuilder messageBuilder, String usernameExpression) {
            super(userApi, openApiSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
            pathParameter("username", usernameExpression, ParameterStyle.SIMPLE, false, false);
        }

        public DeleteUserSendActionBuilder username(String username) {
            pathParameter("username", username, ParameterStyle.SIMPLE, false, false);
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

    public static class DeleteUserReceiveActionBuilder extends
                        RestApiReceiveMessageActionBuilder {

        private static final String METHOD = "DELETE";

        private static final String ENDPOINT = "/api/v3/user/{username}";

        private static final String OPERATION_NAME = "deleteUser";

        public DeleteUserReceiveActionBuilder(UserApi userApi, OpenApiSpecification openApiSpecification,  String statusCode) {
            super(userApi, openApiSpecification, METHOD, ENDPOINT, OPERATION_NAME, statusCode);
        }

        public DeleteUserReceiveActionBuilder(UserApi userApi, OpenApiSpecification openApiSpecification, OpenApiClientResponseMessageBuilder messageBuilder) {
            super(userApi, openApiSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
        }

        @Override
        public ReceiveMessageAction doBuild() {

            if (getCustomizers() != null) {
                getCustomizers().forEach(customizer -> customizer.customizeResponseBuilder(getGeneratedApi(), this));
            }

            return super.doBuild();
        }

    }

    public static class GetUserByNameSendActionBuilder extends
                RestApiSendMessageActionBuilder {

        private static final String METHOD = "GET";

        private static final String ENDPOINT = "/api/v3/user/{username}";

        private static final String OPERATION_NAME = "getUserByName";

        /**
         * Constructor with type safe required parameters.
         */
        public GetUserByNameSendActionBuilder(UserApi userApi, OpenApiSpecification openApiSpecification, String username) {
            super(userApi, openApiSpecification, METHOD, ENDPOINT, OPERATION_NAME);
            pathParameter("username", username, ParameterStyle.SIMPLE, false, false);
        }

        /**
         * Constructor with required parameters as string to allow for dynamic content.
         */
        public GetUserByNameSendActionBuilder(UserApi userApi, OpenApiSpecification openApiSpecification, TestApiClientRequestMessageBuilder messageBuilder, String usernameExpression) {
            super(userApi, openApiSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
            pathParameter("username", usernameExpression, ParameterStyle.SIMPLE, false, false);
        }

        public GetUserByNameSendActionBuilder username(String username) {
            pathParameter("username", username, ParameterStyle.SIMPLE, false, false);
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

    public static class GetUserByNameReceiveActionBuilder extends
                        RestApiReceiveMessageActionBuilder {

        private static final String METHOD = "GET";

        private static final String ENDPOINT = "/api/v3/user/{username}";

        private static final String OPERATION_NAME = "getUserByName";

        public GetUserByNameReceiveActionBuilder(UserApi userApi, OpenApiSpecification openApiSpecification,  String statusCode) {
            super(userApi, openApiSpecification, METHOD, ENDPOINT, OPERATION_NAME, statusCode);
        }

        public GetUserByNameReceiveActionBuilder(UserApi userApi, OpenApiSpecification openApiSpecification, OpenApiClientResponseMessageBuilder messageBuilder) {
            super(userApi, openApiSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
        }

        @Override
        public ReceiveMessageAction doBuild() {

            if (getCustomizers() != null) {
                getCustomizers().forEach(customizer -> customizer.customizeResponseBuilder(getGeneratedApi(), this));
            }

            return super.doBuild();
        }

    }

    public static class LoginUserSendActionBuilder extends
                RestApiSendMessageActionBuilder {

        private static final String METHOD = "GET";

        private static final String ENDPOINT = "/api/v3/user/login";

        private static final String OPERATION_NAME = "loginUser";

        /**
         * Constructor with type safe required parameters.
         */
        public LoginUserSendActionBuilder(UserApi userApi, OpenApiSpecification openApiSpecification) {
            super(userApi, openApiSpecification, METHOD, ENDPOINT, OPERATION_NAME);
        }

        public LoginUserSendActionBuilder(UserApi userApi, OpenApiSpecification openApiSpecification, TestApiClientRequestMessageBuilder messageBuilder) {
            super(userApi, openApiSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
        }

        public LoginUserSendActionBuilder username(String username) {
            queryParameter("username", username, ParameterStyle.FORM, true, false);
            return this;
        }

        public void setUsername(String username) {
            queryParameter("username", username, ParameterStyle.FORM, true, false);
        }

        public LoginUserSendActionBuilder password(String password) {
            queryParameter("password", password, ParameterStyle.FORM, true, false);
            return this;
        }

        public void setPassword(String password) {
            queryParameter("password", password, ParameterStyle.FORM, true, false);
        }

        @Override
        public SendMessageAction doBuild() {

            if (getCustomizers() != null) {
                getCustomizers().forEach(customizer -> customizer.customizeRequestBuilder(getGeneratedApi(), this));
            }

            return super.doBuild();
        }
    }

    public static class LoginUserReceiveActionBuilder extends
                        RestApiReceiveMessageActionBuilder {

        private static final String METHOD = "GET";

        private static final String ENDPOINT = "/api/v3/user/login";

        private static final String OPERATION_NAME = "loginUser";

        public LoginUserReceiveActionBuilder(UserApi userApi, OpenApiSpecification openApiSpecification,  String statusCode) {
            super(userApi, openApiSpecification, METHOD, ENDPOINT, OPERATION_NAME, statusCode);
        }

        public LoginUserReceiveActionBuilder(UserApi userApi, OpenApiSpecification openApiSpecification, OpenApiClientResponseMessageBuilder messageBuilder) {
            super(userApi, openApiSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
        }

        @Override
        public ReceiveMessageAction doBuild() {

            if (getCustomizers() != null) {
                getCustomizers().forEach(customizer -> customizer.customizeResponseBuilder(getGeneratedApi(), this));
            }

            return super.doBuild();
        }

    }

    public static class LogoutUserSendActionBuilder extends
                RestApiSendMessageActionBuilder {

        private static final String METHOD = "GET";

        private static final String ENDPOINT = "/api/v3/user/logout";

        private static final String OPERATION_NAME = "logoutUser";

        /**
         * Constructor with type safe required parameters.
         */
        public LogoutUserSendActionBuilder(UserApi userApi, OpenApiSpecification openApiSpecification) {
            super(userApi, openApiSpecification, METHOD, ENDPOINT, OPERATION_NAME);
        }

        public LogoutUserSendActionBuilder(UserApi userApi, OpenApiSpecification openApiSpecification, TestApiClientRequestMessageBuilder messageBuilder) {
            super(userApi, openApiSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
        }

        @Override
        public SendMessageAction doBuild() {

            if (getCustomizers() != null) {
                getCustomizers().forEach(customizer -> customizer.customizeRequestBuilder(getGeneratedApi(), this));
            }

            return super.doBuild();
        }
    }

    public static class LogoutUserReceiveActionBuilder extends
                        RestApiReceiveMessageActionBuilder {

        private static final String METHOD = "GET";

        private static final String ENDPOINT = "/api/v3/user/logout";

        private static final String OPERATION_NAME = "logoutUser";

        public LogoutUserReceiveActionBuilder(UserApi userApi, OpenApiSpecification openApiSpecification,  String statusCode) {
            super(userApi, openApiSpecification, METHOD, ENDPOINT, OPERATION_NAME, statusCode);
        }

        public LogoutUserReceiveActionBuilder(UserApi userApi, OpenApiSpecification openApiSpecification, OpenApiClientResponseMessageBuilder messageBuilder) {
            super(userApi, openApiSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
        }

        @Override
        public ReceiveMessageAction doBuild() {

            if (getCustomizers() != null) {
                getCustomizers().forEach(customizer -> customizer.customizeResponseBuilder(getGeneratedApi(), this));
            }

            return super.doBuild();
        }

    }

    public static class UpdateUserSendActionBuilder extends
                RestApiSendMessageActionBuilder {

        private static final String METHOD = "PUT";

        private static final String ENDPOINT = "/api/v3/user/{username}";

        private static final String OPERATION_NAME = "updateUser";

        /**
         * Constructor with type safe required parameters.
         */
        public UpdateUserSendActionBuilder(UserApi userApi, OpenApiSpecification openApiSpecification, String username) {
            super(userApi, openApiSpecification, METHOD, ENDPOINT, OPERATION_NAME);
            pathParameter("username", username, ParameterStyle.SIMPLE, false, false);
        }

        /**
         * Constructor with required parameters as string to allow for dynamic content.
         */
        public UpdateUserSendActionBuilder(UserApi userApi, OpenApiSpecification openApiSpecification, TestApiClientRequestMessageBuilder messageBuilder, String usernameExpression) {
            super(userApi, openApiSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
            pathParameter("username", usernameExpression, ParameterStyle.SIMPLE, false, false);
        }

        public UpdateUserSendActionBuilder username(String username) {
            pathParameter("username", username, ParameterStyle.SIMPLE, false, false);
            return this;
        }

        public UpdateUserSendActionBuilder user(User user) {
            return this;
        }

        public void setUser(User user) {
        }

        public UpdateUserSendActionBuilder user(String userExpression) {
            return this;
        }

        public void setUser(String userExpression) {
        }

        @Override
        public SendMessageAction doBuild() {

            if (getCustomizers() != null) {
                getCustomizers().forEach(customizer -> customizer.customizeRequestBuilder(getGeneratedApi(), this));
            }

            return super.doBuild();
        }
    }

    public static class UpdateUserReceiveActionBuilder extends
                        RestApiReceiveMessageActionBuilder {

        private static final String METHOD = "PUT";

        private static final String ENDPOINT = "/api/v3/user/{username}";

        private static final String OPERATION_NAME = "updateUser";

        public UpdateUserReceiveActionBuilder(UserApi userApi, OpenApiSpecification openApiSpecification,  String statusCode) {
            super(userApi, openApiSpecification, METHOD, ENDPOINT, OPERATION_NAME, statusCode);
        }

        public UpdateUserReceiveActionBuilder(UserApi userApi, OpenApiSpecification openApiSpecification, OpenApiClientResponseMessageBuilder messageBuilder) {
            super(userApi, openApiSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
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
