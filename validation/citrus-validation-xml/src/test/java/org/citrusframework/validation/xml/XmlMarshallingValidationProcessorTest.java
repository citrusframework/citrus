package org.citrusframework.validation.xml;

import org.citrusframework.UnitTestSupport;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.SimpleReferenceResolver;
import org.citrusframework.validation.GenericValidationProcessor;
import org.citrusframework.xml.Unmarshaller;
import org.testng.annotations.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class XmlMarshallingValidationProcessorTest extends UnitTestSupport {

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
