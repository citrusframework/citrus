package org.citrusframework.openapi.generator.rest.extpetstore.request;

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

import org.citrusframework.openapi.generator.rest.extpetstore.ExtPetStore;
import org.citrusframework.openapi.generator.rest.extpetstore.model.*;

@SuppressWarnings("unused")
@jakarta.annotation.Generated(value = "org.citrusframework.openapi.generator.CitrusJavaCodegen", date = "2024-10-05T19:07:46.194751400+02:00[Europe/Zurich]", comments = "Generator version: 7.5.0")
public class ExtPetApi implements GeneratedApi
{

    @Value("${" + "extpetstore.base64-encode-api-key:#{false}}")
    private boolean base64EncodeApiKey;

    @Value("${" + "extpetstore.basic.username:#{null}}")
    private String basicUsername;

    @Value("${" + "extpetstore.basic.password:#{null}}")
    private String basicPassword;

    @Value("${" + "extpetstore.bearer.token:#{null}}")
    private String basicAuthBearer;

    @Value("${" + "extpetstore.api-key-header:#{null}}")
    private String defaultApiKeyHeader;

    @Value("${" + "extpetstore.api-key-cookie:#{null}}")
    private String defaultApiKeyCookie;

    @Value("${" + "extpetstore.api-key-query:#{null}}")
    private String defaultApiKeyQuery;

    private final List<ApiActionBuilderCustomizer> customizers;

    private final Endpoint endpoint;

    private final OpenApiSpecification openApiSpecification;

    public ExtPetApi(Endpoint endpoint)  {
        this(endpoint, emptyList());
    }

    public ExtPetApi(Endpoint endpoint, List<ApiActionBuilderCustomizer> customizers)  {
        this.endpoint = endpoint;
        this.customizers = customizers;

        URL resource = ExtPetStore.class.getResource("ExtPetStore_openApi.yaml");
        if (resource == null) {
            throw new IllegalStateException(format("Cannot find resource '%s'. This resource is typically created during API generation and should therefore be present. Check API generation.", "ExtPetStore_openApi.yaml"));
        }
        openApiSpecification = OpenApiSpecification.from(resource);
    }

    public static ExtPetApi extPetApi(Endpoint endpoint) {
        return new ExtPetApi(endpoint);
    }

    @Override
    public String getApiTitle() {
        return "Extended Petstore API";
    }

    @Override
    public String getApiVersion() {
        return "1.0.0";
    }

