package com.consol.citrus.dsl.builder;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.consol.citrus.AbstractTestActionBuilder;
import com.consol.citrus.CitrusSettings;
import com.consol.citrus.actions.ReceiveMessageAction;
import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.Message;
import com.consol.citrus.message.MessageType;
import com.consol.citrus.spi.ReferenceResolver;
import com.consol.citrus.spi.ReferenceResolverAware;
import com.consol.citrus.util.FileUtils;
import com.consol.citrus.validation.HeaderValidator;
import com.consol.citrus.validation.MessageValidator;
import com.consol.citrus.validation.builder.DefaultMessageContentBuilder;
import com.consol.citrus.validation.callback.ValidationCallback;
import com.consol.citrus.validation.context.ValidationContext;
import com.consol.citrus.validation.json.JsonMessageValidationContext;
import com.consol.citrus.validation.json.JsonPathMessageValidationContext;
import com.consol.citrus.validation.script.ScriptValidationContext;
import com.consol.citrus.validation.xml.XmlMessageValidationContext;
import com.consol.citrus.validation.xml.XmlNamespaceAware;
import com.consol.citrus.validation.xml.XpathMessageValidationContext;
import com.consol.citrus.variable.VariableExtractor;
import com.consol.citrus.variable.dictionary.DataDictionary;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.Resource;
import org.springframework.oxm.Marshaller;

import static com.consol.citrus.validation.json.JsonPathVariableExtractor.Builder.jsonPathExtractor;
import static com.consol.citrus.validation.xml.XpathPayloadVariableExtractor.Builder.xpathExtractor;
import static com.consol.citrus.variable.MessageHeaderVariableExtractor.Builder.headerValueExtractor;

/**
 * @author Christoph Deppisch
 */
public class ReceiveMessageActionBuilder<B extends ReceiveMessageActionBuilder<B>> extends AbstractTestActionBuilder<ReceiveMessageAction, B> implements ReferenceResolverAware {

    protected final B self;

    private final ReceiveMessageAction.ReceiveMessageActionBuilder<?, ?> delegate;

    private String messageType = CitrusSettings.DEFAULT_MESSAGE_TYPE;

    private final Map<String, String> namespaces = new HashMap<>();

    private XmlMessageValidationContext.XmlValidationContextBuilder<?, ?> xmlMessageValidationContext;
    private JsonMessageValidationContext.Builder jsonMessageValidationContext;

    /** JSON validation context used in this action builder */
    private JsonPathMessageValidationContext.Builder jsonPathValidationContext;

    /** Script validation context used in this action builder */
    private ScriptValidationContext.Builder scriptValidationContext;

    public ReceiveMessageActionBuilder(ReceiveMessageAction.ReceiveMessageActionBuilder<?, ?> builder) {
        this.self = (B) this;
        this.delegate = builder;
    }

    /**
     * Sets the message endpoint to receive messages from.
     *
     * @param messageEndpoint
     * @return
     */
    public B endpoint(final Endpoint messageEndpoint) {
        delegate.endpoint(messageEndpoint);
        return self;
    }

    /**
     * Sets the message endpoint uri to receive messages from.
     *
     * @param messageEndpointUri
     * @return
     */
    public B endpoint(final String messageEndpointUri) {
        delegate.endpoint(messageEndpointUri);
        return self;
    }

    /**
     * Adds a custom timeout to this message receiving action.
     *
     * @param receiveTimeout
     * @return
     */
    public B timeout(final long receiveTimeout) {
        delegate.timeout(receiveTimeout);
        return self;
    }

    /**
     * Sets the message builder to use.
     * @param messageBuilder
     * @return
     */
    public B messageBuilder(DefaultMessageContentBuilder messageBuilder) {
        delegate.message(messageBuilder);
        return self;
    }

    /**
     * Expect a control message in this receive action.
     *
     * @param controlMessage
     * @return
     */
    public B message(final Message controlMessage) {
        delegate.message(controlMessage);
        return self;
    }

    /**
     * Sets the message name.
     *
     * @param name
     * @return
     */
    public B messageName(final String name) {
        delegate.messageName(name);
        return self;
    }

    /**
     * Expect this message payload data in received message.
     *
     * @param payload
     * @return
     */
    public B payload(final String payload) {
        delegate.payload(payload);
        return self;
    }

    /**
     * Expect this message payload data in received message.
     *
     * @param payloadResource
     * @return
     */
    public B payload(final Resource payloadResource) {
        return payload(payloadResource, FileUtils.getDefaultCharset());
    }

