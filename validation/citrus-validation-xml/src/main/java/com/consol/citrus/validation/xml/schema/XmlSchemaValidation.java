package com.consol.citrus.validation.xml.schema;

import com.consol.citrus.XmlValidationHelper;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.exceptions.ValidationException;
import com.consol.citrus.message.Message;
import com.consol.citrus.util.IsJsonPredicate;
import com.consol.citrus.util.IsXmlPredicate;
import com.consol.citrus.util.XMLUtils;
import com.consol.citrus.validation.SchemaValidator;
import com.consol.citrus.validation.xml.XmlMessageValidationContext;
import com.consol.citrus.xml.XsdSchemaRepository;
import com.consol.citrus.xml.schema.WsdlXsdSchema;
import com.consol.citrus.xml.schema.XsdSchemaCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;
import org.springframework.xml.validation.XmlValidator;
import org.springframework.xml.validation.XmlValidatorFactory;
import org.springframework.xml.xsd.XsdSchema;
import org.w3c.dom.Document;
import org.xml.sax.SAXParseException;

import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class XmlSchemaValidation implements SchemaValidator<XmlMessageValidationContext> {

    /** Logger */
    private Logger log = LoggerFactory.getLogger(XmlSchemaValidation.class);

    /** Transformer factory */
    private final TransformerFactory transformerFactory = TransformerFactory.newInstance();

    /**
     * Validate message with a XML schema.
     *
     * @param message
     * @param context
     * @param validationContext
     */
    @Override
    public void validate(Message message, TestContext context, XmlMessageValidationContext validationContext) {
        validateSchema(message, context, validationContext);
        validateDTD(validationContext.getDTDResource(), message);
    }

    private void validateSchema(Message message, TestContext context, XmlMessageValidationContext validationContext) {
        if (message.getPayload() == null || !StringUtils.hasText(message.getPayload(String.class))) {
            return;
        }

        try {
            Document doc = XMLUtils.parseMessagePayload(message.getPayload(String.class));

            if (!StringUtils.hasText(doc.getFirstChild().getNamespaceURI())) {
                return;
            }

            log.debug("Starting XML schema validation ...");

            XmlValidator validator = null;
            XsdSchemaRepository schemaRepository = null;
            List<XsdSchemaRepository> schemaRepositories = XmlValidationHelper.getSchemaRepositories(context);
            if (validationContext.getSchema() != null) {
                validator = context.getReferenceResolver().resolve(validationContext.getSchema(), XsdSchema.class).createValidator();
            } else if (validationContext.getSchemaRepository() != null) {
                schemaRepository = context.getReferenceResolver().resolve(validationContext.getSchemaRepository(), XsdSchemaRepository.class);
            } else if (schemaRepositories.size() == 1) {
                schemaRepository = schemaRepositories.get(0);
            } else if (schemaRepositories.size() > 0) {
                schemaRepository = schemaRepositories.stream().filter(repository -> repository.canValidate(doc)).findFirst().orElseThrow(() -> new CitrusRuntimeException(String.format("Failed to find proper schema " + "repository for validating element '%s(%s)'", doc.getFirstChild().getLocalName(), doc.getFirstChild().getNamespaceURI())));
            } else {
                log.warn("Neither schema instance nor schema repository defined - skipping XML schema validation");
                return;
            }

            if (schemaRepository != null) {
                if (!schemaRepository.canValidate(doc)) {
                    throw new CitrusRuntimeException(String.format("Unable to find proper XML schema definition for element '%s(%s)' in schema repository '%s'", doc.getFirstChild().getLocalName(), doc.getFirstChild().getNamespaceURI(), schemaRepository.getName()));
                }

                List<Resource> schemas = new ArrayList<>();
                for (XsdSchema xsdSchema : schemaRepository.getSchemas()) {
                    if (xsdSchema instanceof XsdSchemaCollection) {
                        schemas.addAll(((XsdSchemaCollection) xsdSchema).getSchemaResources());
                    } else if (xsdSchema instanceof WsdlXsdSchema) {
                        schemas.addAll(((WsdlXsdSchema) xsdSchema).getSchemaResources());
                    } else {
                        synchronized (transformerFactory) {
                            ByteArrayOutputStream bos = new ByteArrayOutputStream();
                            try {
                                transformerFactory.newTransformer().transform(xsdSchema.getSource(), new StreamResult(bos));
                            } catch (TransformerException e) {
                                throw new CitrusRuntimeException("Failed to read schema " + xsdSchema.getTargetNamespace(), e);
                            }
                            schemas.add(new ByteArrayResource(bos.toByteArray()));
                        }
                    }
                }

                validator = XmlValidatorFactory.createValidator(schemas.toArray(new Resource[schemas.size()]), WsdlXsdSchema.W3C_XML_SCHEMA_NS_URI);
            }

            SAXParseException[] results = validator.validate(new DOMSource(doc));
            if (results.length == 0) {
                log.info("XML schema validation successful: All values OK");
            } else {
                log.error("XML schema validation failed for message:\n" + XMLUtils.prettyPrint(message.getPayload(String.class)));

                // Report all parsing errors
                log.debug("Found " + results.length + " schema validation errors");
                StringBuilder errors = new StringBuilder();
                for (SAXParseException e : results) {
                    errors.append(e.toString());
                    errors.append("\n");
                }
                log.debug(errors.toString());

                throw new ValidationException("XML schema validation failed:", results[0]);
            }
        } catch (IOException e) {
            throw new CitrusRuntimeException(e);
        }
    }

    /**
     * Validate message with a DTD.
     *
     * @param dtdResource
     * @param receivedMessage
     */
    protected void validateDTD(Resource dtdResource, Message receivedMessage) {
        //TODO implement this
    }

    /**
     *
     * @param messageType
     * @param message
     * @return true if the message or message type is supported by this validator
     */
    @Override
    public boolean supportsMessageType(String messageType, Message message) {
        return "XML".equals(messageType) || (message != null && IsXmlPredicate.getInstance().test(message.getPayload(String.class)));
    }
}
