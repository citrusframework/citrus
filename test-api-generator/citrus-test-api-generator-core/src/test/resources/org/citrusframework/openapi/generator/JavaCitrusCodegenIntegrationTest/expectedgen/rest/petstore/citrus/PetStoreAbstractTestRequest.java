/** 
 * ==================================================
 * GENERATED CLASS, ALL CHANGES WILL BE LOST
 * ==================================================
 */

package org.citrusframework.openapi.generator.rest.petstore.citrus;


import static org.springframework.util.CollectionUtils.isEmpty;

import jakarta.annotation.Generated;
import jakarta.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import javax.sql.DataSource;
import org.citrusframework.actions.AbstractTestAction;
import org.citrusframework.actions.ReceiveMessageAction;
import org.citrusframework.context.TestContext;
import org.citrusframework.http.actions.HttpActionBuilder;
import org.citrusframework.http.actions.HttpClientRequestActionBuilder;
import org.citrusframework.http.actions.HttpClientResponseActionBuilder;
import org.citrusframework.http.actions.HttpClientResponseActionBuilder.HttpMessageBuilderSupport;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.message.Message;
import org.citrusframework.testapi.ApiActionBuilderCustomizerService;
import org.citrusframework.testapi.GeneratedApi;
import org.citrusframework.spi.Resources;
import org.citrusframework.validation.DelegatingPayloadVariableExtractor;
import org.citrusframework.validation.PathExpressionValidationContext;
import org.citrusframework.validation.json.JsonMessageValidationContext;
import org.citrusframework.validation.script.ScriptValidationContext;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

@Generated(value = "org.citrusframework.openapi.generator.JavaCitrusCodegen")
public abstract class PetStoreAbstractTestRequest extends AbstractTestAction {

    protected final Marker coverageMarker = MarkerFactory.getMarker("PETSTORE-API-COVERAGE");

    @Autowired
    @Qualifier("petStoreEndpoint")
    protected HttpClient httpClient;

    @Autowired(required = false)
    protected DataSource dataSource;

    @Autowired(required = false)
    private List<ApiActionBuilderCustomizerService> actionBuilderCustomizerServices;

    // attributes of differentNodes
    protected boolean schemaValidation;
    protected String schema;
    protected String bodyContentType;
    protected String bodyLiteralContentType;
    protected String bodyFile;
    protected String bodyLiteral;
    protected String responseAcceptType = "*/*";
    protected String responseType = "json";
    protected int responseStatus = 200;
    protected String responseReasonPhrase = "OK";
    protected String responseVersion = "HTTP/1.1";

    // children of response element
    protected String resource;
    protected Map<String, String> responseVariable; // Contains the 'JSON-PATH' as key and the 'VARIABLE NAME' as value
    protected Map<String, String> responseValue; // Contains the 'JSON-PATH' as key and the 'VALUE TO BE VALIDATED' as value
    protected Map<String, String> cookies;
    protected Map<String, String> headers;
    protected String script;
    protected String type; // default script type is groovy - supported types see com.consol.citrus.script.ScriptTypes

    @Override
    public void doExecute(TestContext context) {
        sendRequest(context);
        recieveResponse(context);
    }

    /**
    * This method receives the HTTP-Response.
    *
    * @deprecated use {@link PetStoreAbstractTestRequest#receiveResponse(TestContext)} instead.
    */
    public ReceiveMessageAction recieveResponse(TestContext context) {

       HttpClientResponseActionBuilder httpClientResponseActionBuilder = new HttpActionBuilder().client(httpClient).receive().response();
       HttpMessageBuilderSupport messageBuilderSupport = httpClientResponseActionBuilder.getMessageBuilderSupport();

       messageBuilderSupport
           .statusCode(responseStatus)
           .reasonPhrase(responseReasonPhrase)
           .version(responseVersion)
           .validate(new JsonMessageValidationContext.Builder().schemaValidation(schemaValidation).schema(schema));

        if (resource != null) {
            messageBuilderSupport.body(Resources.create(resource));
        }

        if (!isEmpty(responseVariable)) {
            DelegatingPayloadVariableExtractor.Builder extractorBuilder = new DelegatingPayloadVariableExtractor.Builder();
            responseVariable.forEach(extractorBuilder::expression);
            messageBuilderSupport.extract(extractorBuilder);
        }

        if (!isEmpty(responseValue)) {
            PathExpressionValidationContext.Builder validationContextBuilder = new PathExpressionValidationContext.Builder();
            responseValue.forEach(validationContextBuilder::expression);
            messageBuilderSupport.validate(validationContextBuilder);
        }

        if (script != null) {
            ScriptValidationContext.Builder scriptValidationContextBuilder = new ScriptValidationContext.Builder();
            if (type != null) {
                scriptValidationContextBuilder.scriptType(type);
            }
            scriptValidationContextBuilder.script(script);
            messageBuilderSupport.validate(scriptValidationContextBuilder);
        }

        messageBuilderSupport.type(responseType);
        httpClientResponseActionBuilder.withReferenceResolver(context.getReferenceResolver());
        var responseAction = httpClientResponseActionBuilder.build();

        responseAction.execute(context);

        return responseAction;
    }