    @Override
    public String getApiPrefix() {
        return "ExtPetStore";
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
    public GenerateVaccinationReportSendActionBuilder sendGenerateVaccinationReport(Resource template, Integer reqIntVal)   {
            return new GenerateVaccinationReportSendActionBuilder(this, openApiSpecification, template, reqIntVal);
    }

    /**
     * Builder with required parameters as string to allow for dynamic content.
     */
    public GenerateVaccinationReportSendActionBuilder sendGenerateVaccinationReport$(String templateExpression,  String reqIntValExpression )   {
            return new GenerateVaccinationReportSendActionBuilder(openApiSpecification, this, templateExpression, reqIntValExpression);
    }

    public GenerateVaccinationReportReceiveActionBuilder receiveGenerateVaccinationReport(@NotNull HttpStatus statusCode)   {
        return new GenerateVaccinationReportReceiveActionBuilder(this, openApiSpecification, Integer.toString(statusCode.value()));
    }

    public GenerateVaccinationReportReceiveActionBuilder receiveGenerateVaccinationReport(@NotNull String statusCode)   {
        return new GenerateVaccinationReportReceiveActionBuilder(this, openApiSpecification,  statusCode);
    }

    /**
     * Builder with type safe required parameters.
     */
    public GetPetByIdWithApiKeyAuthenticationSendActionBuilder sendGetPetByIdWithApiKeyAuthentication(Long petId, Boolean allDetails)   {
            GetPetByIdWithApiKeyAuthenticationSendActionBuilder builder =  new GetPetByIdWithApiKeyAuthenticationSendActionBuilder(this, openApiSpecification, petId, allDetails);
            builder.setBase64EncodeApiKey(base64EncodeApiKey);
            return builder;
    }

    /**
     * Builder with required parameters as string to allow for dynamic content.
     */
    public GetPetByIdWithApiKeyAuthenticationSendActionBuilder sendGetPetByIdWithApiKeyAuthentication$(String petIdExpression,  String allDetailsExpression )   {
            GetPetByIdWithApiKeyAuthenticationSendActionBuilder builder =  new GetPetByIdWithApiKeyAuthenticationSendActionBuilder(openApiSpecification, this, petIdExpression, allDetailsExpression);
            builder.setBase64EncodeApiKey(base64EncodeApiKey);
            builder.setApiKeyQuery(defaultApiKeyQuery);
            builder.setApiKeyHeader(defaultApiKeyHeader);
            builder.setApiKeyCookie(defaultApiKeyCookie);
            return builder;
    }

    public GetPetByIdWithApiKeyAuthenticationReceiveActionBuilder receiveGetPetByIdWithApiKeyAuthentication(@NotNull HttpStatus statusCode)   {
        return new GetPetByIdWithApiKeyAuthenticationReceiveActionBuilder(this, openApiSpecification, Integer.toString(statusCode.value()));
    }

    public GetPetByIdWithApiKeyAuthenticationReceiveActionBuilder receiveGetPetByIdWithApiKeyAuthentication(@NotNull String statusCode)   {
        return new GetPetByIdWithApiKeyAuthenticationReceiveActionBuilder(this, openApiSpecification,  statusCode);
    }

    /**
     * Builder with type safe required parameters.
     */
    public GetPetByIdWithBasicAuthenticationSendActionBuilder sendGetPetByIdWithBasicAuthentication(Long petId, Boolean allDetails)   {
            GetPetByIdWithBasicAuthenticationSendActionBuilder builder =  new GetPetByIdWithBasicAuthenticationSendActionBuilder(this, openApiSpecification, petId, allDetails);
            return builder;
    }

    /**
     * Builder with required parameters as string to allow for dynamic content.
     */
    public GetPetByIdWithBasicAuthenticationSendActionBuilder sendGetPetByIdWithBasicAuthentication$(String petIdExpression,  String allDetailsExpression )   {
            GetPetByIdWithBasicAuthenticationSendActionBuilder builder =  new GetPetByIdWithBasicAuthenticationSendActionBuilder(openApiSpecification, this, petIdExpression, allDetailsExpression);
            builder.setBasicAuthUsername(basicUsername);
            builder.setBasicAuthPassword(basicPassword);
            return builder;
    }

    public GetPetByIdWithBasicAuthenticationReceiveActionBuilder receiveGetPetByIdWithBasicAuthentication(@NotNull HttpStatus statusCode)   {
        return new GetPetByIdWithBasicAuthenticationReceiveActionBuilder(this, openApiSpecification, Integer.toString(statusCode.value()));
    }

    public GetPetByIdWithBasicAuthenticationReceiveActionBuilder receiveGetPetByIdWithBasicAuthentication(@NotNull String statusCode)   {
        return new GetPetByIdWithBasicAuthenticationReceiveActionBuilder(this, openApiSpecification,  statusCode);
    }

    /**
     * Builder with type safe required parameters.
     */
    public GetPetByIdWithBearerAuthenticationSendActionBuilder sendGetPetByIdWithBearerAuthentication(Long petId, Boolean allDetails)   {
            GetPetByIdWithBearerAuthenticationSendActionBuilder builder =  new GetPetByIdWithBearerAuthenticationSendActionBuilder(this, openApiSpecification, petId, allDetails);
            return builder;
    }

    /**
     * Builder with required parameters as string to allow for dynamic content.
     */
    public GetPetByIdWithBearerAuthenticationSendActionBuilder sendGetPetByIdWithBearerAuthentication$(String petIdExpression,  String allDetailsExpression )   {
            GetPetByIdWithBearerAuthenticationSendActionBuilder builder =  new GetPetByIdWithBearerAuthenticationSendActionBuilder(openApiSpecification, this, petIdExpression, allDetailsExpression);
            builder.setBasicAuthBearer(basicAuthBearer);
            return builder;
    }

    public GetPetByIdWithBearerAuthenticationReceiveActionBuilder receiveGetPetByIdWithBearerAuthentication(@NotNull HttpStatus statusCode)   {
        return new GetPetByIdWithBearerAuthenticationReceiveActionBuilder(this, openApiSpecification, Integer.toString(statusCode.value()));
    }

    public GetPetByIdWithBearerAuthenticationReceiveActionBuilder receiveGetPetByIdWithBearerAuthentication(@NotNull String statusCode)   {
        return new GetPetByIdWithBearerAuthenticationReceiveActionBuilder(this, openApiSpecification,  statusCode);
    }

    /**
     * Builder with type safe required parameters.
     */
    public GetPetWithCookieSendActionBuilder sendGetPetWithCookie(Long petId, String sessionId)   {
            return new GetPetWithCookieSendActionBuilder(this, openApiSpecification, petId, sessionId);
    }

    /**
     * Builder with required parameters as string to allow for dynamic content.
     */
    public GetPetWithCookieSendActionBuilder sendGetPetWithCookie$(String petIdExpression,  String sessionIdExpression )   {
            return new GetPetWithCookieSendActionBuilder(openApiSpecification, this, petIdExpression, sessionIdExpression);
    }

    public GetPetWithCookieReceiveActionBuilder receiveGetPetWithCookie(@NotNull HttpStatus statusCode)   {
        return new GetPetWithCookieReceiveActionBuilder(this, openApiSpecification, Integer.toString(statusCode.value()));
    }

    public GetPetWithCookieReceiveActionBuilder receiveGetPetWithCookie(@NotNull String statusCode)   {
        return new GetPetWithCookieReceiveActionBuilder(this, openApiSpecification,  statusCode);
    }

    /**
     * Builder with type safe required parameters.
     */
    public GetPetWithDeepObjectTypeQuerySendActionBuilder sendGetPetWithDeepObjectTypeQuery(PetIdentifier petId)   {
            return new GetPetWithDeepObjectTypeQuerySendActionBuilder(this, openApiSpecification, petId);
    }

    /**
     * Builder with required parameters as string to allow for dynamic content.
     */
    public GetPetWithDeepObjectTypeQuerySendActionBuilder sendGetPetWithDeepObjectTypeQuery$(String petIdExpression )   {
            return new GetPetWithDeepObjectTypeQuerySendActionBuilder(openApiSpecification, this, petIdExpression);
    }

    public GetPetWithDeepObjectTypeQueryReceiveActionBuilder receiveGetPetWithDeepObjectTypeQuery(@NotNull HttpStatus statusCode)   {
        return new GetPetWithDeepObjectTypeQueryReceiveActionBuilder(this, openApiSpecification, Integer.toString(statusCode.value()));
    }

    public GetPetWithDeepObjectTypeQueryReceiveActionBuilder receiveGetPetWithDeepObjectTypeQuery(@NotNull String statusCode)   {
        return new GetPetWithDeepObjectTypeQueryReceiveActionBuilder(this, openApiSpecification,  statusCode);
    }

    /**
     * Builder with type safe required parameters.
     */
    public GetPetWithFormExplodedStyleCookieSendActionBuilder sendGetPetWithFormExplodedStyleCookie(List<Integer> petId)   {
            return new GetPetWithFormExplodedStyleCookieSendActionBuilder(this, openApiSpecification, petId);
    }

    /**
     * Builder with required parameters as string to allow for dynamic content.
     */
    public GetPetWithFormExplodedStyleCookieSendActionBuilder sendGetPetWithFormExplodedStyleCookie$(List<String> petIdExpression )   {
            return new GetPetWithFormExplodedStyleCookieSendActionBuilder(openApiSpecification, this, petIdExpression);
    }

    public GetPetWithFormExplodedStyleCookieReceiveActionBuilder receiveGetPetWithFormExplodedStyleCookie(@NotNull HttpStatus statusCode)   {
        return new GetPetWithFormExplodedStyleCookieReceiveActionBuilder(this, openApiSpecification, Integer.toString(statusCode.value()));
    }

    public GetPetWithFormExplodedStyleCookieReceiveActionBuilder receiveGetPetWithFormExplodedStyleCookie(@NotNull String statusCode)   {
        return new GetPetWithFormExplodedStyleCookieReceiveActionBuilder(this, openApiSpecification,  statusCode);
    }

    /**
     * Builder with type safe required parameters.
     */
    public GetPetWithFormObjectStyleCookieSendActionBuilder sendGetPetWithFormObjectStyleCookie(PetIdentifier petId)   {
            return new GetPetWithFormObjectStyleCookieSendActionBuilder(this, openApiSpecification, petId);
    }

    /**
     * Builder with required parameters as string to allow for dynamic content.
     */
    public GetPetWithFormObjectStyleCookieSendActionBuilder sendGetPetWithFormObjectStyleCookie$(String petIdExpression )   {
            return new GetPetWithFormObjectStyleCookieSendActionBuilder(openApiSpecification, this, petIdExpression);
    }

    public GetPetWithFormObjectStyleCookieReceiveActionBuilder receiveGetPetWithFormObjectStyleCookie(@NotNull HttpStatus statusCode)   {
        return new GetPetWithFormObjectStyleCookieReceiveActionBuilder(this, openApiSpecification, Integer.toString(statusCode.value()));
    }

    public GetPetWithFormObjectStyleCookieReceiveActionBuilder receiveGetPetWithFormObjectStyleCookie(@NotNull String statusCode)   {
        return new GetPetWithFormObjectStyleCookieReceiveActionBuilder(this, openApiSpecification,  statusCode);
    }

    /**
     * Builder with type safe required parameters.
     */
    public GetPetWithFormStyleCookieSendActionBuilder sendGetPetWithFormStyleCookie(List<Integer> petId)   {
            return new GetPetWithFormStyleCookieSendActionBuilder(this, openApiSpecification, petId);
    }

    /**
     * Builder with required parameters as string to allow for dynamic content.
     */
    public GetPetWithFormStyleCookieSendActionBuilder sendGetPetWithFormStyleCookie$(List<String> petIdExpression )   {
            return new GetPetWithFormStyleCookieSendActionBuilder(openApiSpecification, this, petIdExpression);
    }

    public GetPetWithFormStyleCookieReceiveActionBuilder receiveGetPetWithFormStyleCookie(@NotNull HttpStatus statusCode)   {
        return new GetPetWithFormStyleCookieReceiveActionBuilder(this, openApiSpecification, Integer.toString(statusCode.value()));
    }

    public GetPetWithFormStyleCookieReceiveActionBuilder receiveGetPetWithFormStyleCookie(@NotNull String statusCode)   {
        return new GetPetWithFormStyleCookieReceiveActionBuilder(this, openApiSpecification,  statusCode);
    }

    /**
     * Builder with type safe required parameters.
     */
    public GetPetWithFormStyleExplodedObjectQuerySendActionBuilder sendGetPetWithFormStyleExplodedObjectQuery(PetIdentifier petId)   {
            return new GetPetWithFormStyleExplodedObjectQuerySendActionBuilder(this, openApiSpecification, petId);
    }

    /**
     * Builder with required parameters as string to allow for dynamic content.
     */
    public GetPetWithFormStyleExplodedObjectQuerySendActionBuilder sendGetPetWithFormStyleExplodedObjectQuery$(String petIdExpression )   {
            return new GetPetWithFormStyleExplodedObjectQuerySendActionBuilder(openApiSpecification, this, petIdExpression);
    }

    public GetPetWithFormStyleExplodedObjectQueryReceiveActionBuilder receiveGetPetWithFormStyleExplodedObjectQuery(@NotNull HttpStatus statusCode)   {
        return new GetPetWithFormStyleExplodedObjectQueryReceiveActionBuilder(this, openApiSpecification, Integer.toString(statusCode.value()));
    }

    public GetPetWithFormStyleExplodedObjectQueryReceiveActionBuilder receiveGetPetWithFormStyleExplodedObjectQuery(@NotNull String statusCode)   {
        return new GetPetWithFormStyleExplodedObjectQueryReceiveActionBuilder(this, openApiSpecification,  statusCode);
    }

    /**
     * Builder with type safe required parameters.
     */
    public GetPetWithFormStyleExplodedQuerySendActionBuilder sendGetPetWithFormStyleExplodedQuery(List<Integer> petId)   {
            return new GetPetWithFormStyleExplodedQuerySendActionBuilder(this, openApiSpecification, petId);
    }

    /**
     * Builder with required parameters as string to allow for dynamic content.
     */
    public GetPetWithFormStyleExplodedQuerySendActionBuilder sendGetPetWithFormStyleExplodedQuery$(List<String> petIdExpression )   {
            return new GetPetWithFormStyleExplodedQuerySendActionBuilder(openApiSpecification, this, petIdExpression);
    }

    public GetPetWithFormStyleExplodedQueryReceiveActionBuilder receiveGetPetWithFormStyleExplodedQuery(@NotNull HttpStatus statusCode)   {
        return new GetPetWithFormStyleExplodedQueryReceiveActionBuilder(this, openApiSpecification, Integer.toString(statusCode.value()));
    }

    public GetPetWithFormStyleExplodedQueryReceiveActionBuilder receiveGetPetWithFormStyleExplodedQuery(@NotNull String statusCode)   {
        return new GetPetWithFormStyleExplodedQueryReceiveActionBuilder(this, openApiSpecification,  statusCode);
    }

    /**
     * Builder with type safe required parameters.
     */
    public GetPetWithFormStyleObjectQuerySendActionBuilder sendGetPetWithFormStyleObjectQuery(PetIdentifier petId)   {
            return new GetPetWithFormStyleObjectQuerySendActionBuilder(this, openApiSpecification, petId);
    }

    /**
     * Builder with required parameters as string to allow for dynamic content.
     */
    public GetPetWithFormStyleObjectQuerySendActionBuilder sendGetPetWithFormStyleObjectQuery$(String petIdExpression )   {
            return new GetPetWithFormStyleObjectQuerySendActionBuilder(openApiSpecification, this, petIdExpression);
    }

    public GetPetWithFormStyleObjectQueryReceiveActionBuilder receiveGetPetWithFormStyleObjectQuery(@NotNull HttpStatus statusCode)   {
        return new GetPetWithFormStyleObjectQueryReceiveActionBuilder(this, openApiSpecification, Integer.toString(statusCode.value()));
    }

    public GetPetWithFormStyleObjectQueryReceiveActionBuilder receiveGetPetWithFormStyleObjectQuery(@NotNull String statusCode)   {
        return new GetPetWithFormStyleObjectQueryReceiveActionBuilder(this, openApiSpecification,  statusCode);
    }

    /**
     * Builder with type safe required parameters.
     */
    public GetPetWithFormStyleQuerySendActionBuilder sendGetPetWithFormStyleQuery(List<Integer> petId)   {
            return new GetPetWithFormStyleQuerySendActionBuilder(this, openApiSpecification, petId);
    }

    /**
     * Builder with required parameters as string to allow for dynamic content.
     */
    public GetPetWithFormStyleQuerySendActionBuilder sendGetPetWithFormStyleQuery$(List<String> petIdExpression )   {
            return new GetPetWithFormStyleQuerySendActionBuilder(openApiSpecification, this, petIdExpression);
    }

    public GetPetWithFormStyleQueryReceiveActionBuilder receiveGetPetWithFormStyleQuery(@NotNull HttpStatus statusCode)   {
        return new GetPetWithFormStyleQueryReceiveActionBuilder(this, openApiSpecification, Integer.toString(statusCode.value()));
    }

    public GetPetWithFormStyleQueryReceiveActionBuilder receiveGetPetWithFormStyleQuery(@NotNull String statusCode)   {
        return new GetPetWithFormStyleQueryReceiveActionBuilder(this, openApiSpecification,  statusCode);
    }

    /**
     * Builder with type safe required parameters.
     */
    public GetPetWithLabelStyleArraySendActionBuilder sendGetPetWithLabelStyleArray(List<Integer> petId)   {
            return new GetPetWithLabelStyleArraySendActionBuilder(this, openApiSpecification, petId);
    }

    /**
     * Builder with required parameters as string to allow for dynamic content.
     */
    public GetPetWithLabelStyleArraySendActionBuilder sendGetPetWithLabelStyleArray$(List<String> petIdExpression )   {
            return new GetPetWithLabelStyleArraySendActionBuilder(openApiSpecification, this, petIdExpression);
    }

    public GetPetWithLabelStyleArrayReceiveActionBuilder receiveGetPetWithLabelStyleArray(@NotNull HttpStatus statusCode)   {
        return new GetPetWithLabelStyleArrayReceiveActionBuilder(this, openApiSpecification, Integer.toString(statusCode.value()));
    }

    public GetPetWithLabelStyleArrayReceiveActionBuilder receiveGetPetWithLabelStyleArray(@NotNull String statusCode)   {
        return new GetPetWithLabelStyleArrayReceiveActionBuilder(this, openApiSpecification,  statusCode);
    }

    /**
     * Builder with type safe required parameters.
     */
    public GetPetWithLabelStyleArrayExplodedSendActionBuilder sendGetPetWithLabelStyleArrayExploded(List<Integer> petId)   {
            return new GetPetWithLabelStyleArrayExplodedSendActionBuilder(this, openApiSpecification, petId);
    }

    /**
     * Builder with required parameters as string to allow for dynamic content.
     */
    public GetPetWithLabelStyleArrayExplodedSendActionBuilder sendGetPetWithLabelStyleArrayExploded$(List<String> petIdExpression )   {
            return new GetPetWithLabelStyleArrayExplodedSendActionBuilder(openApiSpecification, this, petIdExpression);
    }

    public GetPetWithLabelStyleArrayExplodedReceiveActionBuilder receiveGetPetWithLabelStyleArrayExploded(@NotNull HttpStatus statusCode)   {
        return new GetPetWithLabelStyleArrayExplodedReceiveActionBuilder(this, openApiSpecification, Integer.toString(statusCode.value()));
    }

    public GetPetWithLabelStyleArrayExplodedReceiveActionBuilder receiveGetPetWithLabelStyleArrayExploded(@NotNull String statusCode)   {
        return new GetPetWithLabelStyleArrayExplodedReceiveActionBuilder(this, openApiSpecification,  statusCode);
    }

    /**
     * Builder with type safe required parameters.
     */
    public GetPetWithLabelStyleObjectSendActionBuilder sendGetPetWithLabelStyleObject(PetIdentifier petId)   {
            return new GetPetWithLabelStyleObjectSendActionBuilder(this, openApiSpecification, petId);
    }

    /**
     * Builder with required parameters as string to allow for dynamic content.
     */
    public GetPetWithLabelStyleObjectSendActionBuilder sendGetPetWithLabelStyleObject$(String petIdExpression )   {
            return new GetPetWithLabelStyleObjectSendActionBuilder(openApiSpecification, this, petIdExpression);
    }

    public GetPetWithLabelStyleObjectReceiveActionBuilder receiveGetPetWithLabelStyleObject(@NotNull HttpStatus statusCode)   {
        return new GetPetWithLabelStyleObjectReceiveActionBuilder(this, openApiSpecification, Integer.toString(statusCode.value()));
    }

    public GetPetWithLabelStyleObjectReceiveActionBuilder receiveGetPetWithLabelStyleObject(@NotNull String statusCode)   {
        return new GetPetWithLabelStyleObjectReceiveActionBuilder(this, openApiSpecification,  statusCode);
    }

    /**
     * Builder with type safe required parameters.
     */
    public GetPetWithLabelStyleObjectExplodedSendActionBuilder sendGetPetWithLabelStyleObjectExploded(PetIdentifier petId)   {
            return new GetPetWithLabelStyleObjectExplodedSendActionBuilder(this, openApiSpecification, petId);
    }

    /**
     * Builder with required parameters as string to allow for dynamic content.
     */
    public GetPetWithLabelStyleObjectExplodedSendActionBuilder sendGetPetWithLabelStyleObjectExploded$(String petIdExpression )   {
            return new GetPetWithLabelStyleObjectExplodedSendActionBuilder(openApiSpecification, this, petIdExpression);
    }

    public GetPetWithLabelStyleObjectExplodedReceiveActionBuilder receiveGetPetWithLabelStyleObjectExploded(@NotNull HttpStatus statusCode)   {
        return new GetPetWithLabelStyleObjectExplodedReceiveActionBuilder(this, openApiSpecification, Integer.toString(statusCode.value()));
    }

    public GetPetWithLabelStyleObjectExplodedReceiveActionBuilder receiveGetPetWithLabelStyleObjectExploded(@NotNull String statusCode)   {
        return new GetPetWithLabelStyleObjectExplodedReceiveActionBuilder(this, openApiSpecification,  statusCode);
    }

    /**
     * Builder with type safe required parameters.
     */
    public GetPetWithMatrixStyleArraySendActionBuilder sendGetPetWithMatrixStyleArray(List<Integer> petId)   {
            return new GetPetWithMatrixStyleArraySendActionBuilder(this, openApiSpecification, petId);
    }

    /**
     * Builder with required parameters as string to allow for dynamic content.
     */
    public GetPetWithMatrixStyleArraySendActionBuilder sendGetPetWithMatrixStyleArray$(List<String> petIdExpression )   {
            return new GetPetWithMatrixStyleArraySendActionBuilder(openApiSpecification, this, petIdExpression);
    }

    public GetPetWithMatrixStyleArrayReceiveActionBuilder receiveGetPetWithMatrixStyleArray(@NotNull HttpStatus statusCode)   {
        return new GetPetWithMatrixStyleArrayReceiveActionBuilder(this, openApiSpecification, Integer.toString(statusCode.value()));
    }

    public GetPetWithMatrixStyleArrayReceiveActionBuilder receiveGetPetWithMatrixStyleArray(@NotNull String statusCode)   {
        return new GetPetWithMatrixStyleArrayReceiveActionBuilder(this, openApiSpecification,  statusCode);
    }

    /**
     * Builder with type safe required parameters.
     */
    public GetPetWithMatrixStyleArrayExplodedSendActionBuilder sendGetPetWithMatrixStyleArrayExploded(List<Integer> petId)   {
            return new GetPetWithMatrixStyleArrayExplodedSendActionBuilder(this, openApiSpecification, petId);
    }

    /**
     * Builder with required parameters as string to allow for dynamic content.
     */
    public GetPetWithMatrixStyleArrayExplodedSendActionBuilder sendGetPetWithMatrixStyleArrayExploded$(List<String> petIdExpression )   {
            return new GetPetWithMatrixStyleArrayExplodedSendActionBuilder(openApiSpecification, this, petIdExpression);
    }

    public GetPetWithMatrixStyleArrayExplodedReceiveActionBuilder receiveGetPetWithMatrixStyleArrayExploded(@NotNull HttpStatus statusCode)   {
        return new GetPetWithMatrixStyleArrayExplodedReceiveActionBuilder(this, openApiSpecification, Integer.toString(statusCode.value()));
    }

    public GetPetWithMatrixStyleArrayExplodedReceiveActionBuilder receiveGetPetWithMatrixStyleArrayExploded(@NotNull String statusCode)   {
        return new GetPetWithMatrixStyleArrayExplodedReceiveActionBuilder(this, openApiSpecification,  statusCode);
    }

    /**
     * Builder with type safe required parameters.
     */
    public GetPetWithMatrixStyleObjectSendActionBuilder sendGetPetWithMatrixStyleObject(PetIdentifier petId)   {
            return new GetPetWithMatrixStyleObjectSendActionBuilder(this, openApiSpecification, petId);
    }

    /**
     * Builder with required parameters as string to allow for dynamic content.
     */
    public GetPetWithMatrixStyleObjectSendActionBuilder sendGetPetWithMatrixStyleObject$(String petIdExpression )   {
            return new GetPetWithMatrixStyleObjectSendActionBuilder(openApiSpecification, this, petIdExpression);
    }

    public GetPetWithMatrixStyleObjectReceiveActionBuilder receiveGetPetWithMatrixStyleObject(@NotNull HttpStatus statusCode)   {
        return new GetPetWithMatrixStyleObjectReceiveActionBuilder(this, openApiSpecification, Integer.toString(statusCode.value()));
    }

    public GetPetWithMatrixStyleObjectReceiveActionBuilder receiveGetPetWithMatrixStyleObject(@NotNull String statusCode)   {
        return new GetPetWithMatrixStyleObjectReceiveActionBuilder(this, openApiSpecification,  statusCode);
    }

    /**
     * Builder with type safe required parameters.
     */
    public GetPetWithMatrixStyleObjectExplodedSendActionBuilder sendGetPetWithMatrixStyleObjectExploded(PetIdentifier petId)   {
            return new GetPetWithMatrixStyleObjectExplodedSendActionBuilder(this, openApiSpecification, petId);
    }

    /**
     * Builder with required parameters as string to allow for dynamic content.
     */
    public GetPetWithMatrixStyleObjectExplodedSendActionBuilder sendGetPetWithMatrixStyleObjectExploded$(String petIdExpression )   {
            return new GetPetWithMatrixStyleObjectExplodedSendActionBuilder(openApiSpecification, this, petIdExpression);
    }

    public GetPetWithMatrixStyleObjectExplodedReceiveActionBuilder receiveGetPetWithMatrixStyleObjectExploded(@NotNull HttpStatus statusCode)   {
        return new GetPetWithMatrixStyleObjectExplodedReceiveActionBuilder(this, openApiSpecification, Integer.toString(statusCode.value()));
    }

    public GetPetWithMatrixStyleObjectExplodedReceiveActionBuilder receiveGetPetWithMatrixStyleObjectExploded(@NotNull String statusCode)   {
        return new GetPetWithMatrixStyleObjectExplodedReceiveActionBuilder(this, openApiSpecification,  statusCode);
    }

    /**
     * Builder with type safe required parameters.
     */
    public GetPetWithSimpleStyleArraySendActionBuilder sendGetPetWithSimpleStyleArray(List<Integer> petId)   {
            return new GetPetWithSimpleStyleArraySendActionBuilder(this, openApiSpecification, petId);
    }

    /**
     * Builder with required parameters as string to allow for dynamic content.
     */
    public GetPetWithSimpleStyleArraySendActionBuilder sendGetPetWithSimpleStyleArray$(List<String> petIdExpression )   {
            return new GetPetWithSimpleStyleArraySendActionBuilder(openApiSpecification, this, petIdExpression);
    }

    public GetPetWithSimpleStyleArrayReceiveActionBuilder receiveGetPetWithSimpleStyleArray(@NotNull HttpStatus statusCode)   {
        return new GetPetWithSimpleStyleArrayReceiveActionBuilder(this, openApiSpecification, Integer.toString(statusCode.value()));
    }

    public GetPetWithSimpleStyleArrayReceiveActionBuilder receiveGetPetWithSimpleStyleArray(@NotNull String statusCode)   {
        return new GetPetWithSimpleStyleArrayReceiveActionBuilder(this, openApiSpecification,  statusCode);
    }

    /**
     * Builder with type safe required parameters.
     */
    public GetPetWithSimpleStyleArrayExplodedSendActionBuilder sendGetPetWithSimpleStyleArrayExploded(List<Integer> petId)   {
            return new GetPetWithSimpleStyleArrayExplodedSendActionBuilder(this, openApiSpecification, petId);
    }

    /**
     * Builder with required parameters as string to allow for dynamic content.
     */
    public GetPetWithSimpleStyleArrayExplodedSendActionBuilder sendGetPetWithSimpleStyleArrayExploded$(List<String> petIdExpression )   {
            return new GetPetWithSimpleStyleArrayExplodedSendActionBuilder(openApiSpecification, this, petIdExpression);
    }

    public GetPetWithSimpleStyleArrayExplodedReceiveActionBuilder receiveGetPetWithSimpleStyleArrayExploded(@NotNull HttpStatus statusCode)   {
        return new GetPetWithSimpleStyleArrayExplodedReceiveActionBuilder(this, openApiSpecification, Integer.toString(statusCode.value()));
    }

    public GetPetWithSimpleStyleArrayExplodedReceiveActionBuilder receiveGetPetWithSimpleStyleArrayExploded(@NotNull String statusCode)   {
        return new GetPetWithSimpleStyleArrayExplodedReceiveActionBuilder(this, openApiSpecification,  statusCode);
    }

    /**
     * Builder with type safe required parameters.
     */
    public GetPetWithSimpleStyleExplodedHeaderSendActionBuilder sendGetPetWithSimpleStyleExplodedHeader(List<Integer> petId)   {
            return new GetPetWithSimpleStyleExplodedHeaderSendActionBuilder(this, openApiSpecification, petId);
    }

    /**
     * Builder with required parameters as string to allow for dynamic content.
     */
    public GetPetWithSimpleStyleExplodedHeaderSendActionBuilder sendGetPetWithSimpleStyleExplodedHeader$(List<String> petIdExpression )   {
            return new GetPetWithSimpleStyleExplodedHeaderSendActionBuilder(openApiSpecification, this, petIdExpression);
    }

    public GetPetWithSimpleStyleExplodedHeaderReceiveActionBuilder receiveGetPetWithSimpleStyleExplodedHeader(@NotNull HttpStatus statusCode)   {
        return new GetPetWithSimpleStyleExplodedHeaderReceiveActionBuilder(this, openApiSpecification, Integer.toString(statusCode.value()));
    }

    public GetPetWithSimpleStyleExplodedHeaderReceiveActionBuilder receiveGetPetWithSimpleStyleExplodedHeader(@NotNull String statusCode)   {
        return new GetPetWithSimpleStyleExplodedHeaderReceiveActionBuilder(this, openApiSpecification,  statusCode);
    }

    /**
     * Builder with type safe required parameters.
     */
    public GetPetWithSimpleStyleExplodedObjectHeaderSendActionBuilder sendGetPetWithSimpleStyleExplodedObjectHeader(PetIdentifier petId)   {
            return new GetPetWithSimpleStyleExplodedObjectHeaderSendActionBuilder(this, openApiSpecification, petId);
    }

    /**
     * Builder with required parameters as string to allow for dynamic content.
     */
    public GetPetWithSimpleStyleExplodedObjectHeaderSendActionBuilder sendGetPetWithSimpleStyleExplodedObjectHeader$(String petIdExpression )   {
            return new GetPetWithSimpleStyleExplodedObjectHeaderSendActionBuilder(openApiSpecification, this, petIdExpression);
    }

    public GetPetWithSimpleStyleExplodedObjectHeaderReceiveActionBuilder receiveGetPetWithSimpleStyleExplodedObjectHeader(@NotNull HttpStatus statusCode)   {
        return new GetPetWithSimpleStyleExplodedObjectHeaderReceiveActionBuilder(this, openApiSpecification, Integer.toString(statusCode.value()));
    }

    public GetPetWithSimpleStyleExplodedObjectHeaderReceiveActionBuilder receiveGetPetWithSimpleStyleExplodedObjectHeader(@NotNull String statusCode)   {
        return new GetPetWithSimpleStyleExplodedObjectHeaderReceiveActionBuilder(this, openApiSpecification,  statusCode);
    }

    /**
     * Builder with type safe required parameters.
     */
    public GetPetWithSimpleStyleHeaderSendActionBuilder sendGetPetWithSimpleStyleHeader(List<Integer> petId)   {
            return new GetPetWithSimpleStyleHeaderSendActionBuilder(this, openApiSpecification, petId);
    }

    /**
     * Builder with required parameters as string to allow for dynamic content.
     */
    public GetPetWithSimpleStyleHeaderSendActionBuilder sendGetPetWithSimpleStyleHeader$(List<String> petIdExpression )   {
            return new GetPetWithSimpleStyleHeaderSendActionBuilder(openApiSpecification, this, petIdExpression);
    }

    public GetPetWithSimpleStyleHeaderReceiveActionBuilder receiveGetPetWithSimpleStyleHeader(@NotNull HttpStatus statusCode)   {
        return new GetPetWithSimpleStyleHeaderReceiveActionBuilder(this, openApiSpecification, Integer.toString(statusCode.value()));
    }

    public GetPetWithSimpleStyleHeaderReceiveActionBuilder receiveGetPetWithSimpleStyleHeader(@NotNull String statusCode)   {
        return new GetPetWithSimpleStyleHeaderReceiveActionBuilder(this, openApiSpecification,  statusCode);
    }

    /**
     * Builder with type safe required parameters.
     */
    public GetPetWithSimpleStyleObjectSendActionBuilder sendGetPetWithSimpleStyleObject(PetIdentifier petId)   {
            return new GetPetWithSimpleStyleObjectSendActionBuilder(this, openApiSpecification, petId);
    }

    /**
     * Builder with required parameters as string to allow for dynamic content.
     */
    public GetPetWithSimpleStyleObjectSendActionBuilder sendGetPetWithSimpleStyleObject$(String petIdExpression )   {
            return new GetPetWithSimpleStyleObjectSendActionBuilder(openApiSpecification, this, petIdExpression);
    }

    public GetPetWithSimpleStyleObjectReceiveActionBuilder receiveGetPetWithSimpleStyleObject(@NotNull HttpStatus statusCode)   {
        return new GetPetWithSimpleStyleObjectReceiveActionBuilder(this, openApiSpecification, Integer.toString(statusCode.value()));
    }

    public GetPetWithSimpleStyleObjectReceiveActionBuilder receiveGetPetWithSimpleStyleObject(@NotNull String statusCode)   {
        return new GetPetWithSimpleStyleObjectReceiveActionBuilder(this, openApiSpecification,  statusCode);
    }

    /**
     * Builder with type safe required parameters.
     */
    public GetPetWithSimpleStyleObjectExplodedSendActionBuilder sendGetPetWithSimpleStyleObjectExploded(PetIdentifier petId)   {
            return new GetPetWithSimpleStyleObjectExplodedSendActionBuilder(this, openApiSpecification, petId);
    }

    /**
     * Builder with required parameters as string to allow for dynamic content.
     */
    public GetPetWithSimpleStyleObjectExplodedSendActionBuilder sendGetPetWithSimpleStyleObjectExploded$(String petIdExpression )   {
            return new GetPetWithSimpleStyleObjectExplodedSendActionBuilder(openApiSpecification, this, petIdExpression);
    }

    public GetPetWithSimpleStyleObjectExplodedReceiveActionBuilder receiveGetPetWithSimpleStyleObjectExploded(@NotNull HttpStatus statusCode)   {
        return new GetPetWithSimpleStyleObjectExplodedReceiveActionBuilder(this, openApiSpecification, Integer.toString(statusCode.value()));
    }

    public GetPetWithSimpleStyleObjectExplodedReceiveActionBuilder receiveGetPetWithSimpleStyleObjectExploded(@NotNull String statusCode)   {
        return new GetPetWithSimpleStyleObjectExplodedReceiveActionBuilder(this, openApiSpecification,  statusCode);
    }

    /**
     * Builder with type safe required parameters.
     */
    public GetPetWithSimpleStyleObjectHeaderSendActionBuilder sendGetPetWithSimpleStyleObjectHeader(PetIdentifier petId)   {
            return new GetPetWithSimpleStyleObjectHeaderSendActionBuilder(this, openApiSpecification, petId);
    }

    /**
     * Builder with required parameters as string to allow for dynamic content.
     */
    public GetPetWithSimpleStyleObjectHeaderSendActionBuilder sendGetPetWithSimpleStyleObjectHeader$(String petIdExpression )   {
            return new GetPetWithSimpleStyleObjectHeaderSendActionBuilder(openApiSpecification, this, petIdExpression);
    }

    public GetPetWithSimpleStyleObjectHeaderReceiveActionBuilder receiveGetPetWithSimpleStyleObjectHeader(@NotNull HttpStatus statusCode)   {
        return new GetPetWithSimpleStyleObjectHeaderReceiveActionBuilder(this, openApiSpecification, Integer.toString(statusCode.value()));
    }

    public GetPetWithSimpleStyleObjectHeaderReceiveActionBuilder receiveGetPetWithSimpleStyleObjectHeader(@NotNull String statusCode)   {
        return new GetPetWithSimpleStyleObjectHeaderReceiveActionBuilder(this, openApiSpecification,  statusCode);
    }

    /**
     * Builder with type safe required parameters.
     */
    public PostVaccinationDocumentSendActionBuilder sendPostVaccinationDocument(String bucket, String filename)   {
            return new PostVaccinationDocumentSendActionBuilder(this, openApiSpecification, bucket, filename);
    }

    public PostVaccinationDocumentReceiveActionBuilder receivePostVaccinationDocument(@NotNull HttpStatus statusCode)   {
        return new PostVaccinationDocumentReceiveActionBuilder(this, openApiSpecification, Integer.toString(statusCode.value()));
    }

    public PostVaccinationDocumentReceiveActionBuilder receivePostVaccinationDocument(@NotNull String statusCode)   {
        return new PostVaccinationDocumentReceiveActionBuilder(this, openApiSpecification,  statusCode);
    }

    /**
     * Builder with type safe required parameters.
     */
    public PostVaccinationFormDataSendActionBuilder sendPostVaccinationFormData()   {
            return new PostVaccinationFormDataSendActionBuilder(this, openApiSpecification);
    }

    public PostVaccinationFormDataReceiveActionBuilder receivePostVaccinationFormData(@NotNull HttpStatus statusCode)   {
        return new PostVaccinationFormDataReceiveActionBuilder(this, openApiSpecification, Integer.toString(statusCode.value()));
    }

    public PostVaccinationFormDataReceiveActionBuilder receivePostVaccinationFormData(@NotNull String statusCode)   {
        return new PostVaccinationFormDataReceiveActionBuilder(this, openApiSpecification,  statusCode);
    }

    /**
     * Builder with type safe required parameters.
     */
    public UpdatePetWithArrayQueryDataSendActionBuilder sendUpdatePetWithArrayQueryData(Long petId, String _name, String status, List<String> tags, List<String> nicknames, String sampleStringHeader)   {
            return new UpdatePetWithArrayQueryDataSendActionBuilder(this, openApiSpecification, petId, _name, status, tags, nicknames, sampleStringHeader);
    }

    /**
     * Builder with required parameters as string to allow for dynamic content.
     */
    public UpdatePetWithArrayQueryDataSendActionBuilder sendUpdatePetWithArrayQueryData$(String petIdExpression,  String _nameExpression,  String statusExpression,  List<String> tagsExpression,  List<String> nicknamesExpression,  String sampleStringHeaderExpression )   {
            return new UpdatePetWithArrayQueryDataSendActionBuilder(openApiSpecification, this, petIdExpression, _nameExpression, statusExpression, tagsExpression, nicknamesExpression, sampleStringHeaderExpression);
    }

    public UpdatePetWithArrayQueryDataReceiveActionBuilder receiveUpdatePetWithArrayQueryData(@NotNull HttpStatus statusCode)   {
        return new UpdatePetWithArrayQueryDataReceiveActionBuilder(this, openApiSpecification, Integer.toString(statusCode.value()));
    }

    public UpdatePetWithArrayQueryDataReceiveActionBuilder receiveUpdatePetWithArrayQueryData(@NotNull String statusCode)   {
        return new UpdatePetWithArrayQueryDataReceiveActionBuilder(this, openApiSpecification,  statusCode);
    }

    /**
     * Builder with type safe required parameters.
     */
    public UpdatePetWithFormUrlEncodedSendActionBuilder sendUpdatePetWithFormUrlEncoded(Long petId, String _name, String status, Integer age, List<String> tags)   {
            return new UpdatePetWithFormUrlEncodedSendActionBuilder(this, openApiSpecification, petId, _name, status, age, tags);
    }

    /**
     * Builder with required parameters as string to allow for dynamic content.
     */
    public UpdatePetWithFormUrlEncodedSendActionBuilder sendUpdatePetWithFormUrlEncoded$(String petIdExpression,  String _nameExpression,  String statusExpression,  String ageExpression,  List<String> tagsExpression )   {
            return new UpdatePetWithFormUrlEncodedSendActionBuilder(openApiSpecification, this, petIdExpression, _nameExpression, statusExpression, ageExpression, tagsExpression);
    }

    public UpdatePetWithFormUrlEncodedReceiveActionBuilder receiveUpdatePetWithFormUrlEncoded(@NotNull HttpStatus statusCode)   {
        return new UpdatePetWithFormUrlEncodedReceiveActionBuilder(this, openApiSpecification, Integer.toString(statusCode.value()));
    }

    public UpdatePetWithFormUrlEncodedReceiveActionBuilder receiveUpdatePetWithFormUrlEncoded(@NotNull String statusCode)   {
        return new UpdatePetWithFormUrlEncodedReceiveActionBuilder(this, openApiSpecification,  statusCode);
    }

    public static class GenerateVaccinationReportSendActionBuilder extends
                RestApiSendMessageActionBuilder {

        private static final String METHOD = "POST";

        private static final String ENDPOINT = "/api/v3/ext/pet/vaccination/status-report";

        private static final String OPERATION_NAME = "generateVaccinationReport";

        /**
         * Constructor with type safe required parameters.
         */
        public GenerateVaccinationReportSendActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification, Resource template, Integer reqIntVal) {
            super(extPetApi, openApiSpecification, METHOD, ENDPOINT, OPERATION_NAME);
            formParameter("template", toBinary(template) );
            formParameter("reqIntVal",  reqIntVal);
        }

        /**
         * Constructor with required parameters as string to allow for dynamic content.
         */
            public GenerateVaccinationReportSendActionBuilder(OpenApiSpecification openApiSpecification, ExtPetApi extPetApi, String templateExpression, String reqIntValExpression) {
            super(extPetApi, openApiSpecification,  METHOD, ENDPOINT, OPERATION_NAME);
            formParameter("template", toBinary(templateExpression) );
            formParameter("reqIntVal",  reqIntValExpression);
        }

        /**
         * Constructor with required parameters as string to allow for dynamic content.
         */
        public GenerateVaccinationReportSendActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification, TestApiClientRequestMessageBuilder messageBuilder, String templateExpression, String reqIntValExpression) {
            super(extPetApi, openApiSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
            formParameter("template", toBinary(templateExpression) );
            formParameter("reqIntVal",  reqIntValExpression);
        }

        public GenerateVaccinationReportSendActionBuilder template(Resource template) {
            formParameter("template", toBinary(template) );
            return this;
        }

        public GenerateVaccinationReportSendActionBuilder template(String templateExpression) {
            formParameter("template", toBinary(templateExpression) );
                return this;
        }

        public GenerateVaccinationReportSendActionBuilder reqIntVal(Integer reqIntVal) {
            formParameter("reqIntVal",  reqIntVal);
            return this;
        }

        public GenerateVaccinationReportSendActionBuilder reqIntVal(String reqIntValExpression) {
            formParameter("reqIntVal",  reqIntValExpression);
                return this;
        }

        public GenerateVaccinationReportSendActionBuilder optIntVal(Integer optIntVal) {
            formParameter("optIntVal",  optIntVal);
            return this;
        }

        public void setOptIntVal(Integer optIntVal) {
            formParameter("optIntVal",  optIntVal);
        }

        public GenerateVaccinationReportSendActionBuilder optIntVal(String optIntValExpression) {
            formParameter("optIntVal",  optIntValExpression);
            return this;
        }

        public void setOptIntVal(String optIntValExpression) {
            formParameter("optIntVal",  optIntValExpression);
        }

        public GenerateVaccinationReportSendActionBuilder optBoolVal(Boolean optBoolVal) {
            formParameter("optBoolVal",  optBoolVal);
            return this;
        }

        public void setOptBoolVal(Boolean optBoolVal) {
            formParameter("optBoolVal",  optBoolVal);
        }

        public GenerateVaccinationReportSendActionBuilder optBoolVal(String optBoolValExpression) {
            formParameter("optBoolVal",  optBoolValExpression);
            return this;
        }

        public void setOptBoolVal(String optBoolValExpression) {
            formParameter("optBoolVal",  optBoolValExpression);
        }

        public GenerateVaccinationReportSendActionBuilder optNumberVal(BigDecimal optNumberVal) {
            formParameter("optNumberVal",  optNumberVal);
            return this;
        }

        public void setOptNumberVal(BigDecimal optNumberVal) {
            formParameter("optNumberVal",  optNumberVal);
        }

        public GenerateVaccinationReportSendActionBuilder optNumberVal(String optNumberValExpression) {
            formParameter("optNumberVal",  optNumberValExpression);
            return this;
        }

        public void setOptNumberVal(String optNumberValExpression) {
            formParameter("optNumberVal",  optNumberValExpression);
        }

        public GenerateVaccinationReportSendActionBuilder optStringVal(String optStringVal) {
            formParameter("optStringVal",  optStringVal);
            return this;
        }

        public void setOptStringVal(String optStringVal) {
            formParameter("optStringVal",  optStringVal);
        }

        public GenerateVaccinationReportSendActionBuilder optDateVal(LocalDate optDateVal) {
            formParameter("optDateVal",  optDateVal);
            return this;
        }

        public void setOptDateVal(LocalDate optDateVal) {
            formParameter("optDateVal",  optDateVal);
        }

        public GenerateVaccinationReportSendActionBuilder optDateVal(String optDateValExpression) {
            formParameter("optDateVal",  optDateValExpression);
            return this;
        }

        public void setOptDateVal(String optDateValExpression) {
            formParameter("optDateVal",  optDateValExpression);
        }

        public GenerateVaccinationReportSendActionBuilder additionalData(HistoricalData additionalData) {
            formParameter("additionalData",  additionalData);
            return this;
        }

        public void setAdditionalData(HistoricalData additionalData) {
            formParameter("additionalData",  additionalData);
        }

        public GenerateVaccinationReportSendActionBuilder additionalData(String additionalDataExpression) {
            formParameter("additionalData",  additionalDataExpression);
            return this;
        }

        public void setAdditionalData(String additionalDataExpression) {
            formParameter("additionalData",  additionalDataExpression);
        }

        public GenerateVaccinationReportSendActionBuilder schema(Resource schema) {
            formParameter("schema", toBinary(schema) );
            return this;
        }

        public void setSchema(Resource schema) {
            formParameter("schema", toBinary(schema) );
        }

        public GenerateVaccinationReportSendActionBuilder schema(String schemaExpression) {
            formParameter("schema", toBinary(schemaExpression) );
            return this;
        }

        public void setSchema(String schemaExpression) {
            formParameter("schema", toBinary(schemaExpression) );
        }

        @Override
        public SendMessageAction doBuild() {

            if (getCustomizers() != null) {
                getCustomizers().forEach(customizer -> customizer.customizeRequestBuilder(getGeneratedApi(), this));
            }

            return super.doBuild();
        }
    }

