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

package org.citrusframework.openapi.generator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.citrusframework.openapi.generator.exception.WsdlToOpenApiTransformationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import javax.wsdl.Binding;
import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.soap.SOAPOperation;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static com.fasterxml.jackson.databind.MapperFeature.SORT_PROPERTIES_ALPHABETICALLY;
import static java.lang.String.format;

/**
 * Transforms a WSDL specification into a simple OpenAPI specification for usage with the OpenApiGenerator.
 * <p>
 * This transformer primarily focuses on mapping WSDL bindings into OpenAPI operations.
 * It converts SOAP operations described in the WSDL into corresponding HTTP POST operations
 * in the OpenAPI format. However, it does not convert or map schema information, such as
 * types or message definitions, from the WSDL.
 * </p>
 *
 * <h3>WSDL to OpenAPI Mapping</h3>
 * The transformer processes the following WSDL elements and maps them to the OpenAPI specification:
 * <ul>
 *     <li><b>WSDL Bindings:</b> Mapped to OpenAPI paths and operations. Each binding operation is
 *         converted into a corresponding POST operation in OpenAPI.</li>
 *     <li><b>WSDL Operation Name:</b> The operation name in the WSDL is used as the operation ID
 *         in OpenAPI and forms part of the path in the OpenAPI specification.</li>
 *     <li><b>Binding Name:</b> The binding name (for example, "SoapBinding") is used to tag the operation
 *         in the OpenAPI specification, allowing operations to be grouped logically by their binding.</li>
 *     <li><b>WSDL Documentation:</b> If available, the documentation from the WSDL is extracted and used
 *         as the description for the OpenAPI operation. This provides human-readable documentation for
 *         each operation in the OpenAPI spec.</li>
 * </ul>
 * <p>
 * The following elements of the WSDL are <strong>not</strong> mapped to the OpenAPI specification:
 * <ul>
 *     <li><b>WSDL Types and Schema:</b> The schema and type definitions from the WSDL are not included in the
 *         resulting OpenAPI specification. This transformer focuses solely on operations, not data models.</li>
 *     <li><b>WSDL Messages:</b> The message parts (input/output) associated with operations are not included
 *         in the OpenAPI output. This transformation only extracts the operations without message payload details.</li>
 * </ul>
 *
 * <h3>Usage Example</h3>
 * <pre>
 * {@code
 * URI wsdlUri = new URI("http://example.com/my-service.wsdl");
 * SimpleWsdlToOpenApiTransformer transformer = new SimpleWsdlToOpenApiTransformer(wsdlUri);
 * String openApiYaml = transformer.transformToOpenApi();
 * System.out.println(openApiYaml);
 * }
 * </pre>
 *
 * @see io.swagger.v3.oas.models.OpenAPI
 * @see io.swagger.v3.oas.models.Operation
 * @see javax.wsdl.Definition
 * @see javax.wsdl.Binding
 * @see javax.wsdl.BindingOperation
 */
public class WsdlToOpenApiTransformer {

    private static final Logger logger = LoggerFactory.getLogger(WsdlToOpenApiTransformer.class);

    private static final YAMLMapper yamlMapper = (YAMLMapper) YAMLMapper.builder()
            .enable(SORT_PROPERTIES_ALPHABETICALLY)
            .build()
            .setSerializationInclusion(NON_NULL);

    private final URI wsdlUri;

    public WsdlToOpenApiTransformer(URI wsdlUri) {
        this.wsdlUri = wsdlUri;
    }

    /**
     * Transforms the wsdl of this transformer into a OpenApi yaml representation.
     *
     * @return the OpenApi yaml
     * @throws WsdlToOpenApiTransformationException if the parsing fails
     */
    public String transformToOpenApi() throws WsdlToOpenApiTransformationException {
        try {
            Definition wsdlDefinition = readWSDL();
            Map<?, ?> bindings = wsdlDefinition.getBindings();
            OpenAPI openAPI = transformToOpenApi(bindings);
            return convertToYaml(openAPI);
        } catch (Exception e) {
            throw new WsdlToOpenApiTransformationException("Unable to parse wsdl", e);
        }
    }