    /**
     * Expect this message payload data in received message.
     *
     * @param payloadResource
     * @param charset
     * @return
     */
    public B payload(final Resource payloadResource, final Charset charset) {
        delegate.payload(payloadResource, charset);
        return self;
    }

    /**
     * Expect this message payload as model object which is marshalled to a character sequence
     * using the default object to xml mapper before validation is performed.
     *
     * @param payload
     * @param marshaller
     * @return
     */
    public B payload(final Object payload, final Marshaller marshaller) {
        delegate.payload(payload, marshaller);
        return self;
    }

    /**
     * Expect this message payload as model object which is mapped to a character sequence
     * using the default object to json mapper before validation is performed.
     *
     * @param payload
     * @param objectMapper
     * @return
     */
    public B payload(final Object payload, final ObjectMapper objectMapper) {
        delegate.payload(payload, objectMapper);
        return self;
    }

    /**
     * Expect this message payload as model object which is marshalled to a character sequence using the default object to xml mapper that
     * is available in Spring bean application context.
     *
     * @param payload
     * @return
     */
    public B payloadModel(final Object payload) {
        delegate.payloadModel(payload);
        return self;
    }

    /**
     * Expect this message payload as model object which is marshalled to a character sequence using the given object to xml mapper that
     * is accessed by its bean name in Spring bean application context.
     *
     * @param payload
     * @param mapperName
     * @return
     */
    public B payload(final Object payload, final String mapperName) {
        delegate.payload(payload, mapperName);
        return self;
    }

    /**
     * Expect this message header entry in received message.
     *
     * @param name
     * @param value
     * @return
     */
    public B header(final String name, final Object value) {
        delegate.header(name, value);
        return self;
    }

    /**
     * Expect this message header entries in received message.
     *
     * @param headers
     * @return
     */
    public B headers(final Map<String, Object> headers) {
        delegate.headers(headers);
        return self;
    }

    /**
     * Expect this message header data in received message. Message header data is used in
     * SOAP messages as XML fragment for instance.
     *
     * @param data
     * @return
     */
    public B header(final String data) {
        delegate.header(data);
        return self;
    }

    /**
     * Expect this message header data as model object which is marshalled to a character sequence using the default object to xml mapper that
     * is available in Spring bean application context.
     *
     * @param model
     * @return
     */
    public B headerFragment(final Object model) {
        delegate.headerFragment(model);
        return self;
    }

    /**
     * Expect this message header data as model object which is marshalled to a character sequence using the given object to xml mapper that
     * is accessed by its bean name in Spring bean application context.
     *
     * @param model
     * @param mapperName
     * @return
     */
    public B headerFragment(final Object model, final String mapperName) {
        delegate.headerFragment(model, mapperName);
        return self;
    }

    /**
     * Expect this message header data as model object which is marshalled to a character sequence
     * using the default object to xml mapper before validation is performed.
     *
     * @param model
     * @param marshaller
     * @return
     */
    public B headerFragment(final Object model, final Marshaller marshaller) {
        delegate.headerFragment(model, marshaller);
        return self;
    }

    /**
     * Expect this message header data as model object which is mapped to a character sequence
     * using the default object to json mapper before validation is performed.
     *
     * @param model
     * @param objectMapper
     * @return
     */
    public B headerFragment(final Object model, final ObjectMapper objectMapper) {
        delegate.headerFragment(model, objectMapper);
        return self;
    }

    /**
     * Expect this message header data in received message from file resource. Message header data is used in
     * SOAP messages as XML fragment for instance.
     *
     * @param resource
     * @return
     */
    public B header(final Resource resource) {
        return header(resource, FileUtils.getDefaultCharset());
    }

    /**
     * Expect this message header data in received message from file resource. Message header data is used in
     * SOAP messages as XML fragment for instance.
     *
     * @param resource
     * @param charset
     * @return
     */
    public B header(final Resource resource, final Charset charset) {
        delegate.header(resource, charset);
        return self;
    }

    /**
     * Validate header names with case insensitive keys.
     *
     * @param value
     * @return
     */
    public B headerNameIgnoreCase(final boolean value) {
        delegate.headerNameIgnoreCase(value);
        return self;
    }

    /**
     * Adds script validation.
     *
     * @param validationScript
     * @return
     */
    public B validateScript(final String validationScript) {
        getScriptValidationContext().script(validationScript);
        return self;
    }

    /**
     * Reads validation script file resource and sets content as validation script.
     *
     * @param scriptResource
     * @return
     */
    public B validateScript(final Resource scriptResource) {
        return validateScript(scriptResource, FileUtils.getDefaultCharset());
    }

