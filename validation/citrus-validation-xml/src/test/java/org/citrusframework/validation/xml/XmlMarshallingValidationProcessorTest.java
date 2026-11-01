package org.citrusframework.validation.xml;

import org.citrusframework.xml.UnitTestSupport;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.SimpleReferenceResolver;
import org.citrusframework.validation.GenericValidationProcessor;
import org.citrusframework.api.xml.Unmarshaller;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class XmlMarshallingValidationProcessorTest extends UnitTestSupport {

    private static final Logger logger = LoggerFactory.getLogger(XmlMarshallingValidationProcessorTest.class);

    final Unmarshaller unmarshaller = Object::toString;
    final GenericValidationProcessor<String> validationProcessor = (payload, headers, context) ->
            logger.info("Validating message %s".formatted(payload));

    @Test
    void builderWithoutUnmarshallerTest() {
        XmlMarshallingValidationProcessor<String> build = XmlMarshallingValidationProcessor.Builder.validate(validationProcessor)
                .unmarshaller(unmarshaller)
                .build();

        build.setReferenceResolver(new SimpleReferenceResolver());

        assertDoesNotThrow(() -> build.validate(new DefaultMessage().setPayload("hi"), null));
    }

    @Test
    void builderWithUnmarshallerTest() {
        final ReferenceResolver referenceResolver = new SimpleReferenceResolver();
        referenceResolver.bind("anyName", unmarshaller);

        XmlMarshallingValidationProcessor<String> build = XmlMarshallingValidationProcessor.Builder.validate(validationProcessor)
                .withReferenceResolver(referenceResolver)
                .build();

        build.setReferenceResolver(referenceResolver);

        assertDoesNotThrow(() -> build.validate(new DefaultMessage().setPayload("bye"), null));
    }
}
