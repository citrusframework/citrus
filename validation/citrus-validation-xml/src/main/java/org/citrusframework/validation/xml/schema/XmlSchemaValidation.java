package org.citrusframework.validation.xml.schema;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.citrusframework.XmlValidationHelper;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.message.Message;
import org.citrusframework.spi.Resource;
import org.citrusframework.spi.Resources;
import org.citrusframework.util.IsXmlPredicate;
import org.citrusframework.util.StringUtils;
import org.citrusframework.util.XMLUtils;
import org.citrusframework.validation.SchemaValidator;
import org.citrusframework.validation.xml.XmlMessageValidationContext;
import org.citrusframework.xml.XsdSchemaRepository;
import org.citrusframework.xml.schema.AbstractSchemaCollection;
import org.citrusframework.xml.schema.WsdlXsdSchema;
import org.citrusframework.xml.schema.XsdSchemaCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.xml.validation.XmlValidator;
import org.springframework.xml.validation.XmlValidatorFactory;
import org.springframework.xml.xsd.XsdSchema;
import org.w3c.dom.Document;
import org.xml.sax.SAXParseException;

public class XmlSchemaValidation implements SchemaValidator<XmlMessageValidationContext> {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(XmlSchemaValidation.class);

    /** Transformer factory */
    private final TransformerFactory transformerFactory = TransformerFactory.newInstance();

    /**
     * Validate message with an XML schema.
     *
     * @param message
     * @param context
     * @param validationContext
     */
    @Override
    public void validate(Message message, TestContext context, XmlMessageValidationContext validationContext) {
        validateSchema(message, context, validationContext);
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

            logger.debug("Starting XML schema validation ...");

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
                logger.warn("Neither schema instance nor schema repository defined - skipping XML schema validation");
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
                            schemas.add(Resources.create(bos.toByteArray()));
                        }
                    }
                }

                validator = XmlValidatorFactory.createValidator(schemas
                        .stream()
                        .map(AbstractSchemaCollection::toSpringResource)
                        .toList()
                        .toArray(new org.springframework.core.io.Resource[]{}), WsdlXsdSchema.W3C_XML_SCHEMA_NS_URI);
            }

            SAXParseException[] results = validator.validate(new DOMSource(doc));
            if (results.length == 0) {
                logger.info("XML schema validation successful: All values OK");
            } else {
                logger.error("XML schema validation failed for message:\n" + XMLUtils.prettyPrint(message.getPayload(String.class)));

                // Report all parsing errors
                logger.debug("Found " + results.length + " schema validation errors");
                StringBuilder errors = new StringBuilder();
                for (SAXParseException e : results) {
                    errors.append(e.toString());
                    errors.append("\n");
                }
                logger.debug(errors.toString());

                throw new ValidationException("XML schema validation failed:", results[0]);
            }
        } catch (IOException e) {
            throw new CitrusRuntimeException(e);
        }
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