    /**
     * Reads validation script file resource and sets content as validation script.
     *
     * @param scriptResource
     * @param charset
     * @return
     */
    public B validateScript(final Resource scriptResource, final Charset charset) {
        try {
            validateScript(FileUtils.readToString(scriptResource, charset));
        } catch (final IOException e) {
            throw new CitrusRuntimeException("Failed to read script resource file", e);
        }
        return self;
    }

    /**
     * Adds script validation file resource.
     *
     * @param fileResourcePath
     * @return
     */
    public B validateScriptResource(final String fileResourcePath) {
        getScriptValidationContext().scriptResource(fileResourcePath);
        return self;
    }

    /**
     * Adds custom validation script type.
     *
     * @param type
     * @return
     */
    public B validateScriptType(final String type) {
        getScriptValidationContext().scriptType(type);
        return self;
    }

    /**
     * Sets a explicit message type for this receive action.
     *
     * @param messageType
     * @return
     */
    public B messageType(final MessageType messageType) {
        messageType(messageType.name());
        return self;
    }

    /**
     * Sets a explicit message type for this receive action.
     *
     * @param messageType
     * @return
     */
    public B messageType(final String messageType) {
        this.messageType = messageType;
        delegate.messageType(messageType);

        if (MessageType.JSON.name().equalsIgnoreCase(messageType)) {
            getJsonMessageValidationContext();
        }

        if (MessageType.XML.name().equalsIgnoreCase(messageType)
                || MessageType.XHTML.name().equalsIgnoreCase(messageType)) {
            getXmlMessageValidationContext();
        }

        return self;
    }

    /**
     * Adds a validation context.
     * @param validationContext
     * @return
     */
    public B validationContext(final ValidationContext.Builder<?, ?> validationContext) {
        this.delegate.validate(validationContext);
        return self;
    }

    /**
     * Adds a validation context.
     * @param validationContext
     * @return
     */
    public B validationContext(final ValidationContext validationContext) {
        this.delegate.validate(validationContext);
        return self;
    }

    /**
     * Sets validation contexts.
     * @param validationContexts
     * @return
     */
    public B validationContexts(final List<ValidationContext> validationContexts) {
        validationContexts.forEach(this::validationContext);
        return self;
    }

    /**
     * Sets schema validation enabled/disabled for this message.
     *
     * @param enabled
     * @return
     */
    public B schemaValidation(final boolean enabled) {
        getXmlMessageValidationContext().schemaValidation(enabled);
        getJsonMessageValidationContext().schemaValidation(enabled);
        return self;
    }

    /**
     * Validates XML namespace with prefix and uri.
     *
     * @param prefix
     * @param namespaceUri
     * @return
     */
    public B validateNamespace(final String prefix, final String namespaceUri) {
        getXmlMessageValidationContext().namespace(prefix, namespaceUri);
        return self;
    }

    /**
     * Adds message element validation.
     *
     * @param path
     * @param controlValue
     * @return
     */
    public B validate(final String path, final Object controlValue) {
        if (JsonPathMessageValidationContext.isJsonPathExpression(path)) {
            getJsonPathValidationContext().expression(path, controlValue);
        } else {
            getXPathValidationContext().expression(path, controlValue);
        }
        return self;
    }

    /**
     * Adds the given map of paths with their corresponding control values for validation.
     *
     * @param map Map of paths with control values
     * @return The modified builder
     */
    public B validate(final Map<String, Object> map) {
        map.forEach(this::validate);
        return self;
    }

    /**
     * Adds ignore path expression for message element.
     *
     * @param path
     * @return
     */
    public B ignore(final String path) {
        if (messageType.equalsIgnoreCase(MessageType.XML.name())
                || messageType.equalsIgnoreCase(MessageType.XHTML.name())) {
            getXmlMessageValidationContext().ignore(path);
        } else if (messageType.equalsIgnoreCase(MessageType.JSON.name())) {
            getJsonMessageValidationContext().ignore(path);
        }
        return self;
    }

    /**
     * Adds XPath message element validation.
     *
     * @param xPathExpression
     * @param controlValue
     * @return
     */
    public B xpath(final String xPathExpression, final Object controlValue) {
        getXPathValidationContext().expression(xPathExpression, controlValue);
        return self;
    }

