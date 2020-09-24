package com.consol.citrus.dsl.builder;

import java.nio.charset.Charset;
import java.util.Map;

import com.consol.citrus.AbstractTestActionBuilder;
import com.consol.citrus.actions.SendMessageAction;
import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.message.Message;
import com.consol.citrus.message.MessageType;
import com.consol.citrus.spi.ReferenceResolver;
import com.consol.citrus.util.FileUtils;
import com.consol.citrus.validation.builder.MessageContentBuilder;
import com.consol.citrus.validation.interceptor.BinaryMessageProcessor;
import com.consol.citrus.validation.interceptor.GzipMessageProcessor;
import com.consol.citrus.validation.json.JsonPathMessageProcessor;
import com.consol.citrus.validation.json.JsonPathMessageValidationContext;
import com.consol.citrus.validation.xml.XpathMessageProcessor;
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
public class SendMessageActionBuilder<B extends SendMessageActionBuilder<B>> extends AbstractTestActionBuilder<SendMessageAction, B> {

    protected final B self;

    private final SendMessageAction.SendMessageActionBuilder<?, ?> delegate;

    private final GzipMessageProcessor gzipMessageProcessor = new GzipMessageProcessor();
    private final BinaryMessageProcessor binaryMessageProcessor = new BinaryMessageProcessor();

    /** Message processor */
    private XpathMessageProcessor xpathMessageProcessor;
    private JsonPathMessageProcessor jsonPathMessageProcessor;

    public SendMessageActionBuilder() {
        this(new SendMessageAction.Builder());
    }

    public SendMessageActionBuilder(SendMessageAction.SendMessageActionBuilder<?, ?> builder) {
        this.self = (B) this;
        this.delegate = builder;
    }

    /**
     * Sets the message endpoint to send messages to.
     * @param messageEndpoint
     * @return
     */
    public B endpoint(Endpoint messageEndpoint) {
        delegate.endpoint(messageEndpoint);
        return self;
    }

    /**
     * Sets the message endpoint uri to send messages to.
     * @param messageEndpointUri
     * @return
     */
    public B endpoint(String messageEndpointUri) {
        delegate.endpoint(messageEndpointUri);
        return self;
    }

    /**
     * Sets the fork mode for this send action builder.
     * @param forkMode
     * @return
     */
    public B fork(boolean forkMode) {
        delegate.fork(forkMode);
        return self;
    }

    /**
     * Sets the message builder to use.
     * @param messageBuilder
     * @return
     */
    public B messageBuilder(MessageContentBuilder messageBuilder) {
        delegate.message(messageBuilder);
        return self;
    }

    /**
     * Sets the message instance to send.
     * @param message
     * @return
     */
    public B message(Message message) {
        delegate.message(message);
        return self;
    }

    /**
     * Sets the message name.
     * @param name
     * @return
     */
    public B messageName(String name) {
        delegate.messageName(name);
        return self;
    }

    /**
     * Adds message payload data to this builder.
     * @param payload
     * @return
     */
    public B payload(String payload) {
        delegate.payload(payload);
        return self;
    }

    /**
     * Adds message payload resource to this builder.
     * @param payloadResource
     * @return
     */
    public B payload(Resource payloadResource) {
        return payload(payloadResource, FileUtils.getDefaultCharset());
    }

    /**
     * Adds message payload resource to this builder.
     * @param payloadResource
     * @param charset
     * @return
     */
    public B payload(Resource payloadResource, Charset charset) {
        delegate.payload(payloadResource, charset);
        return self;
    }

    /**
     * Sets payload POJO object which is marshalled to a character sequence using the given object to xml mapper.
     * @param payload
     * @param marshaller
     * @return
     */
    public B payload(Object payload, Marshaller marshaller) {
        delegate.payload(payload, marshaller);
        return self;
    }

    /**
     * Sets payload POJO object which is mapped to a character sequence using the given object to json mapper.
     * @param payload
     * @param objectMapper
     * @return
     */
    public B payload(Object payload, ObjectMapper objectMapper) {
        delegate.payload(payload, objectMapper);
        return self;
    }

    /**
     * Sets payload POJO object which is marshalled to a character sequence using the default object to xml or object
     * to json mapper that is available in Spring bean application context.
     *
     * @param payload
     * @return
     */
    public B payloadModel(Object payload) {
        delegate.payloadModel(payload);
        return self;
    }

    /**
     * Sets payload POJO object which is marshalled to a character sequence using the given object to xml mapper that
     * is accessed by its bean name in Spring bean application context.
     *
     * @param payload
     * @param mapperName
     * @return
     */
    public B payload(Object payload, String mapperName) {
        delegate.payload(payload, mapperName);
        return self;
    }

    /**
     * Adds message header name value pair to this builder's message sending action.
     * @param name
     * @param value
     */
    public B header(String name, Object value) {
        delegate.header(name, value);
        return self;
    }

    /**
     * Adds message headers to this builder's message sending action.
     * @param headers
     */
    public B headers(Map<String, Object> headers) {
        delegate.headers(headers);
        return self;
    }

