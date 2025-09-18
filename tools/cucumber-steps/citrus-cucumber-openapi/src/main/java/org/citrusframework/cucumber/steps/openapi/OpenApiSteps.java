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

package org.citrusframework.cucumber.steps.openapi;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import io.apicurio.datamodels.openapi.models.OasDocument;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.Given;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.variable.dictionary.AbstractDataDictionary;
import org.citrusframework.variable.dictionary.json.JsonPathMappingDataDictionary;
import org.citrusframework.cucumber.steps.openapi.model.OasModelHelper;
import org.citrusframework.cucumber.util.ResourceUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class OpenApiSteps {

    @CitrusResource
    private TestContext context;

    static OasDocument openApiDoc;

    static String openApiUrl;

    static AbstractDataDictionary<String> outboundDictionary;
    static AbstractDataDictionary<String> inboundDictionary;

    static boolean generateOptionalFields = OpenApiSettings.isGenerateOptionalFields();
    static boolean validateOptionalFields = OpenApiSettings.isValidateOptionalFields();

    @Before
    public void before(Scenario scenario) {
        outboundDictionary = new JsonPathMappingDataDictionary();
        inboundDictionary = new JsonPathMappingDataDictionary();
    }

    @Given("^Disable OpenAPI generate optional fields$")
    public void disableGenerateOptionalFields() {
        generateOptionalFields = false;
    }

    @Given("^Enable OpenAPI generate optional fields$")
    public void enableGenerateOptionalFields() {
        generateOptionalFields = true;
    }

    @Given("^Disable OpenAPI validate optional fields$")
    public void disableValidateOptionalFields() {
        validateOptionalFields = false;
    }

    @Given("^Enable OpenAPI validate optional fields$")
    public void enableValidateOptionalFields() {
        validateOptionalFields = true;
    }

    @Given("^(?:OpenAPI URL|url): ([^\\s]+)$")
    public void setUrl(String url) {
        openApiUrl = url;
    }

    @Given("^OpenAPI (?:specification|resource): ([^\\s]+)$")
    public void loadOpenApiResource(String resource) {
        String location = context.replaceDynamicContentInString(resource);

        if (location.startsWith("http")) {
            try {
                URL url = new URL(location);
                if (location.startsWith("https")) {
                    openApiDoc = OpenApiResourceLoader.fromSecuredWebResource(url);
                } else {
                    openApiDoc = OpenApiResourceLoader.fromWebResource(url);
                }
                openApiUrl = String.format("%s://%s%s%s", url.getProtocol(), url.getHost(), url.getPort() > 0 ? ":" + url.getPort() : "", OasModelHelper.getBasePath(openApiDoc));
            } catch (MalformedURLException e) {
                throw new IllegalStateException("Failed to retrieve Open API specification as web resource: " + location, e);
            }
        } else {
            openApiDoc = OpenApiResourceLoader.fromFile(ResourceUtils.resolve(location, context));

            String schemeToUse = Optional.ofNullable(OasModelHelper.getSchemes(openApiDoc))
                    .orElse(Collections.singletonList("http"))
                    .stream()
                    .filter(s -> s.equals("http") || s.equals("https"))
                    .findFirst()
                    .orElse("http");

            openApiUrl = String.format("%s://%s%s", schemeToUse, OasModelHelper.getHost(openApiDoc), OasModelHelper.getBasePath(openApiDoc));
        }
    }

    @Given("^OpenAPI outbound dictionary$")
    public void createOutboundDictionary(DataTable dataTable) {
        Map<String, String> mappings = dataTable.asMap(String.class, String.class);
        for (Map.Entry<String, String> mapping : mappings.entrySet()) {
            outboundDictionary.getMappings().put(mapping.getKey(), mapping.getValue());
        }
    }

    @Given("^load OpenAPI outbound dictionary ([^\\s]+)$")
    public void createOutboundDictionary(String fileName) {
        addMappingsFromFile(fileName, outboundDictionary);
    }

    @Given("^OpenAPI inbound dictionary$")
    public void createInboundDictionary(DataTable dataTable) {
        Map<String, String> mappings = dataTable.asMap(String.class, String.class);
        for (Map.Entry<String, String> mapping : mappings.entrySet()) {
            inboundDictionary.getMappings().put(mapping.getKey(), mapping.getValue());
        }
    }

    @Given("^load OpenAPI inbound dictionary ([^\\s]+)$")
    public void createInboundDictionary(String fileName) {
        addMappingsFromFile(fileName, inboundDictionary);
    }

    /**
     * Read given file resource and add mappings to provided data dictionary.
     */
    private void addMappingsFromFile(String fileName, AbstractDataDictionary<?> dictionary) {
        try {
            Resource resource = new ClassPathResource(fileName);
            Properties properties = new Properties();
            properties.load(resource.getInputStream());

            for (Map.Entry<Object, Object> mapping : properties.entrySet()) {
                dictionary.getMappings().put(mapping.getKey().toString(), mapping.getValue().toString());
            }
        } catch (IOException e) {
            throw new CitrusRuntimeException(String.format("Failed to load dictionary from resource %s", fileName));
        }
    }
}