    /**
     * Adds JsonPath message element validation.
     *
     * @param jsonPathExpression
     * @param controlValue
     * @return
     */
    public B jsonPath(final String jsonPathExpression, final Object controlValue) {
        getJsonPathValidationContext().expression(jsonPathExpression, controlValue);
        return self;
    }

    /**
     * Sets explicit schema instance name to use for schema validation.
     *
     * @param schemaName
     * @return
     */
    public B xsd(final String schemaName) {
        getXmlMessageValidationContext().schema(schemaName);
        return self;
    }

    /**
     * Sets explicit schema instance name to use for schema validation.
     *
     * @param schemaName The name of the schema bean
     */
    public B jsonSchema(final String schemaName) {
        getJsonMessageValidationContext().schema(schemaName);
        return self;
    }

    /**
     * Sets explicit xsd schema repository instance to use for validation.
     *
     * @param schemaRepository
     * @return
     */
    public B xsdSchemaRepository(final String schemaRepository) {
        getXmlMessageValidationContext().schemaRepository(schemaRepository);
        return self;
    }

    /**
     * Sets explicit json schema repository instance to use for validation.
     *
     * @param schemaRepository The name of the schema repository bean
     * @return
     */
    public B jsonSchemaRepository(final String schemaRepository) {
        getJsonMessageValidationContext().schemaRepository(schemaRepository);
        return self;
    }

    /**
     * Adds explicit namespace declaration for later path validation expressions.
     *
     * @param prefix
     * @param namespaceUri
     * @return
     */
    public B namespace(final String prefix, final String namespaceUri) {
        namespaces.put(prefix, namespaceUri);
        return self;
    }

    /**
     * Sets default namespace declarations on this action builder.
     *
     * @param namespaceMappings
     * @return
     */
    public B namespaces(final Map<String, String> namespaceMappings) {
        namespaces.putAll(namespaceMappings);
        return self;
    }

    /**
     * Sets message selector string.
     *
     * @param messageSelector
     * @return
     */
    public B selector(final String messageSelector) {
        delegate.selector(messageSelector);
        return self;
    }

    /**
     * Sets message selector elements.
     *
     * @param messageSelector
     * @return
     */
    public B selector(final Map<String, String> messageSelector) {
        this.delegate.selector(messageSelector);
        return self;
    }

    /**
     * Extract message header entry as variable.
     *
     * @param headerName
     * @param variable
     * @return
     */
    public B extractFromHeader(final String headerName, final String variable) {
        variableExtractor(headerValueExtractor()
                    .header(headerName, variable)
                .build());
        return self;
    }

    /**
     * Extract message element via XPath or JSONPath from message payload as new test variable.
     *
     * @param path
     * @param variable
     * @return
     */
    public B extractFromPayload(final String path, final String variable) {
        if (JsonPathMessageValidationContext.isJsonPathExpression(path)) {
            variableExtractor(jsonPathExtractor()
                    .expression(path, variable)
                .build());
        } else {
            variableExtractor(xpathExtractor()
                    .expression(path, variable)
                .build());
        }
        return self;
    }

    /**
     * Sets explicit message validators for this receive action.
     *
     * @param validator
     * @return
     */
    public B validator(final MessageValidator<? extends ValidationContext> validator) {
        this.delegate.validator(validator);
        return self;
    }

    /**
     * Sets explicit message validators for this receive action.
     *
     * @param validators
     * @return
     */
    public B validators(MessageValidator<? extends ValidationContext>... validators) {
        return validators(Arrays.asList(validators));
    }

    /**
     * Sets explicit message validators for this receive action.
     *
     * @param validators
     * @return
     */
    public B validators(final List<MessageValidator<? extends ValidationContext>> validators) {
        this.delegate.validators(validators);
        return self;
    }

    /**
     * Sets explicit message validators by name.
     *
     * @param validatorNames
     * @return
     */
    public B validator(final String... validatorNames) {
        this.delegate.validator(validatorNames);
        return self;
    }

    /**
     * Sets explicit header validator for this receive action.
     *
     * @param validators
     * @return
     */
    public B headerValidator(final HeaderValidator... validators) {
        this.delegate.validator(validators);
        return self;
    }

    /**
     * Sets explicit header validators by name.
     *
     * @param validatorNames
     * @return
     */
    public B headerValidator(final String... validatorNames) {
        this.delegate.validator(validatorNames);
        return self;
    }

    /**
     * Sets explicit data dictionary for this receive action.
     *
     * @param dictionary
     * @return
     */
    public B dictionary(final DataDictionary<?> dictionary) {
        this.delegate.dictionary(dictionary);
        return self;
    }

