package org.citrusframework.openapi.random;

import static java.lang.Boolean.TRUE;
import static java.lang.String.format;
import static org.citrusframework.openapi.OpenApiConstants.TYPE_INTEGER;

import io.apicurio.datamodels.openapi.models.OasSchema;
import java.math.BigDecimal;
import org.citrusframework.openapi.util.OpenApiUtils;

/**
 * A generator for producing random numbers based on an OpenAPI schema. This class extends the
 * {@link RandomGenerator} and provides a specific implementation for generating random numbers with
 * constraints defined in the schema.
 *
 * <p>Supported constraints:
 * <ul>
 *   <li><b>minimum</b>: The minimum value for the generated number.</li>
 *   <li><b>maximum</b>: The maximum value for the generated number.</li>
 *   <li><b>exclusiveMinimum</b>: If true, the generated number will be strictly greater than the minimum.</li>
 *   <li><b>exclusiveMaximum</b>: If true, the generated number will be strictly less than the maximum.</li>
 *   <li><b>multipleOf</b>: The generated number will be a multiple of this value.</li>
 * </ul>
 *
 * <p>The generator supports generating numbers for both integer and floating-point types, including
 * <code>int32</code>, <code>int64</code>, <code>double</code>, and <code>float</code>. This support
 * extends to the <code>multipleOf</code> constraint, ensuring that the generated numbers can be precise
 * multiples of the specified value.
 *
 * <p>The generator determines the appropriate bounds and constraints based on the provided schema
 * and generates a random number accordingly.
 */
public class RandomNumberGenerator extends RandomGenerator {

    public static final BigDecimal THOUSAND = new BigDecimal(1000);
    public static final BigDecimal HUNDRED = java.math.BigDecimal.valueOf(100);
    public static final BigDecimal MINUS_THOUSAND = new BigDecimal(-1000);

    @Override
    public boolean handles(OasSchema other) {
        return OpenApiUtils.isAnyNumberScheme(other);
    }

    @Override
    void generate(RandomContext randomContext, OasSchema schema) {

        boolean exclusiveMaximum = TRUE.equals(schema.exclusiveMaximum);
        boolean exclusiveMinimum = TRUE.equals(schema.exclusiveMinimum);

        BigDecimal[] bounds = determineBounds(schema);

        BigDecimal minimum = bounds[0];
        BigDecimal maximum = bounds[1];

        if (schema.multipleOf != null) {
            randomContext.getRandomModelBuilder().appendSimple(format(
                "citrus:randomNumberGenerator('%d', '%s', '%s', '%s', '%s', '%s')",
                determineDecimalPlaces(schema, minimum, maximum),
                minimum,
                maximum,
                exclusiveMinimum,
                exclusiveMaximum,
                schema.multipleOf
            ));
        } else {
            randomContext.getRandomModelBuilder().appendSimple(format(
                "citrus:randomNumberGenerator('%d', '%s', '%s', '%s', '%s')",
                determineDecimalPlaces(schema, minimum, maximum),
                minimum,
                maximum,
                exclusiveMinimum,
                exclusiveMaximum
            ));
        }
    }

    /**
     * Determines the number of decimal places to use based on the given schema and
     * minimum/maximum/multipleOf values. For integer types, it returns 0. For other types, it
     * returns the maximum number of decimal places found between the minimum and maximum values,
     * with a minimum of 2 decimal places.
     */
    private int determineDecimalPlaces(OasSchema schema, BigDecimal minimum,
        BigDecimal maximum) {
        if (TYPE_INTEGER.equals(schema.type)) {
            return 0;
        } else {
            Number multipleOf = schema.multipleOf;
            if (multipleOf != null) {
                return findLeastSignificantDecimalPlace(new BigDecimal(multipleOf.toString()));
            }

            return Math.max(2, Math.max(findLeastSignificantDecimalPlace(minimum),
                findLeastSignificantDecimalPlace(maximum)));

        }
    }

    /**
     * Determine some reasonable bounds for a random number
     */
    private static BigDecimal[] determineBounds(OasSchema schema) {
        Number maximum = schema.maximum;
        Number minimum = schema.minimum;
        Number multipleOf = schema.multipleOf;

        BigDecimal bdMinimum;
        BigDecimal bdMaximum;

        if (minimum == null && maximum == null) {
            bdMinimum = MINUS_THOUSAND;
            bdMaximum = THOUSAND;
        } else if (minimum == null) {
            bdMaximum = new BigDecimal(maximum.toString());
            bdMinimum = calculateMinRelativeToMax(bdMaximum, multipleOf);
        } else if (maximum == null) {
            bdMinimum = new BigDecimal(minimum.toString());
            bdMaximum = calculateMaxRelativeToMin(bdMinimum, multipleOf);
        } else {
            bdMinimum = new BigDecimal(minimum.toString());
            bdMaximum = new BigDecimal(maximum.toString());
        }

        return new BigDecimal[]{bdMinimum, bdMaximum};
    }

    static BigDecimal calculateMinRelativeToMax(BigDecimal max, Number multipleOf) {
        if (multipleOf != null) {
            return max.subtract(new BigDecimal(multipleOf.toString()).abs().multiply(HUNDRED));
        } else {
            return max.subtract(max.multiply(BigDecimal.valueOf(2)).max(THOUSAND));
        }
    }

    static BigDecimal calculateMaxRelativeToMin(BigDecimal min, Number multipleOf) {
        if (multipleOf != null) {
            return min.add(new BigDecimal(multipleOf.toString()).abs().multiply(HUNDRED));
        } else {
            return min.add(min.multiply(BigDecimal.valueOf(2)).max(THOUSAND));
        }
    }

    int findLeastSignificantDecimalPlace(BigDecimal number) {
        number = number.stripTrailingZeros();

        String[] parts = number.toPlainString().split("\\.");

        if (parts.length == 1) {
            return 0;
        }

        return parts[1].length();
    }

}
