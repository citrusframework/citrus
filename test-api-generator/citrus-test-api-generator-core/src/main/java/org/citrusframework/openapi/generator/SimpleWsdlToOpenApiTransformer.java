package org.citrusframework.openapi.generator;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static com.fasterxml.jackson.databind.MapperFeature.SORT_PROPERTIES_ALPHABETICALLY;
import static java.lang.String.format;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.responses.ApiResponses;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.wsdl.Binding;
import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;
import org.citrusframework.openapi.generator.exception.WsdlToOpenApiTransformationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

/**
 * Transforms a wsdl specification into a simple OpenApi specification for usage with the OpenApiGenerator.
 * <p>
 *
 * Note that this transformer only transforms bindings from the wsdl into operations in the OpenApi.
 */
public class SimpleWsdlToOpenApiTransformer {

    private static final Logger logger = LoggerFactory.getLogger(SimpleWsdlToOpenApiTransformer.class);

    private static final YAMLMapper yamlMapper = (YAMLMapper) YAMLMapper.builder()
        .enable(SORT_PROPERTIES_ALPHABETICALLY)
        .build()
        .setSerializationInclusion(NON_NULL);

    private final URI wsdlUri;

    public SimpleWsdlToOpenApiTransformer(URI wsdlUri) {
        this.wsdlUri = wsdlUri;
    }

    /**
     * Transforms the wsdl of this transfromer into a OpenApi yaml representation.
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
     *
     * @param bindings
     * @return
     */
    private OpenAPI transformToOpenApi(Map<?, ?> bindings) {
        OpenAPI openAPI = new OpenAPI();
        openAPI.setInfo(createInfo());

        Paths paths = new Paths();
        openAPI.setPaths(paths);
        for (Entry<?, ?> entry : bindings.entrySet()) {
            Object key = entry.getKey();
            Object value = entry.getValue();

            if (key instanceof QName && value instanceof Binding) {
                addOperations(openAPI, (QName) key, (Binding) value);
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
                    bindingApiName
                );
            }
        }
    }

    private void addOperation(Paths paths, String name, String description, String tag) {
        Operation postOperation = new Operation();

        logger.debug("Adding operation to spec: {}", name);

        postOperation.setOperationId(name);
        postOperation.setDescription(description);
        postOperation.tags(Collections.singletonList(tag));
        ApiResponses responses = new ApiResponses();
        postOperation.responses(responses);

        PathItem pi = new PathItem();
        pi.setPost(postOperation);

        paths.addPathItem("/" + name, pi);
    }

    /**
     * Retrieve the description of the bindingOperation via the documentation of the associated operation.
     */
    private String retrieveOperationDescription(BindingOperation bindingOperation) {
        String description = "";
        javax.wsdl.Operation soapOperation = bindingOperation.getOperation();
        if (soapOperation != null) {
            Element documentationElement = soapOperation.getDocumentationElement();
            if (documentationElement != null) {
                description = documentationElement.getTextContent();
            }
        }

        return description;
    }

    private String convertToYaml(OpenAPI openAPI) throws JsonProcessingException {
        return yamlMapper.writeValueAsString(openAPI);
    }
}