    /**
     * Sets explicit data dictionary by name.
     *
     * @param dictionaryName
     * @return
     */
    public B dictionary(final String dictionaryName) {
        this.delegate.dictionary(dictionaryName);
        return self;
    }

    /**
     * Adds validation callback to the receive action for validating
     * the received message with Java code.
     *
     * @param callback
     * @return
     */
    public B validationCallback(final ValidationCallback callback) {
        this.delegate.validationCallback(callback);
        return self;
    }

    /**
     * Adds variable extractor.
     * @param extractor
     * @return
     */
    public B variableExtractor(VariableExtractor extractor) {
        if (extractor instanceof XmlNamespaceAware) {
            ((XmlNamespaceAware) extractor).setNamespaces(namespaces);
        }

        this.delegate.process(extractor);
        return self;
    }

    /**
     * Adds variable extractor builder.
     * @param builder
     * @return
     */
    public B variableExtractor(VariableExtractor.Builder<?, ?> builder) {
        return variableExtractor(builder.build());
    }

    /**
     * Sets the bean reference resolver.
     *
     * @param referenceResolver
     */
    public B withReferenceResolver(final ReferenceResolver referenceResolver) {
        this.delegate.withReferenceResolver(referenceResolver);
        return self;
    }

    @Override
    public void setReferenceResolver(ReferenceResolver referenceResolver) {
        delegate.setReferenceResolver(referenceResolver);
    }

    @Override
    public final ReceiveMessageAction build() {
        delegate.getValidationContextBuilders().stream()
                .filter(context -> context instanceof XmlNamespaceAware)
                .map(XmlNamespaceAware.class::cast)
                .forEach(context -> context.setNamespaces(namespaces));

        return delegate.build();
    }

    /**
     * Gets the validation context as XML validation context an raises exception if existing validation context is
     * not a XML validation context.
     *
     * @return
     */
    private XpathMessageValidationContext.Builder getXPathValidationContext() {
        if (getXmlMessageValidationContext() instanceof XpathMessageValidationContext.Builder) {
            return ((XpathMessageValidationContext.Builder) getXmlMessageValidationContext());
        } else {
            XmlMessageValidationContext context = getXmlMessageValidationContext().build();
            final XpathMessageValidationContext.Builder xPathContext = new XpathMessageValidationContext.Builder();
            xPathContext.setNamespaces(context.getNamespaces());

            context.getControlNamespaces().forEach(xPathContext::namespace);
            context.getIgnoreExpressions().forEach(xPathContext::ignore);
            xPathContext.schema(context.getSchema());
            xPathContext.schemaRepository(context.getSchemaRepository());
            xPathContext.schemaValidation(context.isSchemaValidationEnabled());
            xPathContext.dtd(context.getDTDResource());

            delegate.getValidationContextBuilders().remove(getXmlMessageValidationContext());
            delegate.validate(xPathContext);

            xmlMessageValidationContext = xPathContext;
            return xPathContext;
        }
    }

    /**
     * Creates new xml validation context if not done before and gets the xml validation context.
     */
    protected XmlMessageValidationContext.XmlValidationContextBuilder<?, ?> getXmlMessageValidationContext() {
        if (xmlMessageValidationContext == null) {
            xmlMessageValidationContext = new XmlMessageValidationContext.Builder();

            delegate.validate(xmlMessageValidationContext);
        }

        return xmlMessageValidationContext;
    }

    /**
     * Creates new json validation context if not done before and gets the json validation context.
     */
    private JsonMessageValidationContext.Builder getJsonMessageValidationContext() {
        if (jsonMessageValidationContext == null) {
            jsonMessageValidationContext = new JsonMessageValidationContext.Builder();

            delegate.validate(jsonMessageValidationContext);
        }

        return jsonMessageValidationContext;
    }

    /**
     * Creates new script validation context if not done before and gets the script validation context.
     */
    private ScriptValidationContext.Builder getScriptValidationContext() {
        if (scriptValidationContext == null) {
            scriptValidationContext = new ScriptValidationContext.Builder();

            delegate.validate(scriptValidationContext);
        }

        return scriptValidationContext;
    }

    /**
     * Creates new JSONPath validation context if not done before and gets the validation context.
     */
    private JsonPathMessageValidationContext.Builder getJsonPathValidationContext() {
        if (jsonPathValidationContext == null) {
            jsonPathValidationContext = new JsonPathMessageValidationContext.Builder();

            delegate.validate(jsonPathValidationContext);
        }

        return jsonPathValidationContext;
    }
}
