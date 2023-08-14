/*
 * Copyright 2006-2018 the original author or authors.
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

package org.citrusframework.validation.json.schema;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.json.JsonSchemaRepository;
import org.citrusframework.json.schema.SimpleJsonSchema;
import org.citrusframework.validation.json.JsonMessageValidationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.util.StringUtils;

/**
 * This class is responsible for filtering {@link SimpleJsonSchema}s based on a {@link JsonMessageValidationContext}.
 */
public class JsonSchemaFilter {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * Filters the all schema repositories based on the configuration in the {@code jsonMessageValidationContext} and
     * returns a list of the relevant underlying {@link SimpleJsonSchema}s for the validation.
     *
     * @param schemaRepositories The repositories to be filtered
     * @param jsonMessageValidationContext The context for the json message validation
     * @param referenceResolver holding bean references for lookup
     * @return a list of json schemas relevant for the validation based on the configuration
     */
    public List<SimpleJsonSchema> filter(List<JsonSchemaRepository> schemaRepositories,
                                         JsonMessageValidationContext jsonMessageValidationContext,
                                         ReferenceResolver referenceResolver) {
        if (isSchemaRepositorySpecified(jsonMessageValidationContext)) {
            return filterByRepositoryName(schemaRepositories, jsonMessageValidationContext);
        } else if (isSchemaSpecified(jsonMessageValidationContext)) {
            return getSchemaFromContext(jsonMessageValidationContext, referenceResolver);
        } else {
            return mergeRepositories(schemaRepositories);
        }
    }

    private List<SimpleJsonSchema> getSchemaFromContext(JsonMessageValidationContext jsonMessageValidationContext,
                                                        ReferenceResolver referenceResolver) {
        try {
            SimpleJsonSchema simpleJsonSchema =
                    referenceResolver.resolve(jsonMessageValidationContext.getSchema(), SimpleJsonSchema.class);

            if (log.isDebugEnabled()) {
                log.debug("Found specified schema: \"" + jsonMessageValidationContext.getSchema() + "\".");
            }

            return Collections.singletonList(simpleJsonSchema);
        } catch (NoSuchBeanDefinitionException e) {
            throw new CitrusRuntimeException(
                    "Could not find the specified schema: \"" + jsonMessageValidationContext.getSchema() + "\".",
                    e);
        }
    }

    private List<SimpleJsonSchema> filterByRepositoryName(List<JsonSchemaRepository> schemaRepositories,
                                                          JsonMessageValidationContext jsonMessageValidationContext) {
        for (JsonSchemaRepository jsonSchemaRepository : schemaRepositories) {
            if (Objects.equals(jsonSchemaRepository.getName(), jsonMessageValidationContext.getSchemaRepository())) {
                if (log.isDebugEnabled()) {
                    log.debug("Found specified schema-repository: \"" +
                            jsonMessageValidationContext.getSchemaRepository() + "\".");
                }
                return jsonSchemaRepository.getSchemas();
            }
        }

        throw new CitrusRuntimeException("Could not find the specified schema repository: " +
                "\"" + jsonMessageValidationContext.getSchemaRepository() + "\".");
    }

    private List<SimpleJsonSchema> mergeRepositories(List<JsonSchemaRepository> schemaRepositories) {
        return schemaRepositories.stream()
                .map(JsonSchemaRepository::getSchemas)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    private boolean isSchemaSpecified(JsonMessageValidationContext context) {
        return StringUtils.hasText(context.getSchema());
    }

    private boolean isSchemaRepositorySpecified(JsonMessageValidationContext context) {
        return StringUtils.hasText(context.getSchemaRepository());
    }
}