    /**
     * Performs the actual transformation from bindings into OpenApi operations.
     */
    private OpenAPI transformToOpenApi(Map<?, ?> bindings) {
        OpenAPI openAPI = new OpenAPI();
        openAPI.setInfo(createInfo());

        Paths paths = new Paths();
        openAPI.setPaths(paths);
        for (Entry<?, ?> entry : bindings.entrySet()) {
            Object key = entry.getKey();
            Object value = entry.getValue();

            if (key instanceof QName qName && value instanceof Binding binding) {
                addOperations(openAPI, qName, binding);
            }
        }

        return openAPI;
    }

    private Definition readWSDL() throws WSDLException {
        logger.debug("Reading wsdl file from path: {}", wsdlUri);

        WSDLReader reader = WSDLFactory.newInstance().newWSDLReader();

        // switch off the verbose mode
        reader.setFeature("javax.wsdl.verbose", false);
        reader.setFeature("javax.wsdl.importDocuments", true);

        if (logger.isDebugEnabled()) {
            logger.debug("Reading  the WSDL. Base uri is {}", wsdlUri);
        }

        return reader.readWSDL(wsdlUri.toString());
    }

    private Info createInfo() {
        Info info = new Info();
        info.setTitle("Generated api from wsdl");

        info.setDescription(
                format(
                        "This api has been generated from the following wsdl '%s'. It's purpose is solely to serve as input for SOAP API generation. Note that only operations are extracted from the WSDL. No schema information whatsoever is generated!",
                        java.nio.file.Paths.get(wsdlUri).getFileName()
                )
        );
        info.setVersion("1.0.0");

        Contact contact = new Contact();
        contact.setName("org.citrusframework.openapi.generator.SimpleWsdlToOpenApiTransformer");
        info.setContact(contact);

        return info;
    }

    private void addOperations(OpenAPI openApi, QName qName, Binding binding) {
        String localPart = qName.getLocalPart();

        String bindingApiName;
        if (localPart.endsWith("SoapBinding")) {
            bindingApiName = localPart.substring(0, localPart.length() - "SoapBinding".length());
        } else {
            bindingApiName = localPart;
        }
        List<?> bindingOperations = binding.getBindingOperations();
        for (Object operation : bindingOperations) {
            if (operation instanceof BindingOperation bindingOperation) {
                addOperation(
                        openApi.getPaths(),
                        bindingOperation.getName(),
                        retrieveOperationDescription(bindingOperation),
                        retrieveSoapAction(bindingOperation),
                        bindingApiName
                );
            }
        }
    }

    private void addOperation(Paths paths, String name, String description, String soapAction, String tag) {
        Operation postOperation = new Operation();

        logger.debug("Adding operation to spec: {}", name);

        postOperation.setOperationId(name);
        postOperation.setDescription(description);
        postOperation.setSummary(soapAction);
        postOperation.tags(Collections.singletonList(tag));
        ApiResponses responses = new ApiResponses();
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setDescription("Generic Response");
        responses.addApiResponse("default", apiResponse);
        postOperation.responses(responses);

        PathItem pi = new PathItem();
        pi.setPost(postOperation);

        paths.addPathItem("/" + name, pi);
    }

    /**
     * Retrieve the description of the bindingOperation via the documentation of the associated operation.
     */
    private String retrieveOperationDescription(BindingOperation bindingOperation) {
        StringBuilder description = new StringBuilder();
        javax.wsdl.Operation soapOperation = bindingOperation.getOperation();
        Element documentationElement = bindingOperation.getDocumentationElement();
        if (documentationElement != null) {
            String documentationText = documentationElement.getTextContent().trim();
            description.append(format("%s", documentationText));
        }

        if (soapOperation != null) {
            documentationElement = soapOperation.getDocumentationElement();
            if (documentationElement != null) {
                String documentationText = documentationElement.getTextContent().trim();
                if (!description.isEmpty()) {
                    description.append(" ");
                }
                description.append(format("%s", documentationText));
            }
        }

        return description.toString();
    }

    /**
     * Retrieve the soap action.
     */
    private String retrieveSoapAction(BindingOperation bindingOperation) {
        String soapAction = "";

        List<?> extensibilityElements = bindingOperation.getExtensibilityElements();
        for (Object element : extensibilityElements) {
            if (element instanceof SOAPOperation soapOperation) {
                soapAction = soapOperation.getSoapActionURI();
            }
        }

        return soapAction;
    }

    private String convertToYaml(OpenAPI openAPI) throws JsonProcessingException {
        return yamlMapper.writeValueAsString(openAPI);
    }
}