    /**
     * Adds message header data to this builder's message sending action. Message header data is used in SOAP
     * messages for instance as header XML fragment.
     * @param data
     */
    public B header(String data) {
        delegate.header(data);
        return self;
    }

    /**
     * Adds message header data as file resource to this builder's message sending action. Message header data is used in SOAP
     * messages for instance as header XML fragment.
     * @param resource
     */
    public B header(Resource resource) {
        return header(resource, FileUtils.getDefaultCharset());
    }

    /**
     * Adds message header data as file resource to this builder's message sending action. Message header data is used in SOAP
     * messages for instance as header XML fragment.
     * @param resource
     * @param charset
     */
    public B header(Resource resource, Charset charset) {
        delegate.header(resource, charset);
        return self;
    }

    /**
     * Sets header data POJO object which is marshalled to a character sequence using the given object to xml mapper.
     * @param model
     * @param marshaller
     * @return
     */
    public B headerFragment(Object model, Marshaller marshaller) {
        delegate.headerFragment(model, marshaller);
        return self;
    }

    /**
     * Sets header data POJO object which is mapped to a character sequence using the given object to json mapper.
     * @param model
     * @param objectMapper
     * @return
     */
    public B headerFragment(Object model, ObjectMapper objectMapper) {
        delegate.headerFragment(model, objectMapper);
        return self;
    }

    /**
     * Sets header data POJO object which is marshalled to a character sequence using the default object to xml or object
     * to json mapper that is available in Spring bean application context.
     *
     * @param model
     * @return
     */
    public B headerFragment(Object model) {
        delegate.headerFragment(model);
        return self;
    }

    /**
     * Sets header data POJO object which is marshalled to a character sequence using the given object to xml mapper that
     * is accessed by its bean name in Spring bean application context.
     *
     * @param model
     * @param mapperName
     * @return
     */
    public B headerFragment(Object model, String mapperName) {
        delegate.headerFragment(model, mapperName);
        return self;
    }

    /**
     * Sets a explicit message type for this send action.
     * @param messageType
     * @return
     */
    public B messageType(MessageType messageType) {
        messageType(messageType.name());
        return self;
    }

    /**
     * Sets a explicit message type for this send action.
     * @param messageType The message type to send the message in
     * @return The modified send message
     */
    public B messageType(String messageType) {
        delegate.messageType(messageType);

        if (MessageType.BINARY.name().equalsIgnoreCase(messageType)) {
            delegate.process(binaryMessageProcessor);
        }

        if (MessageType.GZIP.name().equalsIgnoreCase(messageType)) {
            delegate.process(gzipMessageProcessor);
        }

        return self;
    }

    /**
     * Extract message header entry as variable before message is sent.
     * @param headerName
     * @param variable
     * @return
     */
    public B extractFromHeader(String headerName, String variable) {
        variableExtractor(headerValueExtractor()
                    .expression(headerName, variable)
                .build());
        return self;
    }

    /**
     * Extract message element via XPath or JSONPath from payload before message is sent.
     * @param path
     * @param variable
     * @return
     */
    public B extractFromPayload(String path, String variable) {
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
     * Adds variable extractor.
     * @param extractor
     * @return
     */
    public B variableExtractor(VariableExtractor extractor) {
        delegate.process(extractor);
        return self;
    }

    /**
     * Adds XPath manipulating expression that evaluates to message payload before sending.
     * @param expression
     * @param value
     * @return
     */
    public B xpath(String expression, String value) {
        if (xpathMessageProcessor == null) {
            xpathMessageProcessor = new XpathMessageProcessor();
            delegate.process(xpathMessageProcessor);
        }

        xpathMessageProcessor.getXPathExpressions().put(expression, value);
        return self;
    }

    /**
     * Adds JSONPath manipulating expression that evaluates to message payload before sending.
     * @param expression
     * @param value
     * @return
     */
    public B jsonPath(String expression, String value) {
        if (jsonPathMessageProcessor == null) {
            jsonPathMessageProcessor = new JsonPathMessageProcessor();
            delegate.process(jsonPathMessageProcessor);
        }

        jsonPathMessageProcessor.getJsonPathExpressions().put(expression, value);
        return self;
    }

    /**
     * Sets the bean reference resolver.
     * @param referenceResolver
     */
    public B withReferenceResolver(ReferenceResolver referenceResolver) {
        delegate.withReferenceResolver(referenceResolver);
        return self;
    }

    /**
     * Sets explicit data dictionary for this receive action.
     * @param dictionary
     * @return
     */
    public B dictionary(DataDictionary<?> dictionary) {
        delegate.dictionary(dictionary);
        return self;
    }

    /**
     * Sets explicit data dictionary by name.
     * @param dictionaryName
     * @return
     */
    public B dictionary(String dictionaryName) {
        delegate.dictionary(dictionaryName);
        return self;
    }

    @Override
    public SendMessageAction build() {
        return delegate.build();
    }
}
