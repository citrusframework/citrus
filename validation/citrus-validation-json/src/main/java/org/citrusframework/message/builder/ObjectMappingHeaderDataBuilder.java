/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.message.builder;

import java.util.Map;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Christoph Deppisch
 */
public class ObjectMappingHeaderDataBuilder extends DefaultHeaderDataBuilder {

    private final ObjectMapper mapper;
    private final String mapperName;

    /**
     * Default constructor using just model object.
     * @param model
     */
    public ObjectMappingHeaderDataBuilder(Object model) {
        super(model);

        this.mapper = null;
        this.mapperName = null;
    }

    /**
     * Default constructor using object mapper and model object.
     * @param model
     * @param mapper
     */
    public ObjectMappingHeaderDataBuilder(Object model, ObjectMapper mapper) {
        super(model);

        this.mapper = mapper;
        this.mapperName = null;
    }

    /**
     * Default constructor using object mapper name and model object.
     * @param model
     * @param mapperName
     */
    public ObjectMappingHeaderDataBuilder(Object model, String mapperName) {
        super(model);

        this.mapperName = mapperName;
        this.mapper = null;
    }

    @Override
    public String buildHeaderData(TestContext context) {
        if (getHeaderData() == null || getHeaderData() instanceof String) {
            return super.buildHeaderData(context);
        }

        if (mapper != null) {
            return buildHeaderData(mapper, getHeaderData(), context);
        }

        if (mapperName != null) {
            if (context.getReferenceResolver().isResolvable(mapperName)) {
                ObjectMapper objectMapper = context.getReferenceResolver().resolve(mapperName, ObjectMapper.class);
                return buildHeaderData(objectMapper, getHeaderData(), context);
            } else {
                throw new CitrusRuntimeException(String.format("Unable to find proper object mapper for name '%s'", mapperName));
            }
        }

        Map<String, ObjectMapper> mappers = context.getReferenceResolver().resolveAll(ObjectMapper.class);
        if (mappers.size() == 1) {
            return buildHeaderData(mappers.values().iterator().next(), getHeaderData(), context);
        } else {
            throw new CitrusRuntimeException(String.format("Unable to auto detect object mapper - " +
                    "found %d matching mapper instances in reference resolver", mappers.size()));
        }
    }

    private String buildHeaderData(ObjectMapper mapper, Object model, TestContext context) {
        try {
            return context.replaceDynamicContentInString(mapper.writer().writeValueAsString(model));
        } catch (final JsonProcessingException e) {
            throw new CitrusRuntimeException("Failed to map object graph for message header data", e);
        }
    }


}