    public static class GenerateVaccinationReportReceiveActionBuilder extends
                        RestApiReceiveMessageActionBuilder {

        private static final String METHOD = "POST";

        private static final String ENDPOINT = "/api/v3/ext/pet/vaccination/status-report";

        private static final String OPERATION_NAME = "generateVaccinationReport";

        public GenerateVaccinationReportReceiveActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification,  String statusCode) {
            super(extPetApi, openApiSpecification, METHOD, ENDPOINT, OPERATION_NAME, statusCode);
        }

        public GenerateVaccinationReportReceiveActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification, OpenApiClientResponseMessageBuilder messageBuilder) {
            super(extPetApi, openApiSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
        }

        @Override
        public ReceiveMessageAction doBuild() {

            if (getCustomizers() != null) {
                getCustomizers().forEach(customizer -> customizer.customizeResponseBuilder(getGeneratedApi(), this));
            }

            return super.doBuild();
        }

    }

    public static class GetPetByIdWithApiKeyAuthenticationSendActionBuilder extends
                RestApiSendMessageActionBuilder {

        private static final String METHOD = "GET";

        private static final String ENDPOINT = "/api/v3/ext/secure-api-key/pet/{petId}";

        private static final String OPERATION_NAME = "getPetByIdWithApiKeyAuthentication";

        @Value("${" + "extpetstore.base64-encode-api-key:#{false}}")
        private boolean base64EncodeApiKey;

        @Value("${" + "extpetstore.api-key-query:#{null}}")
        private String defaultApiKeyQuery;

        private String apiKeyQuery;

        @Value("${" + "extpetstore.api-key-header:#{null}}")
        private String defaultApiKeyHeader;

        private String apiKeyHeader;

        @Value("${" + "extpetstore.api-key-cookie:#{null}}")
        private String defaultApiKeyCookie;

        private String apiKeyCookie;

        /**
         * Constructor with type safe required parameters.
         */
        public GetPetByIdWithApiKeyAuthenticationSendActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification, Long petId, Boolean allDetails) {
            super(extPetApi, openApiSpecification, METHOD, ENDPOINT, OPERATION_NAME);
            pathParameter("petId", petId, ParameterStyle.SIMPLE, false, false);
            queryParameter("allDetails", allDetails, ParameterStyle.FORM, true, false);
        }

        /**
         * Constructor with required parameters as string to allow for dynamic content.
         */
            public GetPetByIdWithApiKeyAuthenticationSendActionBuilder(OpenApiSpecification openApiSpecification, ExtPetApi extPetApi, String petIdExpression, String allDetailsExpression) {
            super(extPetApi, openApiSpecification,  METHOD, ENDPOINT, OPERATION_NAME);
            pathParameter("petId", petIdExpression, ParameterStyle.SIMPLE, false, false);
            queryParameter("allDetails", allDetailsExpression, ParameterStyle.FORM, true, false);
        }

        /**
         * Constructor with required parameters as string to allow for dynamic content.
         */
        public GetPetByIdWithApiKeyAuthenticationSendActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification, TestApiClientRequestMessageBuilder messageBuilder, String petIdExpression, String allDetailsExpression) {
            super(extPetApi, openApiSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
            pathParameter("petId", petIdExpression, ParameterStyle.SIMPLE, false, false);
            queryParameter("allDetails", allDetailsExpression, ParameterStyle.FORM, true, false);
        }

        public GetPetByIdWithApiKeyAuthenticationSendActionBuilder petId(Long petId) {
            pathParameter("petId", petId, ParameterStyle.SIMPLE, false, false);
            return this;
        }

        public GetPetByIdWithApiKeyAuthenticationSendActionBuilder petId(String petIdExpression) {
            pathParameter("petId", petIdExpression, ParameterStyle.SIMPLE, false, false);
                return this;
        }

        public GetPetByIdWithApiKeyAuthenticationSendActionBuilder allDetails(Boolean allDetails) {
            queryParameter("allDetails", allDetails, ParameterStyle.FORM, true, false);
            return this;
        }

        public GetPetByIdWithApiKeyAuthenticationSendActionBuilder allDetails(String allDetailsExpression) {
            queryParameter("allDetails", allDetailsExpression, ParameterStyle.FORM, true, false);
                return this;
        }

        public GetPetByIdWithApiKeyAuthenticationSendActionBuilder details(String...details) {
            queryParameter("details", details, ParameterStyle.FORM, true, false);
            return this;
        }

        public void setDetails(String...details) {
            queryParameter("details", details, ParameterStyle.FORM, true, false);
        }

        public GetPetByIdWithApiKeyAuthenticationSendActionBuilder requesterInformation(String...requesterInformation) {
            queryParameter("requesterInformation", requesterInformation, ParameterStyle.FORM, true, false);
            return this;
        }

        public void setRequesterInformation(String...requesterInformation) {
            queryParameter("requesterInformation", requesterInformation, ParameterStyle.FORM, true, false);
        }

        public void setBase64EncodeApiKey(boolean encode) {
            this.base64EncodeApiKey = encode;
        }

        public GetPetByIdWithApiKeyAuthenticationSendActionBuilder apiKeyQuery(String apiKeyQuery) {
            this.apiKeyQuery = apiKeyQuery;
            return this;
        }

        public void setApiKeyQuery(String apiKeyQuery) {
            this.apiKeyQuery = apiKeyQuery;
        }

        public GetPetByIdWithApiKeyAuthenticationSendActionBuilder apiKeyHeader(String apiKeyHeader) {
            this.apiKeyHeader = apiKeyHeader;
            return this;
        }

        public void setApiKeyHeader(String apiKeyHeader) {
            this.apiKeyHeader = apiKeyHeader;
        }

        public GetPetByIdWithApiKeyAuthenticationSendActionBuilder apiKeyCookie(String apiKeyCookie) {
            this.apiKeyCookie = apiKeyCookie;
            return this;
        }

        public void setApiKeyCookie(String apiKeyCookie) {
            this.apiKeyCookie = apiKeyCookie;
        }

        @Override
        public SendMessageAction doBuild() {
            queryParameter("api_key_query", getOrDefault(apiKeyQuery, defaultApiKeyQuery, base64EncodeApiKey));
            headerParameter("api_key_header", getOrDefault(apiKeyHeader, defaultApiKeyHeader, base64EncodeApiKey));
            cookieParameter("api_key_cookie", getOrDefault(apiKeyCookie, defaultApiKeyCookie, base64EncodeApiKey));

            if (getCustomizers() != null) {
                getCustomizers().forEach(customizer -> customizer.customizeRequestBuilder(getGeneratedApi(), this));
            }

            return super.doBuild();
        }
    }

    public static class GetPetByIdWithApiKeyAuthenticationReceiveActionBuilder extends
                        RestApiReceiveMessageActionBuilder {

        private static final String METHOD = "GET";

        private static final String ENDPOINT = "/api/v3/ext/secure-api-key/pet/{petId}";

        private static final String OPERATION_NAME = "getPetByIdWithApiKeyAuthentication";

        public GetPetByIdWithApiKeyAuthenticationReceiveActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification,  String statusCode) {
            super(extPetApi, openApiSpecification, METHOD, ENDPOINT, OPERATION_NAME, statusCode);
        }

        public GetPetByIdWithApiKeyAuthenticationReceiveActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification, OpenApiClientResponseMessageBuilder messageBuilder) {
            super(extPetApi, openApiSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
        }

        @Override
        public ReceiveMessageAction doBuild() {

            if (getCustomizers() != null) {
                getCustomizers().forEach(customizer -> customizer.customizeResponseBuilder(getGeneratedApi(), this));
            }

            return super.doBuild();
        }

    }

    public static class GetPetByIdWithBasicAuthenticationSendActionBuilder extends
                RestApiSendMessageActionBuilder {

        private static final String METHOD = "GET";

        private static final String ENDPOINT = "/api/v3/ext/secure-basic/pet/{petId}";

        private static final String OPERATION_NAME = "getPetByIdWithBasicAuthentication";

        @Value("${" + "extpetstore.basic.username:#{null}}")
        private String defaultBasicUsername;

        private String basicUsername;

        @Value("${" + "extpetstore.basic.password:#{null}}")
        private String defaultBasicPassword;

        private String basicPassword;

        /**
         * Constructor with type safe required parameters.
         */
        public GetPetByIdWithBasicAuthenticationSendActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification, Long petId, Boolean allDetails) {
            super(extPetApi, openApiSpecification, METHOD, ENDPOINT, OPERATION_NAME);
            pathParameter("petId", petId, ParameterStyle.SIMPLE, false, false);
            queryParameter("allDetails", allDetails, ParameterStyle.FORM, true, false);
        }

        /**
         * Constructor with required parameters as string to allow for dynamic content.
         */
            public GetPetByIdWithBasicAuthenticationSendActionBuilder(OpenApiSpecification openApiSpecification, ExtPetApi extPetApi, String petIdExpression, String allDetailsExpression) {
            super(extPetApi, openApiSpecification,  METHOD, ENDPOINT, OPERATION_NAME);
            pathParameter("petId", petIdExpression, ParameterStyle.SIMPLE, false, false);
            queryParameter("allDetails", allDetailsExpression, ParameterStyle.FORM, true, false);
        }

        /**
         * Constructor with required parameters as string to allow for dynamic content.
         */
        public GetPetByIdWithBasicAuthenticationSendActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification, TestApiClientRequestMessageBuilder messageBuilder, String petIdExpression, String allDetailsExpression) {
            super(extPetApi, openApiSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
            pathParameter("petId", petIdExpression, ParameterStyle.SIMPLE, false, false);
            queryParameter("allDetails", allDetailsExpression, ParameterStyle.FORM, true, false);
        }

        public GetPetByIdWithBasicAuthenticationSendActionBuilder petId(Long petId) {
            pathParameter("petId", petId, ParameterStyle.SIMPLE, false, false);
            return this;
        }

        public GetPetByIdWithBasicAuthenticationSendActionBuilder petId(String petIdExpression) {
            pathParameter("petId", petIdExpression, ParameterStyle.SIMPLE, false, false);
                return this;
        }

        public GetPetByIdWithBasicAuthenticationSendActionBuilder allDetails(Boolean allDetails) {
            queryParameter("allDetails", allDetails, ParameterStyle.FORM, true, false);
            return this;
        }

        public GetPetByIdWithBasicAuthenticationSendActionBuilder allDetails(String allDetailsExpression) {
            queryParameter("allDetails", allDetailsExpression, ParameterStyle.FORM, true, false);
                return this;
        }

        public GetPetByIdWithBasicAuthenticationSendActionBuilder details(String...details) {
            queryParameter("details", details, ParameterStyle.FORM, true, false);
            return this;
        }

        public void setDetails(String...details) {
            queryParameter("details", details, ParameterStyle.FORM, true, false);
        }

        public GetPetByIdWithBasicAuthenticationSendActionBuilder requesterInformation(String...requesterInformation) {
            queryParameter("requesterInformation", requesterInformation, ParameterStyle.FORM, true, false);
            return this;
        }

        public void setRequesterInformation(String...requesterInformation) {
            queryParameter("requesterInformation", requesterInformation, ParameterStyle.FORM, true, false);
        }

        public GetPetByIdWithBasicAuthenticationSendActionBuilder basicAuthUsername(String basicUsername) {
            this.basicUsername = basicUsername;
            return this;
        }

        public void setBasicAuthUsername(String basicUsername) {
            this.basicUsername = basicUsername;
        }

        public GetPetByIdWithBasicAuthenticationSendActionBuilder basicAuthPassword(String password) {
            this.basicPassword = password;
            return this;
        }

        public void setBasicAuthPassword(String password) {
            this.basicPassword = password;
        }

        protected void addBasicAuthHeader(String basicUsername, String basicPassword,
            HttpMessageBuilderSupport messageBuilderSupport) {
            TestApiUtils.addBasicAuthHeader(
                isNotEmpty(basicUsername) ? basicUsername : defaultBasicUsername,
                isNotEmpty(basicPassword) ? basicPassword : defaultBasicPassword,
                messageBuilderSupport);
        }

        @Override
        public SendMessageAction doBuild() {
            addBasicAuthHeader(basicUsername, basicPassword, getMessageBuilderSupport());

            if (getCustomizers() != null) {
                getCustomizers().forEach(customizer -> customizer.customizeRequestBuilder(getGeneratedApi(), this));
            }

            return super.doBuild();
        }
    }

    public static class GetPetByIdWithBasicAuthenticationReceiveActionBuilder extends
                        RestApiReceiveMessageActionBuilder {

        private static final String METHOD = "GET";

        private static final String ENDPOINT = "/api/v3/ext/secure-basic/pet/{petId}";

        private static final String OPERATION_NAME = "getPetByIdWithBasicAuthentication";

        public GetPetByIdWithBasicAuthenticationReceiveActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification,  String statusCode) {
            super(extPetApi, openApiSpecification, METHOD, ENDPOINT, OPERATION_NAME, statusCode);
        }

        public GetPetByIdWithBasicAuthenticationReceiveActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification, OpenApiClientResponseMessageBuilder messageBuilder) {
            super(extPetApi, openApiSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
        }

        @Override
        public ReceiveMessageAction doBuild() {

            if (getCustomizers() != null) {
                getCustomizers().forEach(customizer -> customizer.customizeResponseBuilder(getGeneratedApi(), this));
            }

            return super.doBuild();
        }

    }

    public static class GetPetByIdWithBearerAuthenticationSendActionBuilder extends
                RestApiSendMessageActionBuilder {

        private static final String METHOD = "GET";

        private static final String ENDPOINT = "/api/v3/ext/secure-bearer/pet/{petId}";

        private static final String OPERATION_NAME = "getPetByIdWithBearerAuthentication";

        @Value("${" + "extpetstore.bearer.token:#{null}}")
        private String defaultBasicAuthBearer;

        private String basicAuthBearer;

        /**
         * Constructor with type safe required parameters.
         */
        public GetPetByIdWithBearerAuthenticationSendActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification, Long petId, Boolean allDetails) {
            super(extPetApi, openApiSpecification, METHOD, ENDPOINT, OPERATION_NAME);
            pathParameter("petId", petId, ParameterStyle.SIMPLE, false, false);
            queryParameter("allDetails", allDetails, ParameterStyle.FORM, true, false);
        }

        /**
         * Constructor with required parameters as string to allow for dynamic content.
         */
            public GetPetByIdWithBearerAuthenticationSendActionBuilder(OpenApiSpecification openApiSpecification, ExtPetApi extPetApi, String petIdExpression, String allDetailsExpression) {
            super(extPetApi, openApiSpecification,  METHOD, ENDPOINT, OPERATION_NAME);
            pathParameter("petId", petIdExpression, ParameterStyle.SIMPLE, false, false);
            queryParameter("allDetails", allDetailsExpression, ParameterStyle.FORM, true, false);
        }

        /**
         * Constructor with required parameters as string to allow for dynamic content.
         */
        public GetPetByIdWithBearerAuthenticationSendActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification, TestApiClientRequestMessageBuilder messageBuilder, String petIdExpression, String allDetailsExpression) {
            super(extPetApi, openApiSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
            pathParameter("petId", petIdExpression, ParameterStyle.SIMPLE, false, false);
            queryParameter("allDetails", allDetailsExpression, ParameterStyle.FORM, true, false);
        }

        public GetPetByIdWithBearerAuthenticationSendActionBuilder petId(Long petId) {
            pathParameter("petId", petId, ParameterStyle.SIMPLE, false, false);
            return this;
        }

        public GetPetByIdWithBearerAuthenticationSendActionBuilder petId(String petIdExpression) {
            pathParameter("petId", petIdExpression, ParameterStyle.SIMPLE, false, false);
                return this;
        }

        public GetPetByIdWithBearerAuthenticationSendActionBuilder allDetails(Boolean allDetails) {
            queryParameter("allDetails", allDetails, ParameterStyle.FORM, true, false);
            return this;
        }

        public GetPetByIdWithBearerAuthenticationSendActionBuilder allDetails(String allDetailsExpression) {
            queryParameter("allDetails", allDetailsExpression, ParameterStyle.FORM, true, false);
                return this;
        }

        public GetPetByIdWithBearerAuthenticationSendActionBuilder details(String...details) {
            queryParameter("details", details, ParameterStyle.FORM, true, false);
            return this;
        }

        public void setDetails(String...details) {
            queryParameter("details", details, ParameterStyle.FORM, true, false);
        }

        public GetPetByIdWithBearerAuthenticationSendActionBuilder requesterInformation(String...requesterInformation) {
            queryParameter("requesterInformation", requesterInformation, ParameterStyle.FORM, true, false);
            return this;
        }

        public void setRequesterInformation(String...requesterInformation) {
            queryParameter("requesterInformation", requesterInformation, ParameterStyle.FORM, true, false);
        }

        public GetPetByIdWithBearerAuthenticationSendActionBuilder basicAuthBearer(String basicAuthBearer) {
            this.basicAuthBearer = basicAuthBearer;
            return this;
        }

        public void setBasicAuthBearer(String basicAuthBearer) {
            this.basicAuthBearer = basicAuthBearer;
        }

        @Override
        public SendMessageAction doBuild() {
            if (!isEmpty(basicAuthBearer) || !isEmpty(defaultBasicAuthBearer)) {
                headerParameter("Authorization", "Bearer " +getOrDefault(basicAuthBearer, defaultBasicAuthBearer, true));
            }

            if (getCustomizers() != null) {
                getCustomizers().forEach(customizer -> customizer.customizeRequestBuilder(getGeneratedApi(), this));
            }

            return super.doBuild();
        }
    }

    public static class GetPetByIdWithBearerAuthenticationReceiveActionBuilder extends
                        RestApiReceiveMessageActionBuilder {

        private static final String METHOD = "GET";

        private static final String ENDPOINT = "/api/v3/ext/secure-bearer/pet/{petId}";

        private static final String OPERATION_NAME = "getPetByIdWithBearerAuthentication";

        public GetPetByIdWithBearerAuthenticationReceiveActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification,  String statusCode) {
            super(extPetApi, openApiSpecification, METHOD, ENDPOINT, OPERATION_NAME, statusCode);
        }

        public GetPetByIdWithBearerAuthenticationReceiveActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification, OpenApiClientResponseMessageBuilder messageBuilder) {
            super(extPetApi, openApiSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
        }

        @Override
        public ReceiveMessageAction doBuild() {

            if (getCustomizers() != null) {
                getCustomizers().forEach(customizer -> customizer.customizeResponseBuilder(getGeneratedApi(), this));
            }

            return super.doBuild();
        }

    }

    public static class GetPetWithCookieSendActionBuilder extends
                RestApiSendMessageActionBuilder {

        private static final String METHOD = "GET";

        private static final String ENDPOINT = "/api/v3/ext/pet/{petId}";

        private static final String OPERATION_NAME = "getPetWithCookie";

        /**
         * Constructor with type safe required parameters.
         */
        public GetPetWithCookieSendActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification, Long petId, String sessionId) {
            super(extPetApi, openApiSpecification, METHOD, ENDPOINT, OPERATION_NAME);
            pathParameter("petId", petId, ParameterStyle.SIMPLE, false, false);
            cookieParameter("session_id", sessionId, ParameterStyle.FORM, true, false);
        }

        /**
         * Constructor with required parameters as string to allow for dynamic content.
         */
            public GetPetWithCookieSendActionBuilder(OpenApiSpecification openApiSpecification, ExtPetApi extPetApi, String petIdExpression, String sessionIdExpression) {
            super(extPetApi, openApiSpecification,  METHOD, ENDPOINT, OPERATION_NAME);
            pathParameter("petId", petIdExpression, ParameterStyle.SIMPLE, false, false);
            cookieParameter("session_id", sessionIdExpression, ParameterStyle.FORM, true, false);
        }

        /**
         * Constructor with required parameters as string to allow for dynamic content.
         */
        public GetPetWithCookieSendActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification, TestApiClientRequestMessageBuilder messageBuilder, String petIdExpression, String sessionIdExpression) {
            super(extPetApi, openApiSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
            pathParameter("petId", petIdExpression, ParameterStyle.SIMPLE, false, false);
            cookieParameter("session_id", sessionIdExpression, ParameterStyle.FORM, true, false);
        }

        public GetPetWithCookieSendActionBuilder petId(Long petId) {
            pathParameter("petId", petId, ParameterStyle.SIMPLE, false, false);
            return this;
        }

        public GetPetWithCookieSendActionBuilder petId(String petIdExpression) {
            pathParameter("petId", petIdExpression, ParameterStyle.SIMPLE, false, false);
                return this;
        }

        public GetPetWithCookieSendActionBuilder sessionId(String sessionId) {
            cookieParameter("session_id", sessionId, ParameterStyle.FORM, true, false);
            return this;
        }

        public GetPetWithCookieSendActionBuilder optTrxId(String optTrxId) {
            cookieParameter("opt_trx_id", optTrxId, ParameterStyle.FORM, true, false);
            return this;
        }

        public void setOptTrxId(String optTrxId) {
            cookieParameter("opt_trx_id", optTrxId, ParameterStyle.FORM, true, false);
        }

        @Override
        public SendMessageAction doBuild() {

            if (getCustomizers() != null) {
                getCustomizers().forEach(customizer -> customizer.customizeRequestBuilder(getGeneratedApi(), this));
            }

            return super.doBuild();
        }
    }

    public static class GetPetWithCookieReceiveActionBuilder extends
                        RestApiReceiveMessageActionBuilder {

        private static final String METHOD = "GET";

        private static final String ENDPOINT = "/api/v3/ext/pet/{petId}";

        private static final String OPERATION_NAME = "getPetWithCookie";

        public GetPetWithCookieReceiveActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification,  String statusCode) {
            super(extPetApi, openApiSpecification, METHOD, ENDPOINT, OPERATION_NAME, statusCode);
        }

        public GetPetWithCookieReceiveActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification, OpenApiClientResponseMessageBuilder messageBuilder) {
            super(extPetApi, openApiSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
        }

        @Override
        public ReceiveMessageAction doBuild() {

            if (getCustomizers() != null) {
                getCustomizers().forEach(customizer -> customizer.customizeResponseBuilder(getGeneratedApi(), this));
            }

            return super.doBuild();
        }

    }

    public static class GetPetWithDeepObjectTypeQuerySendActionBuilder extends
                RestApiSendMessageActionBuilder {

        private static final String METHOD = "GET";

        private static final String ENDPOINT = "/api/v3/ext/pet/query/deep/object";

        private static final String OPERATION_NAME = "getPetWithDeepObjectTypeQuery";

        /**
         * Constructor with type safe required parameters.
         */
        public GetPetWithDeepObjectTypeQuerySendActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification, PetIdentifier petId) {
            super(extPetApi, openApiSpecification, METHOD, ENDPOINT, OPERATION_NAME);
            queryParameter("petId", petId, ParameterStyle.DEEPOBJECT, true, true);
        }

        /**
         * Constructor with required parameters as string to allow for dynamic content.
         */
            public GetPetWithDeepObjectTypeQuerySendActionBuilder(OpenApiSpecification openApiSpecification, ExtPetApi extPetApi, String petIdExpression) {
            super(extPetApi, openApiSpecification,  METHOD, ENDPOINT, OPERATION_NAME);
            queryParameter("petId", petIdExpression, ParameterStyle.DEEPOBJECT, true, true);
        }

        /**
         * Constructor with required parameters as string to allow for dynamic content.
         */
        public GetPetWithDeepObjectTypeQuerySendActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification, TestApiClientRequestMessageBuilder messageBuilder, String petIdExpression) {
            super(extPetApi, openApiSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
            queryParameter("petId", petIdExpression, ParameterStyle.DEEPOBJECT, true, true);
        }

        public GetPetWithDeepObjectTypeQuerySendActionBuilder petId(PetIdentifier petId) {
            queryParameter("petId", petId, ParameterStyle.DEEPOBJECT, true, true);
            return this;
        }

        public GetPetWithDeepObjectTypeQuerySendActionBuilder petId(String petIdExpression) {
            queryParameter("petId", petIdExpression, ParameterStyle.DEEPOBJECT, true, true);
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

    public static class GetPetWithDeepObjectTypeQueryReceiveActionBuilder extends
                        RestApiReceiveMessageActionBuilder {

        private static final String METHOD = "GET";

        private static final String ENDPOINT = "/api/v3/ext/pet/query/deep/object";

        private static final String OPERATION_NAME = "getPetWithDeepObjectTypeQuery";

        public GetPetWithDeepObjectTypeQueryReceiveActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification,  String statusCode) {
            super(extPetApi, openApiSpecification, METHOD, ENDPOINT, OPERATION_NAME, statusCode);
        }

        public GetPetWithDeepObjectTypeQueryReceiveActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification, OpenApiClientResponseMessageBuilder messageBuilder) {
            super(extPetApi, openApiSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
        }

        @Override
        public ReceiveMessageAction doBuild() {

            if (getCustomizers() != null) {
                getCustomizers().forEach(customizer -> customizer.customizeResponseBuilder(getGeneratedApi(), this));
            }

            return super.doBuild();
        }

    }

    public static class GetPetWithFormExplodedStyleCookieSendActionBuilder extends
                RestApiSendMessageActionBuilder {

        private static final String METHOD = "GET";

        private static final String ENDPOINT = "/api/v3/ext/pet/cookie/form/exploded";

        private static final String OPERATION_NAME = "getPetWithFormExplodedStyleCookie";

        /**
         * Constructor with type safe required parameters.
         */
        public GetPetWithFormExplodedStyleCookieSendActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification, List<Integer> petId) {
            super(extPetApi, openApiSpecification, METHOD, ENDPOINT, OPERATION_NAME);
            cookieParameter("petId", petId, ParameterStyle.FORM, true, false);
        }

        /**
         * Constructor with required parameters as string to allow for dynamic content.
         */
            public GetPetWithFormExplodedStyleCookieSendActionBuilder(OpenApiSpecification openApiSpecification, ExtPetApi extPetApi, List<String> petIdExpression) {
            super(extPetApi, openApiSpecification,  METHOD, ENDPOINT, OPERATION_NAME);
            cookieParameter("petId", petIdExpression, ParameterStyle.FORM, true, false);
        }

        /**
         * Constructor with required parameters as string to allow for dynamic content.
         */
        public GetPetWithFormExplodedStyleCookieSendActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification, TestApiClientRequestMessageBuilder messageBuilder, List<String> petId) {
            super(extPetApi, openApiSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
            cookieParameter("petId", petId, ParameterStyle.FORM, true, false);
        }

        public GetPetWithFormExplodedStyleCookieSendActionBuilder petId(Integer...petId) {
            cookieParameter("petId", petId, ParameterStyle.FORM, true, false);
            return this;
        }

        public GetPetWithFormExplodedStyleCookieSendActionBuilder petId(String...petIdExpression) {
            cookieParameter("petId", petIdExpression, ParameterStyle.FORM, true, false);
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

    public static class GetPetWithFormExplodedStyleCookieReceiveActionBuilder extends
                        RestApiReceiveMessageActionBuilder {

        private static final String METHOD = "GET";

        private static final String ENDPOINT = "/api/v3/ext/pet/cookie/form/exploded";

        private static final String OPERATION_NAME = "getPetWithFormExplodedStyleCookie";

        public GetPetWithFormExplodedStyleCookieReceiveActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification,  String statusCode) {
            super(extPetApi, openApiSpecification, METHOD, ENDPOINT, OPERATION_NAME, statusCode);
        }

        public GetPetWithFormExplodedStyleCookieReceiveActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification, OpenApiClientResponseMessageBuilder messageBuilder) {
            super(extPetApi, openApiSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
        }

        @Override
        public ReceiveMessageAction doBuild() {

            if (getCustomizers() != null) {
                getCustomizers().forEach(customizer -> customizer.customizeResponseBuilder(getGeneratedApi(), this));
            }

            return super.doBuild();
        }

    }

    public static class GetPetWithFormObjectStyleCookieSendActionBuilder extends
                RestApiSendMessageActionBuilder {

        private static final String METHOD = "GET";

        private static final String ENDPOINT = "/api/v3/ext/pet/cookie/form/object";

        private static final String OPERATION_NAME = "getPetWithFormObjectStyleCookie";

        /**
         * Constructor with type safe required parameters.
         */
        public GetPetWithFormObjectStyleCookieSendActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification, PetIdentifier petId) {
            super(extPetApi, openApiSpecification, METHOD, ENDPOINT, OPERATION_NAME);
            cookieParameter("petId", petId, ParameterStyle.FORM, false, true);
        }

        /**
         * Constructor with required parameters as string to allow for dynamic content.
         */
            public GetPetWithFormObjectStyleCookieSendActionBuilder(OpenApiSpecification openApiSpecification, ExtPetApi extPetApi, String petIdExpression) {
            super(extPetApi, openApiSpecification,  METHOD, ENDPOINT, OPERATION_NAME);
            cookieParameter("petId", petIdExpression, ParameterStyle.FORM, false, true);
        }

        /**
         * Constructor with required parameters as string to allow for dynamic content.
         */
        public GetPetWithFormObjectStyleCookieSendActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification, TestApiClientRequestMessageBuilder messageBuilder, String petIdExpression) {
            super(extPetApi, openApiSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
            cookieParameter("petId", petIdExpression, ParameterStyle.FORM, false, true);
        }

        public GetPetWithFormObjectStyleCookieSendActionBuilder petId(PetIdentifier petId) {
            cookieParameter("petId", petId, ParameterStyle.FORM, false, true);
            return this;
        }

        public GetPetWithFormObjectStyleCookieSendActionBuilder petId(String petIdExpression) {
            cookieParameter("petId", petIdExpression, ParameterStyle.FORM, false, true);
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

    public static class GetPetWithFormObjectStyleCookieReceiveActionBuilder extends
                        RestApiReceiveMessageActionBuilder {

        private static final String METHOD = "GET";

        private static final String ENDPOINT = "/api/v3/ext/pet/cookie/form/object";

        private static final String OPERATION_NAME = "getPetWithFormObjectStyleCookie";

        public GetPetWithFormObjectStyleCookieReceiveActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification,  String statusCode) {
            super(extPetApi, openApiSpecification, METHOD, ENDPOINT, OPERATION_NAME, statusCode);
        }

        public GetPetWithFormObjectStyleCookieReceiveActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification, OpenApiClientResponseMessageBuilder messageBuilder) {
            super(extPetApi, openApiSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
        }

        @Override
        public ReceiveMessageAction doBuild() {

            if (getCustomizers() != null) {
                getCustomizers().forEach(customizer -> customizer.customizeResponseBuilder(getGeneratedApi(), this));
            }

            return super.doBuild();
        }

    }

    public static class GetPetWithFormStyleCookieSendActionBuilder extends
                RestApiSendMessageActionBuilder {

        private static final String METHOD = "GET";

        private static final String ENDPOINT = "/api/v3/ext/pet/cookie/form";

        private static final String OPERATION_NAME = "getPetWithFormStyleCookie";

        /**
         * Constructor with type safe required parameters.
         */
        public GetPetWithFormStyleCookieSendActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification, List<Integer> petId) {
            super(extPetApi, openApiSpecification, METHOD, ENDPOINT, OPERATION_NAME);
            cookieParameter("petId", petId, ParameterStyle.FORM, false, false);
        }

        /**
         * Constructor with required parameters as string to allow for dynamic content.
         */
            public GetPetWithFormStyleCookieSendActionBuilder(OpenApiSpecification openApiSpecification, ExtPetApi extPetApi, List<String> petIdExpression) {
            super(extPetApi, openApiSpecification,  METHOD, ENDPOINT, OPERATION_NAME);
            cookieParameter("petId", petIdExpression, ParameterStyle.FORM, false, false);
        }

        /**
         * Constructor with required parameters as string to allow for dynamic content.
         */
        public GetPetWithFormStyleCookieSendActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification, TestApiClientRequestMessageBuilder messageBuilder, List<String> petId) {
            super(extPetApi, openApiSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
            cookieParameter("petId", petId, ParameterStyle.FORM, false, false);
        }

        public GetPetWithFormStyleCookieSendActionBuilder petId(Integer...petId) {
            cookieParameter("petId", petId, ParameterStyle.FORM, false, false);
            return this;
        }

        public GetPetWithFormStyleCookieSendActionBuilder petId(String...petIdExpression) {
            cookieParameter("petId", petIdExpression, ParameterStyle.FORM, false, false);
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

    public static class GetPetWithFormStyleCookieReceiveActionBuilder extends
                        RestApiReceiveMessageActionBuilder {

        private static final String METHOD = "GET";

        private static final String ENDPOINT = "/api/v3/ext/pet/cookie/form";

        private static final String OPERATION_NAME = "getPetWithFormStyleCookie";

        public GetPetWithFormStyleCookieReceiveActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification,  String statusCode) {
            super(extPetApi, openApiSpecification, METHOD, ENDPOINT, OPERATION_NAME, statusCode);
        }

        public GetPetWithFormStyleCookieReceiveActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification, OpenApiClientResponseMessageBuilder messageBuilder) {
            super(extPetApi, openApiSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
        }

        @Override
        public ReceiveMessageAction doBuild() {

            if (getCustomizers() != null) {
                getCustomizers().forEach(customizer -> customizer.customizeResponseBuilder(getGeneratedApi(), this));
            }

            return super.doBuild();
        }

    }

    public static class GetPetWithFormStyleExplodedObjectQuerySendActionBuilder extends
                RestApiSendMessageActionBuilder {

        private static final String METHOD = "GET";

        private static final String ENDPOINT = "/api/v3/ext/pet/query/form/exploded/object";

        private static final String OPERATION_NAME = "getPetWithFormStyleExplodedObjectQuery";

        /**
         * Constructor with type safe required parameters.
         */
        public GetPetWithFormStyleExplodedObjectQuerySendActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification, PetIdentifier petId) {
            super(extPetApi, openApiSpecification, METHOD, ENDPOINT, OPERATION_NAME);
            queryParameter("petId", petId, ParameterStyle.FORM, true, true);
        }

        /**
         * Constructor with required parameters as string to allow for dynamic content.
         */
            public GetPetWithFormStyleExplodedObjectQuerySendActionBuilder(OpenApiSpecification openApiSpecification, ExtPetApi extPetApi, String petIdExpression) {
            super(extPetApi, openApiSpecification,  METHOD, ENDPOINT, OPERATION_NAME);
            queryParameter("petId", petIdExpression, ParameterStyle.FORM, true, true);
        }

        /**
         * Constructor with required parameters as string to allow for dynamic content.
         */
        public GetPetWithFormStyleExplodedObjectQuerySendActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification, TestApiClientRequestMessageBuilder messageBuilder, String petIdExpression) {
            super(extPetApi, openApiSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
            queryParameter("petId", petIdExpression, ParameterStyle.FORM, true, true);
        }

        public GetPetWithFormStyleExplodedObjectQuerySendActionBuilder petId(PetIdentifier petId) {
            queryParameter("petId", petId, ParameterStyle.FORM, true, true);
            return this;
        }

        public GetPetWithFormStyleExplodedObjectQuerySendActionBuilder petId(String petIdExpression) {
            queryParameter("petId", petIdExpression, ParameterStyle.FORM, true, true);
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

    public static class GetPetWithFormStyleExplodedObjectQueryReceiveActionBuilder extends
                        RestApiReceiveMessageActionBuilder {

        private static final String METHOD = "GET";

        private static final String ENDPOINT = "/api/v3/ext/pet/query/form/exploded/object";

        private static final String OPERATION_NAME = "getPetWithFormStyleExplodedObjectQuery";

        public GetPetWithFormStyleExplodedObjectQueryReceiveActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification,  String statusCode) {
            super(extPetApi, openApiSpecification, METHOD, ENDPOINT, OPERATION_NAME, statusCode);
        }

        public GetPetWithFormStyleExplodedObjectQueryReceiveActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification, OpenApiClientResponseMessageBuilder messageBuilder) {
            super(extPetApi, openApiSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
        }

        @Override
        public ReceiveMessageAction doBuild() {

            if (getCustomizers() != null) {
                getCustomizers().forEach(customizer -> customizer.customizeResponseBuilder(getGeneratedApi(), this));
            }

            return super.doBuild();
        }

    }

    public static class GetPetWithFormStyleExplodedQuerySendActionBuilder extends
                RestApiSendMessageActionBuilder {

        private static final String METHOD = "GET";

        private static final String ENDPOINT = "/api/v3/ext/pet/query/form/exploded";

        private static final String OPERATION_NAME = "getPetWithFormStyleExplodedQuery";

        /**
         * Constructor with type safe required parameters.
         */
        public GetPetWithFormStyleExplodedQuerySendActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification, List<Integer> petId) {
            super(extPetApi, openApiSpecification, METHOD, ENDPOINT, OPERATION_NAME);
            queryParameter("petId", petId, ParameterStyle.FORM, true, false);
        }

        /**
         * Constructor with required parameters as string to allow for dynamic content.
         */
            public GetPetWithFormStyleExplodedQuerySendActionBuilder(OpenApiSpecification openApiSpecification, ExtPetApi extPetApi, List<String> petIdExpression) {
            super(extPetApi, openApiSpecification,  METHOD, ENDPOINT, OPERATION_NAME);
            queryParameter("petId", petIdExpression, ParameterStyle.FORM, true, false);
        }

        /**
         * Constructor with required parameters as string to allow for dynamic content.
         */
        public GetPetWithFormStyleExplodedQuerySendActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification, TestApiClientRequestMessageBuilder messageBuilder, List<String> petId) {
            super(extPetApi, openApiSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
            queryParameter("petId", petId, ParameterStyle.FORM, true, false);
        }

        public GetPetWithFormStyleExplodedQuerySendActionBuilder petId(Integer...petId) {
            queryParameter("petId", petId, ParameterStyle.FORM, true, false);
            return this;
        }

        public GetPetWithFormStyleExplodedQuerySendActionBuilder petId(String...petIdExpression) {
            queryParameter("petId", petIdExpression, ParameterStyle.FORM, true, false);
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

    public static class GetPetWithFormStyleExplodedQueryReceiveActionBuilder extends
                        RestApiReceiveMessageActionBuilder {

        private static final String METHOD = "GET";

        private static final String ENDPOINT = "/api/v3/ext/pet/query/form/exploded";

        private static final String OPERATION_NAME = "getPetWithFormStyleExplodedQuery";

        public GetPetWithFormStyleExplodedQueryReceiveActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification,  String statusCode) {
            super(extPetApi, openApiSpecification, METHOD, ENDPOINT, OPERATION_NAME, statusCode);
        }

        public GetPetWithFormStyleExplodedQueryReceiveActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification, OpenApiClientResponseMessageBuilder messageBuilder) {
            super(extPetApi, openApiSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
        }

        @Override
        public ReceiveMessageAction doBuild() {

            if (getCustomizers() != null) {
                getCustomizers().forEach(customizer -> customizer.customizeResponseBuilder(getGeneratedApi(), this));
            }

            return super.doBuild();
        }

    }

    public static class GetPetWithFormStyleObjectQuerySendActionBuilder extends
                RestApiSendMessageActionBuilder {

        private static final String METHOD = "GET";

        private static final String ENDPOINT = "/api/v3/ext/pet/query/form/object";

        private static final String OPERATION_NAME = "getPetWithFormStyleObjectQuery";

        /**
         * Constructor with type safe required parameters.
         */
        public GetPetWithFormStyleObjectQuerySendActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification, PetIdentifier petId) {
            super(extPetApi, openApiSpecification, METHOD, ENDPOINT, OPERATION_NAME);
            queryParameter("petId", petId, ParameterStyle.FORM, false, true);
        }

        /**
         * Constructor with required parameters as string to allow for dynamic content.
         */
            public GetPetWithFormStyleObjectQuerySendActionBuilder(OpenApiSpecification openApiSpecification, ExtPetApi extPetApi, String petIdExpression) {
            super(extPetApi, openApiSpecification,  METHOD, ENDPOINT, OPERATION_NAME);
            queryParameter("petId", petIdExpression, ParameterStyle.FORM, false, true);
        }

        /**
         * Constructor with required parameters as string to allow for dynamic content.
         */
        public GetPetWithFormStyleObjectQuerySendActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification, TestApiClientRequestMessageBuilder messageBuilder, String petIdExpression) {
            super(extPetApi, openApiSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
            queryParameter("petId", petIdExpression, ParameterStyle.FORM, false, true);
        }

        public GetPetWithFormStyleObjectQuerySendActionBuilder petId(PetIdentifier petId) {
            queryParameter("petId", petId, ParameterStyle.FORM, false, true);
            return this;
        }

        public GetPetWithFormStyleObjectQuerySendActionBuilder petId(String petIdExpression) {
            queryParameter("petId", petIdExpression, ParameterStyle.FORM, false, true);
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

    public static class GetPetWithFormStyleObjectQueryReceiveActionBuilder extends
                        RestApiReceiveMessageActionBuilder {

        private static final String METHOD = "GET";

        private static final String ENDPOINT = "/api/v3/ext/pet/query/form/object";

        private static final String OPERATION_NAME = "getPetWithFormStyleObjectQuery";

        public GetPetWithFormStyleObjectQueryReceiveActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification,  String statusCode) {
            super(extPetApi, openApiSpecification, METHOD, ENDPOINT, OPERATION_NAME, statusCode);
        }

        public GetPetWithFormStyleObjectQueryReceiveActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification, OpenApiClientResponseMessageBuilder messageBuilder) {
            super(extPetApi, openApiSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
        }

        @Override
        public ReceiveMessageAction doBuild() {

            if (getCustomizers() != null) {
                getCustomizers().forEach(customizer -> customizer.customizeResponseBuilder(getGeneratedApi(), this));
            }

            return super.doBuild();
        }

    }

    public static class GetPetWithFormStyleQuerySendActionBuilder extends
                RestApiSendMessageActionBuilder {

        private static final String METHOD = "GET";

        private static final String ENDPOINT = "/api/v3/ext/pet/query/form";

        private static final String OPERATION_NAME = "getPetWithFormStyleQuery";

        /**
         * Constructor with type safe required parameters.
         */
        public GetPetWithFormStyleQuerySendActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification, List<Integer> petId) {
            super(extPetApi, openApiSpecification, METHOD, ENDPOINT, OPERATION_NAME);
            queryParameter("petId", petId, ParameterStyle.FORM, false, false);
        }

        /**
         * Constructor with required parameters as string to allow for dynamic content.
         */
            public GetPetWithFormStyleQuerySendActionBuilder(OpenApiSpecification openApiSpecification, ExtPetApi extPetApi, List<String> petIdExpression) {
            super(extPetApi, openApiSpecification,  METHOD, ENDPOINT, OPERATION_NAME);
            queryParameter("petId", petIdExpression, ParameterStyle.FORM, false, false);
        }

        /**
         * Constructor with required parameters as string to allow for dynamic content.
         */
        public GetPetWithFormStyleQuerySendActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification, TestApiClientRequestMessageBuilder messageBuilder, List<String> petId) {
            super(extPetApi, openApiSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
            queryParameter("petId", petId, ParameterStyle.FORM, false, false);
        }

        public GetPetWithFormStyleQuerySendActionBuilder petId(Integer...petId) {
            queryParameter("petId", petId, ParameterStyle.FORM, false, false);
            return this;
        }

        public GetPetWithFormStyleQuerySendActionBuilder petId(String...petIdExpression) {
            queryParameter("petId", petIdExpression, ParameterStyle.FORM, false, false);
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

    public static class GetPetWithFormStyleQueryReceiveActionBuilder extends
                        RestApiReceiveMessageActionBuilder {

        private static final String METHOD = "GET";

        private static final String ENDPOINT = "/api/v3/ext/pet/query/form";

        private static final String OPERATION_NAME = "getPetWithFormStyleQuery";

        public GetPetWithFormStyleQueryReceiveActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification,  String statusCode) {
            super(extPetApi, openApiSpecification, METHOD, ENDPOINT, OPERATION_NAME, statusCode);
        }

        public GetPetWithFormStyleQueryReceiveActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification, OpenApiClientResponseMessageBuilder messageBuilder) {
            super(extPetApi, openApiSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
        }

        @Override
        public ReceiveMessageAction doBuild() {

            if (getCustomizers() != null) {
                getCustomizers().forEach(customizer -> customizer.customizeResponseBuilder(getGeneratedApi(), this));
            }

            return super.doBuild();
        }

    }

    public static class GetPetWithLabelStyleArraySendActionBuilder extends
                RestApiSendMessageActionBuilder {

        private static final String METHOD = "GET";

        private static final String ENDPOINT = "/api/v3/ext/pet/label/{petId}";

        private static final String OPERATION_NAME = "getPetWithLabelStyleArray";

        /**
         * Constructor with type safe required parameters.
         */
        public GetPetWithLabelStyleArraySendActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification, List<Integer> petId) {
            super(extPetApi, openApiSpecification, METHOD, ENDPOINT, OPERATION_NAME);
            pathParameter("petId", petId, ParameterStyle.LABEL, false, false);
        }

        /**
         * Constructor with required parameters as string to allow for dynamic content.
         */
            public GetPetWithLabelStyleArraySendActionBuilder(OpenApiSpecification openApiSpecification, ExtPetApi extPetApi, List<String> petIdExpression) {
            super(extPetApi, openApiSpecification,  METHOD, ENDPOINT, OPERATION_NAME);
            pathParameter("petId", petIdExpression, ParameterStyle.LABEL, false, false);
        }

        /**
         * Constructor with required parameters as string to allow for dynamic content.
         */
        public GetPetWithLabelStyleArraySendActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification, TestApiClientRequestMessageBuilder messageBuilder, List<String> petId) {
            super(extPetApi, openApiSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
            pathParameter("petId", petId, ParameterStyle.LABEL, false, false);
        }

        public GetPetWithLabelStyleArraySendActionBuilder petId(Integer...petId) {
            pathParameter("petId", petId, ParameterStyle.LABEL, false, false);
            return this;
        }

        public GetPetWithLabelStyleArraySendActionBuilder petId(String...petIdExpression) {
            pathParameter("petId", petIdExpression, ParameterStyle.LABEL, false, false);
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

    public static class GetPetWithLabelStyleArrayReceiveActionBuilder extends
                        RestApiReceiveMessageActionBuilder {

        private static final String METHOD = "GET";

        private static final String ENDPOINT = "/api/v3/ext/pet/label/{petId}";

        private static final String OPERATION_NAME = "getPetWithLabelStyleArray";

        public GetPetWithLabelStyleArrayReceiveActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification,  String statusCode) {
            super(extPetApi, openApiSpecification, METHOD, ENDPOINT, OPERATION_NAME, statusCode);
        }

        public GetPetWithLabelStyleArrayReceiveActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification, OpenApiClientResponseMessageBuilder messageBuilder) {
            super(extPetApi, openApiSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
        }

        @Override
        public ReceiveMessageAction doBuild() {

            if (getCustomizers() != null) {
                getCustomizers().forEach(customizer -> customizer.customizeResponseBuilder(getGeneratedApi(), this));
            }

            return super.doBuild();
        }

    }

    public static class GetPetWithLabelStyleArrayExplodedSendActionBuilder extends
                RestApiSendMessageActionBuilder {

        private static final String METHOD = "GET";

        private static final String ENDPOINT = "/api/v3/ext/pet/label/exploded/{petId}";

        private static final String OPERATION_NAME = "getPetWithLabelStyleArrayExploded";

        /**
         * Constructor with type safe required parameters.
         */
        public GetPetWithLabelStyleArrayExplodedSendActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification, List<Integer> petId) {
            super(extPetApi, openApiSpecification, METHOD, ENDPOINT, OPERATION_NAME);
            pathParameter("petId", petId, ParameterStyle.LABEL, true, false);
        }

        /**
         * Constructor with required parameters as string to allow for dynamic content.
         */
            public GetPetWithLabelStyleArrayExplodedSendActionBuilder(OpenApiSpecification openApiSpecification, ExtPetApi extPetApi, List<String> petIdExpression) {
            super(extPetApi, openApiSpecification,  METHOD, ENDPOINT, OPERATION_NAME);
            pathParameter("petId", petIdExpression, ParameterStyle.LABEL, true, false);
        }

        /**
         * Constructor with required parameters as string to allow for dynamic content.
         */
        public GetPetWithLabelStyleArrayExplodedSendActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification, TestApiClientRequestMessageBuilder messageBuilder, List<String> petId) {
            super(extPetApi, openApiSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
            pathParameter("petId", petId, ParameterStyle.LABEL, true, false);
        }

        public GetPetWithLabelStyleArrayExplodedSendActionBuilder petId(Integer...petId) {
            pathParameter("petId", petId, ParameterStyle.LABEL, true, false);
            return this;
        }

        public GetPetWithLabelStyleArrayExplodedSendActionBuilder petId(String...petIdExpression) {
            pathParameter("petId", petIdExpression, ParameterStyle.LABEL, true, false);
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

    public static class GetPetWithLabelStyleArrayExplodedReceiveActionBuilder extends
                        RestApiReceiveMessageActionBuilder {

        private static final String METHOD = "GET";

        private static final String ENDPOINT = "/api/v3/ext/pet/label/exploded/{petId}";

        private static final String OPERATION_NAME = "getPetWithLabelStyleArrayExploded";

        public GetPetWithLabelStyleArrayExplodedReceiveActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification,  String statusCode) {
            super(extPetApi, openApiSpecification, METHOD, ENDPOINT, OPERATION_NAME, statusCode);
        }

        public GetPetWithLabelStyleArrayExplodedReceiveActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification, OpenApiClientResponseMessageBuilder messageBuilder) {
            super(extPetApi, openApiSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
        }

        @Override
        public ReceiveMessageAction doBuild() {

            if (getCustomizers() != null) {
                getCustomizers().forEach(customizer -> customizer.customizeResponseBuilder(getGeneratedApi(), this));
            }

            return super.doBuild();
        }

    }

    public static class GetPetWithLabelStyleObjectSendActionBuilder extends
                RestApiSendMessageActionBuilder {

        private static final String METHOD = "GET";

        private static final String ENDPOINT = "/api/v3/ext/pet/label/object/{petId}";

        private static final String OPERATION_NAME = "getPetWithLabelStyleObject";

        /**
         * Constructor with type safe required parameters.
         */
        public GetPetWithLabelStyleObjectSendActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification, PetIdentifier petId) {
            super(extPetApi, openApiSpecification, METHOD, ENDPOINT, OPERATION_NAME);
            pathParameter("petId", petId, ParameterStyle.LABEL, false, true);
        }

        /**
         * Constructor with required parameters as string to allow for dynamic content.
         */
            public GetPetWithLabelStyleObjectSendActionBuilder(OpenApiSpecification openApiSpecification, ExtPetApi extPetApi, String petIdExpression) {
            super(extPetApi, openApiSpecification,  METHOD, ENDPOINT, OPERATION_NAME);
            pathParameter("petId", petIdExpression, ParameterStyle.LABEL, false, true);
        }

        /**
         * Constructor with required parameters as string to allow for dynamic content.
         */
        public GetPetWithLabelStyleObjectSendActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification, TestApiClientRequestMessageBuilder messageBuilder, String petIdExpression) {
            super(extPetApi, openApiSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
            pathParameter("petId", petIdExpression, ParameterStyle.LABEL, false, true);
        }

        public GetPetWithLabelStyleObjectSendActionBuilder petId(PetIdentifier petId) {
            pathParameter("petId", petId, ParameterStyle.LABEL, false, true);
            return this;
        }

        public GetPetWithLabelStyleObjectSendActionBuilder petId(String petIdExpression) {
            pathParameter("petId", petIdExpression, ParameterStyle.LABEL, false, true);
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

    public static class GetPetWithLabelStyleObjectReceiveActionBuilder extends
                        RestApiReceiveMessageActionBuilder {

        private static final String METHOD = "GET";

        private static final String ENDPOINT = "/api/v3/ext/pet/label/object/{petId}";

        private static final String OPERATION_NAME = "getPetWithLabelStyleObject";

        public GetPetWithLabelStyleObjectReceiveActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification,  String statusCode) {
            super(extPetApi, openApiSpecification, METHOD, ENDPOINT, OPERATION_NAME, statusCode);
        }

        public GetPetWithLabelStyleObjectReceiveActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification, OpenApiClientResponseMessageBuilder messageBuilder) {
            super(extPetApi, openApiSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
        }

        @Override
        public ReceiveMessageAction doBuild() {

            if (getCustomizers() != null) {
                getCustomizers().forEach(customizer -> customizer.customizeResponseBuilder(getGeneratedApi(), this));
            }

            return super.doBuild();
        }

    }

    public static class GetPetWithLabelStyleObjectExplodedSendActionBuilder extends
                RestApiSendMessageActionBuilder {

        private static final String METHOD = "GET";

        private static final String ENDPOINT = "/api/v3/ext/pet/label/exploded/object/{petId}";

        private static final String OPERATION_NAME = "getPetWithLabelStyleObjectExploded";

        /**
         * Constructor with type safe required parameters.
         */
        public GetPetWithLabelStyleObjectExplodedSendActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification, PetIdentifier petId) {
            super(extPetApi, openApiSpecification, METHOD, ENDPOINT, OPERATION_NAME);
            pathParameter("petId", petId, ParameterStyle.LABEL, true, true);
        }

        /**
         * Constructor with required parameters as string to allow for dynamic content.
         */
            public GetPetWithLabelStyleObjectExplodedSendActionBuilder(OpenApiSpecification openApiSpecification, ExtPetApi extPetApi, String petIdExpression) {
            super(extPetApi, openApiSpecification,  METHOD, ENDPOINT, OPERATION_NAME);
            pathParameter("petId", petIdExpression, ParameterStyle.LABEL, true, true);
        }

        /**
         * Constructor with required parameters as string to allow for dynamic content.
         */
        public GetPetWithLabelStyleObjectExplodedSendActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification, TestApiClientRequestMessageBuilder messageBuilder, String petIdExpression) {
            super(extPetApi, openApiSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
            pathParameter("petId", petIdExpression, ParameterStyle.LABEL, true, true);
        }

        public GetPetWithLabelStyleObjectExplodedSendActionBuilder petId(PetIdentifier petId) {
            pathParameter("petId", petId, ParameterStyle.LABEL, true, true);
            return this;
        }

        public GetPetWithLabelStyleObjectExplodedSendActionBuilder petId(String petIdExpression) {
            pathParameter("petId", petIdExpression, ParameterStyle.LABEL, true, true);
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

    public static class GetPetWithLabelStyleObjectExplodedReceiveActionBuilder extends
                        RestApiReceiveMessageActionBuilder {

        private static final String METHOD = "GET";

        private static final String ENDPOINT = "/api/v3/ext/pet/label/exploded/object/{petId}";

        private static final String OPERATION_NAME = "getPetWithLabelStyleObjectExploded";

        public GetPetWithLabelStyleObjectExplodedReceiveActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification,  String statusCode) {
            super(extPetApi, openApiSpecification, METHOD, ENDPOINT, OPERATION_NAME, statusCode);
        }

        public GetPetWithLabelStyleObjectExplodedReceiveActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification, OpenApiClientResponseMessageBuilder messageBuilder) {
            super(extPetApi, openApiSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
        }

        @Override
        public ReceiveMessageAction doBuild() {

            if (getCustomizers() != null) {
                getCustomizers().forEach(customizer -> customizer.customizeResponseBuilder(getGeneratedApi(), this));
            }

            return super.doBuild();
        }

    }

    public static class GetPetWithMatrixStyleArraySendActionBuilder extends
                RestApiSendMessageActionBuilder {

        private static final String METHOD = "GET";

        private static final String ENDPOINT = "/api/v3/ext/pet/matrix/{petId}";

        private static final String OPERATION_NAME = "getPetWithMatrixStyleArray";

        /**
         * Constructor with type safe required parameters.
         */
        public GetPetWithMatrixStyleArraySendActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification, List<Integer> petId) {
            super(extPetApi, openApiSpecification, METHOD, ENDPOINT, OPERATION_NAME);
            pathParameter("petId", petId, ParameterStyle.MATRIX, false, false);
        }

        /**
         * Constructor with required parameters as string to allow for dynamic content.
         */
            public GetPetWithMatrixStyleArraySendActionBuilder(OpenApiSpecification openApiSpecification, ExtPetApi extPetApi, List<String> petIdExpression) {
            super(extPetApi, openApiSpecification,  METHOD, ENDPOINT, OPERATION_NAME);
            pathParameter("petId", petIdExpression, ParameterStyle.MATRIX, false, false);
        }

        /**
         * Constructor with required parameters as string to allow for dynamic content.
         */
        public GetPetWithMatrixStyleArraySendActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification, TestApiClientRequestMessageBuilder messageBuilder, List<String> petId) {
            super(extPetApi, openApiSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
            pathParameter("petId", petId, ParameterStyle.MATRIX, false, false);
        }

        public GetPetWithMatrixStyleArraySendActionBuilder petId(Integer...petId) {
            pathParameter("petId", petId, ParameterStyle.MATRIX, false, false);
            return this;
        }

        public GetPetWithMatrixStyleArraySendActionBuilder petId(String...petIdExpression) {
            pathParameter("petId", petIdExpression, ParameterStyle.MATRIX, false, false);
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

    public static class GetPetWithMatrixStyleArrayReceiveActionBuilder extends
                        RestApiReceiveMessageActionBuilder {

        private static final String METHOD = "GET";

        private static final String ENDPOINT = "/api/v3/ext/pet/matrix/{petId}";

        private static final String OPERATION_NAME = "getPetWithMatrixStyleArray";

        public GetPetWithMatrixStyleArrayReceiveActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification,  String statusCode) {
            super(extPetApi, openApiSpecification, METHOD, ENDPOINT, OPERATION_NAME, statusCode);
        }

        public GetPetWithMatrixStyleArrayReceiveActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification, OpenApiClientResponseMessageBuilder messageBuilder) {
            super(extPetApi, openApiSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
        }

        @Override
        public ReceiveMessageAction doBuild() {

            if (getCustomizers() != null) {
                getCustomizers().forEach(customizer -> customizer.customizeResponseBuilder(getGeneratedApi(), this));
            }

            return super.doBuild();
        }

    }

    public static class GetPetWithMatrixStyleArrayExplodedSendActionBuilder extends
                RestApiSendMessageActionBuilder {

        private static final String METHOD = "GET";

        private static final String ENDPOINT = "/api/v3/ext/pet/matrix/exploded/{petId}";

        private static final String OPERATION_NAME = "getPetWithMatrixStyleArrayExploded";

        /**
         * Constructor with type safe required parameters.
         */
        public GetPetWithMatrixStyleArrayExplodedSendActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification, List<Integer> petId) {
            super(extPetApi, openApiSpecification, METHOD, ENDPOINT, OPERATION_NAME);
            pathParameter("petId", petId, ParameterStyle.MATRIX, true, false);
        }

        /**
         * Constructor with required parameters as string to allow for dynamic content.
         */
            public GetPetWithMatrixStyleArrayExplodedSendActionBuilder(OpenApiSpecification openApiSpecification, ExtPetApi extPetApi, List<String> petIdExpression) {
            super(extPetApi, openApiSpecification,  METHOD, ENDPOINT, OPERATION_NAME);
            pathParameter("petId", petIdExpression, ParameterStyle.MATRIX, true, false);
        }

        /**
         * Constructor with required parameters as string to allow for dynamic content.
         */
        public GetPetWithMatrixStyleArrayExplodedSendActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification, TestApiClientRequestMessageBuilder messageBuilder, List<String> petId) {
            super(extPetApi, openApiSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
            pathParameter("petId", petId, ParameterStyle.MATRIX, true, false);
        }

        public GetPetWithMatrixStyleArrayExplodedSendActionBuilder petId(Integer...petId) {
            pathParameter("petId", petId, ParameterStyle.MATRIX, true, false);
            return this;
        }

        public GetPetWithMatrixStyleArrayExplodedSendActionBuilder petId(String...petIdExpression) {
            pathParameter("petId", petIdExpression, ParameterStyle.MATRIX, true, false);
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

    public static class GetPetWithMatrixStyleArrayExplodedReceiveActionBuilder extends
                        RestApiReceiveMessageActionBuilder {

        private static final String METHOD = "GET";

        private static final String ENDPOINT = "/api/v3/ext/pet/matrix/exploded/{petId}";

        private static final String OPERATION_NAME = "getPetWithMatrixStyleArrayExploded";

        public GetPetWithMatrixStyleArrayExplodedReceiveActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification,  String statusCode) {
            super(extPetApi, openApiSpecification, METHOD, ENDPOINT, OPERATION_NAME, statusCode);
        }

        public GetPetWithMatrixStyleArrayExplodedReceiveActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification, OpenApiClientResponseMessageBuilder messageBuilder) {
            super(extPetApi, openApiSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
        }

        @Override
        public ReceiveMessageAction doBuild() {

            if (getCustomizers() != null) {
                getCustomizers().forEach(customizer -> customizer.customizeResponseBuilder(getGeneratedApi(), this));
            }

            return super.doBuild();
        }

    }

    public static class GetPetWithMatrixStyleObjectSendActionBuilder extends
                RestApiSendMessageActionBuilder {

        private static final String METHOD = "GET";

        private static final String ENDPOINT = "/api/v3/ext/pet/matrix/object/{petId}";

        private static final String OPERATION_NAME = "getPetWithMatrixStyleObject";

        /**
         * Constructor with type safe required parameters.
         */
        public GetPetWithMatrixStyleObjectSendActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification, PetIdentifier petId) {
            super(extPetApi, openApiSpecification, METHOD, ENDPOINT, OPERATION_NAME);
            pathParameter("petId", petId, ParameterStyle.MATRIX, false, true);
        }

        /**
         * Constructor with required parameters as string to allow for dynamic content.
         */
            public GetPetWithMatrixStyleObjectSendActionBuilder(OpenApiSpecification openApiSpecification, ExtPetApi extPetApi, String petIdExpression) {
            super(extPetApi, openApiSpecification,  METHOD, ENDPOINT, OPERATION_NAME);
            pathParameter("petId", petIdExpression, ParameterStyle.MATRIX, false, true);
        }

        /**
         * Constructor with required parameters as string to allow for dynamic content.
         */
        public GetPetWithMatrixStyleObjectSendActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification, TestApiClientRequestMessageBuilder messageBuilder, String petIdExpression) {
            super(extPetApi, openApiSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
            pathParameter("petId", petIdExpression, ParameterStyle.MATRIX, false, true);
        }

        public GetPetWithMatrixStyleObjectSendActionBuilder petId(PetIdentifier petId) {
            pathParameter("petId", petId, ParameterStyle.MATRIX, false, true);
            return this;
        }

        public GetPetWithMatrixStyleObjectSendActionBuilder petId(String petIdExpression) {
            pathParameter("petId", petIdExpression, ParameterStyle.MATRIX, false, true);
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

    public static class GetPetWithMatrixStyleObjectReceiveActionBuilder extends
                        RestApiReceiveMessageActionBuilder {

        private static final String METHOD = "GET";

        private static final String ENDPOINT = "/api/v3/ext/pet/matrix/object/{petId}";

        private static final String OPERATION_NAME = "getPetWithMatrixStyleObject";

        public GetPetWithMatrixStyleObjectReceiveActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification,  String statusCode) {
            super(extPetApi, openApiSpecification, METHOD, ENDPOINT, OPERATION_NAME, statusCode);
        }

        public GetPetWithMatrixStyleObjectReceiveActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification, OpenApiClientResponseMessageBuilder messageBuilder) {
            super(extPetApi, openApiSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
        }

        @Override
        public ReceiveMessageAction doBuild() {

            if (getCustomizers() != null) {
                getCustomizers().forEach(customizer -> customizer.customizeResponseBuilder(getGeneratedApi(), this));
            }

            return super.doBuild();
        }

    }

    public static class GetPetWithMatrixStyleObjectExplodedSendActionBuilder extends
                RestApiSendMessageActionBuilder {

        private static final String METHOD = "GET";

        private static final String ENDPOINT = "/api/v3/ext/pet/matrix/exploded/object/{petId}";

        private static final String OPERATION_NAME = "getPetWithMatrixStyleObjectExploded";

        /**
         * Constructor with type safe required parameters.
         */
        public GetPetWithMatrixStyleObjectExplodedSendActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification, PetIdentifier petId) {
            super(extPetApi, openApiSpecification, METHOD, ENDPOINT, OPERATION_NAME);
            pathParameter("petId", petId, ParameterStyle.MATRIX, true, true);
        }

        /**
         * Constructor with required parameters as string to allow for dynamic content.
         */
            public GetPetWithMatrixStyleObjectExplodedSendActionBuilder(OpenApiSpecification openApiSpecification, ExtPetApi extPetApi, String petIdExpression) {
            super(extPetApi, openApiSpecification,  METHOD, ENDPOINT, OPERATION_NAME);
            pathParameter("petId", petIdExpression, ParameterStyle.MATRIX, true, true);
        }

        /**
         * Constructor with required parameters as string to allow for dynamic content.
         */
        public GetPetWithMatrixStyleObjectExplodedSendActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification, TestApiClientRequestMessageBuilder messageBuilder, String petIdExpression) {
            super(extPetApi, openApiSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
            pathParameter("petId", petIdExpression, ParameterStyle.MATRIX, true, true);
        }

        public GetPetWithMatrixStyleObjectExplodedSendActionBuilder petId(PetIdentifier petId) {
            pathParameter("petId", petId, ParameterStyle.MATRIX, true, true);
            return this;
        }

        public GetPetWithMatrixStyleObjectExplodedSendActionBuilder petId(String petIdExpression) {
            pathParameter("petId", petIdExpression, ParameterStyle.MATRIX, true, true);
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

    public static class GetPetWithMatrixStyleObjectExplodedReceiveActionBuilder extends
                        RestApiReceiveMessageActionBuilder {

        private static final String METHOD = "GET";

        private static final String ENDPOINT = "/api/v3/ext/pet/matrix/exploded/object/{petId}";

        private static final String OPERATION_NAME = "getPetWithMatrixStyleObjectExploded";

        public GetPetWithMatrixStyleObjectExplodedReceiveActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification,  String statusCode) {
            super(extPetApi, openApiSpecification, METHOD, ENDPOINT, OPERATION_NAME, statusCode);
        }

        public GetPetWithMatrixStyleObjectExplodedReceiveActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification, OpenApiClientResponseMessageBuilder messageBuilder) {
            super(extPetApi, openApiSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
        }

        @Override
        public ReceiveMessageAction doBuild() {

            if (getCustomizers() != null) {
                getCustomizers().forEach(customizer -> customizer.customizeResponseBuilder(getGeneratedApi(), this));
            }

            return super.doBuild();
        }

    }

    public static class GetPetWithSimpleStyleArraySendActionBuilder extends
                RestApiSendMessageActionBuilder {

        private static final String METHOD = "GET";

        private static final String ENDPOINT = "/api/v3/ext/pet/simple/{petId}";

        private static final String OPERATION_NAME = "getPetWithSimpleStyleArray";

        /**
         * Constructor with type safe required parameters.
         */
        public GetPetWithSimpleStyleArraySendActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification, List<Integer> petId) {
            super(extPetApi, openApiSpecification, METHOD, ENDPOINT, OPERATION_NAME);
            pathParameter("petId", petId, ParameterStyle.SIMPLE, false, false);
        }

        /**
         * Constructor with required parameters as string to allow for dynamic content.
         */
            public GetPetWithSimpleStyleArraySendActionBuilder(OpenApiSpecification openApiSpecification, ExtPetApi extPetApi, List<String> petIdExpression) {
            super(extPetApi, openApiSpecification,  METHOD, ENDPOINT, OPERATION_NAME);
            pathParameter("petId", petIdExpression, ParameterStyle.SIMPLE, false, false);
        }

        /**
         * Constructor with required parameters as string to allow for dynamic content.
         */
        public GetPetWithSimpleStyleArraySendActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification, TestApiClientRequestMessageBuilder messageBuilder, List<String> petId) {
            super(extPetApi, openApiSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
            pathParameter("petId", petId, ParameterStyle.SIMPLE, false, false);
        }

        public GetPetWithSimpleStyleArraySendActionBuilder petId(Integer...petId) {
            pathParameter("petId", petId, ParameterStyle.SIMPLE, false, false);
            return this;
        }

        public GetPetWithSimpleStyleArraySendActionBuilder petId(String...petIdExpression) {
            pathParameter("petId", petIdExpression, ParameterStyle.SIMPLE, false, false);
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

    public static class GetPetWithSimpleStyleArrayReceiveActionBuilder extends
                        RestApiReceiveMessageActionBuilder {

        private static final String METHOD = "GET";

        private static final String ENDPOINT = "/api/v3/ext/pet/simple/{petId}";

        private static final String OPERATION_NAME = "getPetWithSimpleStyleArray";

        public GetPetWithSimpleStyleArrayReceiveActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification,  String statusCode) {
            super(extPetApi, openApiSpecification, METHOD, ENDPOINT, OPERATION_NAME, statusCode);
        }

        public GetPetWithSimpleStyleArrayReceiveActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification, OpenApiClientResponseMessageBuilder messageBuilder) {
            super(extPetApi, openApiSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
        }

        @Override
        public ReceiveMessageAction doBuild() {

            if (getCustomizers() != null) {
                getCustomizers().forEach(customizer -> customizer.customizeResponseBuilder(getGeneratedApi(), this));
            }

            return super.doBuild();
        }

    }

    public static class GetPetWithSimpleStyleArrayExplodedSendActionBuilder extends
                RestApiSendMessageActionBuilder {

        private static final String METHOD = "GET";

        private static final String ENDPOINT = "/api/v3/ext/pet/simple/exploded/{petId}";

        private static final String OPERATION_NAME = "getPetWithSimpleStyleArrayExploded";

        /**
         * Constructor with type safe required parameters.
         */
        public GetPetWithSimpleStyleArrayExplodedSendActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification, List<Integer> petId) {
            super(extPetApi, openApiSpecification, METHOD, ENDPOINT, OPERATION_NAME);
            pathParameter("petId", petId, ParameterStyle.SIMPLE, false, false);
        }

        /**
         * Constructor with required parameters as string to allow for dynamic content.
         */
            public GetPetWithSimpleStyleArrayExplodedSendActionBuilder(OpenApiSpecification openApiSpecification, ExtPetApi extPetApi, List<String> petIdExpression) {
            super(extPetApi, openApiSpecification,  METHOD, ENDPOINT, OPERATION_NAME);
            pathParameter("petId", petIdExpression, ParameterStyle.SIMPLE, false, false);
        }

        /**
         * Constructor with required parameters as string to allow for dynamic content.
         */
        public GetPetWithSimpleStyleArrayExplodedSendActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification, TestApiClientRequestMessageBuilder messageBuilder, List<String> petId) {
            super(extPetApi, openApiSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
            pathParameter("petId", petId, ParameterStyle.SIMPLE, false, false);
        }

        public GetPetWithSimpleStyleArrayExplodedSendActionBuilder petId(Integer...petId) {
            pathParameter("petId", petId, ParameterStyle.SIMPLE, false, false);
            return this;
        }

        public GetPetWithSimpleStyleArrayExplodedSendActionBuilder petId(String...petIdExpression) {
            pathParameter("petId", petIdExpression, ParameterStyle.SIMPLE, false, false);
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

    public static class GetPetWithSimpleStyleArrayExplodedReceiveActionBuilder extends
                        RestApiReceiveMessageActionBuilder {

        private static final String METHOD = "GET";

        private static final String ENDPOINT = "/api/v3/ext/pet/simple/exploded/{petId}";

        private static final String OPERATION_NAME = "getPetWithSimpleStyleArrayExploded";

        public GetPetWithSimpleStyleArrayExplodedReceiveActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification,  String statusCode) {
            super(extPetApi, openApiSpecification, METHOD, ENDPOINT, OPERATION_NAME, statusCode);
        }

        public GetPetWithSimpleStyleArrayExplodedReceiveActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification, OpenApiClientResponseMessageBuilder messageBuilder) {
            super(extPetApi, openApiSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
        }

        @Override
        public ReceiveMessageAction doBuild() {

            if (getCustomizers() != null) {
                getCustomizers().forEach(customizer -> customizer.customizeResponseBuilder(getGeneratedApi(), this));
            }

            return super.doBuild();
        }

    }

    public static class GetPetWithSimpleStyleExplodedHeaderSendActionBuilder extends
                RestApiSendMessageActionBuilder {

        private static final String METHOD = "GET";

        private static final String ENDPOINT = "/api/v3/ext/pet/header/simple/exploded";

        private static final String OPERATION_NAME = "getPetWithSimpleStyleExplodedHeader";

        /**
         * Constructor with type safe required parameters.
         */
        public GetPetWithSimpleStyleExplodedHeaderSendActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification, List<Integer> petId) {
            super(extPetApi, openApiSpecification, METHOD, ENDPOINT, OPERATION_NAME);
            headerParameter("petId", petId, ParameterStyle.SIMPLE, true, false);
        }

        /**
         * Constructor with required parameters as string to allow for dynamic content.
         */
            public GetPetWithSimpleStyleExplodedHeaderSendActionBuilder(OpenApiSpecification openApiSpecification, ExtPetApi extPetApi, List<String> petIdExpression) {
            super(extPetApi, openApiSpecification,  METHOD, ENDPOINT, OPERATION_NAME);
            headerParameter("petId", petIdExpression, ParameterStyle.SIMPLE, true, false);
        }

        /**
         * Constructor with required parameters as string to allow for dynamic content.
         */
        public GetPetWithSimpleStyleExplodedHeaderSendActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification, TestApiClientRequestMessageBuilder messageBuilder, List<String> petId) {
            super(extPetApi, openApiSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
            headerParameter("petId", petId, ParameterStyle.SIMPLE, true, false);
        }

        public GetPetWithSimpleStyleExplodedHeaderSendActionBuilder petId(Integer...petId) {
            headerParameter("petId", petId, ParameterStyle.SIMPLE, true, false);
            return this;
        }

        public GetPetWithSimpleStyleExplodedHeaderSendActionBuilder petId(String...petIdExpression) {
            headerParameter("petId", petIdExpression, ParameterStyle.SIMPLE, true, false);
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

    public static class GetPetWithSimpleStyleExplodedHeaderReceiveActionBuilder extends
                        RestApiReceiveMessageActionBuilder {

        private static final String METHOD = "GET";

        private static final String ENDPOINT = "/api/v3/ext/pet/header/simple/exploded";

        private static final String OPERATION_NAME = "getPetWithSimpleStyleExplodedHeader";

        public GetPetWithSimpleStyleExplodedHeaderReceiveActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification,  String statusCode) {
            super(extPetApi, openApiSpecification, METHOD, ENDPOINT, OPERATION_NAME, statusCode);
        }

        public GetPetWithSimpleStyleExplodedHeaderReceiveActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification, OpenApiClientResponseMessageBuilder messageBuilder) {
            super(extPetApi, openApiSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
        }

        @Override
        public ReceiveMessageAction doBuild() {

            if (getCustomizers() != null) {
                getCustomizers().forEach(customizer -> customizer.customizeResponseBuilder(getGeneratedApi(), this));
            }

            return super.doBuild();
        }

    }

    public static class GetPetWithSimpleStyleExplodedObjectHeaderSendActionBuilder extends
                RestApiSendMessageActionBuilder {

        private static final String METHOD = "GET";

        private static final String ENDPOINT = "/api/v3/ext/pet/header/simple/exploded/object";

        private static final String OPERATION_NAME = "getPetWithSimpleStyleExplodedObjectHeader";

        /**
         * Constructor with type safe required parameters.
         */
        public GetPetWithSimpleStyleExplodedObjectHeaderSendActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification, PetIdentifier petId) {
            super(extPetApi, openApiSpecification, METHOD, ENDPOINT, OPERATION_NAME);
            headerParameter("petId", petId, ParameterStyle.SIMPLE, true, true);
        }

        /**
         * Constructor with required parameters as string to allow for dynamic content.
         */
            public GetPetWithSimpleStyleExplodedObjectHeaderSendActionBuilder(OpenApiSpecification openApiSpecification, ExtPetApi extPetApi, String petIdExpression) {
            super(extPetApi, openApiSpecification,  METHOD, ENDPOINT, OPERATION_NAME);
            headerParameter("petId", petIdExpression, ParameterStyle.SIMPLE, true, true);
        }

        /**
         * Constructor with required parameters as string to allow for dynamic content.
         */
        public GetPetWithSimpleStyleExplodedObjectHeaderSendActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification, TestApiClientRequestMessageBuilder messageBuilder, String petIdExpression) {
            super(extPetApi, openApiSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
            headerParameter("petId", petIdExpression, ParameterStyle.SIMPLE, true, true);
        }

        public GetPetWithSimpleStyleExplodedObjectHeaderSendActionBuilder petId(PetIdentifier petId) {
            headerParameter("petId", petId, ParameterStyle.SIMPLE, true, true);
            return this;
        }

        public GetPetWithSimpleStyleExplodedObjectHeaderSendActionBuilder petId(String petIdExpression) {
            headerParameter("petId", petIdExpression, ParameterStyle.SIMPLE, true, true);
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

    public static class GetPetWithSimpleStyleExplodedObjectHeaderReceiveActionBuilder extends
                        RestApiReceiveMessageActionBuilder {

        private static final String METHOD = "GET";

        private static final String ENDPOINT = "/api/v3/ext/pet/header/simple/exploded/object";

        private static final String OPERATION_NAME = "getPetWithSimpleStyleExplodedObjectHeader";

        public GetPetWithSimpleStyleExplodedObjectHeaderReceiveActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification,  String statusCode) {
            super(extPetApi, openApiSpecification, METHOD, ENDPOINT, OPERATION_NAME, statusCode);
        }

        public GetPetWithSimpleStyleExplodedObjectHeaderReceiveActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification, OpenApiClientResponseMessageBuilder messageBuilder) {
            super(extPetApi, openApiSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
        }

        @Override
        public ReceiveMessageAction doBuild() {

            if (getCustomizers() != null) {
                getCustomizers().forEach(customizer -> customizer.customizeResponseBuilder(getGeneratedApi(), this));
            }

            return super.doBuild();
        }

    }

    public static class GetPetWithSimpleStyleHeaderSendActionBuilder extends
                RestApiSendMessageActionBuilder {

        private static final String METHOD = "GET";

        private static final String ENDPOINT = "/api/v3/ext/pet/header/simple";

        private static final String OPERATION_NAME = "getPetWithSimpleStyleHeader";

        /**
         * Constructor with type safe required parameters.
         */
        public GetPetWithSimpleStyleHeaderSendActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification, List<Integer> petId) {
            super(extPetApi, openApiSpecification, METHOD, ENDPOINT, OPERATION_NAME);
            headerParameter("petId", petId, ParameterStyle.SIMPLE, false, false);
        }

        /**
         * Constructor with required parameters as string to allow for dynamic content.
         */
            public GetPetWithSimpleStyleHeaderSendActionBuilder(OpenApiSpecification openApiSpecification, ExtPetApi extPetApi, List<String> petIdExpression) {
            super(extPetApi, openApiSpecification,  METHOD, ENDPOINT, OPERATION_NAME);
            headerParameter("petId", petIdExpression, ParameterStyle.SIMPLE, false, false);
        }

        /**
         * Constructor with required parameters as string to allow for dynamic content.
         */
        public GetPetWithSimpleStyleHeaderSendActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification, TestApiClientRequestMessageBuilder messageBuilder, List<String> petId) {
            super(extPetApi, openApiSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
            headerParameter("petId", petId, ParameterStyle.SIMPLE, false, false);
        }

        public GetPetWithSimpleStyleHeaderSendActionBuilder petId(Integer...petId) {
            headerParameter("petId", petId, ParameterStyle.SIMPLE, false, false);
            return this;
        }

        public GetPetWithSimpleStyleHeaderSendActionBuilder petId(String...petIdExpression) {
            headerParameter("petId", petIdExpression, ParameterStyle.SIMPLE, false, false);
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

    public static class GetPetWithSimpleStyleHeaderReceiveActionBuilder extends
                        RestApiReceiveMessageActionBuilder {

        private static final String METHOD = "GET";

        private static final String ENDPOINT = "/api/v3/ext/pet/header/simple";

        private static final String OPERATION_NAME = "getPetWithSimpleStyleHeader";

        public GetPetWithSimpleStyleHeaderReceiveActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification,  String statusCode) {
            super(extPetApi, openApiSpecification, METHOD, ENDPOINT, OPERATION_NAME, statusCode);
        }

        public GetPetWithSimpleStyleHeaderReceiveActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification, OpenApiClientResponseMessageBuilder messageBuilder) {
            super(extPetApi, openApiSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
        }

        @Override
        public ReceiveMessageAction doBuild() {

            if (getCustomizers() != null) {
                getCustomizers().forEach(customizer -> customizer.customizeResponseBuilder(getGeneratedApi(), this));
            }

            return super.doBuild();
        }

    }

    public static class GetPetWithSimpleStyleObjectSendActionBuilder extends
                RestApiSendMessageActionBuilder {

        private static final String METHOD = "GET";

        private static final String ENDPOINT = "/api/v3/ext/pet/simple/object/{petId}";

        private static final String OPERATION_NAME = "getPetWithSimpleStyleObject";

        /**
         * Constructor with type safe required parameters.
         */
        public GetPetWithSimpleStyleObjectSendActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification, PetIdentifier petId) {
            super(extPetApi, openApiSpecification, METHOD, ENDPOINT, OPERATION_NAME);
            pathParameter("petId", petId, ParameterStyle.SIMPLE, false, true);
        }

        /**
         * Constructor with required parameters as string to allow for dynamic content.
         */
            public GetPetWithSimpleStyleObjectSendActionBuilder(OpenApiSpecification openApiSpecification, ExtPetApi extPetApi, String petIdExpression) {
            super(extPetApi, openApiSpecification,  METHOD, ENDPOINT, OPERATION_NAME);
            pathParameter("petId", petIdExpression, ParameterStyle.SIMPLE, false, true);
        }

        /**
         * Constructor with required parameters as string to allow for dynamic content.
         */
        public GetPetWithSimpleStyleObjectSendActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification, TestApiClientRequestMessageBuilder messageBuilder, String petIdExpression) {
            super(extPetApi, openApiSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
            pathParameter("petId", petIdExpression, ParameterStyle.SIMPLE, false, true);
        }

        public GetPetWithSimpleStyleObjectSendActionBuilder petId(PetIdentifier petId) {
            pathParameter("petId", petId, ParameterStyle.SIMPLE, false, true);
            return this;
        }

        public GetPetWithSimpleStyleObjectSendActionBuilder petId(String petIdExpression) {
            pathParameter("petId", petIdExpression, ParameterStyle.SIMPLE, false, true);
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

    public static class GetPetWithSimpleStyleObjectReceiveActionBuilder extends
                        RestApiReceiveMessageActionBuilder {

        private static final String METHOD = "GET";

        private static final String ENDPOINT = "/api/v3/ext/pet/simple/object/{petId}";

        private static final String OPERATION_NAME = "getPetWithSimpleStyleObject";

        public GetPetWithSimpleStyleObjectReceiveActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification,  String statusCode) {
            super(extPetApi, openApiSpecification, METHOD, ENDPOINT, OPERATION_NAME, statusCode);
        }

        public GetPetWithSimpleStyleObjectReceiveActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification, OpenApiClientResponseMessageBuilder messageBuilder) {
            super(extPetApi, openApiSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
        }

        @Override
        public ReceiveMessageAction doBuild() {

            if (getCustomizers() != null) {
                getCustomizers().forEach(customizer -> customizer.customizeResponseBuilder(getGeneratedApi(), this));
            }

            return super.doBuild();
        }

    }

    public static class GetPetWithSimpleStyleObjectExplodedSendActionBuilder extends
                RestApiSendMessageActionBuilder {

        private static final String METHOD = "GET";

        private static final String ENDPOINT = "/api/v3/ext/pet/simple/exploded/object/{petId}";

        private static final String OPERATION_NAME = "getPetWithSimpleStyleObjectExploded";

        /**
         * Constructor with type safe required parameters.
         */
        public GetPetWithSimpleStyleObjectExplodedSendActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification, PetIdentifier petId) {
            super(extPetApi, openApiSpecification, METHOD, ENDPOINT, OPERATION_NAME);
            pathParameter("petId", petId, ParameterStyle.SIMPLE, true, true);
        }

        /**
         * Constructor with required parameters as string to allow for dynamic content.
         */
            public GetPetWithSimpleStyleObjectExplodedSendActionBuilder(OpenApiSpecification openApiSpecification, ExtPetApi extPetApi, String petIdExpression) {
            super(extPetApi, openApiSpecification,  METHOD, ENDPOINT, OPERATION_NAME);
            pathParameter("petId", petIdExpression, ParameterStyle.SIMPLE, true, true);
        }

        /**
         * Constructor with required parameters as string to allow for dynamic content.
         */
        public GetPetWithSimpleStyleObjectExplodedSendActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification, TestApiClientRequestMessageBuilder messageBuilder, String petIdExpression) {
            super(extPetApi, openApiSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
            pathParameter("petId", petIdExpression, ParameterStyle.SIMPLE, true, true);
        }

        public GetPetWithSimpleStyleObjectExplodedSendActionBuilder petId(PetIdentifier petId) {
            pathParameter("petId", petId, ParameterStyle.SIMPLE, true, true);
            return this;
        }

        public GetPetWithSimpleStyleObjectExplodedSendActionBuilder petId(String petIdExpression) {
            pathParameter("petId", petIdExpression, ParameterStyle.SIMPLE, true, true);
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

    public static class GetPetWithSimpleStyleObjectExplodedReceiveActionBuilder extends
                        RestApiReceiveMessageActionBuilder {

        private static final String METHOD = "GET";

        private static final String ENDPOINT = "/api/v3/ext/pet/simple/exploded/object/{petId}";

        private static final String OPERATION_NAME = "getPetWithSimpleStyleObjectExploded";

        public GetPetWithSimpleStyleObjectExplodedReceiveActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification,  String statusCode) {
            super(extPetApi, openApiSpecification, METHOD, ENDPOINT, OPERATION_NAME, statusCode);
        }

        public GetPetWithSimpleStyleObjectExplodedReceiveActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification, OpenApiClientResponseMessageBuilder messageBuilder) {
            super(extPetApi, openApiSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
        }

        @Override
        public ReceiveMessageAction doBuild() {

            if (getCustomizers() != null) {
                getCustomizers().forEach(customizer -> customizer.customizeResponseBuilder(getGeneratedApi(), this));
            }

            return super.doBuild();
        }

    }

    public static class GetPetWithSimpleStyleObjectHeaderSendActionBuilder extends
                RestApiSendMessageActionBuilder {

        private static final String METHOD = "GET";

        private static final String ENDPOINT = "/api/v3/ext/pet/header/simple/object";

        private static final String OPERATION_NAME = "getPetWithSimpleStyleObjectHeader";

        /**
         * Constructor with type safe required parameters.
         */
        public GetPetWithSimpleStyleObjectHeaderSendActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification, PetIdentifier petId) {
            super(extPetApi, openApiSpecification, METHOD, ENDPOINT, OPERATION_NAME);
            headerParameter("petId", petId, ParameterStyle.SIMPLE, false, true);
        }

        /**
         * Constructor with required parameters as string to allow for dynamic content.
         */
            public GetPetWithSimpleStyleObjectHeaderSendActionBuilder(OpenApiSpecification openApiSpecification, ExtPetApi extPetApi, String petIdExpression) {
            super(extPetApi, openApiSpecification,  METHOD, ENDPOINT, OPERATION_NAME);
            headerParameter("petId", petIdExpression, ParameterStyle.SIMPLE, false, true);
        }

        /**
         * Constructor with required parameters as string to allow for dynamic content.
         */
        public GetPetWithSimpleStyleObjectHeaderSendActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification, TestApiClientRequestMessageBuilder messageBuilder, String petIdExpression) {
            super(extPetApi, openApiSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
            headerParameter("petId", petIdExpression, ParameterStyle.SIMPLE, false, true);
        }

        public GetPetWithSimpleStyleObjectHeaderSendActionBuilder petId(PetIdentifier petId) {
            headerParameter("petId", petId, ParameterStyle.SIMPLE, false, true);
            return this;
        }

        public GetPetWithSimpleStyleObjectHeaderSendActionBuilder petId(String petIdExpression) {
            headerParameter("petId", petIdExpression, ParameterStyle.SIMPLE, false, true);
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

    public static class GetPetWithSimpleStyleObjectHeaderReceiveActionBuilder extends
                        RestApiReceiveMessageActionBuilder {

        private static final String METHOD = "GET";

        private static final String ENDPOINT = "/api/v3/ext/pet/header/simple/object";

        private static final String OPERATION_NAME = "getPetWithSimpleStyleObjectHeader";

        public GetPetWithSimpleStyleObjectHeaderReceiveActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification,  String statusCode) {
            super(extPetApi, openApiSpecification, METHOD, ENDPOINT, OPERATION_NAME, statusCode);
        }

        public GetPetWithSimpleStyleObjectHeaderReceiveActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification, OpenApiClientResponseMessageBuilder messageBuilder) {
            super(extPetApi, openApiSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
        }

        @Override
        public ReceiveMessageAction doBuild() {

            if (getCustomizers() != null) {
                getCustomizers().forEach(customizer -> customizer.customizeResponseBuilder(getGeneratedApi(), this));
            }

            return super.doBuild();
        }

    }

    public static class PostVaccinationDocumentSendActionBuilder extends
                RestApiSendMessageActionBuilder {

        private static final String METHOD = "POST";

        private static final String ENDPOINT = "/api/v3/ext/pet/vaccination/{bucket}/{filename}";

        private static final String OPERATION_NAME = "postVaccinationDocument";

        /**
         * Constructor with type safe required parameters.
         */
        public PostVaccinationDocumentSendActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification, String bucket, String filename) {
            super(extPetApi, openApiSpecification, METHOD, ENDPOINT, OPERATION_NAME);
            pathParameter("bucket", bucket, ParameterStyle.SIMPLE, false, false);
            pathParameter("filename", filename, ParameterStyle.SIMPLE, false, false);
        }

        /**
         * Constructor with required parameters as string to allow for dynamic content.
         */
        public PostVaccinationDocumentSendActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification, TestApiClientRequestMessageBuilder messageBuilder, String bucketExpression, String filenameExpression) {
            super(extPetApi, openApiSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
            pathParameter("bucket", bucketExpression, ParameterStyle.SIMPLE, false, false);
            pathParameter("filename", filenameExpression, ParameterStyle.SIMPLE, false, false);
        }

        public PostVaccinationDocumentSendActionBuilder bucket(String bucket) {
            pathParameter("bucket", bucket, ParameterStyle.SIMPLE, false, false);
            return this;
        }

        public PostVaccinationDocumentSendActionBuilder filename(String filename) {
            pathParameter("filename", filename, ParameterStyle.SIMPLE, false, false);
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

    public static class PostVaccinationDocumentReceiveActionBuilder extends
                        RestApiReceiveMessageActionBuilder {

        private static final String METHOD = "POST";

        private static final String ENDPOINT = "/api/v3/ext/pet/vaccination/{bucket}/{filename}";

        private static final String OPERATION_NAME = "postVaccinationDocument";

        public PostVaccinationDocumentReceiveActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification,  String statusCode) {
            super(extPetApi, openApiSpecification, METHOD, ENDPOINT, OPERATION_NAME, statusCode);
        }

        public PostVaccinationDocumentReceiveActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification, OpenApiClientResponseMessageBuilder messageBuilder) {
            super(extPetApi, openApiSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
        }

        @Override
        public ReceiveMessageAction doBuild() {

            if (getCustomizers() != null) {
                getCustomizers().forEach(customizer -> customizer.customizeResponseBuilder(getGeneratedApi(), this));
            }

            return super.doBuild();
        }

    }

    public static class PostVaccinationFormDataSendActionBuilder extends
                RestApiSendMessageActionBuilder {

        private static final String METHOD = "POST";

        private static final String ENDPOINT = "/api/v3/ext/pet/vaccination/form";

        private static final String OPERATION_NAME = "postVaccinationFormData";

        /**
         * Constructor with type safe required parameters.
         */
        public PostVaccinationFormDataSendActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification) {
            super(extPetApi, openApiSpecification, METHOD, ENDPOINT, OPERATION_NAME);
        }

        public PostVaccinationFormDataSendActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification, TestApiClientRequestMessageBuilder messageBuilder) {
            super(extPetApi, openApiSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
        }

        public PostVaccinationFormDataSendActionBuilder vaccine(String vaccine) {
            formParameter("vaccine",  vaccine);
            return this;
        }

        public void setVaccine(String vaccine) {
            formParameter("vaccine",  vaccine);
        }

        public PostVaccinationFormDataSendActionBuilder isFirstVaccination(Boolean isFirstVaccination) {
            formParameter("isFirstVaccination",  isFirstVaccination);
            return this;
        }

        public void setIsFirstVaccination(Boolean isFirstVaccination) {
            formParameter("isFirstVaccination",  isFirstVaccination);
        }

        public PostVaccinationFormDataSendActionBuilder isFirstVaccination(String isFirstVaccinationExpression) {
            formParameter("isFirstVaccination",  isFirstVaccinationExpression);
            return this;
        }

        public void setIsFirstVaccination(String isFirstVaccinationExpression) {
            formParameter("isFirstVaccination",  isFirstVaccinationExpression);
        }

        public PostVaccinationFormDataSendActionBuilder doseNumber(Integer doseNumber) {
            formParameter("doseNumber",  doseNumber);
            return this;
        }

        public void setDoseNumber(Integer doseNumber) {
            formParameter("doseNumber",  doseNumber);
        }

        public PostVaccinationFormDataSendActionBuilder doseNumber(String doseNumberExpression) {
            formParameter("doseNumber",  doseNumberExpression);
            return this;
        }

        public void setDoseNumber(String doseNumberExpression) {
            formParameter("doseNumber",  doseNumberExpression);
        }

        public PostVaccinationFormDataSendActionBuilder vaccinationDate(LocalDate vaccinationDate) {
            formParameter("vaccinationDate",  vaccinationDate);
            return this;
        }

        public void setVaccinationDate(LocalDate vaccinationDate) {
            formParameter("vaccinationDate",  vaccinationDate);
        }

        public PostVaccinationFormDataSendActionBuilder vaccinationDate(String vaccinationDateExpression) {
            formParameter("vaccinationDate",  vaccinationDateExpression);
            return this;
        }

        public void setVaccinationDate(String vaccinationDateExpression) {
            formParameter("vaccinationDate",  vaccinationDateExpression);
        }

        @Override
        public SendMessageAction doBuild() {

            if (getCustomizers() != null) {
                getCustomizers().forEach(customizer -> customizer.customizeRequestBuilder(getGeneratedApi(), this));
            }

            return super.doBuild();
        }
    }

    public static class PostVaccinationFormDataReceiveActionBuilder extends
                        RestApiReceiveMessageActionBuilder {

        private static final String METHOD = "POST";

        private static final String ENDPOINT = "/api/v3/ext/pet/vaccination/form";

        private static final String OPERATION_NAME = "postVaccinationFormData";

        public PostVaccinationFormDataReceiveActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification,  String statusCode) {
            super(extPetApi, openApiSpecification, METHOD, ENDPOINT, OPERATION_NAME, statusCode);
        }

        public PostVaccinationFormDataReceiveActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification, OpenApiClientResponseMessageBuilder messageBuilder) {
            super(extPetApi, openApiSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
        }

        @Override
        public ReceiveMessageAction doBuild() {

            if (getCustomizers() != null) {
                getCustomizers().forEach(customizer -> customizer.customizeResponseBuilder(getGeneratedApi(), this));
            }

            return super.doBuild();
        }

    }

    public static class UpdatePetWithArrayQueryDataSendActionBuilder extends
                RestApiSendMessageActionBuilder {

        private static final String METHOD = "PUT";

        private static final String ENDPOINT = "/api/v3/ext/pet/{petId}";

        private static final String OPERATION_NAME = "updatePetWithArrayQueryData";

        /**
         * Constructor with type safe required parameters.
         */
        public UpdatePetWithArrayQueryDataSendActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification, Long petId, String _name, String status, List<String> tags, List<String> nicknames, String sampleStringHeader) {
            super(extPetApi, openApiSpecification, METHOD, ENDPOINT, OPERATION_NAME);
            pathParameter("petId", petId, ParameterStyle.SIMPLE, false, false);
            queryParameter("name", _name, ParameterStyle.FORM, true, false);
            queryParameter("status", status, ParameterStyle.FORM, true, false);
            queryParameter("tags", tags, ParameterStyle.FORM, true, false);
            queryParameter("nicknames", nicknames, ParameterStyle.FORM, true, false);
            headerParameter("sampleStringHeader", sampleStringHeader, ParameterStyle.SIMPLE, false, false);
        }

        /**
         * Constructor with required parameters as string to allow for dynamic content.
         */
            public UpdatePetWithArrayQueryDataSendActionBuilder(OpenApiSpecification openApiSpecification, ExtPetApi extPetApi, String petIdExpression, String _nameExpression, String statusExpression, List<String> tagsExpression, List<String> nicknamesExpression, String sampleStringHeaderExpression) {
            super(extPetApi, openApiSpecification,  METHOD, ENDPOINT, OPERATION_NAME);
            pathParameter("petId", petIdExpression, ParameterStyle.SIMPLE, false, false);
            queryParameter("name", _nameExpression, ParameterStyle.FORM, true, false);
            queryParameter("status", statusExpression, ParameterStyle.FORM, true, false);
            queryParameter("tags", tagsExpression, ParameterStyle.FORM, true, false);
            queryParameter("nicknames", nicknamesExpression, ParameterStyle.FORM, true, false);
            headerParameter("sampleStringHeader", sampleStringHeaderExpression, ParameterStyle.SIMPLE, false, false);
        }

        /**
         * Constructor with required parameters as string to allow for dynamic content.
         */
        public UpdatePetWithArrayQueryDataSendActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification, TestApiClientRequestMessageBuilder messageBuilder, String petIdExpression, String _nameExpression, String statusExpression, List<String> tags, List<String> nicknames, String sampleStringHeaderExpression) {
            super(extPetApi, openApiSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
            pathParameter("petId", petIdExpression, ParameterStyle.SIMPLE, false, false);
            queryParameter("name", _nameExpression, ParameterStyle.FORM, true, false);
            queryParameter("status", statusExpression, ParameterStyle.FORM, true, false);
            queryParameter("tags", tags, ParameterStyle.FORM, true, false);
            queryParameter("nicknames", nicknames, ParameterStyle.FORM, true, false);
            headerParameter("sampleStringHeader", sampleStringHeaderExpression, ParameterStyle.SIMPLE, false, false);
        }

        public UpdatePetWithArrayQueryDataSendActionBuilder petId(Long petId) {
            pathParameter("petId", petId, ParameterStyle.SIMPLE, false, false);
            return this;
        }

        public UpdatePetWithArrayQueryDataSendActionBuilder petId(String petIdExpression) {
            pathParameter("petId", petIdExpression, ParameterStyle.SIMPLE, false, false);
                return this;
        }

        public UpdatePetWithArrayQueryDataSendActionBuilder _name(String _name) {
            queryParameter("name", _name, ParameterStyle.FORM, true, false);
            return this;
        }

        public UpdatePetWithArrayQueryDataSendActionBuilder status(String status) {
            queryParameter("status", status, ParameterStyle.FORM, true, false);
            return this;
        }

        public UpdatePetWithArrayQueryDataSendActionBuilder tags(String...tags) {
            queryParameter("tags", tags, ParameterStyle.FORM, true, false);
            return this;
        }

        public UpdatePetWithArrayQueryDataSendActionBuilder nicknames(String...nicknames) {
            queryParameter("nicknames", nicknames, ParameterStyle.FORM, true, false);
            return this;
        }

        public UpdatePetWithArrayQueryDataSendActionBuilder sampleStringHeader(String sampleStringHeader) {
            headerParameter("sampleStringHeader", sampleStringHeader, ParameterStyle.SIMPLE, false, false);
            return this;
        }

        public UpdatePetWithArrayQueryDataSendActionBuilder sampleIntHeader(Integer sampleIntHeader) {
            headerParameter("sampleIntHeader", sampleIntHeader, ParameterStyle.SIMPLE, false, false);
            return this;
        }

        public void setSampleIntHeader(Integer sampleIntHeader) {
            headerParameter("sampleIntHeader", sampleIntHeader, ParameterStyle.SIMPLE, false, false);
        }

        public UpdatePetWithArrayQueryDataSendActionBuilder sampleIntHeader(String sampleIntHeaderExpression) {
            headerParameter("sampleIntHeader", sampleIntHeaderExpression, ParameterStyle.SIMPLE, false, false);
            return this;
        }

        public void setSampleIntHeader(String sampleIntHeaderExpression) {
            headerParameter("sampleIntHeader", sampleIntHeaderExpression, ParameterStyle.SIMPLE, false, false);
        }

        @Override
        public SendMessageAction doBuild() {

            if (getCustomizers() != null) {
                getCustomizers().forEach(customizer -> customizer.customizeRequestBuilder(getGeneratedApi(), this));
            }

            return super.doBuild();
        }
    }

    public static class UpdatePetWithArrayQueryDataReceiveActionBuilder extends
                        RestApiReceiveMessageActionBuilder {

        private static final String METHOD = "PUT";

        private static final String ENDPOINT = "/api/v3/ext/pet/{petId}";

        private static final String OPERATION_NAME = "updatePetWithArrayQueryData";

        public UpdatePetWithArrayQueryDataReceiveActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification,  String statusCode) {
            super(extPetApi, openApiSpecification, METHOD, ENDPOINT, OPERATION_NAME, statusCode);
        }

        public UpdatePetWithArrayQueryDataReceiveActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification, OpenApiClientResponseMessageBuilder messageBuilder) {
            super(extPetApi, openApiSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
        }

        @Override
        public ReceiveMessageAction doBuild() {

            if (getCustomizers() != null) {
                getCustomizers().forEach(customizer -> customizer.customizeResponseBuilder(getGeneratedApi(), this));
            }

            return super.doBuild();
        }

    }

    public static class UpdatePetWithFormUrlEncodedSendActionBuilder extends
                RestApiSendMessageActionBuilder {

        private static final String METHOD = "PUT";

        private static final String ENDPOINT = "/api/v3/ext/pet/form/{petId}";

        private static final String OPERATION_NAME = "updatePetWithFormUrlEncoded";

        /**
         * Constructor with type safe required parameters.
         */
        public UpdatePetWithFormUrlEncodedSendActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification, Long petId, String _name, String status, Integer age, List<String> tags) {
            super(extPetApi, openApiSpecification, METHOD, ENDPOINT, OPERATION_NAME);
            pathParameter("petId", petId, ParameterStyle.SIMPLE, false, false);
            formParameter("name",  _name);
            formParameter("status",  status);
            formParameter("age",  age);
            formParameter("tags",  tags);
        }

        /**
         * Constructor with required parameters as string to allow for dynamic content.
         */
            public UpdatePetWithFormUrlEncodedSendActionBuilder(OpenApiSpecification openApiSpecification, ExtPetApi extPetApi, String petIdExpression, String _nameExpression, String statusExpression, String ageExpression, List<String> tagsExpression) {
            super(extPetApi, openApiSpecification,  METHOD, ENDPOINT, OPERATION_NAME);
            pathParameter("petId", petIdExpression, ParameterStyle.SIMPLE, false, false);
            formParameter("name",  _nameExpression);
            formParameter("status",  statusExpression);
            formParameter("age",  ageExpression);
            formParameter("tags",  tagsExpression);
        }

        /**
         * Constructor with required parameters as string to allow for dynamic content.
         */
        public UpdatePetWithFormUrlEncodedSendActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification, TestApiClientRequestMessageBuilder messageBuilder, String petIdExpression, String _nameExpression, String statusExpression, String ageExpression, List<String> tags) {
            super(extPetApi, openApiSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
            pathParameter("petId", petIdExpression, ParameterStyle.SIMPLE, false, false);
            formParameter("name",  _nameExpression);
            formParameter("status",  statusExpression);
            formParameter("age",  ageExpression);
            formParameter("tags",  tags);
        }

        public UpdatePetWithFormUrlEncodedSendActionBuilder petId(Long petId) {
            pathParameter("petId", petId, ParameterStyle.SIMPLE, false, false);
            return this;
        }

        public UpdatePetWithFormUrlEncodedSendActionBuilder petId(String petIdExpression) {
            pathParameter("petId", petIdExpression, ParameterStyle.SIMPLE, false, false);
                return this;
        }

        public UpdatePetWithFormUrlEncodedSendActionBuilder _name(String _name) {
            formParameter("name",  _name);
            return this;
        }

        public UpdatePetWithFormUrlEncodedSendActionBuilder status(String status) {
            formParameter("status",  status);
            return this;
        }

        public UpdatePetWithFormUrlEncodedSendActionBuilder age(Integer age) {
            formParameter("age",  age);
            return this;
        }

        public UpdatePetWithFormUrlEncodedSendActionBuilder age(String ageExpression) {
            formParameter("age",  ageExpression);
                return this;
        }

        public UpdatePetWithFormUrlEncodedSendActionBuilder tags(String...tags) {
            formParameter("tags",  tags);
            return this;
        }

        public UpdatePetWithFormUrlEncodedSendActionBuilder owners(Integer owners) {
            formParameter("owners",  owners);
            return this;
        }

        public void setOwners(Integer owners) {
            formParameter("owners",  owners);
        }

        public UpdatePetWithFormUrlEncodedSendActionBuilder owners(String ownersExpression) {
            formParameter("owners",  ownersExpression);
            return this;
        }

        public void setOwners(String ownersExpression) {
            formParameter("owners",  ownersExpression);
        }

        public UpdatePetWithFormUrlEncodedSendActionBuilder nicknames(String...nicknames) {
            formParameter("nicknames",  nicknames);
            return this;
        }

        public void setNicknames(String...nicknames) {
            formParameter("nicknames",  nicknames);
        }

        @Override
        public SendMessageAction doBuild() {

            if (getCustomizers() != null) {
                getCustomizers().forEach(customizer -> customizer.customizeRequestBuilder(getGeneratedApi(), this));
            }

            return super.doBuild();
        }
    }

    public static class UpdatePetWithFormUrlEncodedReceiveActionBuilder extends
                        RestApiReceiveMessageActionBuilder {

        private static final String METHOD = "PUT";

        private static final String ENDPOINT = "/api/v3/ext/pet/form/{petId}";

        private static final String OPERATION_NAME = "updatePetWithFormUrlEncoded";

        public UpdatePetWithFormUrlEncodedReceiveActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification,  String statusCode) {
            super(extPetApi, openApiSpecification, METHOD, ENDPOINT, OPERATION_NAME, statusCode);
        }

        public UpdatePetWithFormUrlEncodedReceiveActionBuilder(ExtPetApi extPetApi, OpenApiSpecification openApiSpecification, OpenApiClientResponseMessageBuilder messageBuilder) {
            super(extPetApi, openApiSpecification, messageBuilder, messageBuilder.getMessage(), METHOD, ENDPOINT, OPERATION_NAME);
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