    public @Nullable Message receiveResponse(TestContext context) {
        var responseAction = recieveResponse(context);

        var messageStore = context.getMessageStore();
        return messageStore.getMessage(messageStore.constructMessageName(responseAction, httpClient));
    }

    public abstract void sendRequest(TestContext context);

    public void setSchemaValidation(boolean schemaValidation) {
        this.schemaValidation = schemaValidation;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public void setBodyLiteral(String bodyLiteral) {
        this.bodyLiteral = bodyLiteral;
    }

    public void setBodyContentType(String bodyContentType) {
        this.bodyContentType = bodyContentType;
    }

    public void setBodyLiteralContentType(String bodyLiteralContentType) {
        this.bodyLiteralContentType = bodyLiteralContentType;
    }

    public void setResponseAcceptType(String responseAcceptType) {
        this.responseAcceptType = responseAcceptType;
    }

    public void setCookie(Map<String, String> cookies) {
        this.cookies = cookies;
    }

    public void setHeader(Map<String, String> headers) {
        this.headers = headers;
    }

    public void setBodyFile(String bodyFile) {
        this.bodyFile = bodyFile;
    }

    public void setResponseType(String responseType) {
        this.responseType = responseType;
    }

    public void setResponseStatus(int responseStatus) {
        this.responseStatus = responseStatus;
    }

    public void setResponseReasonPhrase(String responseReasonPhrase) {
        this.responseReasonPhrase = responseReasonPhrase;
    }

    public void setResponseVersion(String responseVersion) {
        this.responseVersion = responseVersion;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public void setResponseVariable(Map<String, String> responseVariable) {
        this.responseVariable = responseVariable;
    }

    public void setResponseValue(Map<String, String> responseValue) {
        this.responseValue = responseValue;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public void setType(String type) {
        this.type = type;
    }

    protected HttpClientRequestActionBuilder customizeBuilder(GeneratedApi generatedApi,
        TestContext context, HttpClientRequestActionBuilder httpClientRequestActionBuilder) {

        httpClientRequestActionBuilder = customizeByBeans(generatedApi, context,
        httpClientRequestActionBuilder);

        httpClientRequestActionBuilder = customizeBySpi(generatedApi, context, httpClientRequestActionBuilder);

        return httpClientRequestActionBuilder;
    }

    private HttpClientRequestActionBuilder customizeBySpi(GeneratedApi generatedApi, TestContext context,
        HttpClientRequestActionBuilder httpClientRequestActionBuilder) {
        ServiceLoader<ApiActionBuilderCustomizerService> serviceLoader = ServiceLoader.load(
        ApiActionBuilderCustomizerService.class, ApiActionBuilderCustomizerService.class.getClassLoader());
        for (ApiActionBuilderCustomizerService service :serviceLoader) {
            httpClientRequestActionBuilder = service.build(generatedApi, this, context, httpClientRequestActionBuilder);
        }
        return httpClientRequestActionBuilder;
    }

    private HttpClientRequestActionBuilder customizeByBeans(
        GeneratedApi generatedApi, TestContext context,
        HttpClientRequestActionBuilder httpClientRequestActionBuilder) {
        if (actionBuilderCustomizerServices != null) {
            for (ApiActionBuilderCustomizerService apiActionBuilderCustomizer : actionBuilderCustomizerServices) {
                httpClientRequestActionBuilder = apiActionBuilderCustomizer.build(generatedApi, this,
                context, httpClientRequestActionBuilder);
            }
        }
        return httpClientRequestActionBuilder;
    }
}
