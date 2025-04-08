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

package org.citrusframework.openapi.random;

import java.util.List;

import io.apicurio.datamodels.openapi.models.OasSchema;

import static java.util.stream.Collectors.joining;

public class RandomEnumGenerator extends RandomGenerator {

    @Override
    public boolean handles(OasSchema other) {
        return other.enum_ != null;
    }

    @Override
    void generate(RandomContext randomContext, OasSchema schema) {
        List<String> anEnum = schema.enum_;
        if (anEnum != null) {
            String enumValues = schema.enum_.stream()
                    .map(value -> "'" + value + "'")
                    .collect(joining(","));
            randomContext.getRandomModelBuilder().appendSimpleQuoted("citrus:randomEnumValue(%s)".formatted(enumValues));
        }
    }
}
