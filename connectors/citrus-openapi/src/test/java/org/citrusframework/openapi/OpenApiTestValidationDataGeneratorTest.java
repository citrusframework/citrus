/*
 * Copyright the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.openapi;

import io.apicurio.datamodels.openapi.models.OasSchema;
import io.apicurio.datamodels.openapi.v2.models.Oas20Schema;
import io.apicurio.datamodels.openapi.v2.models.Oas20Schema.Oas20AllOfSchema;
import io.apicurio.datamodels.openapi.v3.models.Oas30Schema;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.citrusframework.openapi.OpenApiTestValidationDataGenerator.createValidationExpression;
import static org.citrusframework.openapi.OpenApiTestValidationDataGenerator.createValidationRegex;
import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;

public class OpenApiTestValidationDataGeneratorTest {

    @DataProvider(name = "createValidationRegexDataProvider")
    public static Object[][] createValidationRegexDataProvider() {

        Oas30Schema stringSchema = new Oas30Schema();
        stringSchema.type = OpenApiConstants.TYPE_STRING;

        Oas30Schema uuidSchema = new Oas30Schema();
        uuidSchema.type = OpenApiConstants.TYPE_STRING;
        uuidSchema.format = OpenApiConstants.FORMAT_UUID;

        Oas30Schema dateSchema = new Oas30Schema();
        dateSchema.type = OpenApiConstants.TYPE_STRING;
        dateSchema.format = OpenApiConstants.FORMAT_DATE;

        Oas30Schema dateTimeSchema = new Oas30Schema();
        dateTimeSchema.type = OpenApiConstants.TYPE_STRING;
        dateTimeSchema.format = OpenApiConstants.FORMAT_DATE_TIME;

        Oas30Schema integerSchema = new Oas30Schema();
        integerSchema.type = OpenApiConstants.TYPE_INTEGER;

        Oas30Schema numberSchema = new Oas30Schema();
        numberSchema.type = OpenApiConstants.TYPE_NUMBER;

        Oas30Schema booleanSchema = new Oas30Schema();
        booleanSchema.type = OpenApiConstants.TYPE_BOOLEAN;

        Oas30Schema regexSchema = new Oas30Schema();
        regexSchema.type = OpenApiConstants.TYPE_STRING;
        regexSchema.pattern = "[1234]5[6789]";

        Oas30Schema enumSchema = new Oas30Schema();
        enumSchema.type = OpenApiConstants.TYPE_STRING;
        enumSchema.enum_ = List.of("A", "B", "C");

        return new Object[][]{
                {stringSchema, "xyz", true},
                {uuidSchema, "123e4567-e89b-12d3-a456-426614174000", true},
                {uuidSchema, "123e4567-e89b-12d3-a456-42661417400", false},
                {dateSchema, "2023-05-15", true},
                {dateSchema, "2023-15-15", false},
                {dateTimeSchema, "2023-05-15T10:15:30Z", true},
                {dateTimeSchema, "2023-05-15T25:15:30Z", false},
                {integerSchema, "2023", true},
                {integerSchema, "2023.05", false},
                {numberSchema, "2023", true},
                {numberSchema, "2023.xx", false},
                {booleanSchema, "true", true},
                {booleanSchema, "false", true},
                {booleanSchema, "yes", false},
                {booleanSchema, "no", false},
                {booleanSchema, "yes", false},
                {booleanSchema, "no", false},
                {regexSchema, "156", true},
                {regexSchema, "651", false},
                {enumSchema, "A", true},
                {enumSchema, "B", true},
                {enumSchema, "C", true},
                {enumSchema, "a", false},
                {enumSchema, "D", false},
        };
    }

    @Test
    public void anyOfIsIgnoredForOas3() {
        Oas30Schema anyOfSchema = new Oas30Schema();
        anyOfSchema.anyOf = List.of(new Oas30Schema(), new Oas30Schema());

        assertEquals(createValidationExpression(
                anyOfSchema, new HashMap<>(), true, mock()), "\"@ignore@\"");
    }

    @Test
    public void allOfIsIgnoredForOas3() {
        Oas30Schema allOfSchema = new Oas30Schema();
        allOfSchema.allOf = List.of(new Oas30Schema(), new Oas30Schema());

        assertEquals(createValidationExpression(
                allOfSchema, new HashMap<>(), true, mock()), "\"@ignore@\"");
    }

    @Test
    public void oneOfIsIgnoredForOas3() {
        Oas30Schema oneOfSchema = new Oas30Schema();
        oneOfSchema.oneOf = List.of(new Oas30Schema(), new Oas30Schema());

        assertEquals(createValidationExpression(
                oneOfSchema, new HashMap<>(), true, mock()), "\"@ignore@\"");
    }

    @Test
    public void allOfIsIgnoredForOas2() {
        Oas20AllOfSchema allOfSchema = new Oas20AllOfSchema();
        allOfSchema.allOf = List.of(new Oas20Schema(), new Oas20Schema());

        assertEquals(createValidationExpression(
                allOfSchema, new HashMap<>(), true, mock()), "\"@ignore@\"");
    }

    @Test(dataProvider = "createValidationRegexDataProvider")
    public void createValidationRegex_shouldValidateRealDataCorrectly(OasSchema schema, String toValidate, boolean result) {
        String regex = createValidationRegex(schema);
        assertThat(Pattern.matches(regex, toValidate)).isEqualTo(result);
    }

    @Test
    public void validationRegexOfNullIsEmpty() {
        assertThat(createValidationRegex(null)).isEmpty();
    }

    @Test
    public void defaultvalidationRegexIsEmpty() {
        Oas30Schema oas30Schema = new Oas30Schema();
        oas30Schema.type = "xxxx";
        assertThat(createValidationRegex(oas30Schema)).isEmpty();
    }
}
