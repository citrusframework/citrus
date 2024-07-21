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

package org.citrusframework.openapi.generator.sample;

import static org.springframework.util.CollectionUtils.isEmpty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ServiceLoader;
import javax.sql.DataSource;
import org.citrusframework.actions.SendMessageAction;
import org.citrusframework.context.TestContext;
import org.citrusframework.http.actions.HttpClientRequestActionBuilder;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.openapi.OpenApiSpecification;
import org.citrusframework.openapi.actions.OpenApiClientRequestActionBuilder;
import org.citrusframework.testapi.ApiActionBuilderCustomizerService;
import org.citrusframework.testapi.GeneratedApi;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@jakarta.annotation.Generated(value = "org.citrusframework.openapi.generator.JavaCitrusCodegen", date = "2024-07-20T08:47:39.378047600+02:00[Europe/Zurich]", comments = "Generator version: 7.5.0")
public abstract class PetStoreAbstractSendAction extends SendMessageAction {

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

    public static class Builder extends OpenApiClientRequestActionBuilder {

        // TODO: do we really need this?
        protected OpenApiSpecification openApiSpec;

        private final String path;

        private final Map<String, String> pathParameters = new HashMap<>();

        private final MultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();

        // TODO: can we just pass in the operation?
        public Builder(OpenApiSpecification openApiSpec, String method, String path, String operationName) {
            super(openApiSpec, "%s_%s".formatted(method, path));
            name(String.format("%s:%s", "PetStore".toLowerCase(), operationName));
            getMessageBuilderSupport().header("citrus_open_api_operation_name", operationName);
            getMessageBuilderSupport().header("citrus_open_api_method", method);
            getMessageBuilderSupport().header("citrus_open_api_path", path);

            this.openApiSpec = openApiSpec;
            this.path = path;
        }

        protected void pathParameter(String name, String value) {
            pathParameters.put(name, value);
        }

        protected void formData(String name, String value) {
            formData.add(name, value);
        }

        protected String qualifiedPath(String path) {

            String qualifiedPath = path;
            for (Entry<String, String> entry : pathParameters.entrySet()) {
                qualifiedPath = qualifiedPath.replace("{%s}".formatted(entry.getKey()), entry.getValue());
            }
            return qualifiedPath;
        }

        protected String toQueryParam(String...arrayElements) {
            return String.join(",", arrayElements);
        }

        @Override
        public SendMessageAction doBuild() {
            // TODO: register callback to modify builder
            path(qualifiedPath(path));
            if (!formData.isEmpty()) {
                // TODO: do we have to explicitly set the content type or is this done by citrus
                messageBuilderSupport.contentType(MediaType.MULTIPART_FORM_DATA_VALUE);
                getMessageBuilderSupport().body(formData);
            }
            return super.doBuild();
        }

    }
}
