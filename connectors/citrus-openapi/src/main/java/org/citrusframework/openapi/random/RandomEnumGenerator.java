package org.citrusframework.openapi.random;

import io.apicurio.datamodels.openapi.models.OasSchema;
import java.util.List;
import java.util.stream.Collectors;

public class RandomEnumGenerator extends RandomGenerator {


    @Override
    public boolean handles(OasSchema other) {
        return other.enum_ != null;
    }

    @Override
    void generate(RandomContext randomContext, OasSchema schema) {
        List<String> anEnum = schema.enum_;
        if (anEnum != null) {
            String enumValues = schema.enum_.stream().map(value -> "'" + value + "'")
                .collect(Collectors.joining(","));
            randomContext.getRandomModelBuilder().appendSimpleQuoted("citrus:randomEnumValue(%s)".formatted(enumValues));
        }
    }

}