package org.citrusframework.openapi.random;

import io.apicurio.datamodels.openapi.models.OasSchema;
import io.apicurio.datamodels.openapi.v3.models.Oas30Schema;
import org.citrusframework.openapi.OpenApiConstants;

/**
 * A generator for producing random strings based on an OpenAPI schema.
 * This class extends the {@link RandomGenerator} and provides a specific implementation
 * for generating random strings with constraints defined in the schema.
 */
public class RandomStringGenerator extends RandomGenerator {

    private static final OasSchema STRING_SCHEMA = new Oas30Schema();

    static {
        STRING_SCHEMA.type = OpenApiConstants.TYPE_STRING;
    }

    public RandomStringGenerator() {
        super(STRING_SCHEMA);
    }

    @Override
    void generate(RandomContext randomContext, OasSchema schema) {
        int min = 1;
        int max = 10;

        if (schema.minLength != null && schema.minLength.intValue() > 0) {
            min = schema.minLength.intValue();
        }

        if (schema.maxLength != null && schema.maxLength.intValue() > 0) {
            max = schema.maxLength.intValue();
        }

        randomContext.getRandomModelBuilder().appendSimpleQuoted("citrus:randomString(%s,MIXED,true,%s)".formatted(max, min));
    }
}
